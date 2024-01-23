import React from "react";
import ReactDOM from "react-dom/client";
import BuildsComponent from "./BuildsComponent.tsx";
import "./index.css";

const root = document.getElementById("root");

if (root) {
  ReactDOM.createRoot(root).render(
    <React.StrictMode>
      <BuildsComponent />
    </React.StrictMode>,
  );
}
