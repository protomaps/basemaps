function get_name_block(script_segment: 'name' | 'name2' | 'name3') {
    let script;

    if (script_segment === 'name') {
        script = 'script';
    }
    else if (script_segment === 'name2') {
        script = 'script2';
    }
    else if (script_segment === 'name3') {
        script = 'script3';
    }

    return [
        [
            "coalesce",
            ["get", `pmap:pgf:${script_segment}`],
            ["get", script_segment]
        ],
        {
            "text-font": [
                "case",
                ["==", ["get", `pmap:${script}`], "Devanagari"],
                ["literal", ["Noto Sans Devanagari Regular v1"]],
                ["literal", ["Noto Sans Regular"]]
            ]
        }
    ];
}

function is_not_in_target_script(lang: string, script: string, script_segment: 'name' | 'name2' | 'name3') {
    
    let suffix;
    if (script_segment === 'name'){
        suffix = '';
    }
    else if (script_segment === 'name2') {
        suffix = '2';
    }
    else if (script_segment === 'name3') {
        suffix = '3';
    }

    if (script === 'Latin') {
        return ["has", `pmap:script${suffix}`]
    }
    else if (lang === 'ja') {
        return [
            "all",
            ["!=", ["get", `pmap:script${suffix}`], "Han"],
            ["!=", ["get", `pmap:script${suffix}`], "Hiragana"],
            ["!=", ["get", `pmap:script${suffix}`], "Katakana"],
            ["!=", ["get", `pmap:script${suffix}`], "Mixed-Japanese"] 
        ];
    }
    else {
        return ["!=", ["get", `pmap:script${suffix}`], script];
    }
}

function get_font_formatting(script: string) {
    if (script === "Devanagari") {
        return {
            "text-font": ["literal", ["Noto Sans Devanagari Regular v1"]]
        };
    }
    else {
        return {};
    }
}

export function get_country_name(lang: string, script: string) {
    let name_prefix;
    if (script === 'Devanagari') {
        name_prefix = 'pmap:pgf:';
    }
    else {
        name_prefix = '';
    }
    return [
        "format",
        [
            "coalesce",
            ["get", `${name_prefix}name:${lang}`],
            ["get", "name:en"]
        ],
        get_font_formatting(script)
    ]
}

export function get_multiline_name(
    lang: string,
    script: string
) {
    let name_prefix;
    if (script === 'Devanagari') {
        name_prefix = 'pmap:pgf:';
    }
    else {
        name_prefix = '';
    }
    
    const result = [
        "case",
        [
            "all",
            [
                "any",
                ["has", "name"],
                ["has", "pmap:pgf:name"]
            ],
            ["!", [
                "any",
                ["has", "name2"],
                ["has", "pmap:pgf:name2"]
            ]],
            ["!", [
                "any",
                ["has", "name3"],
                ["has", "pmap:pgf:name3"]
            ]]
        ],
        // The local name has 1 script segment: `name`
        [
            "case",
            is_not_in_target_script(lang, script, 'name'),
            // `name` is not in the target script
            [
                "format",
                [
                    "coalesce",
                    ["get", `${name_prefix}name:${lang}`],
                    ["get", "name:en"] // Always fallback to English 
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
                        ["!", ["has", "pmap:script"]]
                    ],
                    // We did fallback to English in the first line and `name` is Latin
                    "",
                    [
                        "coalesce",
                        ["get", "pmap:pgf:name"],
                        ["get", "name"]
                    ]
                ],
                {
                    "text-font": [
                        "case",
                        ["==", ["get", "pmap:script"], "Devanagari"],
                        ["literal", ["Noto Sans Devanagari Regular v1"]],
                        ["literal", ["Noto Sans Regular"]]
                    ]
                }
            ],
            // `name` is in the target script
            [
                "format",
                [
                    "coalesce",
                    ["get", `${name_prefix}name:${lang}`],
                    ["get", "pmap:pgf:name"],
                    ["get", "name"]
                ],
                get_font_formatting(script)
            ]
        ],
        [
            "all",
            [
                "any",
                ["has", "name"],
                ["has", "pmap:pgf:name"]
            ],
            [
                "any",
                ["has", "name2"],
                ["has", "pmap:pgf:name2"]
            ],
            ["!", [
                "any",
                ["has", "name3"],
                ["has", "pmap:pgf:name3"]
            ]]
        ],
        // The local name has 2 script segments: `name` and `name2`
        [
            "case",
            [
                "all",
                is_not_in_target_script(lang, script, 'name'),
                is_not_in_target_script(lang, script, 'name2')
            ],
            // Both `name` and `name2` are not in the target script
            [
                "format",
                ["get", `${name_prefix}name:${lang}`],
                get_font_formatting(script),
                "\n",
                {},
                ...get_name_block('name'),
                "\n",
                {},
                ...get_name_block('name2')
            ],
            // Either `name` or `name2` is in the target script
            [
                "case",
                is_not_in_target_script(lang, script, 'name2'),
                // `name2` is not in the target script, therefore `name` is in the target script
                [
                    "format",
                    [
                        "coalesce",
                        ["get", `${name_prefix}name:${lang}`],
                        ["get", "pmap:pgf:name"],
                        ["get", "name"]
                    ],
                    get_font_formatting(script),
                    "\n",
                    {},
                    ...get_name_block('name2')
                ],
                // `name2` is in the target script, therefore `name` is not in the target script
                [
                    "format",
                    [
                        "coalesce",
                        ["get", `${name_prefix}name:${lang}`],
                        ["get", "pmap:pgf:name2"],
                        ["get", "name2"]
                    ],
                    get_font_formatting(script),
                    "\n",
                    {},
                    ...get_name_block('name')
                ]
            ]
        ],
        // The local name has 3 script segments: `name`, `name2`, and `name3`
        [
            "case",
            [
                "all",
                is_not_in_target_script(lang, script, 'name'),
                is_not_in_target_script(lang, script, 'name2'),
                is_not_in_target_script(lang, script, 'name3')
            ],
            // All three `name`, `name2`, and `name3` are not in the target script
            [
                "format",
                ["get", `${name_prefix}name:${lang}`],
                get_font_formatting(script),
                "\n",
                {},
                ...get_name_block('name'),
                "\n",
                {},
                ...get_name_block('name2'),
                "\n",
                {},
                ...get_name_block('name3')
            ],
            // Exactly one of the 3 script segments `name`, `name2`, or `name3` is in the target script
            [
                "case",
                ["!", is_not_in_target_script(lang, script, 'name')],
                // `name` is in the target script, and `name2` and `name3` are not
                [
                    "format",
                    [
                        "coalesce",
                        ["get", `${name_prefix}name:${lang}`],
                        ["get", "pmap:pgf:name"],
                        ["get", "name"]
                    ],
                    get_font_formatting(script),
                    "\n",
                    {},
                    ...get_name_block('name2'),
                    "\n",
                    {},
                    ...get_name_block('name3')
                ],
                ["!", is_not_in_target_script(lang, script, 'name2')],
                // `name2` is in the target script, and `name` and `name3` are not
                [
                    "format",
                    [
                        "coalesce",
                        ["get", `${name_prefix}name:${lang}`],
                        ["get", "pmap:pgf:name2"],
                        ["get", "name2"]
                    ],
                    get_font_formatting(script),
                    "\n",
                    {},
                    ...get_name_block('name'),
                    "\n",
                    {},
                    ...get_name_block('name3')
                ],
                // `name3` is in the target script, and `name` and `name2` are not
                [
                    "format",
                    [
                        "coalesce",
                        ["get", `${name_prefix}name:${lang}`],
                        ["get", "pmap:pgf:name3"],
                        ["get", "name3"]
                    ],
                    get_font_formatting(script),
                    "\n",
                    {},
                    ...get_name_block('name'),
                    "\n",
                    {},
                    ...get_name_block('name2')
                ]
            ]
        ]
    ];
    return result;
}