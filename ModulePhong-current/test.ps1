$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try {
    & ./build.ps1
    New-Item -ItemType Directory -Force build/test-classes,verification | Out-Null
    javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes test/SyncTest.java test/ModulePhongTest.java test/PortalViewTest.java test/PortalTest.java test/SharedContextTest.java
    if($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
    java '-Djava.awt.headless=true' -cp 'build/classes;build/test-classes' SharedContextTest
    if($LASTEXITCODE -ne 0) { throw 'Shared context test failed' }
    java '-Djava.awt.headless=true' -cp 'build/classes;build/test-classes' SyncTest
    if($LASTEXITCODE -ne 0) { throw 'Integration test failed' }
    java '-Djava.awt.headless=true' -cp 'build/classes;build/test-classes' PortalTest
    if($LASTEXITCODE -ne 0) { throw 'Room portal contract test failed' }
    java '-Djava.awt.headless=true' -cp 'build/classes;build/test-classes' ModulePhongTest verification
    if($LASTEXITCODE -ne 0) { throw 'Room test failed' }
    java '-Djava.awt.headless=true' -cp 'build/classes;build/test-classes' PortalViewTest
    if($LASTEXITCODE -ne 0) { throw 'Integrated view test failed' }
} finally { Pop-Location }
