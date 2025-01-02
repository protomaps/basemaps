const GIT_SHA = (import.meta.env.VITE_GIT_SHA || "main").substr(0, 8);

function Nav(props: { page: number }) {
  return (
    <nav class="max-w-[1500px] mx-auto py-4 space-x-4">
      <a class={props.page === 0 ? "font-bold" : "underline"} href="/">
        Basemap
      </a>
      <a class={props.page === 1 ? "font-bold" : "underline"} href="/builds/">
        Builds
      </a>
      <a
        class={props.page === 2 ? "font-bold" : "underline"}
        href="/visualtests/"
      >
        Visual Tests
      </a>
      <a
        class="font-mono"
        target="_blank"
        rel="noreferrer"
        href="https://github.com/protomaps/basemaps"
      >
        @{GIT_SHA}
      </a>
    </nav>
  );
}

export default Nav;
