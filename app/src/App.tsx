import { useState, useEffect } from "react";

const GIT_SHA = (import.meta.env.VITE_GIT_SHA || "").substr(0, 8);

function App() {

  return (
    <div>
      {GIT_SHA}
    </div>
  );
}

export default App;