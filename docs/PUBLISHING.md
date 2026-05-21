# Publishing Setup (reference)

KoraFX uses `com.vanniktech.maven.publish` and reads publishing metadata from environment variables.
Current coordinates are under groupId `io.github.daiyuang`.
Published artifactIds use the `korafx-*` prefix, for example `korafx-framework`.
The BOM artifactId is `korafx-bom`.

## Env file example

Create `.env` in the repository root (this file is already ignored by `.gitignore`):

```properties
mavenCentralUsername=<your-sonatype-username>
mavenCentralPassword=<your-sonatype-password>

signingInMemoryKey=<gpg-private-key-block>
signingInMemoryKeyId=<gpg-key-id>
signingInMemoryKeyPassword=<gpg-key-password>

POM_NAME=korafx-framework
POM_DESCRIPTION=Kotlin-first JavaFX application framework with Koin, MVVM, navigation and theme services.
POM_INCEPTION_YEAR=2026
POM_URL=https://github.com/DaiYuANg/koraFX
POM_LICENSE_NAME=The Apache License, Version 2.0
POM_LICENSE_URL=https://www.apache.org/licenses/LICENSE-2.0.txt
POM_LICENSE_DIST=repo
POM_DEVELOPER_ID=DaiYuANg
POM_DEVELOPER_NAME=DaiYuANg
POM_DEVELOPER_URL=https://github.com/DaiYuANg
POM_SCM_URL=https://github.com/DaiYuANg/koraFX
POM_SCM_CONNECTION=scm:git:https://github.com/DaiYuANg/koraFX.git
POM_SCM_DEV_CONNECTION=scm:git:ssh://git@github.com/DaiYuANg/koraFX.git
```

Per-module publish usually only needs different `POM_NAME` and `POM_DESCRIPTION`.

## Typical commands

```powershell
.\gradlew.bat :korafx-framework:publishToMavenCentral --no-configuration-cache
```

```properties
implementation(platform("io.github.daiyuang:korafx-bom:<version>"))
implementation("io.github.daiyuang:korafx-framework:<version>")
```

With the BOM, omit repeated module versions:

```properties
implementation(platform("io.github.daiyuang:korafx-bom:<version>"))
implementation("io.github.daiyuang:korafx-framework")
implementation("io.github.daiyuang:korafx-navigation")
implementation("io.github.daiyuang:korafx-command-palette")
implementation("io.github.daiyuang:korafx-components")
implementation("io.github.daiyuang:korafx-data-grid")
implementation("io.github.daiyuang:korafx-inspector-panel")
implementation("io.github.daiyuang:korafx-resource-explorer")
implementation("io.github.daiyuang:korafx-source-editor")
testImplementation("io.github.daiyuang:korafx-test")
```

Local validation:

```powershell
.\gradlew.bat :korafx-framework:publishToMavenLocal --no-configuration-cache
```

Close and release Central staging repository:

```powershell
.\gradlew.bat :korafx-framework:closeAndReleaseRepository --no-configuration-cache
```

Publish all library modules (sample app is excluded):

```powershell
pwsh .\scripts\release.ps1 -Mode local
```

Central publish:

```powershell
pwsh .\scripts\release.ps1 -Mode central -Version <release-version>
```

## CI publishing

The repository also supports publishing by GitHub Actions:

- `.github/workflows/ci.yml`: builds on push/PR.
- `.github/workflows/release.yml`: publishes on `vX.Y.Z` tags and on manual `workflow_dispatch`.

Recommended repository secrets:

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `SIGNING_IN_MEMORY_KEY`
- `SIGNING_IN_MEMORY_KEY_ID`
- `SIGNING_IN_MEMORY_KEY_PASSWORD`

Optional repository variables (or rely on default values):

- `POM_NAME`
- `POM_DESCRIPTION`
- `POM_INCEPTION_YEAR`
- `POM_URL`
- `POM_LICENSE_NAME`
- `POM_LICENSE_URL`
- `POM_LICENSE_DIST`
- `POM_DEVELOPER_ID`
- `POM_DEVELOPER_NAME`
- `POM_DEVELOPER_URL`
- `POM_SCM_URL`
- `POM_SCM_CONNECTION`
- `POM_SCM_DEV_CONNECTION`
