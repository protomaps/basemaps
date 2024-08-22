function get_name_block(script_segment: "name" | "name2" | "name3") {
  let script;

  if (script_segment === "name") {
    script = "script";
  } else if (script_segment === "name2") {
    script = "script2";
  } else if (script_segment === "name3") {
    script = "script3";
  }

  return [
    [
      "coalesce",
      ["get", `pmap:pgf:${script_segment}`],
      ["get", script_segment],
    ],
    {
      "text-font": [
        "case",
        ["==", ["get", `pmap:${script}`], "Devanagari"],
        ["literal", ["Noto Sans Devanagari Regular v1"]],
        ["literal", ["Noto Sans Regular"]],
      ],
    },
  ];
}

function is_not_in_target_script(
  lang: string,
  script: string,
  script_segment: "name" | "name2" | "name3",
) {
  let suffix;
  if (script_segment === "name") {
    suffix = "";
  } else if (script_segment === "name2") {
    suffix = "2";
  } else if (script_segment === "name3") {
    suffix = "3";
  }

  if (script === "Latin") {
    return ["has", `pmap:script${suffix}`];
  } else if (lang === "ja") {
    return [
      "all",
      ["!=", ["get", `pmap:script${suffix}`], "Han"],
      ["!=", ["get", `pmap:script${suffix}`], "Hiragana"],
      ["!=", ["get", `pmap:script${suffix}`], "Katakana"],
      ["!=", ["get", `pmap:script${suffix}`], "Mixed-Japanese"],
    ];
  } else {
    return ["!=", ["get", `pmap:script${suffix}`], script];
  }
}

function get_font_formatting(script: string) {
  if (script === "Devanagari") {
    return {
      "text-font": ["literal", ["Noto Sans Devanagari Regular v1"]],
    };
  } else {
    return {};
  }
}

export function get_country_name(lang: string, script: string) {
  let name_prefix;
  if (script === "Devanagari") {
    name_prefix = "pmap:pgf:";
  } else {
    name_prefix = "";
  }
  return [
    "format",
    ["coalesce", ["get", `${name_prefix}name:${lang}`], ["get", "name:en"]],
    get_font_formatting(script),
  ];
}

export function get_multiline_name(lang: string, script: string) {
  let name_prefix;
  if (script === "Devanagari") {
    name_prefix = "pmap:pgf:";
  } else {
    name_prefix = "";
  }

  const result = [
    "case",
    [
      "all",
      ["any", ["has", "name"], ["has", "pmap:pgf:name"]],
      ["!", ["any", ["has", "name2"], ["has", "pmap:pgf:name2"]]],
      ["!", ["any", ["has", "name3"], ["has", "pmap:pgf:name3"]]],
    ],
    // The local name has 1 script segment: `name`
    [
      "case",
      is_not_in_target_script(lang, script, "name"),
      // `name` is not in the target script
      [
        "format",
        [
          "coalesce",
          ["get", `${name_prefix}name:${lang}`],
          ["get", "name:en"], // Always fallback to English
        ],
        get_font_formatting(script),
        "\n",
        {},
        [
          "case",
          [
            "all",
            ["!", ["has", `${name_prefix}name:${lang}`]],
            ["has", "name:en"],
            ["!", ["has", "pmap:script"]],
          ],
          // We did fallback to English in the first line and `name` is Latin
          "",
          ["coalesce", ["get", "pmap:pgf:name"], ["get", "name"]],
        ],
        {
          "text-font": [
            "case",
            ["==", ["get", "pmap:script"], "Devanagari"],
            ["literal", ["Noto Sans Devanagari Regular v1"]],
            ["literal", ["Noto Sans Regular"]],
          ],
        },
      ],
      // `name` is in the target script
      [
        "format",
        [
          "coalesce",
          ["get", `${name_prefix}name:${lang}`],
          ["get", "pmap:pgf:name"],
          ["get", "name"],
        ],
        get_font_formatting(script),
      ],
    ],
    [
      "all",
      ["any", ["has", "name"], ["has", "pmap:pgf:name"]],
      ["any", ["has", "name2"], ["has", "pmap:pgf:name2"]],
      ["!", ["any", ["has", "name3"], ["has", "pmap:pgf:name3"]]],
    ],
    // The local name has 2 script segments: `name` and `name2`
    [
      "case",
      [
        "all",
        is_not_in_target_script(lang, script, "name"),
        is_not_in_target_script(lang, script, "name2"),
      ],
      // Both `name` and `name2` are not in the target script
      [
        "format",
        ["get", `${name_prefix}name:${lang}`],
        get_font_formatting(script),
        "\n",
        {},
        ...get_name_block("name"),
        "\n",
        {},
        ...get_name_block("name2"),
      ],
      // Either `name` or `name2` is in the target script
      [
        "case",
        is_not_in_target_script(lang, script, "name2"),
        // `name2` is not in the target script, therefore `name` is in the target script
        [
          "format",
          [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "pmap:pgf:name"],
            ["get", "name"],
          ],
          get_font_formatting(script),
          "\n",
          {},
          ...get_name_block("name2"),
        ],
        // `name2` is in the target script, therefore `name` is not in the target script
        [
          "format",
          [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "pmap:pgf:name2"],
            ["get", "name2"],
          ],
          get_font_formatting(script),
          "\n",
          {},
          ...get_name_block("name"),
        ],
      ],
    ],
    // The local name has 3 script segments: `name`, `name2`, and `name3`
    [
      "case",
      [
        "all",
        is_not_in_target_script(lang, script, "name"),
        is_not_in_target_script(lang, script, "name2"),
        is_not_in_target_script(lang, script, "name3"),
      ],
      // All three `name`, `name2`, and `name3` are not in the target script
      [
        "format",
        ["get", `${name_prefix}name:${lang}`],
        get_font_formatting(script),
        "\n",
        {},
        ...get_name_block("name"),
        "\n",
        {},
        ...get_name_block("name2"),
        "\n",
        {},
        ...get_name_block("name3"),
      ],
      // Exactly one of the 3 script segments `name`, `name2`, or `name3` is in the target script
      [
        "case",
        ["!", is_not_in_target_script(lang, script, "name")],
        // `name` is in the target script, and `name2` and `name3` are not
        [
          "format",
          [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "pmap:pgf:name"],
            ["get", "name"],
          ],
          get_font_formatting(script),
          "\n",
          {},
          ...get_name_block("name2"),
          "\n",
          {},
          ...get_name_block("name3"),
        ],
        ["!", is_not_in_target_script(lang, script, "name2")],
        // `name2` is in the target script, and `name` and `name3` are not
        [
          "format",
          [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "pmap:pgf:name2"],
            ["get", "name2"],
          ],
          get_font_formatting(script),
          "\n",
          {},
          ...get_name_block("name"),
          "\n",
          {},
          ...get_name_block("name3"),
        ],
        // `name3` is in the target script, and `name` and `name2` are not
        [
          "format",
          [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "pmap:pgf:name3"],
            ["get", "name3"],
          ],
          get_font_formatting(script),
          "\n",
          {},
          ...get_name_block("name"),
          "\n",
          {},
          ...get_name_block("name2"),
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
