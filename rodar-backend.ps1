# Roda o back-end SEM precisar do Maven (so precisa do JDK 17 ou superior).
# Uso (dentro da pasta 01_java_backend_database):
#   powershell -ExecutionPolicy Bypass -File .\rodar-backend.ps1
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# Acha a pasta do back-end (a que tem src\main\java), mesmo que este script
# tenha sido copiado para outra pasta acima dela.
if (-not (Test-Path "src\main\java")) {
    $achada = Get-ChildItem -Path $PSScriptRoot -Recurse -Directory -Filter "01_java_backend_database" -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName "src\main\java") } | Select-Object -First 1
    if ($achada) {
        Write-Host "Usando a pasta: $($achada.FullName)"
        Set-Location $achada.FullName
    } else {
        Write-Host "Nao achei a pasta do back-end (com src\main\java). Copie este script para dentro de 01_java_backend_database."
        exit 1
    }
}

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    Write-Host "JDK nao encontrado. Instale o JDK 17 ou superior (winget install EclipseAdoptium.Temurin.17.JDK), feche e abra o terminal e rode de novo."
    exit 1
}

# Bibliotecas (o Maven baixaria sozinho; aqui baixamos uma unica vez).
# json 20250107 funciona em Java 8+ (versoes mais novas exigem Java 21).
New-Item -ItemType Directory -Force lib | Out-Null
$base = "https://repo1.maven.org/maven2"
$jars = @(
    @{ arq = "lib\ojdbc11-23.6.0.24.10.jar"; url = "$base/com/oracle/database/jdbc/ojdbc11/23.6.0.24.10/ojdbc11-23.6.0.24.10.jar" },
    @{ arq = "lib\json-20250107.jar";        url = "$base/org/json/json/20250107/json-20250107.jar" }
)
foreach ($j in $jars) {
    if (-not (Test-Path $j.arq)) {
        Write-Host "Baixando $($j.arq)..."
        Invoke-WebRequest -Uri $j.url -OutFile $j.arq
    }
}
# Classpath explicito: ignora qualquer outro .jar antigo que esteja em lib\
$cp = ($jars | ForEach-Object { $_.arq }) -join ";"

New-Item -ItemType Directory -Force out | Out-Null
$fontes = Get-ChildItem -Path "src\main\java" -Recurse -Filter *.java | ForEach-Object { $_.FullName }
Write-Host "Compilando..."
javac -encoding UTF-8 -cp $cp -d out $fontes
if ($LASTEXITCODE -ne 0) { Write-Host "Erro de compilacao."; exit 1 }

Write-Host "Subindo o servidor em http://localhost:8080 (Ctrl+C para parar)"
if (-not $env:GROQ_API_KEY) {
    Write-Host ""
    Write-Host "AVISO: GROQ_API_KEY nao definida. O servidor sobe, mas a IA fica indisponivel:" -ForegroundColor Yellow
    Write-Host "       toda evidencia enviada vira PENDENTE (sem pontos) ate voce configurar." -ForegroundColor Yellow
    Write-Host '       Defina antes de rodar:  $env:GROQ_API_KEY = "sua_chave"' -ForegroundColor Yellow
    Write-Host ""
}
java -cp "out;$cp" api.ServidorApi
