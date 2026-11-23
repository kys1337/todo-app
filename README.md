# Todo App

[![CI / Build & Release](https://github.com/kys1337/todo-app/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/kys1337/todo-app/actions/workflows/ci.yml)

A small console todo list application built with Java 21 and Gradle. It demonstrates a complete
GitHub-based delivery pipeline: feature branches and pull requests, continuous integration,
automatic GitHub Releases with a fat JAR, publishing to GitHub Packages, and a nightly build.

## Features

- Add, remove, list, complete, search and clear tasks.
- Task text is trimmed; `null` and blank values are rejected.
- Completion status is preserved and displayed as `[ ]` / `[x]`.
- Case-insensitive substring search.
- Fat JAR produced by the Gradle Shadow plugin.

## Requirements

- JDK 21
- Git
- No local Gradle installation is needed: the Gradle Wrapper is committed.

## Build and run

```bash
git clone https://github.com/kys1337/todo-app.git
cd todo-app
./gradlew clean build
java -jar build/libs/todo-app-0.4.0.jar
```

On Windows PowerShell:

```powershell
.\gradlew.bat clean build
java -jar build/libs\todo-app-0.4.0.jar
```

Run the tests only:

```bash
./gradlew test
```

## Commands

Indices are zero-based.

| Command        | Description                              |
|----------------|------------------------------------------|
| `add <task>`   | Add a new task                           |
| `remove <index>` | Remove the task at the given index     |
| `done <index>` | Mark the task at the given index as done |
| `search <text>` | Find tasks by a case-insensitive substring |
| `list`         | Show all tasks with their status         |
| `clear`        | Remove all tasks                         |
| `help`         | Show the command reference               |
| `exit`         | Quit the application                     |

Example session:

```text
> add Buy milk
Added: Buy milk
> add Read a book
Added: Read a book
> done 1
Marked task at index 1 as done.
> list
Todo list:
  0. [ ] Buy milk
  1. [x] Read a book
> search milk
Found 1 task(s):
  1. [ ] Buy milk
> exit
Goodbye!
```

## Project structure

```text
.
├── .github/workflows/        # CI, GitHub Packages and nightly workflows
├── gradle/wrapper/           # Gradle Wrapper
├── src/main/java/org/example/
│   ├── TodoApp.java          # Console entry point and command parsing
│   └── TodoList.java         # Business logic
├── src/test/java/org/example/
│   ├── TodoAppTest.java      # Console-level tests
│   └── TodoListTest.java     # Unit tests
├── build.gradle
├── gradle.properties         # Project version
└── settings.gradle
```

## CI/CD

- **CI / Build & Release** (`.github/workflows/ci.yml`) runs on pushes and pull requests to `main`:
  builds and tests the project, then creates a GitHub Release `v<version>` with the fat JAR when
  the build runs on `main`.
- **Publish to GitHub Packages (Maven)** (`.github/workflows/publish-package.yml`) runs after a
  successful `CI / Build & Release` run on `main` and publishes the fat JAR as
  `org.example:todo-app:<version>`.
- **Nightly Build** (`.github/workflows/nightly.yml`) runs every day at 20:00 UTC and can be
  started manually. It only verifies build and test stability.

The version is stored in `gradle.properties`. GitHub Packages does not allow overwriting an
existing Maven version, so the version is increased before every release.

## Links

- Repository: https://github.com/kys1337/todo-app
- Actions: https://github.com/kys1337/todo-app/actions
- Releases: https://github.com/kys1337/todo-app/releases
- Packages: https://github.com/kys1337/todo-app/packages
- Maven package: https://github.com/kys1337/todo-app/packages/3413026
- Pull requests: https://github.com/kys1337/todo-app/pulls

## Using the Maven package

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/kys1337/todo-app")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'org.example:todo-app:0.4.0'
}
```
