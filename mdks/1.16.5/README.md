# MDKs folder for Minecraft 1.16.5

Place the Forge MDK folder here, for example:

`mdks/1.16.5/forge-1.16.5-mdk-test/`

The build script will prefer the MDK under `mdks/<version>/forge-<version>-mdk-test`.

After placing the MDK, run:

```powershell
powershell -File ..\..\scripts\build-all.ps1 -Version 1.16.5
```
