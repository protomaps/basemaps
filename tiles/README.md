## Docker

```
docker build -t protomaps/basemaps .
```

```
docker run -v ./data:/tiles/data --rm -it protomaps/basemaps --output=data/monaco.pmtiles --area=monaco
```