import ReactDOM from "react-dom/client";
import VisualTestsComponent from "./VisualTestsComponent.tsx";
import "./index.css";

// we turn off strict mode because this page only loads once.
ReactDOM.createRoot(document.getElementById("root")!).render(
  <VisualTestsComponent />,
);
