# Contributing to MeoMods

Thanks for your interest! Please follow these steps to contribute code or documentation.

1. Fork the repository and create a feature branch from `main`.
2. Keep commits small and focused, write clear commit messages.
3. Follow Java 8 compatibility (sourceCompatibility=1.8) in module `build.gradle` files.
4. Run local build before opening PR:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-all.ps1 -Local -Version 1.12.2
```

5. Add tests where applicable and include usage examples in module README.
6. Open PR against `main` with description and testing steps.

Maintainers will review and request changes or merge.
