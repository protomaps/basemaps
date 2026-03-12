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
- Too large to bulk-query Wikidata for all places.
- Feasible as a **pre-built lookup table** scoped to notable categories: `zoo`, `museum`, `airport`, `stadium`, `aquarium`, `university`, `library` — estimated ~5–10k distinct domains.

### Caveats

- Multiple Overture features can share the same domain (e.g. all Oakland Public Library branches → `oaklandlibrary.org`), so the match links to the organization entity, not a specific location.
- No coverage for places without websites (~half of all features).

## Recommended next step

Build a one-time lookup table by:
1. Extracting root domains from `websites` for notable-category features
2. Batching SPARQL queries to Wikidata P856 to retrieve QIDs
3. Joining back to Overture IDs and storing as a small CSV/parquet for use in tile generation
