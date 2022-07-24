import { Rule } from "protomaps";
import { LineSymbolizer } from "protomaps";

const paintRules = (): Rule[] => {
	return [
		{
			dataLayer: "transit",
			symbolizer: new LineSymbolizer({ color: "red", width: 1 }),
		},
	];
};

export default paintRules;
