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
        ["get", `pmap:pgf:${script_segment}`],
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

export function places_locality_text_field(
    lang: string,
    script: string
) {
    const result = [
        "case",
        [
            "all",
            ["has", "pmap:pgf:name"],
            ["!", ["has", "pmap:pgf:name2"]],
            ["!", ["has", "pmap:pgf:name3"]]
        ],
        // The local name has 1 script segment: `name`
        [
            "case",
            script === "Latin" ? ["has", "pmap:script"] : ["!=", ["get", "pmap:script"], script],
            // `name` is not in the target script
            [
                "format",
                [
                    "coalesce",
                    ["get", `pmap:pgf:name:${lang}`],
                    ["get", "pmap:pgf:name:en"] // Always fallback to English 
                ],
                get_font_formatting(script),
                "\n",
                {},
                [
                    "case",
                    [
                        "all",
                        ["!", ["has", `pmap:pgf:name:${lang}`]],
                        ["has", "pmap:pgf:name:en"],
                        ["!", ["has", "pmap:script"]]
                    ],
                    // We did fallback to English in the first line and `name` is Latin
                    "",
                    ["get", "pmap:pgf:name"]
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
                    ["get", `pmap:pgf:name:${lang}`],
                    ["get", "pmap:pgf:name"]
                ],
                get_font_formatting(script)
            ]
        ],
        [
            "all",
            ["has", "pmap:pgf:name"],
            ["has", "pmap:pgf:name2"],
            ["!", ["has", "pmap:pgf:name3"]]
        ],
        // The local name has 2 script segments: `name` and `name2`
        [
            "case",
            [
                "all",
                script === "Latin" ? ["has", "pmap:script"] : ["!=", ["get", "pmap:script"], script],
                script === "Latin" ? ["has", "pmap:script2"] : ["!=", ["get", "pmap:script2"], script]
            ],
            // Both `name` and `name2` are not in the target script
            [
                "format",
                ["get", `pmap:pgf:name:${lang}`],
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
                script === "Latin" ? ["!", ["has", "pmap:script"]] : ["==", ["get", "pmap:script"], script],
                // `name` is in the target script, and `name2` is not in the target script
                [
                    "format",
                    [
                        "coalesce",
                        ["get", `pmap:pgf:name:${lang}`],
                        ["get", "pmap:pgf:name"]
                    ],
                    get_font_formatting(script),
                    "\n",
                    {},
                    ...get_name_block('name2')
                ],
                // `name2` is in the target script, and `name` is not in the target script
                [
                    "format",
                    [
                        "coalesce",
                        ["get", `pmap:pgf:name:${lang}`],
                        ["get", "pmap:pgf:name2"]
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
                script === "Latin" ? ["has", "pmap:script"] : ["!=", ["get", "pmap:script"], script],
                script === "Latin" ? ["has", "pmap:script2"] : ["!=", ["get", "pmap:script2"], script],
                script === "Latin" ? ["has", "pmap:script2"] : ["!=", ["get", "pmap:script3"], script]
            ],
            // All three `name`, `name2`, and `name3` are not in the target script
            [
                "format",
                ["get", `pmap:pgf:name:${lang}`],
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
                script === "Latin" ? ["!", ["has", "pmap:script"]] : ["==", ["get", "pmap:script"], script],
                // `name` is in the target script, and `name2` and `name3` are not
                [
                    "format",
                    [
                        "coalesce",
                        ["get", `pmap:pgf:name:${lang}`],
                        ["get", "pmap:pgf:name"]
                    ],
                    get_font_formatting(script),
                    "\n",
                    {},
                    ...get_name_block('name2'),
                    "\n",
                    {},
                    ...get_name_block('name3')
                ],
                script === "Latin" ? ["!", ["has", "pmap:script2"]] : ["==", ["get", "pmap:script2"], script],
                // `name2` is in the target script, and `name` and `name3` are not
                [
                    "format",
                    [
                        "coalesce",
                        ["get", `pmap:pgf:name:${lang}`],
                        ["get", "pmap:pgf:name2"]
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
                        ["get", `pmap:pgf:name:${lang}`],
                        ["get", "pmap:pgf:name3"]
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