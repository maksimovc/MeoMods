# MDKs folder for Minecraft 1.20.1

Place the MDK folder for the target toolchain here, for example:

`mdks/1.20.1/forge-1.20.1-mdk-test/` or a Fabric/other MDK.

Then run:

```powershell
powershell -File ..\..\scripts\build-all.ps1 -Version 1.20.1
```

Note: 1.20.1 often requires a newer Java (11+) and newer Gradle wrapper.
