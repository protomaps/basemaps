import express from "express";
import layers from "../base/src/index";
import open from "open";
import fs from "fs/promises";
let app = express();

const TILES_PATH = '../tiles'

app.use(express.static("."));
app.use("/tiles", express.static(TILES_PATH));

const PORT = 3857;

const gazetteer = {
	Locations: [
		{
			"San Francisco, CA": {
				zoom: 18,
				center: { lng: -122.4193, lat: 37.7648 },
			},
		},
		{
			"Washington DC": { zoom: 12, center: { lng: -77.0435, lat: 38.9098 } },
		},
	],
};

// Maperture requires JS configuration,
// so we create JS programatically. gross!
app.get("/config.js", async (req: any, res: any) => {
	const stylePresets = [
		{
			id: "api",
			name: "API",
			type: "maplibre-gl",
			url: "/api/light.json",
		},
	];

	const files = await fs.readdir(TILES_PATH);

	files.forEach((file) => {
		if (file.endsWith(".pmtiles")) {
			stylePresets.push({
				id: file,
				name: file,
				type: "maplibre-gl",
				url: `/local/${file}/light.json`,
			});
		}
	});

	res.set("Content-Type", "application/javascript");
	res.send(`
		const accessToken = '';
		const gazetteer = ${JSON.stringify(gazetteer)};
		const stylePresets = ${JSON.stringify(stylePresets)};
		export { gazetteer, accessToken, stylePresets };
	`);
});

app.get("/api/:variant.json", async (req: any, res: any) => {
	const source: any = {
		type: "vector",
		tiles: [
			"https://api.protomaps.com/tiles/v2/{z}/{x}/{y}.pbf?key=1003762824b9687f",
		],
		maxzoom: 14,
	};
	res.set("Access-Control-Allow-Origin", "*");
	let style = {
		version: 8,
		sources: {
			protomaps: source,
		},
		glyphs: "https://cdn.protomaps.com/fonts/pbf/{fontstack}/{range}.pbf",
		sprite: "",
		layers: layers("protomaps", req.params.variant),
	};
	res.send(style);
});

app.get("/local/:pmtiles/:variant.json", async (req: any, res: any) => {
	const source: any = { type: "vector" };
	source["url"] = "pmtiles:///tiles/" + req.params.pmtiles;
	source["maxzoom"] = 15;

	res.set("Access-Control-Allow-Origin", "*");
	let style = {
		version: 8,
		sources: {
			protomaps: source,
		},
		glyphs: "https://cdn.protomaps.com/fonts/pbf/{fontstack}/{range}.pbf",
		sprite: "",
		layers: layers("protomaps", req.params.variant),
	};
	res.send(style);
});

process.stdin.on("keypress", (str, key) => {
	console.log(str, key);
});

app.listen(PORT, () => {
	console.log(`Example app listening on port ${PORT}`);
	// open(`http://localhost:${PORT}`);
});
