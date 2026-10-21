# Cloud Gaming RTC Example

## Overview

Use Agora RTC for low-latency cloud game streaming, player viewing, and interactive media transport.

Repository: [AgoraIO-Community/Agora-CloudGame-Example](https://github.com/AgoraIO-Community/Agora-CloudGame-Example)

## Prerequisites and Setup

1. Provision a test project with access to the cloud-gaming service. The sample demonstrates service REST operations and RTC reception together.
2. Open `Android/Agora-CloudGame-Example/` in Android Studio (the guide lists Android Studio 4.2+ and Android API 21+).
3. Set `APP_ID` and `APP_CERTIFICATE` in that Android project's `local.properties`, sync Gradle and run on a physical device.
4. Use the sample's game start/stop flow and send its control commands while receiving the game video. Inspect the separate `iOS/` project for the iOS variant.

## What To Check

- Verify game session creation and teardown using service responses.
- Confirm the game video renders and control input affects the remote game.
- Test stop/restart and ensure the previous game session is terminated.

## Integration Notes

The upstream example intentionally omits an application server. Production game/service requests, especially billable actions, need an authenticated business backend.

Keep App Certificates, customer secrets and privileged token generation on a trusted backend for production. Use only your own test credentials while configuring historical demos; do not commit them. Preserve the sample's existing dependency versions and lifecycle until a separate migration is tested.

## Source References

- [Android/Agora-CloudGame-Example/README.md](../../Android/Agora-CloudGame-Example/README.md)

## Validation Status

This recipe was checked against the repository's source and setup documents. The platform build and live service/device flow have not been run as part of this documentation change. Complete the checks above in an environment with the required SDKs, hardware and service access before treating it as a validated deployment.

## Implementation Prompt

```text
You are working in AgoraIO-Community/Agora-CloudGame-Example. Use docs/ai/RECIPE.md and its linked source files to adapt the Cloud Gaming RTC Example reference to this project.

Start by inspecting the actual sample directory and configuration described above. Resolve its listed missing prerequisites before claiming it is runnable. Preserve the existing architecture and compatible dependency versions. Implement the recipe's feature-specific flow, permissions and resource cleanup. Use a trusted backend for privileged credentials and tokens in production.

Run the repository-specific build or launch flow, then perform each What To Check item. Report the commands run, the observed results and anything blocked by service access, hardware or incomplete sample code. Do not claim live validation from a documentation or catalog build alone.
```
