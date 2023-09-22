import { defineConfig, searchForWorkspaceRoot } from "vite";
import sirv from "sirv";
import react from "@vitejs/plugin-react";
import { resolve } from "path";

const serve = sirv("../tiles");

const servePmtilesInTilesDir = () => ({
  name: "serve-pmtiles-in-tiles-dir",
  configureServer(server) {
    server.middlewares.use((req, res, next) => {
      if (req.url.split("?")[0].endsWith(".pmtiles")) {
        return serve(req, res);
      } else {
        next();
      }
    });
  },
});

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), servePmtilesInTilesDir()],
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
      allow: [searchForWorkspaceRoot(process.cwd()), '../styles/src']
    },
  },
});
