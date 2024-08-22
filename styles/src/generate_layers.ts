// @ts-nocheck
declare const process: unknown;

import i from "./index";

if (process.argv.length < 4) {
  process.stdout.write("usage: generate-layers SOURCE_NAME THEME LANG SCRIPT");
  process.exit(1);
}
const args = process.argv.slice(2);

process.stdout.write(JSON.stringify(i(args[0], args[1], args[2], args[3]), null, 2));
