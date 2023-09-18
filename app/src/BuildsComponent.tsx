import { useEffect, useState } from "react";

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

function BuildComponent(props: { build: Build }) {
  const build = props.build;
  const link = `https://build.protomaps.com/${build.key}`;
  return (
    <tr>
      <td>
        <input type="radio" />
        <input type="radio" />
      </td>
      <td>{build.key}</td>
      <td>{formatBytes(build.size)}</td>
      <td>{build.uploaded}</td>
      <td>
        <a href={"/?url=" + link}>map</a>
      </td>
      <td>
        <a href={link}>download</a>
      </td>
    </tr>
  );
}

export default function BuildsComponent() {
  const [builds, setBuilds] = useState<Build[]>([]);

  useEffect(() => {
    fetch("https://build-metadata.protomaps.dev/builds.json")
      .then((r) => {
        return r.json();
      })
      .then((j) => {
        setBuilds(j);
      });
  }, []);

  return (
    <div>
      <h1>Builds</h1>
      <button>Compare Examples</button>
      {/*<button>Compare Maperture</button>*/}
      <table>
        <tbody>
          {builds.map((build: Build) => (
            <BuildComponent key={build.key} build={build} />
          ))}
        </tbody>
      </table>
    </div>
  );
}
