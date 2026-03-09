$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$modsDir = "C:\Users\Ender\curseforge\minecraft\Instances\ODYG\mods"
$artifactPrefix = "florence-client-"

if (!(Test-Path -LiteralPath $modsDir)) {
    throw "Mods folder not found: $modsDir"
}

Push-Location $repoRoot
try {
    & .\gradlew.bat build
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE."
    }

    $artifact = Get-ChildItem -Path ".\build\libs" -File |
        Where-Object {
            $_.Extension -eq ".jar" -and
            $_.Name -like "$artifactPrefix*" -and
            $_.Name -notlike "*-sources.jar" -and
            $_.Name -notlike "*-javadoc.jar" -and
            $_.Name -notlike "*-dev.jar"
        } |
        Sort-Object LastWriteTimeUtc -Descending |
        Select-Object -First 1

    if ($null -eq $artifact) {
        throw "No built mod jar found in build\libs."
    }

    Get-ChildItem -Path $modsDir -File |
        Where-Object { $_.Name -like "$artifactPrefix*.jar" } |
        Remove-Item -Force

    $destination = Join-Path $modsDir $artifact.Name
    Copy-Item -LiteralPath $artifact.FullName -Destination $destination -Force

    Write-Host "Synced $($artifact.Name) to $destination"
}
finally {
    Pop-Location
}
