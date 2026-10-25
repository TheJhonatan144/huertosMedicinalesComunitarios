# Ejecuta la aplicación en el equipo (fuera de Docker) usando la base de datos de compose.yaml.
# Requisitos: JDK 21 en JAVA_HOME, Docker en ejecución y un archivo .env (copia de .env.example).
# Uso: .\scripts\iniciar-local.ps1
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)

if (-not (Test-Path .env)) { throw 'Falta el archivo .env. Copia .env.example como .env y ajusta los valores.' }
$valores = @{}
Get-Content .env | Where-Object { $_ -match '^\s*[^#\s][^=]*=' } | ForEach-Object {
    $k, $v = $_ -split '=', 2
    $valores[$k.Trim()] = $v.Trim()
}

docker compose up -d db
if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar la base de datos con Docker Compose.' }

$env:DB_URL = "jdbc:postgresql://localhost:$($valores['DB_PUERTO'] ?? '5432')/$($valores['DB_NOMBRE'] ?? 'huertos')"
$env:DB_USUARIO = $valores['DB_USUARIO'] ?? 'huertos'
$env:DB_CLAVE = $valores['DB_CLAVE']
$env:DEMO_CLAVE = $valores['DEMO_CLAVE'] ?? 'Demo-2026!'
$env:SPRING_PROFILES_ACTIVE = $valores['PERFIL'] ?? 'demo'
$env:SERVER_PORT = $valores['APP_PUERTO'] ?? '8080'

.\mvnw.cmd spring-boot:run
