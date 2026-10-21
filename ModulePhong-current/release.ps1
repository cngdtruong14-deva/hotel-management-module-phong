param([switch]$VerifyOnly)
$ErrorActionPreference='Stop'
$taskRoot=[IO.Path]::GetFullPath($PSScriptRoot)
$taskManifest=Join-Path $taskRoot 'FILE_HASHES.json'
function Get-ReleaseFiles {
    Get-ChildItem -LiteralPath $taskRoot -File -Recurse | Where-Object {
        $relative=$_.FullName.Substring($taskRoot.Length+1).Replace('\','/')
        $relative -notmatch '^build/|^nbproject/private/|^verification/.*\.(properties|sql)$|^config/db\.properties$|^FILE_HASHES\.json$'
    } | Sort-Object FullName
}
if($VerifyOnly) {
    $taskEntries=Get-Content -LiteralPath $taskManifest -Raw | ConvertFrom-Json
    $taskActual=@(Get-ReleaseFiles)
    if($taskActual.Count -ne @($taskEntries).Count) { throw 'File set changed since release' }
    foreach($entry in $taskEntries) {
        $path=Join-Path $taskRoot $entry.path
        if(-not (Test-Path -LiteralPath $path) -or (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ne $entry.sha256) { throw "Release mismatch: $($entry.path)" }
    }
    Write-Output 'PASS: current files match release manifest'; return
}
$taskEntries=@(Get-ReleaseFiles | ForEach-Object { [ordered]@{ path=$_.FullName.Substring($taskRoot.Length+1).Replace('\','/');sha256=(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash } })
$taskEntries | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $taskManifest -Encoding utf8
$taskParent=Split-Path $taskRoot -Parent
$taskDeliver=Join-Path $taskParent 'deliverables'
New-Item -ItemType Directory -Force $taskDeliver | Out-Null
$taskZip=Join-Path $taskDeliver 'ModulePhong-current.zip'
if(Test-Path -LiteralPath $taskZip) {
    $taskArchive=Join-Path $taskParent ('archive/releases/'+(Get-Date -Format 'yyyyMMdd-HHmmssfff'))
    New-Item -ItemType Directory -Force $taskArchive | Out-Null
    # Both source and destination are explicit children of the workspace.
    Move-Item -LiteralPath $taskZip -Destination (Join-Path $taskArchive 'ModulePhong-current.zip')
}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$taskArchiveZip=[IO.Compression.ZipFile]::Open($taskZip,[IO.Compression.ZipArchiveMode]::Create)
try {
    foreach($file in @(Get-ReleaseFiles)+(Get-Item -LiteralPath $taskManifest)) {
        $entry='ModulePhong-current/'+$file.FullName.Substring($taskRoot.Length+1).Replace('\','/')
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($taskArchiveZip,$file.FullName,$entry) | Out-Null
    }
} finally { $taskArchiveZip.Dispose() }
(Get-FileHash -LiteralPath $taskZip -Algorithm SHA256).Hash | Set-Content (Join-Path $taskDeliver 'ModulePhong-current.sha256')
Write-Output $taskZip
