import React from "react";
import ReactDOM from "react-dom/client";
import MapViewComponent from "./MapViewComponent.tsx";
import "./index.css";

const root = document.getElementById("root");

if (root) {
  ReactDOM.createRoot(root).render(
    <React.StrictMode>
      <MapViewComponent />
    </React.StrictMode>,
  );
}
