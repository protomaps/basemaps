import { useEffect, useState, Dispatch, SetStateAction } from "react";

interface Build {
  uploaded: string;
  key: string;
  size: number;
}

function formatBytes(bytes: number, decimals = 2) {
  if (!+bytes) return "0 Bytes";
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = [
    "Bytes",
    "KiB",
    "MiB",
    "GiB",
    "TiB",
    "PiB",
    "EiB",
    "ZiB",
    "YiB",
  ];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
}

function BuildComponent(props: {
  build: Build;
  idx: number;
  cmpA: number;
  cmpB: number;
  setCmpA: Dispatch<SetStateAction<number>>;
  setCmpB: Dispatch<SetStateAction<number>>;
}) {
  const build = props.build;
  const link = `https://build.protomaps.com/${build.key}`;
  const idx = props.idx;

  const onChangeA = () => {
    props.setCmpA(idx);
  };

  const onChangeB = () => {
    props.setCmpB(idx);
  };

  return (
    <tr>
      <td>
        <span style={{ display: "inline-block", width: "20px" }}>
          {idx > props.cmpB && (
            <input
              type="radio"
              onChange={onChangeA}
              checked={idx === props.cmpA}
            />
          )}
        </span>
        <span style={{ display: "inline-block", width: "20px" }}>
          {idx < props.cmpA && (
            <input
              type="radio"
              onChange={onChangeB}
              checked={idx === props.cmpB}
            />
          )}
        </span>
      </td>
      <td>{build.key}</td>
      <td>{formatBytes(build.size)}</td>
      <td>{build.uploaded}</td>
      <td>
        <a href={"/#tiles=" + link}>map</a>
      </td>
      <td>
        <a href={link}>download</a>
      </td>
    </tr>
  );
}

interface MaperturePayload {
  name: string;
  type: string;
  renderer: string;
  url: string;
  index: number;
}

// [
//   {
//     "name":"20230101",
//     "type":"maplibre-gl",
//     "renderer":"maplibre-gl",
//     "url":"https://build-metadata.protomaps.dev/style@2.0.0-alpha.0+theme@dark+tiles@20230920.json",
//     "index":0
//   },
//   {"id":"outdoors-v11","name":"Mapbox Outdoors","type":"mapbox-gl","url":"mapbox://styles/mapbox/outdoors-v11","index":1,"renderer":"mapbox-gl"}
// ]

export default function BuildsComponent() {
  const [builds, setBuilds] = useState<Build[]>([]);

  // the index is starting from the top (0)
  const [cmpA, setCmpA] = useState<number>(1);
  const [cmpB, setCmpB] = useState<number>(0);

  const style = "2.0.0-alpha.0";
  const theme = "light";

  const openMaperture = () => {
    const leftKey = builds[cmpA].key.replace(".pmtiles", "");
    const rightKey = builds[cmpB].key.replace(".pmtiles", "");
    const left: MaperturePayload = {
      name: `${leftKey} ${style} ${theme}`,
      type: "maplibre-gl",
      renderer: "maplibre-gl",
      index: 0,
      url: `https://build-metadata.protomaps.dev/style@${style}+theme@${theme}+tiles@${leftKey}.json`,
    };
    const right: MaperturePayload = {
      name: `${rightKey} ${style} ${theme}`,
      type: "maplibre-gl",
      renderer: "maplibre-gl",
      index: 0,
      url: `https://build-metadata.protomaps.dev/style@${style}+theme@${theme}+tiles@${rightKey}.json`,
    };
    const payload = JSON.stringify([left, right]);
    open(
      "https://stamen.github.io/maperture/#maps=" + encodeURIComponent(payload),
    );
  };

  useEffect(() => {
    fetch("https://build-metadata.protomaps.dev/builds.json")
      .then((r) => {
        return r.json();
      })
      .then((j) => {
        setBuilds(
          j.sort((a: Build, b: Build) => {
            return a.key < b.key;
          }),
        );
      });
  }, []);

  return (
    <div>
      <h1>Builds</h1>
      {/*<button>Compare Examples</button>*/}
      <button onClick={openMaperture}>Compare in Maperture</button>
      <table>
        <tbody>
          {builds.map((build: Build, idx: number) => (
            <BuildComponent
              key={build.key}
              build={build}
              idx={idx}
              cmpA={cmpA}
              cmpB={cmpB}
              setCmpA={setCmpA}
              setCmpB={setCmpB}
            />
          ))}
        </tbody>
      </table>
    </div>
  );
}
