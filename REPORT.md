# Отчёт по практическому занятию

## 1. Номер и название практического занятия

**Практическое занятие 2. Организация проекта с системой контроля версий и CI/CD.**

## 2. Цели

- Освоить организацию Java-проекта с использованием Git и GitHub.
- Применить модель ветвления GitHub Flow: отдельная ветка и Pull Request для каждой задачи.
- Настроить непрерывную интеграцию (CI) на GitHub Actions: сборка, тесты и автоматический выпуск релиза.
- Автоматизировать публикацию артефакта в GitHub Packages (Maven).
- Добавить nightly-сборку для регулярной проверки стабильности.
- Закрепить навыки написания модульных тестов на JUnit 5 и оформления технической документации.

## 3. Ход выполнения работы

### 3.1. Создание репозитория и начальная настройка

Проект создан с нуля в каталоге `C:\pr 2 sanya`:

- инициализирован Git-репозиторий с веткой `main`;
- сгенерирован Gradle Wrapper 8.10.2 командой `gradle wrapper --gradle-version 8.10.2`;
- подготовлены `settings.gradle`, `build.gradle`, `gradle.properties` и `.gitignore`;
- выполнен начальный коммит `chore: initialize Gradle project with wrapper`;
- создан публичный репозиторий GitHub и отправлена ветка `main`:

```bash
gh repo create todo-app --public --source=. --remote=origin --push
```

Репозиторий: https://github.com/kys1337/todo-app

В `.gitignore` исключены каталоги `build/`, `.gradle/`, файлы IDE, `*.class`, `*.log`, `out/` и другой служебный мусор. При этом `gradle/wrapper/gradle-wrapper.jar` намеренно не игнорируется, поэтому Wrapper полностью хранится в репозитории.

### 3.2. GitHub Flow и работа с ветками

После начального коммита изменения в `main` вносились только через Pull Request. Для каждой задачи создавалась отдельная ветка от актуального `main`. После проверки CI выполнялось слияние:

```bash
gh pr merge <номер> --merge --delete-branch
git checkout main
git pull
```

| PR | Ветка | Содержание | Ссылка |
|----|-------|------------|--------|
| #1 | `feat/todo-app-implementation` | бизнес-логика `TodoList`, консольный `TodoApp`, тесты JUnit 5 | https://github.com/kys1337/todo-app/pull/1 |
| #2 | `feat/actions` | Shadow fat-JAR, CI workflow со сборкой, тестами и релизом | https://github.com/kys1337/todo-app/pull/2 |
| #3 | `feat/github-packages` | публикация Maven-пакета в GitHub Packages | https://github.com/kys1337/todo-app/pull/3 |
| #4 | `feat/extra-commands` | консольные тесты команд `clear`, `done`, `search`, повышение версии | https://github.com/kys1337/todo-app/pull/4 |
| #5 | `feat/nightly-build` | nightly-workflow и повышение версии | https://github.com/kys1337/todo-app/pull/5 |
| #6 | `docs/report` | README, отчёт и финальное повышение версии | https://github.com/kys1337/todo-app/pull/6 |

Все Pull Request содержат осмысленные заголовки и описания; после слияния удалённые ветки удалены.

### 3.3. Реализация приложения

Код расположен в пакете `org.example`.

**`TodoList`** — бизнес-логика списка задач:

- `add(String)` — игнорирует `null`, обрезает пробелы, не добавляет пустые строки;
- `remove(int)` — удаляет задачу по индексу, возвращает `false` при выходе за границы;
- `getAll()` — возвращает копию списка в формате `[ ] задача` / `[x] задача`;
- `size()` — количество задач;
- `clear()` — очищает список;
- `markDone(int)` — помечает задачу выполненной, возвращает `false` при неверном индексе;
- `search(String)` — поиск по подстроке без учёта регистра, возвращает совпадения со статусом.

Внутри класс хранит `List<String>` с текстами задач и параллельный `List<Boolean>` с признаками выполнения. Это сохраняет порядок и корректные статусы при удалении задач.

**`TodoApp`** — консольное приложение:

- приветствие и список доступных команд;
- цикл чтения команд из `Scanner`;
- команды `add`, `remove`, `done`, `search`, `list`, `clear`, `help`, `exit`;
- понятные сообщения об ошибках: отсутствующий аргумент, нечисловой индекс, индекс вне диапазона, неизвестная команда;
- индексы нумеруются с нуля.

Идентификаторы оформлены в стиле Java: классы в PascalCase, методы и переменные в camelCase, константы в UPPER_SNAKE_CASE. Транслита и кириллицы в идентификаторах нет.

Версия проекта хранится в `gradle.properties` в формате `ключ=значение`:

```properties
version=0.4.0
```

В `build.gradle` версия читается как `findProperty('version') ?: '0.1.0'`, подключены плагины `java`, `application`, `maven-publish` и `com.github.johnrengelman.shadow` 8.1.1. Настроены Java 21, JUnit 5 через `junit-bom`, `useJUnitPlatform()`, fat-JAR `build/libs/todo-app-<version>.jar`, отключены обычный `jar` и неиспользуемые dist-задачи. Задача `build` зависит от `shadowJar`.

### 3.4. Тесты

Тесты находятся в `src/test/java/org/example`:

- `TodoListTest` — 20 тестов: добавление и `trim`, игнорирование `null` и пустых строк, удаление и граничные индексы, копия `getAll`, `clear`, `markDone`, `search` (совпадение, отсутствие совпадений, регистр), размер списка;
- `TodoAppTest` — 11 тестов: команды `add`, `list`, `done`, `search`, `clear`, `remove`, обработка ошибок, неизвестная команда и выход.

Всего 31 тест, все проходят локально:

```powershell
.\gradlew.bat clean build
```

### 3.5. Непрерывная интеграция и релизы

Файл `.github/workflows/ci.yml`, workflow **«CI / Build & Release»**:

- запускается при `push` в `main` и при `pull_request` в `main`;
- имеет права `contents: write` для создания релиза;
- выполняет checkout, установку JDK 21 (Temurin), кеширование Gradle, `chmod +x gradlew`;
- читает версию из `gradle.properties`;
- запускает `./gradlew --no-daemon clean build` — при падении тестов workflow падает и релиз не создаётся;
- на push в `main` создаёт GitHub Release с тегом `v<version>` и прикрепляет `build/libs/todo-app-<version>.jar` с помощью `softprops/action-gh-release@v2`.

Использованные версии actions зафиксированы: `actions/checkout@v4`, `actions/setup-java@v4`, `actions/cache@v4`, `softprops/action-gh-release@v2`.

Успешные запуски CI для `main`:

- v0.1.0: https://github.com/kys1337/todo-app/actions/runs/45203712938
- v0.2.0: https://github.com/kys1337/todo-app/actions/runs/45204825125
- v0.3.0: https://github.com/kys1337/todo-app/actions/runs/45205255999

Релизы: https://github.com/kys1337/todo-app/releases

В каждом релизе приложен fat-JAR `todo-app-<version>.jar`.

### 3.6. Публикация в GitHub Packages

Файл `.github/workflows/publish-package.yml`, workflow **«Publish to GitHub Packages (Maven)»**:

- запускается по событию `workflow_run` после завершения workflow «CI / Build & Release»;
- выполняется только при `conclusion == 'success'` и только для ветки `main`;
- имеет права `contents: read` и `packages: write`;
- выполняет `./gradlew publish` с переменными `GITHUB_ACTOR` и `GITHUB_TOKEN`.

В `build.gradle` добавлен блок `publishing`:

- публикация `groupId 'org.example'`, `artifactId 'todo-app'`, версия из `project.version`;
- артефакт — fat-JAR из задачи `shadowJar` без классификатора;
- репозиторий `https://maven.pkg.github.com/kys1337/todo-app`;
- учётные данные берутся из `gpr.user`/`gpr.key` или переменных окружения.

Так как GitHub Packages запрещает перезапись уже опубликованной версии, перед каждым релизом версия повышалась: `0.1.0` → `0.2.0` → `0.3.0` → `0.4.0`. Публикации версий 0.1.0–0.3.0 завершились успешно; версия 0.4.0 публикуется после слияния PR #6.

Пакет: https://github.com/kys1337/todo-app/packages/3413026

Успешные запуски публикации:

- 0.1.0: https://github.com/kys1337/todo-app/actions/runs/45204272327
- 0.2.0: https://github.com/kys1337/todo-app/actions/runs/45204920328
- 0.3.0: https://github.com/kys1337/todo-app/actions/runs/45205347452

### 3.7. Дополнительные задания

- Реализованы команды `clear`, `done`, `search`.
- Добавлены тесты для `TodoList` и консольных команд.
- Настроен nightly-workflow `.github/workflows/nightly.yml`: запуск по расписанию `0 20 * * *` и вручную через `workflow_dispatch`, только сборка и тесты без релиза.
- Nightly-сборка запущена вручную и завершилась успешно: https://github.com/kys1337/todo-app/actions/runs/45205524532
- Версия проекта вынесена в `gradle.properties` и повышается перед каждым релизом.

### 3.8. Проверка результата

Выполнены проверки:

```bash
gh run list --limit 10
gh release list
gh api /users/kys1337/packages/maven/org.example.todo-app/versions
```

- последние запуски CI и публикации для `main` завершились успешно;
- созданы релизы v0.1.0, v0.2.0, v0.3.0 (v0.4.0 — после слияния PR #6), в каждом приложен JAR;
- в GitHub Packages присутствует пакет `org.example.todo-app` с версиями 0.1.0–0.3.0 (и 0.4.0 после финального слияния);
- скачанный из релиза fat-JAR проверен командами `add`, `list`, `done`, `search`, `clear`, `exit`;
- локальная сборка `.\gradlew.bat clean build` проходит, все 31 тест зелёные.

## 4. Краткий вывод

В ходе работы был создан полноценный Java-проект с консольным приложением, модульными тестами и автоматизированным жизненным циклом доставки на GitHub. Освоены GitHub Flow, настройка GitHub Actions, кеширование Gradle, автоматическое создание релизов с артефактом, публикация Maven-пакета в GitHub Packages и nightly-сборка. Разделение на небольшие ветки и Pull Request сделало историю изменений прозрачной, а CI гарантирует, что в `main` попадает только собираемый и протестированный код.

## Ссылки

- Репозиторий: https://github.com/kys1337/todo-app
- Actions: https://github.com/kys1337/todo-app/actions
- Releases: https://github.com/kys1337/todo-app/releases
- Packages: https://github.com/kys1337/todo-app/packages
- Pull Requests: https://github.com/kys1337/todo-app/pulls
