# Wikidata ID Availability in Overture Data

Investigation into whether Overture POIs can be linked to Wikidata for richer rendering.

## Data sources examined

`Oakland-visualtests.parquet` — Overture 2026-02-18 release, Bay Area coverage.

## Findings by theme

### `places` theme (618,880 features)

- **Top-level `wikidata` field**: entirely null — 0 out of 618,880 features have a value.
- **`brand.wikidata`**: present for ~11,059 features — chain businesses where the *brand entity* has a Wikidata ID (e.g. `Q177054` for Burger King). Does not help with unique/famous places.
- **Unique/famous places** (Oakland Zoo, Oakland Museum of California, Oakland International Airport, UC Berkeley, etc.): no wikidata at all, neither top-level nor brand.

### `divisions` theme (3,615 features)

- **Top-level `wikidata` field**: populated for cities, counties, etc. (e.g. `Q62` for San Francisco, `Q927122` for South San Francisco).
- These feed the `places` map layer via `Places.java`, which already exports the `wikidata` attribute to output tiles (lines 324–325 and 431).

## Alternative: website domain matching

Overture places features often include `websites` and `socials` arrays.

- **`socials`**: contains Facebook URLs with numeric page IDs (e.g. `facebook.com/353030440227`). Wikidata stores Facebook *usernames* (P2013), not numeric IDs — no direct join possible.
- **`websites`**: contains place-specific URLs. Extracting the root domain and matching against Wikidata P856 (official website) works:
  - `oaklandzoo.org` → Q2008530 (Oakland Zoo) ✅
  - `museumca.org` → Q877714 (Oakland Museum of California) ✅
  - `oaklandairport.com` / `flyoakland.com` → Oakland International Airport ✅

### Scale

- ~303,848 unique meaningful domains across all places features (excluding generic social/link domains).
- Too large to bulk-query the Wikidata SPARQL endpoint (60-second hard timeout).

### Caveats

- Multiple Overture features can share the same domain (e.g. all Oakland Public Library branches → `oaklandlibrary.org`), so the match links to the organization entity, not a specific location.
- Some domains map to multiple QIDs (e.g. `museumca.org` → Q877714, Q133252684, Q30672317) — needs disambiguation.
- No coverage for places without websites (~half of all features).

## Bulk Wikidata P856 export via QLever

The Wikidata SPARQL endpoint times out on full P856 scans. [QLever](https://qlever.dev/wikidata) (University of Freiburg) is a faster alternative engine that handles full-dataset scans. The complete P856 table (2.3M rows) was fetched in one query in ~16 seconds:

```
PREFIX wdt: <http://www.wikidata.org/prop/direct/>
SELECT ?item ?website WHERE { ?item wdt:P856 ?website }
```

```sh
curl -H "Accept: text/tab-separated-values" \
  --data-urlencode "query=PREFIX wdt: <http://www.wikidata.org/prop/direct/> SELECT ?item ?website WHERE { ?item wdt:P856 ?website }" \
  --data-urlencode "send=2400000" \
  "https://qlever.dev/api/wikidata" \
  -o data/sources/wikidata-p856.tsv
```

The TSV was then parsed to extract QID and root domain, and saved as `data/sources/wikidata-p856.parquet` (36 MB, 2.3M rows).

## Match rate against Overture Oakland data

Joining `wikidata-p856.parquet` against `Oakland-visualtests.parquet` on root domain (excluding generic domains like facebook.com, yelp.com, etc.):

- **522,799** places have a usable website URL
- **121,685** (23.3%) matched to at least one Wikidata QID

Confirmed matches for notable places:
- `oaklandzoo.org` → Q2008530 (Oakland Zoo) ✅
- `museumca.org` → Q877714 (Oakland Museum of California) ✅
- `iflyoak.com` → Q1165584 (Oakland International Airport) ✅
- `berkeley.edu` → Q168756 (UC Berkeley) ✅

Top matched categories: doctor, park, government association, medical center, hotel, university, library, landmark.

## Disambiguation: multiple QIDs per domain

142,182 domains map to more than one QID in the P856 table (avg 6.7, max 81,822 for a digital library collection domain where every digitized item has its own Wikidata entry).

### Approaches considered

**Full URL path matching** — prefer the QID whose stored P856 URL most closely matches the full Overture URL, not just the domain. Ruled out: Wikidata typically stores bare root URLs (`http://www.oaklandzoo.org`), so this rarely breaks ties.

**P31 instance-of type matching** — fetch P31 (instance of) for all candidate QIDs and prefer the one whose type aligns with the Overture category (e.g. Overture `museum` → prefer QID with `instance of: Q33506`). Ruled out: P31 has 122M rows in Wikidata; QLever serves at most ~15M rows per query and the full download fails. Batching 920k candidate QIDs via the SPARQL endpoint would be slow and fragile.

**Lowest Q-number tiebreak** — prefer the QID with the smallest numeric value. This works because Wikidata assigns lower Q-numbers to older, more established entities. Exhibitions, digitized sub-items, and branch locations all post-date their parent organizations and receive higher Q-numbers.

### Result

The lowest Q-number heuristic gets the right answer in all tested cases:

| Domain | Winner QID | Entity |
|---|---|---|
| `museumca.org` | Q877714 | Oakland Museum of California (not the exhibitions Q133252684, Q30672317) |
| `oaklandzoo.org` | Q2008530 | Oakland Zoo |
| `iflyoak.com` | Q1165584 | Oakland International Airport |
| `berkeley.edu` | Q168756 | UC Berkeley |
| `oaklandlibrary.org` | Q1090829 | Oakland Public Library (not individual branches) |
| `bart.gov` | Q250113 | Bay Area Rapid Transit |

### Output

`data/sources/wikidata-domain-qid.parquet` — 1,432,271 domain → QID mappings, 30 MB. Built by grouping `wikidata-p856.parquet` by domain and taking the minimum Q-number per domain.

## Integration with the tile build pipeline

### Distribution format

The final file should be published as a dated gzipped two-column CSV, e.g.:

```
wikidata-website-qid-2026-03.csv.gz
domain,qid
oaklandzoo.org,Q2008530
museumca.org,Q877714
...
```

This mirrors the format and hosting pattern of `qrank.csv.gz` (from `qrank.toolforge.org`), but hosted on `r2-public.protomaps.com` since this is a derived file we generate ourselves. A dated URL (like `Overture-QRank-2025-12-17.parquet`) makes renders reproducible. It would be regenerated periodically by re-running the QLever P856 query and rebuilding.

### Runtime lookup (two-hop via QRank)

At render time the file is downloaded once into the sources directory if not present, then loaded into a `WebsiteQidDb` (a new class modeled on `QrankDb`) as a `HashMap<String, Long>` — domain string → numeric Q-ID.

In `Pois.processOverture`, the website lookup acts as a fallback that fills in a wikidata ID when the feature doesn't have one natively, and then the existing QRank machinery takes over unchanged:

```java
String wikidata = sf.getString("wikidata");  // always null for Overture places theme
if (wikidata == null) {
    String website = /* first entry from sf.getList("websites") */;
    wikidata = websiteQidDb.getQid(website);  // domain → "Q2008530"
}
long qrank = (wikidata != null) ? qrankDb.get(wikidata) : 0;
var qrankedZoom = QrankDb.assignZoom(qrankGrading, kind, qrank);
```

The full lookup chain is:

```
sf.websites[0] → domain → Q-ID (WebsiteQidDb)
                           Q-ID → qrank score (QrankDb)
                                   qrank score → minZoom (assignZoom)
```

No changes are needed to `QrankDb` or `assignZoom` — `QrankDb.get(long)` already accepts a numeric ID. A place only benefits if it has a matching website entry *and* a QRank score; otherwise `qrank = 0` and behavior is identical to today.
