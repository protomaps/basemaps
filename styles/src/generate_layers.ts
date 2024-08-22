// @ts-nocheck
declare const process: unknown;

import i from "./index";

if (process.argv.length < 2) {
  process.stdout.write("usage: generate-layers SOURCE_NAME THEME");
  process.exit(1);
}
const args = process.argv.slice(2);

const lang = 'en';
const script = 'Latin';

process.stdout.write(JSON.stringify(i(args[0], args[1], lang, script), null, 2));
