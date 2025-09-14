# Mods template for Minecraft 1.20.1

This folder is a placeholder for mod source projects targeting Minecraft/Forge (or Fabric) 1.20.1.

How to use:
1. Place each mod as a folder here: `mods/1.20.1/<ModName>` with its `build.gradle` and `src/`.
2. Place the corresponding MDK under `mdks/1.20.1/`.
3. Build using: `powershell -File ..\..\scripts\build-all.ps1 -Version 1.20.1`.

Notes:
- 1.20.1 toolchain often needs a newer Java and Gradle. Keep Java and Gradle compatibility in mind when adding MDKs.
