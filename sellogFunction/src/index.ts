import { app } from '@azure/functions';

import "./functions/eventGridTrigger";

app.setup({
  enableHttpStream: true,

});
