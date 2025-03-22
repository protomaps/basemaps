function get_name_block(
  script_segment: "name" | "name2" | "name3",
  regular?: string,
) {
  let script = "script";

  if (script_segment === "name") {
    script = "script";
  } else if (script_segment === "name2") {
    script = "script2";
  } else if (script_segment === "name3") {
    script = "script3";
  }

  return [
    ["coalesce", ["get", `pgf:${script_segment}`], ["get", script_segment]],
    {
      "text-font": [
        "case",
        ["==", ["get", script], "Devanagari"],
        ["literal", ["Noto Sans Devanagari Regular v1"]],
        ["literal", [regular || "Noto Sans Regular"]],
      ],
    },
  ];
}

function is_not_in_target_script(
  lang: string,
  script: string,
  script_segment: "name" | "name2" | "name3",
) {
  let suffix = "name";
  if (script_segment === "name") {
    suffix = "";
  } else if (script_segment === "name2") {
    suffix = "2";
  } else if (script_segment === "name3") {
    suffix = "3";
  }

  if (script === "Latin") {
    return ["has", `script${suffix}`];
  }

  if (lang === "ja") {
    return [
      "all",
      ["!=", ["get", `script${suffix}`], "Han"],
      ["!=", ["get", `script${suffix}`], "Hiragana"],
      ["!=", ["get", `script${suffix}`], "Katakana"],
      ["!=", ["get", `script${suffix}`], "Mixed-Japanese"],
    ];
  }

  return ["!=", ["get", `script${suffix}`], script];
}

function get_font_formatting(script: string) {
  if (script === "Devanagari") {
    return {
      "text-font": ["literal", ["Noto Sans Devanagari Regular v1"]],
    };
  }
  return {};
}

function get_default_script(lang: string) {
  const pair = language_script_pairs.find((d) => d.lang === lang);
  return pair === undefined ? "Latin" : pair.script;
}

export function get_country_name(lang: string, script?: string) {
  const _script = script || get_default_script(lang);
  let name_prefix: string;
  if (_script === "Devanagari") {
    name_prefix = "pgf:";
  } else {
    name_prefix = "";
  }
  return [
    "format",
    ["coalesce", ["get", `${name_prefix}name:${lang}`], ["get", "name:en"]],
    get_font_formatting(_script),
  ];
}

export function get_multiline_name(
  lang: string,
  script?: string,
  regular?: string,
) {
  const _script = script || get_default_script(lang);
  let name_prefix: string;
  if (_script === "Devanagari") {
    name_prefix = "pgf:";
  } else {
    name_prefix = "";
  }

  const result = [
    "case",
    [
      "all",
      ["any", ["has", "name"], ["has", "pgf:name"]],
      ["!", ["any", ["has", "name2"], ["has", "pgf:name2"]]],
      ["!", ["any", ["has", "name3"], ["has", "pgf:name3"]]],
    ],
    // The local name has 1 script segment: `name`
    [
      "case",
      is_not_in_target_script(lang, _script, "name"),
      // `name` is not in the target script
      [
        "case",
        ["any", ["is-supported-script", ["get", "name"]], ["has", "pgf:name"]],
        // `name` can be rendered correctly
        [
          "format",
          [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "name:en"], // Always fallback to English
          ],
          get_font_formatting(_script),
          "\n",
          {},
          [
            "case",
            [
              "all",
              ["!", ["has", `${name_prefix}name:${lang}`]],
              ["has", "name:en"],
              ["!", ["has", "script"]],
            ],
            // We did fallback to English in the first line and `name` is Latin
            "",
            ["coalesce", ["get", "pgf:name"], ["get", "name"]],
          ],
          {
            "text-font": [
              "case",
              ["==", ["get", "script"], "Devanagari"],
              ["literal", ["Noto Sans Devanagari Regular v1"]],
              ["literal", [regular || "Noto Sans Regular"]],
            ],
          },
        ],
        // `name` cannot be rendered correctly, fallback to `name:en`
        ["get", "name:en"],
      ],
      // `name` is in the target script
      [
        "format",
        [
          "coalesce",
          ["get", `${name_prefix}name:${lang}`],
          ["get", "pgf:name"],
          ["get", "name"],
        ],
        get_font_formatting(_script),
      ],
    ],
    [
      "all",
      ["any", ["has", "name"], ["has", "pgf:name"]],
      ["any", ["has", "name2"], ["has", "pgf:name2"]],
      ["!", ["any", ["has", "name3"], ["has", "pgf:name3"]]],
    ],
    // The local name has 2 script segments: `name` and `name2`
    [
      "case",
      [
        "all",
        is_not_in_target_script(lang, _script, "name"),
        is_not_in_target_script(lang, _script, "name2"),
      ],
      // Both `name` and `name2` are not in the target script
      [
        "format",
        ["get", `${name_prefix}name:${lang}`],
        get_font_formatting(_script),
        "\n",
        {},
        ...get_name_block("name", regular),
        "\n",
        {},
        ...get_name_block("name2", regular),
      ],
      // Either `name` or `name2` is in the target script
      [
        "case",
        is_not_in_target_script(lang, _script, "name2"),
        // `name2` is not in the target script, therefore `name` is in the target script
        [
          "format",
          [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "pgf:name"],
            ["get", "name"],
          ],
          get_font_formatting(_script),
          "\n",
          {},
          ...get_name_block("name2", regular),
        ],
        // `name2` is in the target script, therefore `name` is not in the target script
        [
          "format",
          [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "pgf:name2"],
            ["get", "name2"],
          ],
          get_font_formatting(_script),
          "\n",
          {},
          ...get_name_block("name", regular),
        ],
      ],
    ],
    // The local name has 3 script segments: `name`, `name2`, and `name3`
    [
      "case",
      [
        "all",
        is_not_in_target_script(lang, _script, "name"),
        is_not_in_target_script(lang, _script, "name2"),
        is_not_in_target_script(lang, _script, "name3"),
      ],
      // All three `name`, `name2`, and `name3` are not in the target script
      [
        "format",
        ["get", `${name_prefix}name:${lang}`],
        get_font_formatting(_script),
        "\n",
        {},
        ...get_name_block("name", regular),
        "\n",
        {},
        ...get_name_block("name2", regular),
        "\n",
        {},
        ...get_name_block("name3", regular),
      ],
      // Exactly one of the 3 script segments `name`, `name2`, or `name3` is in the target script
      [
        "case",
        ["!", is_not_in_target_script(lang, _script, "name")],
        // `name` is in the target script, and `name2` and `name3` are not
        [
          "format",
          [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "pgf:name"],
            ["get", "name"],
          ],
          get_font_formatting(_script),
          "\n",
          {},
          ...get_name_block("name2", regular),
          "\n",
          {},
          ...get_name_block("name3", regular),
        ],
        ["!", is_not_in_target_script(lang, _script, "name2")],
        // `name2` is in the target script, and `name` and `name3` are not
        [
          "format",
          [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "pgf:name2"],
            ["get", "name2"],
          ],
          get_font_formatting(_script),
          "\n",
          {},
          ...get_name_block("name", regular),
          "\n",
          {},
          ...get_name_block("name3", regular),
        ],
        // `name3` is in the target script, and `name` and `name2` are not
        [
          "format",
          [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "pgf:name3"],
            ["get", "name3"],
          ],
          get_font_formatting(_script),
          "\n",
          {},
          ...get_name_block("name", regular),
          "\n",
          {},
          ...get_name_block("name2", regular),
        ],
      ],
    ],
  ];
  return result;
}

export const language_script_pairs = [
  {
    lang: "ar",
    full_name: "Arabic",
    script: "Arabic",
  },
  {
    lang: "cs",
    full_name: "Czech",
    script: "Latin",
  },
  {
    lang: "bg",
    full_name: "Bulgarian",
    script: "Cyrillic",
  },
  {
    lang: "da",
    full_name: "Danish",
    script: "Latin",
  },
  {
    lang: "de",
    full_name: "German",
    script: "Latin",
  },
  {
    lang: "el",
    full_name: "Greek",
    script: "Greek",
  },
  {
    lang: "en",
    full_name: "English",
    script: "Latin",
  },
  {
    lang: "es",
    full_name: "Spanish",
    script: "Latin",
  },
  {
    lang: "et",
    full_name: "Estonian",
    script: "Latin",
  },
  {
    lang: "fa",
    full_name: "Persian",
    script: "Arabic",
  },
  {
    lang: "fi",
    full_name: "Finnish",
    script: "Latin",
  },
  {
    lang: "fr",
    full_name: "French",
    script: "Latin",
  },
  {
    lang: "ga",
    full_name: "Irish",
    script: "Latin",
  },
  {
    lang: "he",
    full_name: "Hebrew",
    script: "Hebrew",
  },
  {
    lang: "hi",
    full_name: "Hindi",
    script: "Devanagari",
  },
  {
    lang: "hr",
    full_name: "Croatian",
    script: "Latin",
  },
  {
    lang: "hu",
    full_name: "Hungarian",
    script: "Latin",
  },
  {
    lang: "id",
    full_name: "Indonesian",
    script: "Latin",
  },
  {
    lang: "it",
    full_name: "Italian",
    script: "Latin",
  },
  {
    lang: "ja",
    full_name: "Japanese",
    // Japanese is a special case, using multiple scripts
    script: "",
  },
  {
    lang: "ko",
    full_name: "Korean",
    script: "Hangul",
  },
  {
    lang: "lt",
    full_name: "Lithuanian",
    script: "Latin",
  },
  {
    lang: "lv",
    full_name: "Latvian",
    script: "Latin",
  },
  {
    lang: "ne",
    full_name: "Nepali",
    script: "Devanagari",
  },
  {
    lang: "nl",
    full_name: "Dutch",
    script: "Latin",
  },
  {
    lang: "no",
    full_name: "Norwegian",
    script: "Latin",
  },
  {
    lang: "mr",
    full_name: "Marathi",
    script: "Devanagari",
  },
  {
    lang: "mt",
    full_name: "Maltese",
    script: "Latin",
  },
  {
    lang: "pl",
    full_name: "Polish",
    script: "Latin",
  },
  {
    lang: "pt",
    full_name: "Portuguese",
    script: "Latin",
  },
  {
    lang: "ro",
    full_name: "Romanian",
    script: "Latin",
  },
  {
    lang: "ru",
    full_name: "Russian",
    script: "Cyrillic",
  },
  {
    lang: "sk",
    full_name: "Slovak",
    script: "Latin",
  },
  {
    lang: "sl",
    full_name: "Slovenian",
    script: "Latin",
  },
  {
    lang: "sv",
    full_name: "Swedish",
    script: "Latin",
  },
  {
    lang: "tr",
    full_name: "Turkish",
    script: "Latin",
  },
  {
    lang: "uk",
    full_name: "Ukrainian",
    script: "Cyrillic",
  },
  {
    lang: "ur",
    full_name: "Urdu",
    script: "Arabic",
  },
  {
    lang: "vi",
    full_name: "Vietnamese",
    script: "Latin",
  },
  {
    lang: "zh-Hans",
    full_name: "Chinese (Simplified)",
    script: "Han",
  },
  {
    lang: "zh-Hant",
    full_name: "Chinese (Traditional)",
    script: "Han",
  },
];
