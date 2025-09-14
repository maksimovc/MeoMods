# Інструкція користувача — MeoMods (українською)

Цей документ описує, як користуватися середовищем розробки MeoMods, побудованим навколо Forge MDK для Minecraft 1.12.2.

Основні розташування (новий мульти-версійний layout)
- Коренева папка: `MeoMods`
- `mods/<version>/` — джерела модів для конкретної Minecraft версії (наприклад `mods/1.12.2/Permissions`).
- `mdks/<version>/` — місце для Forge MDK для певної версії (наприклад `mdks/1.12.2/forge-1.12.2-mdk-test`).
- `releases/<version>/` — зібрані JAR'и по версіям.
- `archive/<timestamp>/` — архіви очищених/переміщених файлів.
- `tools/` — локальні інструменти, наприклад локальний Gradle (`tools/gradle/bin/gradle.bat`).
- `scripts/` — скрипти ( `build-all.ps1`, `collect-jars.ps1`, `cleanup-archive.ps1`, `reorg-workspace.ps1`).

Передумови
- Java 8 встановлено та доступно (вказуйте через `org.gradle.java.home` у `gradle.properties`, якщо потрібно).
- Перший запуск MDK wrapper завантажить Gradle 4.9 автоматично (якщо ви не використовуєте локальний Gradle).

Швидкий старт — побудова всіх модів (рекомендовано)
1) У PowerShell у корені репозиторія запустіть для конкретної версії (приклад: 1.12.2):

```powershell
powershell -File .\scripts\build-all.ps1 -Version 1.12.2
```

2) Для локальної збірки без завантаження Gradle: розпакуйте Gradle у `tools/gradle` (щоб доріжка була `tools/gradle/bin/gradle.bat`), тоді виконайте:

```powershell
powershell -File .\scripts\build-all.ps1 -Local -Version 1.12.2
```

3) Після успішної збірки зберіть JAR'и до папки `releases/<version>/`:

```powershell
powershell -File .\scripts\collect-jars.ps1 -Version 1.12.2
```

Перевірка без виконання (dry-run)
- Щоб побачіти, що буде зроблено, використайте `-WhatIf` з будь-яким скриптом:

```powershell
powershell -File .\scripts\build-all.ps1 -WhatIf
powershell -File .\scripts\collect-jars.ps1 -WhatIf
powershell -File .\scripts\cleanup-archive.ps1 -WhatIf
```

Очищення і архівація згенерованих файлів
- Щоб прибрати з репозиторію згенеровані папки й файли (build/, .gradle, wrapper, bin, *.jar, тощо), запустіть:

```powershell
powershell -File .\scripts\cleanup-archive.ps1
```

Список VS Code тасків (за замовчуванням вказано `-Version 1.12.2`)
- Build: All Mods — запускає `scripts\build-all.ps1 -Version 1.12.2` (MDK wrapper)
- Build: All Mods (Local Gradle) — запускає `scripts\build-all.ps1 -Local -Version 1.12.2`
- Gradle: Build MDK Test — запускає `mdks\1.12.2\forge-1.12.2-mdk-test\gradlew.bat build` (legacy/explicit path)
- Collect: Releases — запускає `scripts\collect-jars.ps1 -Version 1.12.2`
- Clean: MDK Test — запускає `scripts\build-all.ps1 -Clean -Version 1.12.2`

Поради та troubleshooting
- Якщо збірка повідомляє, що потрібна вища версія Gradle, використайте MDK wrapper (він завантажить потрібну версію) або помістіть сумісний Gradle у `tools/gradle` та використайте `-Local`.
- Якщо щось випадково видалено, знайдіть файли в `archive/<timestamp>/` — скрипт робить переміщення, а не видалення.
- Для відладки Java/Forge проблем запустіть `gradlew.bat --stacktrace` у `forge-1.12.2-mdk-test` для детального логу.

Контакти та подальші кроки
- Якщо потрібно, можу додати CI (GitHub Actions), автоматичну підготовку release ZIP та правила версіювання.

---
Завершення: цей документ доповнює `DEV_README.md` і JOHNS the main instructions українською.