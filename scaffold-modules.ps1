[CmdletBinding()]
param(
    [string]$StructureFile = "COMPLETE_STRUCTURE.txt",
    [string]$OutputRoot = ".",
    [string[]]$TargetRoots = @(
        "platform-shared",
        "bounded-contexts/tenancy-identity",
        "bounded-contexts/finance"
    ),
    [switch]$DryRun,
    [switch]$VerifyConformance,
    [switch]$Force,
    [switch]$Detailed,
    [switch]$KeepTemp,
    [string[]]$IgnorePathSegments = @(
        "build",
        ".gradle",
        ".idea",
        "out",
        "bin",
        "target"
    )
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Normalize-PathToken {
    param([Parameter(Mandatory = $true)][string]$PathToken)

    $normalized = $PathToken.Trim().Replace('\', '/')
    while ($normalized.StartsWith("./")) {
        $normalized = $normalized.Substring(2)
    }
    return $normalized.Trim('/')
}

function Get-RelativeParent {
    param([Parameter(Mandatory = $true)][string]$RelativePath)

    $idx = $RelativePath.LastIndexOf('/')
    if ($idx -lt 0) {
        return $null
    }
    return $RelativePath.Substring(0, $idx)
}

function Test-ShouldIgnoreRelativePath {
    param(
        [Parameter(Mandatory = $true)][string]$RelativePath,
        [Parameter(Mandatory = $true)][System.Collections.Generic.HashSet[string]]$IgnoreSegments
    )

    $segments = $RelativePath.Replace('\', '/').Split('/', [System.StringSplitOptions]::RemoveEmptyEntries)
    foreach ($segment in $segments) {
        if ($IgnoreSegments.Contains($segment.ToLowerInvariant())) {
            return $true
        }
    }
    return $false
}

function Join-RootAndRelative {
    param(
        [Parameter(Mandatory = $true)][string]$RootPath,
        [Parameter(Mandatory = $true)][string]$RelativePath
    )

    $platformRelative = $RelativePath.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
    return Join-Path -Path $RootPath -ChildPath $platformRelative
}

function Get-RelativePathPortable {
    param(
        [Parameter(Mandatory = $true)][string]$BasePath,
        [Parameter(Mandatory = $true)][string]$TargetPath
    )

    $baseFull = [System.IO.Path]::GetFullPath($BasePath)
    $targetFull = [System.IO.Path]::GetFullPath($TargetPath)

    if (-not $baseFull.EndsWith([System.IO.Path]::DirectorySeparatorChar)) {
        $baseFull += [System.IO.Path]::DirectorySeparatorChar
    }

    $baseUri = New-Object System.Uri($baseFull)
    $targetUri = New-Object System.Uri($targetFull)
    $relativeUri = $baseUri.MakeRelativeUri($targetUri)
    $relativePath = [System.Uri]::UnescapeDataString($relativeUri.ToString())
    return $relativePath.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
}

function Test-IsUnderAnyRoot {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string[]]$Roots
    )

    foreach ($root in $Roots) {
        if ($Path -eq $root -or $Path.StartsWith("$root/")) {
            return $true
        }
    }
    return $false
}

function Parse-CompleteStructure {
    param([Parameter(Mandatory = $true)][string]$FilePath)

    if (-not (Test-Path -LiteralPath $FilePath -PathType Leaf)) {
        throw "Structure file not found: $FilePath"
    }

    $entries = New-Object System.Collections.Generic.List[object]
    $stack = New-Object System.Collections.Generic.List[string]
    $lines = Get-Content -LiteralPath $FilePath -Encoding UTF8

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        if ($line -notmatch '^(?<indent>[\u2502 ]*)(?<branch>\u251C\u2500\u2500|\u2514\u2500\u2500)\s+(?<name>.+)$') {
            continue
        }

        $indent = $Matches['indent']
        if (($indent.Length % 4) -ne 0) {
            throw "Malformed indentation at line $($i + 1): $line"
        }
        $depth = [int]($indent.Length / 4)

        $rawName = $Matches['name'].Trim()
        if ([string]::IsNullOrWhiteSpace($rawName)) {
            continue
        }

        $cleanName = ($rawName -replace '\s+#.*$', '').Trim()
        if ([string]::IsNullOrWhiteSpace($cleanName)) {
            continue
        }

        $isDirectory = $cleanName.EndsWith('/')
        $nodeName = $cleanName.TrimEnd('/')
        if ([string]::IsNullOrWhiteSpace($nodeName)) {
            continue
        }

        while ($stack.Count -gt $depth) {
            $stack.RemoveAt($stack.Count - 1)
        }

        if ($stack.Count -lt $depth) {
            throw "Tree depth jumped unexpectedly at line $($i + 1): $line"
        }

        if ($stack.Count -eq $depth) {
            $stack.Add($nodeName)
        }
        else {
            $stack[$depth] = $nodeName
        }

        $relativePath = [string]::Join('/', $stack)
        $entries.Add([pscustomobject]@{
                LineNumber = $i + 1
                Path       = $relativePath
                Kind       = if ($isDirectory) { "Directory" } else { "File" }
            }) | Out-Null
    }

    return $entries
}

function Build-ExpectedPaths {
    param(
        [Parameter(Mandatory = $true)][object[]]$Entries,
        [Parameter(Mandatory = $true)][string[]]$Roots
    )

    $directories = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
    $files = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)

    foreach ($entry in $Entries) {
        if (-not (Test-IsUnderAnyRoot -Path $entry.Path -Roots $Roots)) {
            continue
        }

        if ($entry.Kind -eq "Directory") {
            $null = $directories.Add($entry.Path)
            continue
        }

        $null = $files.Add($entry.Path)
    }

    foreach ($file in $files) {
        $parent = Get-RelativeParent -RelativePath $file
        while ($null -ne $parent -and $parent.Length -gt 0) {
            if (-not (Test-IsUnderAnyRoot -Path $parent -Roots $Roots)) {
                break
            }
            $null = $directories.Add($parent)
            $parent = Get-RelativeParent -RelativePath $parent
        }
    }

    return [pscustomobject]@{
        Directories = @($directories | Sort-Object @{ Expression = { ($_ -split '/').Count } }, @{ Expression = { $_ } })
        Files       = @($files | Sort-Object)
    }
}

function Get-KotlinPackage {
    param([Parameter(Mandatory = $true)][string]$RelativeFilePath)

    $normalized = $RelativeFilePath.Replace('\', '/')
    if ($normalized -match '/src/(main|test)/kotlin/(?<pkgPath>.+)/[^/]+$') {
        return ($Matches['pkgPath'] -replace '/', '.')
    }
    return $null
}

function Get-SafeIdentifier {
    param([Parameter(Mandatory = $true)][string]$InputText)

    $name = $InputText -replace '[^A-Za-z0-9]', '_'
    $name = $name.Trim('_')
    if ([string]::IsNullOrWhiteSpace($name)) {
        $name = "FILE"
    }
    if ($name -match '^[0-9]') {
        $name = "_$name"
    }
    return $name
}

function Get-PlaceholderContent {
    param([Parameter(Mandatory = $true)][string]$RelativeFilePath)

    $extension = [System.IO.Path]::GetExtension($RelativeFilePath).ToLowerInvariant()
    $fileNameWithoutExtension = [System.IO.Path]::GetFileNameWithoutExtension($RelativeFilePath)
    $safeBase = Get-SafeIdentifier -InputText $fileNameWithoutExtension

    switch ($extension) {
        ".kt" {
            $package = Get-KotlinPackage -RelativeFilePath $RelativeFilePath
            $packageLine = ""
            if ($null -ne $package -and $package.Length -gt 0) {
                $packageLine = "package $package`n`n"
            }

            $constName = "PLACEHOLDER_{0}" -f ($safeBase.ToUpperInvariant())
            return @"
$packageLine/*
 * Placeholder generated from COMPLETE_STRUCTURE.txt
 * Path: $RelativeFilePath
 */
@Suppress("unused")
private const val $constName = "TODO: Implement $RelativeFilePath"
"@
        }
        ".sql" {
            return @"
-- Placeholder generated from COMPLETE_STRUCTURE.txt
-- Path: $RelativeFilePath
-- TODO: Add SQL statements.
"@
        }
        ".avro" {
            $recordName = $safeBase
            if ($recordName -notmatch '^[A-Za-z_]') {
                $recordName = "R$recordName"
            }

            return @"
{
  "type": "record",
  "name": "$recordName",
  "namespace": "com.chiroerp.placeholder",
  "fields": [
    {
      "name": "todo",
      "type": "string"
    }
  ]
}
"@
        }
        ".yml" {
            return @"
# Placeholder generated from COMPLETE_STRUCTURE.txt
placeholder:
  path: "$RelativeFilePath"
  todo: "Replace with actual configuration"
"@
        }
        ".yaml" {
            return @"
# Placeholder generated from COMPLETE_STRUCTURE.txt
placeholder:
  path: "$RelativeFilePath"
  todo: "Replace with actual configuration"
"@
        }
        ".json" {
            return @"
{
  "placeholder": true,
  "path": "$RelativeFilePath",
  "todo": "Replace with actual JSON content"
}
"@
        }
        ".kts" {
            return @"
/*
 * Placeholder generated from COMPLETE_STRUCTURE.txt
 * Path: $RelativeFilePath
 * TODO: Add Gradle Kotlin DSL content.
 */
"@
        }
        ".md" {
            return @"
# Placeholder

Generated from `COMPLETE_STRUCTURE.txt`.

- Path: `$RelativeFilePath`
- TODO: Replace with actual documentation
"@
        }
        default {
            return @"
# Placeholder generated from COMPLETE_STRUCTURE.txt
# Path: $RelativeFilePath
# TODO: Replace with actual content.
"@
        }
    }
}

function Get-ActualPathsUnderRoots {
    param(
        [Parameter(Mandatory = $true)][string]$RootPath,
        [Parameter(Mandatory = $true)][string[]]$Roots,
        [Parameter(Mandatory = $true)][System.Collections.Generic.HashSet[string]]$IgnoreSegments
    )

    $resolvedRoot = (Resolve-Path -LiteralPath $RootPath).Path
    $directories = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
    $files = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)

    foreach ($root in $Roots) {
        $absRoot = Join-RootAndRelative -RootPath $resolvedRoot -RelativePath $root
        if (-not (Test-Path -LiteralPath $absRoot -PathType Container)) {
            continue
        }

        $null = $directories.Add($root)
        Get-ChildItem -LiteralPath $absRoot -Recurse -Force | ForEach-Object {
            $relative = (Get-RelativePathPortable -BasePath $resolvedRoot -TargetPath $_.FullName).Replace('\', '/')
            if (-not (Test-ShouldIgnoreRelativePath -RelativePath $relative -IgnoreSegments $IgnoreSegments)) {
                if ($_.PSIsContainer) {
                    $null = $directories.Add($relative)
                }
                else {
                    $null = $files.Add($relative)
                }
            }
        }
    }

    return [pscustomobject]@{
        Directories = @($directories | Sort-Object)
        Files       = @($files | Sort-Object)
    }
}

function Compare-PathSets {
    param(
        [Parameter(Mandatory = $true)][string[]]$ExpectedDirectories,
        [Parameter(Mandatory = $true)][string[]]$ExpectedFiles,
        [Parameter(Mandatory = $true)][string[]]$ActualDirectories,
        [Parameter(Mandatory = $true)][string[]]$ActualFiles
    )

    $expectedDirSet = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
    $actualDirSet = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
    $expectedFileSet = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
    $actualFileSet = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)

    foreach ($item in $ExpectedDirectories) { $null = $expectedDirSet.Add($item) }
    foreach ($item in $ActualDirectories) { $null = $actualDirSet.Add($item) }
    foreach ($item in $ExpectedFiles) { $null = $expectedFileSet.Add($item) }
    foreach ($item in $ActualFiles) { $null = $actualFileSet.Add($item) }

    $missingDirectories = @()
    $extraDirectories = @()
    $missingFiles = @()
    $extraFiles = @()

    foreach ($item in $expectedDirSet) {
        if (-not $actualDirSet.Contains($item)) {
            $missingDirectories += $item
        }
    }
    foreach ($item in $actualDirSet) {
        if (-not $expectedDirSet.Contains($item)) {
            $extraDirectories += $item
        }
    }
    foreach ($item in $expectedFileSet) {
        if (-not $actualFileSet.Contains($item)) {
            $missingFiles += $item
        }
    }
    foreach ($item in $actualFileSet) {
        if (-not $expectedFileSet.Contains($item)) {
            $extraFiles += $item
        }
    }

    return [pscustomobject]@{
        MissingDirectories = @($missingDirectories | Sort-Object)
        ExtraDirectories   = @($extraDirectories | Sort-Object)
        MissingFiles       = @($missingFiles | Sort-Object)
        ExtraFiles         = @($extraFiles | Sort-Object)
        IsMatch            = ($missingDirectories.Count -eq 0 -and $extraDirectories.Count -eq 0 -and $missingFiles.Count -eq 0 -and $extraFiles.Count -eq 0)
    }
}

$normalizedRoots = @($TargetRoots | ForEach-Object { Normalize-PathToken -PathToken $_ } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -Unique)
if ($normalizedRoots.Count -eq 0) {
    throw "TargetRoots cannot be empty."
}

$normalizedIgnoreSegments = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
foreach ($segment in $IgnorePathSegments) {
    if (-not [string]::IsNullOrWhiteSpace($segment)) {
        $null = $normalizedIgnoreSegments.Add($segment.Trim().ToLowerInvariant())
    }
}

$structurePath = Resolve-Path -LiteralPath $StructureFile
$outputPath = Resolve-Path -LiteralPath $OutputRoot

$entries = Parse-CompleteStructure -FilePath $structurePath.Path
$targetRootMissing = @()
foreach ($root in $normalizedRoots) {
    $foundRoot = $entries | Where-Object { $_.Path -eq $root -and $_.Kind -eq "Directory" } | Select-Object -First 1
    if ($null -eq $foundRoot) {
        $targetRootMissing += $root
    }
}
if ($targetRootMissing.Count -gt 0) {
    throw "Target root(s) not found in ${StructureFile}: $($targetRootMissing -join ', ')"
}

$expected = Build-ExpectedPaths -Entries $entries -Roots $normalizedRoots

Write-Host "Scaffold roots: $($normalizedRoots -join ', ')"
Write-Host "Expected directories: $($expected.Directories.Count)"
Write-Host "Expected files: $($expected.Files.Count)"
Write-Host "Mode: $(if ($DryRun) { 'DRY-RUN' } else { 'APPLY' })"
Write-Host "Overwrite existing files: $(if ($Force) { 'Yes' } else { 'No' })"

if ($Detailed) {
    Write-Host ""
    Write-Host "Directories to ensure:"
    foreach ($dir in $expected.Directories) {
        Write-Host "  [D] $dir"
    }
    Write-Host ""
    Write-Host "Files to scaffold with placeholders:"
    foreach ($file in $expected.Files) {
        Write-Host "  [F] $file"
    }
}

function Invoke-ScaffoldIntoRoot {
    param(
        [Parameter(Mandatory = $true)][string]$RootPath,
        [Parameter(Mandatory = $true)][string[]]$Directories,
        [Parameter(Mandatory = $true)][string[]]$Files,
        [switch]$WriteFiles,
        [switch]$AllowOverwrite
    )

    $stats = [ordered]@{
        CreatedDirectories = 0
        ExistingDirectories = 0
        CreatedFiles = 0
        OverwrittenFiles = 0
        ExistingFilesSkipped = 0
    }

    foreach ($dir in $Directories) {
        $absDir = Join-RootAndRelative -RootPath $RootPath -RelativePath $dir
        if (Test-Path -LiteralPath $absDir -PathType Container) {
            $stats.ExistingDirectories++
            continue
        }

        if (Test-Path -LiteralPath $absDir) {
            throw "Path exists and is not a directory: $absDir"
        }

        if ($WriteFiles) {
            New-Item -ItemType Directory -Path $absDir -Force | Out-Null
        }
        $stats.CreatedDirectories++
    }

    foreach ($file in $Files) {
        $absFile = Join-RootAndRelative -RootPath $RootPath -RelativePath $file
        $parent = Split-Path -Path $absFile -Parent
        if (-not (Test-Path -LiteralPath $parent -PathType Container)) {
            if ($WriteFiles) {
                New-Item -ItemType Directory -Path $parent -Force | Out-Null
            }
        }

        if (Test-Path -LiteralPath $absFile -PathType Container) {
            throw "Path exists and is a directory, expected file: $absFile"
        }

        $fileExists = Test-Path -LiteralPath $absFile -PathType Leaf
        if ($fileExists -and -not $AllowOverwrite) {
            $stats.ExistingFilesSkipped++
            continue
        }

        if ($WriteFiles) {
            $content = Get-PlaceholderContent -RelativeFilePath $file
            Set-Content -LiteralPath $absFile -Value $content -Encoding utf8
        }

        if ($fileExists) {
            $stats.OverwrittenFiles++
        }
        else {
            $stats.CreatedFiles++
        }
    }

    return [pscustomobject]$stats
}

if ($DryRun) {
    $dryStats = Invoke-ScaffoldIntoRoot -RootPath $outputPath.Path -Directories $expected.Directories -Files $expected.Files -WriteFiles:$false -AllowOverwrite:$Force
    Write-Host ""
    Write-Host "Dry-run plan summary:"
    Write-Host "  Directories to create: $($dryStats.CreatedDirectories)"
    Write-Host "  Directories already present: $($dryStats.ExistingDirectories)"
    Write-Host "  Files to create: $($dryStats.CreatedFiles)"
    Write-Host "  Files to overwrite: $($dryStats.OverwrittenFiles)"
    Write-Host "  Existing files skipped: $($dryStats.ExistingFilesSkipped)"
}
else {
    $applyStats = Invoke-ScaffoldIntoRoot -RootPath $outputPath.Path -Directories $expected.Directories -Files $expected.Files -WriteFiles -AllowOverwrite:$Force
    Write-Host ""
    Write-Host "Apply summary:"
    Write-Host "  Directories created: $($applyStats.CreatedDirectories)"
    Write-Host "  Directories already present: $($applyStats.ExistingDirectories)"
    Write-Host "  Files created: $($applyStats.CreatedFiles)"
    Write-Host "  Files overwritten: $($applyStats.OverwrittenFiles)"
    Write-Host "  Existing files skipped: $($applyStats.ExistingFilesSkipped)"
}

if ($VerifyConformance) {
    Write-Host ""
    Write-Host "Running conformance verification..."

    if ($DryRun) {
        $tempRoot = Join-Path -Path ([System.IO.Path]::GetTempPath()) -ChildPath ("chiroerp-scaffold-" + [Guid]::NewGuid().ToString("N"))
        New-Item -ItemType Directory -Path $tempRoot -Force | Out-Null

        try {
            $null = Invoke-ScaffoldIntoRoot -RootPath $tempRoot -Directories $expected.Directories -Files $expected.Files -WriteFiles -AllowOverwrite:$true
            $actual = Get-ActualPathsUnderRoots -RootPath $tempRoot -Roots $normalizedRoots -IgnoreSegments $normalizedIgnoreSegments
            $comparison = Compare-PathSets `
                -ExpectedDirectories $expected.Directories `
                -ExpectedFiles $expected.Files `
                -ActualDirectories $actual.Directories `
                -ActualFiles $actual.Files

            if (-not $comparison.IsMatch) {
                Write-Host "Conformance check failed in dry-run sandbox."
                Write-Host "  Missing directories: $($comparison.MissingDirectories.Count)"
                Write-Host "  Extra directories: $($comparison.ExtraDirectories.Count)"
                Write-Host "  Missing files: $($comparison.MissingFiles.Count)"
                Write-Host "  Extra files: $($comparison.ExtraFiles.Count)"

                if ($Detailed) {
                    foreach ($item in $comparison.MissingDirectories) { Write-Host "  MISSING DIR  $item" }
                    foreach ($item in $comparison.ExtraDirectories) { Write-Host "  EXTRA DIR    $item" }
                    foreach ($item in $comparison.MissingFiles) { Write-Host "  MISSING FILE $item" }
                    foreach ($item in $comparison.ExtraFiles) { Write-Host "  EXTRA FILE   $item" }
                }

                throw "Dry-run conformance verification failed."
            }

            Write-Host "Conformance check passed (exact match in dry-run sandbox)."
            if ($KeepTemp) {
                Write-Host "Dry-run sandbox retained at: $tempRoot"
            }
        }
        finally {
            if (-not $KeepTemp -and (Test-Path -LiteralPath $tempRoot -PathType Container)) {
                Remove-Item -LiteralPath $tempRoot -Recurse -Force
            }
        }
    }
    else {
        $actual = Get-ActualPathsUnderRoots -RootPath $outputPath.Path -Roots $normalizedRoots -IgnoreSegments $normalizedIgnoreSegments
        $comparison = Compare-PathSets `
            -ExpectedDirectories $expected.Directories `
            -ExpectedFiles $expected.Files `
            -ActualDirectories $actual.Directories `
            -ActualFiles $actual.Files

        if (-not $comparison.IsMatch) {
            Write-Host "Conformance check failed in target workspace."
            Write-Host "  Missing directories: $($comparison.MissingDirectories.Count)"
            Write-Host "  Extra directories: $($comparison.ExtraDirectories.Count)"
            Write-Host "  Missing files: $($comparison.MissingFiles.Count)"
            Write-Host "  Extra files: $($comparison.ExtraFiles.Count)"
            if ($Detailed) {
                foreach ($item in $comparison.MissingDirectories) { Write-Host "  MISSING DIR  $item" }
                foreach ($item in $comparison.ExtraDirectories) { Write-Host "  EXTRA DIR    $item" }
                foreach ($item in $comparison.MissingFiles) { Write-Host "  MISSING FILE $item" }
                foreach ($item in $comparison.ExtraFiles) { Write-Host "  EXTRA FILE   $item" }
            }
            throw "Workspace conformance verification failed."
        }

        Write-Host "Conformance check passed (target workspace matches expected paths)."
    }
}
