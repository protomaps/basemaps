// @ts-nocheck
declare const process: unknown;

import i from "./index";
import { writeFile } from 'fs/promises';

if (process.argv.length < 3) {
  process.stdout.write("usage: generate-style SOURCE_NAME THEME TILES_URL");
  process.exit(1);
}
const args = process.argv.slice(2);

const language_script_pairs = [
    {
        "lang": "ar",
        "full_name": "Arabic",
        "script": "Arabic"
    },
    {
        "lang": "cs",
        "full_name": "Czech",
        "script": "Latin"
    },
    {
        "lang": "bg",
        "full_name": "Bulgarian",
        "script": "Cyrillic"
    },
    {
        "lang": "da",
        "full_name": "Danish",
        "script": "Latin"
    },
    {
        "lang": "de",
        "full_name": "German",
        "script": "Latin"
    },
    {
        "lang": "el",
        "full_name": "Greek",
        "script": "Greek"
    },
    {
        "lang": "en",
        "full_name": "English",
        "script": "Latin"
    },
    {
        "lang": "es",
        "full_name": "Spanish",
        "script": "Latin"
    },
    {
        "lang": "et",
        "full_name": "Estonian",
        "script": "Latin"
    },
    {
        "lang": "fa",
        "full_name": "Persian",
        "script": "Arabic"
    },
    {
        "lang": "fi",
        "full_name": "Finnish",
        "script": "Latin"
    },
    {
        "lang": "fr",
        "full_name": "French",
        "script": "Latin"
    },
    {
        "lang": "ga",
        "full_name": "Irish",
        "script": "Latin"
    },
    {
        "lang": "he",
        "full_name": "Hebrew",
        "script": "Hebrew"
    },
    {
        "lang": "hi",
        "full_name": "Hindi",
        "script": "Devanagari"
    },
    {
        "lang": "hr",
        "full_name": "Croatian",
        "script": "Latin"
    },
    {
        "lang": "hu",
        "full_name": "Hungarian",
        "script": "Latin"
    },
    {
        "lang": "id",
        "full_name": "Indonesian",
        "script": "Latin"
    },
    {
        "lang": "it",
        "full_name": "Italian",
        "script": "Latin"
    },
    {
        "lang": "ja",
        "full_name": "Japanese",
        "script": ""
    },
    {
        "lang": "ko",
        "full_name": "Korean",
        "script": ""
    },
    {
        "lang": "lt",
        "full_name": "Lithuanian",
        "script": "Latin"
    },
    {
        "lang": "lv",
        "full_name": "Latvian",
        "script": "Latin"
    },
    {
        "lang": "ne",
        "full_name": "Nepali",
        "script": "Devanagari"
    },
    {
        "lang": "nl",
        "full_name": "Dutch",
        "script": "Latin"
    },
    {
        "lang": "no",
        "full_name": "Norwegian",
        "script": "Latin"
    },
    {
        "lang": "mr",
        "full_name": "Marathi",
        "script": "Devanagari"
    },
    {
        "lang": "mt",
        "full_name": "Maltese",
        "script": "Latin"
    },
    {
        "lang": "pl",
        "full_name": "Polish",
        "script": "Latin"
    },
    {
        "lang": "pt",
        "full_name": "Portuguese",
        "script": "Latin"
    },
    {
        "lang": "ro",
        "full_name": "Romanian",
        "script": "Latin"
    },
    {
        "lang": "ru",
        "full_name": "Russian",
        "script": "Cyrillic"
    },
    {
        "lang": "sk",
        "full_name": "Slovak",
        "script": "Latin"
    },
    {
        "lang": "sl",
        "full_name": "Slovenian",
        "script": "Latin"
    },
    {
        "lang": "sv",
        "full_name": "Swedish",
        "script": "Latin"
    },
    {
        "lang": "tr",
        "full_name": "Turkish",
        "script": "Latin"
    },
    {
        "lang": "uk",
        "full_name": "Ukrainian",
        "script": "Cyrillic"
    },
    {
        "lang": "ur",
        "full_name": "Urdu",
        "script": "Arabic"
    },
    {
        "lang": "vi",
        "full_name": "Vietnamese",
        "script": "Latin"
    },
    {
        "lang": "zh",
        "full_name": "Chinese (General)",
        "script": ""
    },
    {
        "lang": "zh-Hans",
        "full_name": "Chinese (Simplified)",
        "script": ""
    },
    {
        "lang": "zh-Hant",
        "full_name": "Chinese (Traditional)",
        "script": ""
    }
];

for (const {lang, full_name, script} of language_script_pairs) {

    // console.log(`<option value="${lang}">${full_name}</option>`)
    //     console.log(`else if (['${lang}'].includes(language)) {
    //     script = '${script}';
    // }`)
    const layers = i(args[0], args[1], lang, script);

    const style = {
      "version": 8,
      "sources": {
          "protomaps": {
              "type": "vector",
              "attribution": "<a href=\"https://github.com/protomaps/basemaps\">Protomaps</a> © <a href=\"https://openstreetmap.org\">OpenStreetMap</a>",
              "url": args[2]
          }
      },
      "layers": layers,
      "sprite": "https://protomaps.github.io/basemaps-assets/sprites/v3/light",
      "glyphs": "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf"
    }

    try {
        await writeFile(`languages/style-${lang}-${script}.json`, JSON.stringify(style, null, 2));
    } catch (err) {
        console.error('An error occurred while writing to the file:', err); 
    }
}
