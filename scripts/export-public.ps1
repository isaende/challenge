$ErrorActionPreference = 'Stop'
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$archivePath = Join-Path $projectRoot 'public-source.zip'

Push-Location -LiteralPath $projectRoot
try {
    # Git supplies the allowlist; ignored local files and .git are never included.
    $gitFiles = & git -c core.quotepath=false ls-files --cached --others --exclude-standard
    if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel listar os arquivos do projeto.' }
    $files = @($gitFiles | Sort-Object -Unique | Where-Object {
        $name = $_
        $name -notmatch '(^|/)(\.git|\.gradle|\.kotlin|\.idea|build|uploads)(/|$)' -and
        $name -notmatch '(^|/)(\.env(?:\..*)?|database\.properties|local\.properties)$' -and
        $name -notmatch '\.(jks|keystore|pem|key|p12|pfx|log|hprof|apk|aab|zip)$' -and
        (Test-Path -LiteralPath (Join-Path $projectRoot $name) -PathType Leaf)
    })
    # Include the public environment template explicitly.
    $files += '.env.example'

    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $stream = [System.IO.File]::Open($archivePath, [System.IO.FileMode]::Create)
    try {
        $archive = [System.IO.Compression.ZipArchive]::new($stream, [System.IO.Compression.ZipArchiveMode]::Create)
        try {
            foreach ($name in ($files | Sort-Object -Unique)) {
                $sourcePath = [System.IO.Path]::GetFullPath((Join-Path $projectRoot $name))
                if (-not $sourcePath.StartsWith($projectRoot + [System.IO.Path]::DirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase)) {
                    throw 'Arquivo fora do diretorio do projeto.'
                }
                [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $sourcePath, $name.Replace('\', '/')) | Out-Null
            }
        } finally { $archive.Dispose() }
    } finally { $stream.Dispose() }
    Write-Output "Codigo para um novo repositorio: $archivePath"
} finally { Pop-Location }
