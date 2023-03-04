const express = require("express");
const path = require("path");
const fs = require('fs');
const cors = require('cors');
const app = express();

app.use(cors());

const { PORT=3000, DATA_DIR='../../data', NODE_ENV='development' } = process.env;

app.use("/tiles", express.static(path.join(__dirname, DATA_DIR)));
app.get("/", (_req, res) => {
  res.sendFile(path.join(__dirname, "maplibre-basemap.html"));
})

app.listen(PORT, () => console.log(`Server is listening on port ${PORT}`));