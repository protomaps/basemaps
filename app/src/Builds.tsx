import React from "react";
import ReactDOM from "react-dom/client";
import BuildsComponent from "./BuildsComponent.tsx";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BuildsComponent />
  </React.StrictMode>,
);
