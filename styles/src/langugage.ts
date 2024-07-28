export function places_locality_text_field(
    lang: string,
    script: string
) {
    const result = [
        "case",
        [
            "all",
            ["has", "name"],
            ["!", ["has", "name2"]],
            ["!", ["has", "name3"]]
        ],
        [
            "case",
            script === "Latin" ? ["has", "pmap:script"] : ["!=", ["get", "pmap:script"], script],
            [
                "format",
                [
                    "coalesce",
                    ["get", `name:${lang}`],
                    ["get", "name:en"]
                ],
                {},
                "\n",
                {},
                [
                    "case",
                    [
                        "all",
                        ["!", ["has", `name:${lang}`]],
                        ["has", "name:en"],
                        ["!", ["has", "pmap:script"]]
                    ],
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
            [
                "coalesce",
                ["get", `name:${lang}`],
                ["get", "name"]
            ]
        ],
        [
            "all",
            ["has", "name"],
            ["has", "name2"],
            ["!", ["has", "name3"]]
        ],
        [
            "case",
            [
                "all",
                script === "Latin" ? ["has", "pmap:script"] : ["!=", ["get", "pmap:script"], script],
                script === "Latin" ? ["has", "pmap:script2"] : ["!=", ["get", "pmap:script2"], script]
            ],
            [
                "format",
                [
                    "coalesce",
                    ["get", `name:${lang}`]
                ],
                {},
                "\n",
                {},
                ["get", "pmap:pgf:name"],
                {
                    "text-font": [
                        "case",
                        ["==", ["get", "pmap:script"], "Devanagari"],
                        ["literal", ["Noto Sans Devanagari Regular v1"]],
                        ["literal", ["Noto Sans Regular"]]
                    ]
                },
                "\n",
                {},
                ["get", "pmap:pgf:name2"],
                {
                    "text-font": [
                        "case",
                        ["==", ["get", "pmap:script2"], "Devanagari"],
                        ["literal", ["Noto Sans Devanagari Regular v1"]],
                        ["literal", ["Noto Sans Regular"]]
                    ]
                }
            ],
            [
                "case",
                script === "Latin" ? ["!", ["has", "pmap:script"]] : ["==", ["get", "pmap:script"], script],
                [
                    "format",
                    ["get", "name"],
                    {},
                    "\n",
                    {},
                    ["get", "pmap:pgf:name2"],
                    {
                        "text-font": [
                            "case",
                            ["==", ["get", "pmap:script2"], "Devanagari"],
                            ["literal", ["Noto Sans Devanagari Regular v1"]],
                            ["literal", ["Noto Sans Regular"]]
                        ]
                    }
                ],
                [
                    "format",
                    ["get", "name2"],
                    {},
                    "\n",
                    {},
                    ["get", "pmap:pgf:name"],
                    {
                        "text-font": [
                            "case",
                            ["==", ["get", "pmap:script"], "Devanagari"],
                            ["literal", ["Noto Sans Devanagari Regular v1"]],
                            ["literal", ["Noto Sans Regular"]]
                        ]
                    }
                ]
            ]
        ],
        [
            "case",
            [
                "all",
                script === "Latin" ? ["has", "pmap:script"] : ["!=", ["get", "pmap:script"], script],
                script === "Latin" ? ["has", "pmap:script2"] : ["!=", ["get", "pmap:script2"], script],
                script === "Latin" ? ["has", "pmap:script2"] : ["!=", ["get", "pmap:script3"], script]
            ],
            [
                "format",
                [
                    "coalesce",
                    ["get", `name:${lang}`]
                ],
                {},
                "\n",
                {},
                ["get", "pmap:pgf:name"],
                {
                    "text-font": [
                        "case",
                        ["==", ["get", "pmap:script"], "Devanagari"],
                        ["literal", ["Noto Sans Devanagari Regular v1"]],
                        ["literal", ["Noto Sans Regular"]]
                    ]
                },
                "\n",
                {},
                ["get", "pmap:pgf:name2"],
                {
                    "text-font": [
                        "case",
                        ["==", ["get", "pmap:script2"], "Devanagari"],
                        ["literal", ["Noto Sans Devanagari Regular v1"]],
                        ["literal", ["Noto Sans Regular"]]
                    ]
                }
            ],
            [
                "case",
                script === "Latin" ? ["!", ["has", "pmap:script"]] : ["==", ["get", "pmap:script"], script],
                [
                    "format",
                    ["get", "name"],
                    {},
                    "\n",
                    {},
                    ["get", "pmap:pgf:name2"],
                    {
                        "text-font": [
                            "case",
                            ["==", ["get", "pmap:script2"], "Devanagari"],
                            ["literal", ["Noto Sans Devanagari Regular v1"]],
                            ["literal", ["Noto Sans Regular"]]
                        ]
                    },
                    "\n",
                    {},
                    ["get", "pmap:pgf:name3"],
                    {
                        "text-font": [
                            "case",
                            ["==", ["get", "pmap:script3"], "Devanagari"],
                            ["literal", ["Noto Sans Devanagari Regular v1"]],
                            ["literal", ["Noto Sans Regular"]]
                        ]
                    }
                ],
                script === "Latin" ? ["!", ["has", "pmap:script2"]] : ["==", ["get", "pmap:script2"], script],
                [
                    "format",
                    ["get", "name2"],
                    {},
                    "\n",
                    {},
                    ["get", "pmap:pgf:name"],
                    {
                        "text-font": [
                            "case",
                            ["==", ["get", "pmap:script"], "Devanagari"],
                            ["literal", ["Noto Sans Devanagari Regular v1"]],
                            ["literal", ["Noto Sans Regular"]]
                        ]
                    },
                    "\n",
                    {},
                    ["get", "pmap:pgf:name3"],
                    {
                        "text-font": [
                            "case",
                            ["==", ["get", "pmap:script3"], "Devanagari"],
                            ["literal", ["Noto Sans Devanagari Regular v1"]],
                            ["literal", ["Noto Sans Regular"]]
                        ]
                    }
                ],
                [
                    "format",
                    ["get", "name3"],
                    {},
                    "\n",
                    {},
                    ["get", "pmap:pgf:name"],
                    {
                        "text-font": [
                            "case",
                            ["==", ["get", "pmap:script"], "Devanagari"],
                            ["literal", ["Noto Sans Devanagari Regular v1"]],
                            ["literal", ["Noto Sans Regular"]]
                        ]
                    },
                    "\n",
                    {},
                    ["get", "pmap:pgf:name2"],
                    {
                        "text-font": [
                            "case",
                            ["==", ["get", "pmap:script2"], "Devanagari"],
                            ["literal", ["Noto Sans Devanagari Regular v1"]],
                            ["literal", ["Noto Sans Regular"]]
                        ]
                    }
                ]
            ]
        ]
    ];
    return result;
}