import { resolve } from "node:path";
import sirv from "sirv";
import { type ViteDevServer, defineConfig, searchForWorkspaceRoot } from "vite";
import solid from "vite-plugin-solid";
import tailwindcss from '@tailwindcss/vite'

const serveTiles = sirv("../tiles", { dev: true });

const servePmtilesInTilesDir = () => ({
	name: "serve-pmtiles-in-tiles-dir",
	configureServer(server: ViteDevServer) {
		server.middlewares.use((req, res, next) => {
			if (req.url?.split("?")[0].endsWith(".pmtiles")) {
				return serveTiles(req, res);
			}
			next();
		});
	},
});

const serveSprites = sirv("../sprites/dist", { dev: true });

const serveSpritesInSpritesDir = () => ({
	name: "serve-sprites-in-sprites-dir",
	configureServer(server: ViteDevServer) {
		server.middlewares.use((req, res, next) => {
			if (req.url?.endsWith(".png") || req.url?.endsWith(".json")) {
				return serveSprites(req, res);
			}
			next();
		});
	},
});

// https://vitejs.dev/config/
export default defineConfig({
	plugins: [solid(), servePmtilesInTilesDir(), serveSpritesInSpritesDir(), tailwindcss()],
	build: {
		rollupOptions: {
			input: {
				mapview: resolve(__dirname, "index.html"),
				builds: resolve(__dirname, "builds/index.html"),
				visualtests: resolve(__dirname, "visualtests/index.html"),
			},
		},
	},
	server: {
		fs: {
			allow: [searchForWorkspaceRoot(process.cwd()), "../styles/src"],
		},
	},
});
