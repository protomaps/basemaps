import { defineConfig, searchForWorkspaceRoot } from "vite";
import sirv from "sirv";
import react from "@vitejs/plugin-react";
import { resolve } from "path";

const serveTiles = sirv("../tiles", { dev: true });

const servePmtilesInTilesDir = () => ({
  name: "serve-pmtiles-in-tiles-dir",
  configureServer(server) {
    server.middlewares.use((req, res, next) => {
      if (req.url.split("?")[0].endsWith(".pmtiles")) {
        return serveTiles(req, res);
      } else {
        next();
      }
    });
  },
});

const serveSprites = sirv("../sprites/dist", { dev: true });

const serveSpritesInSpritesDir = () => ({
  name: "serve-sprites-in-sprites-dir",
  configureServer(server) {
    server.middlewares.use((req, res, next) => {
      if (req.url.endsWith(".png") || req.url.endsWith(".json")) {
        return serveSprites(req, res);
      } else {
        next();
      }
    });
  },
});

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), servePmtilesInTilesDir(), serveSpritesInSpritesDir()],
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
