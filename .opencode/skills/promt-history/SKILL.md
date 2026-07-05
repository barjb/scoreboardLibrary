---
name: prompt-history
description: Create a timestamped markdown file in /promtHistory when the user requests prompt history creation
---

When invoked:
- Create directory `../../promptHistory` if it does not exist
- Create a new `.md` file named with the current timestamp (ISO 8601)
- Write a list of prompts used in the session
- Confirm the file path created