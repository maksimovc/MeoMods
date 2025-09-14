Developer workspace for MeoMods

Layout (multi-version)
- `mods/<version>/`          : Source folders for mods targeting a specific Minecraft version (e.g. `mods/1.12.2/Alerts`).
- `mdks/<version>/`          : MDK/test folders per version (e.g. `mdks/1.12.2/forge-1.12.2-mdk-test`).
- `releases/<version>/`      : Collected JAR artifacts per version.
- `tools/`                   : Optional local tools like a local Gradle distribution.
- `archive/<timestamp>/`     : Where generated/temporary files are moved instead of being deleted.
- `original_plugin_source/`   : Historical sources (kept for reference).

Goals
- Provide a simple, reproducible build flow for all mods using the MDK wrapper.
- Keep the clean MDK untouched.
- Allow building all mods from VS Code or via PowerShell script.

How to build (quick)
1) From PowerShell in workspace root (example for 1.12.2):
   powershell -File .\scripts\build-all.ps1 -Version 1.12.2

2) From VS Code: Run the "Build: All Mods" Task (Terminal -> Run Task...)

Notes
- Builds use the MDK wrapper (gradlew) which will download Gradle 4.9 automatically the first time.
- Java 8 is required and expected at the system level or via `org.gradle.java.home` in gradle.properties.
- If you want to avoid automatic Gradle downloads, we can add a local Gradle distribution and configure scripts to use it.

Next steps
- If you want, I can add a simple `Makefile`/`psake` or CI job to automate releases and reobfuscation steps.

Localization
- Ukrainian usage guide: `docs/USAGE_UA.md` — full instructions in Ukrainian for common workflows.

Finalization
- Workspace tidied: generated/build artifacts, wrappers, and temporary Gradle distributions are moved to `archive/<timestamp>/` by `scripts/cleanup-archive.ps1` (script supports `-Version`).
- All environment helper scripts and VS Code tasks were added and documented.
- The 1.12.2 sources and MDK were relocated into `mods/1.12.2/` and `mdks/1.12.2/` respectively. If any MDK folder is still locked by an editor/process, close the process and re-run `scripts/reorg-workspace.ps1 -Version 1.12.2`.

Multi-version layout (final)
- `mods/<version>/` — source folders for mods targeting a specific Minecraft version (e.g. `mods/1.12.2/Permissions`). Keep each mod as a self-contained Gradle subproject (has `build.gradle`, `src/`).
- `mdks/<version>/` — MDK/test folders per version (e.g. `mdks/1.12.2/forge-1.12.2-mdk-test`). Use the MDK wrapper in these folders for reproducible builds.
- `releases/<version>/` — collected build artifacts per version. Use `scripts/collect-jars.ps1 -Version <version>` to gather JARs into this folder.
- `tools/` — optional local tools such as a local Gradle distribution (`tools/gradle/bin/gradle.bat`). Placing Gradle here allows using `scripts/build-all.ps1 -Local` to avoid runtime downloads.
- `archive/<timestamp>/` — where `scripts/cleanup-archive.ps1 -Version <version>` moves generated artifacts instead of deleting them.

How to add another version (example: 1.16.5)
1) Create the folders for the version (templates created automatically by this repo):
   - `mods/1.16.5/`
   - `mdks/1.16.5/`
   - `releases/1.16.5/`
2) Download the Forge MDK you need and place it under `mdks/1.16.5/` as a folder (for example `mdks/1.16.5/forge-1.16.5-mdk-test`).
3) Add each mod source under `mods/1.16.5/<mod>` (each with its `build.gradle` and `src/`).
4) Build all mods for that version:

```powershell
powershell -File .\scripts\build-all.ps1 -Version 1.16.5
```

Tips:
- If you want to avoid the MDK wrapper downloading Gradle, install a compatible Gradle in `tools/gradle` and run `-Local`.
- Use `scripts\collect-jars.ps1 -Version 1.16.5` to gather JARs after building.

I cleaned up temporary helper scripts used during reorganization. If you want me to restore them for debugging, say so.

Local Gradle and convenience scripts
- You can place a Gradle distribution under `tools/gradle` (so the bat is `tools/gradle/bin/gradle.bat`).
- `scripts\build-all.ps1` now supports a `-Local` switch which will run the local Gradle if present. Example:
   - `powershell -File .\\scripts\\build-all.ps1 -Local` (or use the VS Code task "Build: All Mods (Local Gradle)")
- `scripts\collect-jars.ps1` will gather produced JAR files from each project's `build/libs` into the `releases/` folder with consistent names.

VS Code tasks
- Use the Command Palette -> Run Task... to run:
   - Build: All Mods — runs the MDK wrapper build
   - Build: All Mods (Local Gradle) — prefer local Gradle under `tools/gradle`
   - Gradle: Build MDK Test — run the MDK's gradlew directly
   - Collect: Releases — copy built jars to `releases/`
   - Clean: MDK Test — run a clean via the build script

Quick dry-run
- To test without running builds, use the PowerShell `-WhatIf` with scripts. Example:
   - `powershell -File .\\scripts\\build-all.ps1 -WhatIf`
   - `powershell -File .\\scripts\\collect-jars.ps1 -WhatIf`