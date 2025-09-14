# Mods template for Minecraft 1.16.5

This folder is a placeholder for mod source projects targeting Minecraft/Forge 1.16.5.

How to use:
1. Place each mod as a folder here: `mods/1.16.5/<ModName>` with its `build.gradle` and `src/`.
2. Place the corresponding MDK under `mdks/1.16.5/`.
3. Build using: `powershell -File ..\..\scripts\build-all.ps1 -Version 1.16.5`.

Notes:
- 1.16.5 MDK may require a different Gradle version; use the wrapper inside the MDK or install a compatible Gradle under `tools/gradle` and use `-Local`.
