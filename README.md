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

```powershell
& "${PWD}\mdks\1.12.2\forge-1.12.2-mdk-test\gradlew.bat" build
```

Але зверни увагу: wrapper може використовувати іншу версію Gradle. Якщо FG3 конфліктує з версією Gradle, краще використовувати локальний Gradle 4.9 (в `tools/gradle`).

Як додати нову версію Minecraft/MDK
1. Створи каталоги `mods/<new-version>/` і `mdks/<new-version>/`.
2. Розпакуй Forge MDK у `mdks/<new-version>/forge-<new-version>-mdk-test`.
3. Додай свої модулі у `mods/<new-version>/` (або скопіюй і адаптуй існуючі модулі з `mods/1.12.2`).
4. Якщо потрібно, додай нові include у `settings.gradle` або запускай `build-all.ps1 -Version <new-version>` — скрипт намагається підібрати MDK у `mdks/<version>`.

Перелік корисних скриптів
- `scripts/build-all.ps1` — головний скрипт для збірки всіх модів (має параметри `-Version`, `-Local`, `-Clean`).
- `scripts/collect-jars.ps1` — копіює JAR з `build/libs` у `releases/` з timestamp.
- `scripts/list-releases.ps1` — новий: виводить JAR-і в `releases/` з SHA256; приймає `-Version` для фільтра.

VS Code
- В `.vscode/tasks.json` є таски:
    - `Build: All Mods` — запускає `build-all.ps1 -Version 1.12.2` (wrapper/default behavior).
    - `Build: All Mods (Local Gradle)` — запускає `build-all.ps1 -Local -Version 1.12.2` (використовує `tools/gradle`).
    - `Gradle: Build MDK (Wrapper)` — таск з input `version` для запуску wrapper у `mdks/<version>`.

Типові помилки та як їх вирішувати

1) "Found Gradle version X.X.X ... not supported in FG3"
     - Причина: ForgeGradle (FG3) має обмежену сумісність з Gradle; якщо runner використовує занадто нову версію Gradle — build впаде.
     - Рішення:
         - Використовуй локальний Gradle 4.9 (в `tools/gradle`) і запусти скрипт з `-Local`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-all.ps1 -Local -Version 1.12.2
```

         - Або відредагуй `gradle-wrapper.properties` у MDK, щоб вказати сумісну версію (якщо розумієш наслідки).

2) "Project with path ':1.12.2:Permissions' could not be found"
     - Причина: Gradle не знаходить підпроєкт з таким шляхом — невірні include-и у `settings.gradle` або неправильний `projectDir`.
     - Рішення:
         - Відкрий `settings.gradle` у корені репозиторію і переконайся, що є рядок `include ':1.12.2:Permissions'` і що `project(':1.12.2:Permissions').projectDir` вказує на `mods/1.12.2/Permissions`.
         - Якщо запускаєш MDK wrapper зі всередини `mdks/<version>/forge-...`, переконайся, що MDK `settings.gradle` реєструє ті самі повні project paths або запускай збірку з кореню (рекомендовано для мульти-модульних збірок).

3) "java.lang.UnsupportedClassVersionError" або інші JVM-помилки
     - Причина: Невідповідність версії Java (наприклад, збірка під Java 8, але на машині встановлено Java 11 або навпаки).
     - Рішення:
         - Переконайся, що `java -version` повертає Java 8 під час збірки. На CI вкажи JDK8 (Temurin/AdoptOpenJDK).
         - Можеш встановити `org.gradle.java.home` в `gradle.properties` для вказання JDK, який Gradle повинен використовувати.

4) Помилки компіляції (cannot find symbol, missing method тощо)
     - Причина: Невідповідні імпорти, відсутні методи, або неправильні залежності між локальними підпроєктами.
     - Рішення:
         - Переглянь повідомлення компілятора (Gradle виводить файл і рядок). Виправ код у `mods/<version>/<mod>/src/main/java/...`.
         - Якщо мод залежить від іншого локального моду, переконайся, що в `build.gradle` використовується повний шлях `compile project(':1.12.2:Permissions')` або еквівалент.

5) Проблеми з MDK/mappings або першим запуском (завантаження залежностей)
     - Причина: MDK підвантажує MCP mappings і Forge залежності під час першого build — це може зайняти час або вимагати інтернету.
     - Рішення:
         - Переконайся, що в тебе є інтернет під час першого build, або налаштуй локальний кеш/проксі.

Діагностика (корисні параметри)
- Додай `--stacktrace`/`--info` до Gradle команди щоб отримати повніший трейс:

```powershell
& "tools\gradle\bin\gradle.bat" build --no-daemon --stacktrace --info
```

- Якщо хочеш пропустити тести при відлагодженні: `gradle build -x test`.
- Для швидкого пошуку проблема в коді: зверни увагу на першу помилку у виводі Gradle — вона зазвичай є коренем проблеми.

Приклад конкретного відлагодження
- Помилка: `cannot find symbol PermissionsAPI` у `ChunkGuard` → перевір:
    1) чи `mods/1.12.2/Permissions` включений у `settings.gradle`;
    2) чи у `ChunkGuard` додано `import com.systmeo.permissions.PermissionsAPI;`;
    3) чи `build.gradle` у `ChunkGuard` має `compile project(':1.12.2:Permissions')`.


Контрибуція та стиль
- Коли додаєш мод — додай `mcmod.info` (якщо потрібно), README в папці моду з інструкціями, та онови `settings.gradle` (root) для включення нового моду.
- Дотримуйся Java 8 (sourceCompatibility / targetCompatibility у `build.gradle` кожного моду).

Що зроблено у цьому репозиторії (коротко)
- Реструктуризація workspace у multi-version layout.
- Скрипти для збірки/збору релізів з підтримкою `-Version`.
- Локальна Gradle інсталяція для сумісності з FG3 (4.9).
- Автоматична збірка і збір JAR для `mods/1.12.2` пройдена та JAR скопійовані у `releases/`.

Далі — дорожня карта (детально дивись roadmap у корені або запроси її у цьому репо).

---
README оновлено: 2025-09-14
### ПРОЄКТ: [MEO] - Технічний Опис

## 1. ЗАГАЛЬНА КОНЦЕПЦІЯ

**Назва Проєкту:** [MEO]

**Головна Мета:** Створити серію високоякісних, самодостатніх та взаємопов'язаних модів для Minecraft Forge 1.12.2, які повністю замінюють функціонал популярних плагінів Bukkit/Spigot. Проєкт орієнтований на українську та міжнародну спільноти, з акцентом на продуктивність, гнучкість та зручність у використанні.

**Основні Принципи:**
- **Нативність:** Усі моди розробляються виключно на Forge API, без залежностей від гібридних ядер (Mohist, Magma тощо).
- **Модульність та API:** Кожен мод є самодостатнім, але надає чистий та стабільний API для взаємодії з іншими модами серії [MEO].
- **Продуктивність:** Архітектура модів передбачає кешування та оптимізовані алгоритми для мінімального впливу на продуктивність сервера.
- **Локалізація:** Повна підтримка багатомовності через стандартні файли `.lang`.
- **Зручність:** Наявність як потужних консольних команд, так і інтуїтивно зрозумілих графічних інтерфейсів (GUI) для адміністрування.
- **Відкритість:** Код добре задокументований (Javadoc) та готовий до публікації на платформах, як-от GitHub.

---

## 2. ЗАВЕРШЕНІ МОДИ

### 2.1. Permissions [MEO]

**Версія:** `1.0.0-1.12.2`
**Призначення:** Повноцінна заміна LuckPerms. Потужна система управління правами доступу.

**Ключові Особливості:**
- Управління правами для гравців та груп.
- Спадкування прав від батьківських груп.
- Тимчасові права та тимчасове членство у групах (напр., `1d`, `12h`).
- Префікси, суфікси та вага груп для налаштування чату.
- Система рангів (Треки) для легкого підвищення гравців.
- Високопродуктивна система кешування для миттєвої перевірки прав.
- Повноцінний графічний інтерфейс (`/perms gui`) для управління всіма аспектами.
- Зберігання даних у `config/permissions/` у файлах `users.json`, `groups.json`, `tracks.json`.
- Повна локалізація (13 мов).

**Архітектура:**
- **`Permissions.java`:** Головний клас. Ініціалізує менеджери, команди, GUI та мережевий канал.
- **`PermissionManager.java`:** "Мозок" моду. Обробляє логіку перевірки прав, наслідування, кешування та управління даними в пам'яті.
- **`DataManager.java`:** Відповідає за збереження та завантаження даних у JSON-файли.
- **Класи в `data/`:** `User`, `Group`, `Track`, `PermissionNode`, `GroupNode` - моделі даних, що описують сутності.
- **`PermissionsAPI.java`:** Публічний, стабільний API для інших модів.
- **Класи в `commands/`:** Розгалужена, локалізована система команд.
- **Класи в `gui/`:** Багатоекранний, інтерактивний GUI для адміністрування.
- **Класи в `network/`:** Система пакетів для синхронізації даних між сервером та клієнтським GUI.

**Команди:** `/perms` (аліаси: `/p`)
- `/perms user <гравець> <info|permission|parent|promote> ...`
- `/perms group <група> <info|permission|parent|create|delete|setprefix|setsuffix|setweight> ...`
- `/perms track <трек> <create|delete|append> ...`
- `/perms check <гравець> <право>`
- `/perms gui`

**API для розробників (`com.systmeo.permissions.PermissionsAPI`):**
'''java
// Перевірка права
boolean hasFly = PermissionsAPI.hasPermission(player.getUniqueID(), "essentials.fly");

// Отримання префіксу для чату
String prefix = PermissionsAPI.getPrefix(player.getUniqueID());
'''

---

### 2.2. Wallet [MEO]

**Версія:** `1.0.0-1.12.2`
**Призначення:** Заміна Vault/EssentialsEco. Легке та надійне ядро серверної економіки.

**Ключові Особливості:**
- Серверна економіка з балансами гравців.
- Налаштування стартового балансу та символу валюти у файлі `wallet.cfg`.
- Команди для гравців (`/balance`, `/pay`) та адміністраторів (`/eco`).
- Лідерборд найбагатших гравців (`/baltop`).
- Зручний графічний інтерфейс (`/wallet gui`) для перегляду балансу, переказів та топу.
- Чистий API для інтеграції з іншими модами (магазини, аукціони).
- Зберігання даних у `config/wallet/accounts.json`.
- Повна локалізація (13 мов).

**Архітектура:**
- **`Wallet.java`:** Головний клас. Ініціалізує менеджери, конфігурацію, команди, GUI та мережевий канал.
- **`AccountManager.java`:** Керує балансами гравців у пам'яті.
- **`DataManager.java`:** Відповідає за збереження та завантаження балансів у JSON-файл.
- **`WalletConfig.java`:** Обробляє конфігураційний файл `.cfg`.
- **`WalletAPI.java`:** Публічний, стабільний API для інших модів.
- **Класи в `commands/`:** Локалізована система команд.
- **Класи в `gui/`:** Інтерактивний GUI.
- **Класи в `network/`:** Система пакетів для синхронізації даних з GUI.

**Команди:** `/balance` (аліаси: `/bal`, `/money`), `/eco`, `/wallet`
- `/balance [гравець]`
- `/pay <гравець> <сума>`
- `/baltop`
- `/eco <give|take|set> <гравець> <сума>`
- `/wallet reload`
- `/wallet gui`

**API для розробників (`com.systmeo.wallet.api.WalletAPI`):**
'''java
// Перевірка, чи достатньо грошей для покупки
if (WalletAPI.hasEnough(player.getUniqueID(), 100.50)) {
    // Зняття коштів
    boolean success = WalletAPI.withdraw(player.getUniqueID(), 100.50);
    if (success) {
        // Видати предмет
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
