## MeoMods — Dev workspace

Цей репозиторій містить набір Minecraft модів (MeoMods) з мульти-версійною організацією для розробки та збірки.

Коротко:

Швидке використання
1) Використовуючи локальний Gradle (рекомендовано для сумісності FG3):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-all.ps1 -Local -Version 1.12.2
```

2) Використовуючи MDK wrapper (якщо хочеш запускати MDK-тест):

```powershell
# Викличе wrapper у mdks/1.12.2/forge-1.12.2-mdk-test
${workspaceFolder}\\mdks\\1.12.2\\forge-1.12.2-mdk-test\\gradlew.bat build
```

3) Зібрати та зібрати релізи у `releases/`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\collect-jars.ps1 -Version 1.12.2
```

Як додати нову версію (швидко)
1. Створити директорії:
2. Додати MDK у `mdks/<new-version>/forge-<new-version>-mdk-test` (розпакувати MDK туди).
3. Додати моди у `mods/<new-version>/` або скопіювати існуючі модулі і оновити `build.gradle` (якщо потрібно).
4. Оновити root `settings.gradle` (якщо нова версія має нові модулі) або просто запускати `scripts/build-all.ps1 -Version <new-version>`.

Типові проблеми та рішення

Користувацькі таски VS Code

Допомога

Автор та історія: дивись `DEV_README.md` для деталей по внутрішніх процесах і архівації.

Дата створення README: 2025-09-14
## MeoMods — робочий простір для розробки модів (докладний довідник)

Мета цього репозиторію — підтримувати набір модів MeoMods у зручному для розробки, збірки та релізів вигляді. Проєкт організовано мульти-версійно: кожна підтримувана версія Minecraft має окремі папки для модів та MDK.

Основна структура
- `mods/<version>/` — вихідні коди модів (кожний мод — окремий Gradle-проєкт). Приклад: `mods/1.12.2/Permissions`.
- `mdks/<version>/forge-<version>-mdk-test/` — тестовий MDK (використовується для відлагодження/тестування модів у середовищі Forge MDK).
- `tools/gradle/` — опціональна локальна інсталяція Gradle (корисно для сумісності з ForgeGradle у старих MDK).
- `releases/` — кінцеві JAR-артефакти, зібрані та збережені з timestamp.
- `scripts/` — корисні PowerShell-скрипти для побудови, збирання JAR, інструментів релізу.
- `.vscode/tasks.json` — готові таски для швидкого запуску збірки в VS Code.

Швидкий старт
1) Переконайся, що у тебе встановлений JDK 8 (Java 8) у PATH — Forge 1.12.2 та FG3 налаштовані на Java 8.
2) Зібрати всі моди локально (рекомендую):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-all.ps1 -Local -Version 1.12.2
```

Пояснення: `-Local` змушує скрипт використовувати `tools/gradle/bin/gradle.bat` і запускати з кореню репозиторію, щоб top-level `settings.gradle` визначив всі підпроєкти.

3) Зібрати релізні JAR у папку `releases/` (необов'язково — скрипт збирає JAR в `build/libs`, потім `collect-jars.ps1` копіює їх):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\collect-jars.ps1 -Version 1.12.2
```

4) Перевірити контрольні суми релізів:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\list-releases.ps1 -Version 1.12.2
```

Робота з MDK wrapper
- Якщо хочеш використовувати gradle wrapper з MDK (наприклад для локального MDK-проєкту), запускай wrapper у папці MDK:

## MeoMods — робочий простір та інструкція (укр.)

Цей файл об'єднує інформацію про використання, розробку та релізи — він замінює старі `docs/USAGE_UA.md`, `DEV_README.md` та `RELEASE_INSTRUCTIONS.md`.

У змісті: швидкий старт, структура репозиторію, інструкції для розробника, збірка, релізи та усунення проблем.

---

### Швидкий старт

1) Переконайся, що встановлено JDK 8 (java -version має повертати Java 8).

2) Збери всі моди локально (використовуємо локальний Gradle з `tools/gradle`):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-all.ps1 -Local -Version 1.12.2
```

3) Зібрати та зібрати релізні JAR у папку `releases/`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\collect-jars.ps1 -Version 1.12.2
```

4) Згенерувати JSON-манифест релізів (SHA256):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\list-releases.ps1 -Version 1.12.2 -OutFormat json
```

---

### Структура репозиторію

- `mods/<version>/` — кожний мод як окремий Gradle-проєкт.
- `mdks/<version>/forge-<version>-mdk-test/` — тестовий MDK для локального відлагодження.
- `tools/gradle/` — локальний Gradle (4.9) для сумісності з ForgeGradle 3.x.
- `releases/` — скопійовані JAR з timestamp.
- `scripts/` — PowerShell-скрипти: `build-all.ps1`, `collect-jars.ps1`, `list-releases.ps1`.

---

### CI (GitHub Actions)

- Workflow: `.github/workflows/build.yml` — будує з JDK 8 та локальним Gradle 4.9, збирає JAR, генерує JSON-манифест і завантажує `releases/` як артефакт.
- Виправлено помилку: скрипт тепер перевіряє, чи існує `tools/gradle` і не перезаписує його (помилка на раннері викликала аварію при існуючому каталозі).

Що дивитись у разі проблем: Actions → виберіть job `build-windows` → дивіться кроки "Ensure Gradle 4.9", "Run build script (Local Gradle)".

---

### Скрипти (коротко)

- `scripts/build-all.ps1` — будує всі підпроєкти; опції: `-Version <ver>`, `-Local` (використовує `tools/gradle`), `-Clean`.
- `scripts/collect-jars.ps1` — збирає JAR у `releases/` з timestamp.
- `scripts/list-releases.ps1` — виводить SHA256 та може згенерувати JSON/CSV manifest (-OutFormat json|csv).

---

### Типові проблеми та рішення (швидко)

- "tools\gradle already exists" при CI: виправлено у workflow — тепер перевірка `Test-Path` перед завантаженням.
- "Found Gradle version ... not supported in FG3": використайте `-Local` з Gradle 4.9 або змініть `gradle-wrapper.properties` у MDK.
- "Project with path ':1.12.2:Permissions' could not be found": перевірте `settings.gradle` — include і projectDir повинні вказувати на `mods/1.12.2/<Module>`.
- JVM помилки: використовуйте JDK 8 на CI та локально.

---

### Рекомендації по git

- Додайте `.gitignore` щоб уникнути пушу великих бінарів:

```
.gradle/
**/build/
releases/
.idea/
.vscode/
*.iml
```

---

Якщо потрібно — я можу додати окремі секції (наприклад: CONTRIBUTING, CHANGELOG template), або зробити англомовну версію.

README оновлено: 2025-09-14
    }
}

// Видача нагороди
WalletAPI.deposit(player.getUniqueID(), 50.0);
'''

---

## 3. МОДИ В РОЗРОБЦІ ТА ПЛАНАХ

### 3.1. ChunkGuard [MEO]

**Призначення:** Заміна WorldGuard. Система захисту та управління територіями.
**Статус:** Проведено початковий аналіз вихідного коду плагіна-прототипу `MeoGuard`. Створено базову структуру проєкту.
**Ключові Концепції:**
- **Система регіонів:** Заснована на кубоїдах та чанках.
- **Прапори (Flags):** Гнучка система правил для регіонів (pvp, mob-spawning, use тощо).
- **Наслідування та Пріоритети:** Регіони можуть наслідувати прапори від батьківських, а пріоритети вирішують конфлікти.
- **Інтеграція з [MEO]:**
    - **Буде використовувати `PermissionsAPI`** для перевірки прав на дії в регіоні (напр., `chunkguard.build`, `chunkguard.interact`).
    - **Буде використовувати `WalletAPI`** для реалізації функціоналу купівлі та оренди регіонів.

### 3.2. Інші Заплановані Моди

- **`Alerts`:** Повідомлення про повітряну тривогу в Україні.
- **`Chat`:** Розширений чат з каналами (глобальний, локальний) та форматуванням на основі `PermissionsAPI`.
- **`CmdPack`:** Набір основних команд, аналогічних EssentialsX.
- **`Cosmetics`:** Декоративні предмети для гравців.
- **`Donation`:** Система для видачі донат-послуг, придбаних на сайті.
- **`Market`:** Система аукціонів та ринку між гравцями, що використовує `WalletAPI`.
- **`Tab`:** Налаштування та форматування списку гравців (TAB) на основі `PermissionsAPI`.
