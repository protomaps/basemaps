export const VERSION_COMPATIBILITY: Record<number, number[]> = {
  4: [4, 5],
  3: [3],
  2: [2],
  1: [1],
};

// Get the hash contents as a map.
export function parseHash(hash: string): Record<string, string> {
  const retval: Record<string, string> = {};
  for (const pair of hash.replace("#", "").split("&")) {
    const parts = pair.split("=");
    retval[parts[0]] = parts[1];
  }
  return retval;
}

// Given the current hash and a record of string->strings, creates the new hash
export function createHash(
  currentHash: string,
  newHash: Record<string, string | undefined>,
): string {
  const current = parseHash(currentHash);
  const combined = { ...current, ...newHash };
  return `#${Object.entries(combined)
    .filter(([_, value]) => {
      return value !== undefined;
    })
    .map(([key, value]) => {
      return `${key}=${value}`;
    })
    .join("&")}`;
}

export async function layersForVersion(version: string, theme?: string) {
  const resp = await fetch(
    `https://npm-style.protomaps.dev/layers.json?version=${version}&theme=${theme || "light"}&lang=en`,
  );
  return await resp.json();
}

export const isValidPMTiles = (tiles?: string): boolean => {
  if (!tiles) return false;
  if (!tiles.startsWith("http") && tiles.endsWith(".pmtiles")) return true;
  if (tiles.startsWith("http") && new URL(tiles).pathname.endsWith(".pmtiles"))
    return true;
  return false;
};
