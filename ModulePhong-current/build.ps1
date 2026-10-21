$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath $PSScriptRoot
try {
    $taskBuildPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'build/classes'))
    $taskRootPath = [IO.Path]::GetFullPath($PSScriptRoot) + [IO.Path]::DirectorySeparatorChar
    if (-not $taskBuildPath.StartsWith($taskRootPath)) { throw 'Unsafe build path' }
    if (Test-Path -LiteralPath $taskBuildPath) { Remove-Item -LiteralPath $taskBuildPath -Recurse -Force }
    New-Item -ItemType Directory -Force build/classes | Out-Null
    $taskSources = Get-ChildItem src -Filter *.java -Recurse | ForEach-Object FullName
    javac -encoding UTF-8 --release 17 -d build/classes $taskSources
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
    jar --create --file ModulePhong.jar --main-class demo.HotelSharedApp -C build/classes .
    if ($LASTEXITCODE -ne 0) { throw 'JAR creation failed' }
} finally { Pop-Location }
