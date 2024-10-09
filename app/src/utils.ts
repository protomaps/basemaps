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

export async function layersForVersion (version: string, theme?: string) {
  if (version >= "4.0.0") {
    const resp = await fetch(
      `https://unpkg.com/protomaps-themes-base@${version}/dist/styles/${theme || "light"}/en.json`,
    );
    return (await resp.json()).layers;
  } else {
    const resp = await fetch(
      `https://unpkg.com/protomaps-themes-base@${version}/dist/layers/${theme || "light"}.json`,
    );
    return await resp.json();
  }
};
