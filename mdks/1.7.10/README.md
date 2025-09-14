# MDKs folder for Minecraft 1.7.10

Place the older MDK here, for example:

`mdks/1.7.10/forge-1.7.10-mdk-test/`

Older MDKs will often use older Gradle versions; prefer using the MDK's included wrapper.

After placing the MDK, run:

```powershell
powershell -File ..\..\scripts\build-all.ps1 -Version 1.7.10
```
