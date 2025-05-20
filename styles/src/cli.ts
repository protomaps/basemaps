#!/usr/bin/env tsx

import { writeFile } from "fs/promises";
import { generateStyle } from "./generate_style";

const result = await generateStyle(process.argv.slice(3)).catch((err) => {
  console.error(err);
  process.exit(1);
});

await writeFile(process.argv[2], result);
