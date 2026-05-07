param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("local", "central")]
    [string]$Mode,

    [switch]$NoConfigurationCache,
    [string]$Version
)

$ErrorActionPreference = "Stop"

$gradlew = Join-Path $PSScriptRoot "..\gradlew.bat"

if ($Mode -eq "local") {
    Write-Host "Publishing to local Maven cache (snapshot allowed)."
    $tasks = @(
        ":framework-dsl:publishToMavenLocal",
        ":framework-state:publishToMavenLocal",
        ":framework-mvvm:publishToMavenLocal",
        ":framework-navigation:publishToMavenLocal",
        ":framework-theme:publishToMavenLocal",
        ":framework-components:publishToMavenLocal"
    )
} else {
    $envPath = Join-Path $PSScriptRoot "..\.env"
    if (Test-Path $envPath) {
        $envContent = Get-Content $envPath -Raw
        $required = @(
            "mavenCentralUsername",
            "mavenCentralPassword",
            "signingInMemoryKey",
            "signingInMemoryKeyId",
            "signingInMemoryKeyPassword"
        )

        foreach ($key in $required) {
            $value = [regex]::Match($envContent, "(?m)^$([regex]::Escape($key))=(.*)$")
            if (-not $value.Success -or [string]::IsNullOrWhiteSpace($value.Groups[1].Value)) {
                throw "Missing or empty $key in .env"
            }
        }
    } else {
        Write-Host "No .env found. Using Gradle properties/environment credentials."
    }

    Write-Host "Publishing to Maven Central via publishAndReleaseToMavenCentral."
    $tasks = @("publishAndReleaseToMavenCentral")
    if (-not [string]::IsNullOrWhiteSpace($Version)) {
        Write-Host "Publishing with releaseVersion=$Version."
    }
}

if ($Mode -eq "central" -and [string]::IsNullOrWhiteSpace($Version)) {
    throw "Release mode requires -Version (example: 0.1.0). Snapshot versions are not allowed."
}

if ($Mode -eq "central" -and -not [string]::IsNullOrWhiteSpace($Version)) {
    $Version = $Version.Trim().TrimStart("v")
    if ($Version -like "*-SNAPSHOT") {
        throw "Release mode requires a non-SNAPSHOT version. Got: $Version"
    }
}

$cacheOption = @()
if ($NoConfigurationCache) {
    $cacheOption += "--no-configuration-cache"
}

if ($Mode -eq "central" -and -not [string]::IsNullOrWhiteSpace($Version)) {
    $tasks = @("-PreleaseVersion=$Version") + $tasks
}

$command = @($gradlew) + $cacheOption + $tasks
Write-Host ("Run: {0}" -f ($command -join " "))
& $gradlew @cacheOption @tasks
