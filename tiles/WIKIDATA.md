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

## Remaining open question

When a domain maps to multiple QIDs, which to prefer? Options:
- The QID whose P856 URL most closely matches the full Overture URL (not just domain)
- The QID with the most Wikidata statements (a proxy for "most notable")
- The QID that is an instance of a place type matching the Overture category
