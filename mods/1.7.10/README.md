# Mods template for Minecraft 1.7.10

This folder is a placeholder for mod source projects targeting Minecraft/Forge 1.7.10.

How to use:
1. Place each mod as a folder here: `mods/1.7.10/<ModName>` with its `build.gradle` and `src/`.
2. Place the corresponding MDK under `mdks/1.7.10/`.
3. Build using: `powershell -File ..\..\scripts\build-all.ps1 -Version 1.7.10`.

Notes:
- Older MDKs (1.7.10) may require older Gradle versions; prefer using the MDK wrapper included in the MDK.
