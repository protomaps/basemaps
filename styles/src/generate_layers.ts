// @ts-nocheck
declare const process: unknown;

import i from "./index";

const args = process.argv.slice(2);

if (args.length < 2) {
	process.stdout.write(
		"usage: generate-layers SOURCE_NAME THEME_OR_PATH_TO_THEME"
	);
	process.exit(1);
}

i(...args).then((res) => process.stdout.write(JSON.stringify(res, null, 2)));
