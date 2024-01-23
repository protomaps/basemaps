import ReactDOM from "react-dom/client";
import VisualTestsComponent from "./VisualTestsComponent.tsx";
import "./index.css";

// we turn off strict mode because this page only loads once.
const root = document.getElementById("root");

if (root) {
  ReactDOM.createRoot(root).render(<VisualTestsComponent />);
}
