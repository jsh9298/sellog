import { app } from "@azure/functions";

import "./functions/blobTrigger";

app.setup({
  enableHttpStream: true,
});
