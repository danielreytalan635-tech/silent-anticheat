# Silent AntiCheat

Server-side-only, alert-only anti-cheat for Minecraft 26.2 (Fabric).

- Never kicks, bans, or rubberbands anyone.
- Only sends alerts to the server console and to online ops (dark red, bold chat messages).
- Checks: Flight/Hover, Speed/Teleport, Reach (>5 blocks).
- Runs entirely server-side via the `server` entrypoint — regular players connecting
  to your server do not need to install anything.

## Before you build

Versions move fast on 26.x — double check these against
https://fabricmc.net/develop/ and bump `gradle.properties` if newer ones exist:

- `loader_version` (currently pinned to 0.19.3 — the latest verified release at time of writing)
- `fabric_version`
- `loom_version`

Requires **JDK 25** (Minecraft 26.x raised the minimum from 21).

## Building

```
./gradlew build
```

or just push to `main` / open a PR — the included GitHub Actions workflow
(`.github/workflows/build.yml`) builds the jar automatically and uploads it
as a workflow artifact.

The finished jar lands in `build/libs/`.

## Tuning

`MovementCheckListener` and `ReachCheckListener` have their thresholds as
constants at the top of each file (`MAX_AIRBORNE_TICKS`, `MAX_DISTANCE_PER_TICK`,
`MAX_REACH_DISTANCE`). Adjust them to fit your server's allowed potion effects,
plugins, and TPS stability.
