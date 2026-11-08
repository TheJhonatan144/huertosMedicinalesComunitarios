#requires -Version 7.0
<#
.SYNOPSIS
Valida las fotos de un bloque y, con -Aplicar, las adjunta al catálogo local.
.DESCRIPTION
Usa el formulario autenticado existente. Conserva las fichas y las fotografías
ya registradas. No selecciona especies para comunidades ni cambia su siembra.
Sin -Aplicar solo comprueba archivos, hashes y atribuciones.
#>
[CmdletBinding()]
param(
    [switch]$Aplicar,
    [uri]$BaseUrl = 'http://localhost:8080',
    [PSCredential]$Credencial,
    [ValidateSet('01','02','03')][string]$Bloque = '01'
)
$ErrorActionPreference = 'Stop'
$carpetaFotos = Join-Path $PSScriptRoot '../docs/catalogo/fotografias'
if ($Bloque -ne '01') { $carpetaFotos = Join-Path $carpetaFotos "bloque-$Bloque" }
$manifest = Get-Content (Join-Path $carpetaFotos 'fotografias.json') -Raw | ConvertFrom-Json
foreach ($foto in $manifest.fotografias) {
    if ($foto.archivo -notmatch '^[a-z-]+\.jpg$') { throw 'Nombre de archivo no válido.' }
    $ruta = Join-Path $carpetaFotos $foto.archivo
    if ((Get-FileHash $ruta -Algorithm SHA256).Hash -ne $foto.sha256) { throw "Hash distinto: $($foto.archivo)" }
    if ((Get-Item $ruta).Length -gt 5MB) { throw "Foto demasiado grande: $($foto.archivo)" }
    if ([string]::IsNullOrWhiteSpace($foto.atribucion) -or $foto.atribucion.Length -gt 1000) { throw 'Revisa los créditos.' }
}
Write-Host "$($manifest.fotografias.Count) fotografías verificadas."
if (-not $Aplicar) { return }
if (-not $BaseUrl.IsLoopback -or $BaseUrl.Scheme -notin @('http','https')) {
    throw 'Este script solo admite la aplicación local (localhost o dirección de bucle local).'
}
if (-not $Credencial) { $Credencial = Get-Credential -Message 'Cuenta de gestor de la aplicación local' }
$raiz = $BaseUrl.GetLeftPart([UriPartial]::Authority)
function Decodificar([string]$texto) { [Net.WebUtility]::HtmlDecode($texto) }
function Atributos([string]$etiqueta) {
    $resultado = @{}
    foreach ($m in [regex]::Matches($etiqueta, '([\w-]+)="([^"]*)"')) {
        $resultado[$m.Groups[1].Value] = Decodificar $m.Groups[2].Value
    }
    return $resultado
}
function Campos([string]$html) {
    $campos = @{}
    foreach ($m in [regex]::Matches($html, '<input\b[^>]*>')) {
        $a = Atributos $m.Value
        if ($a.name -and $a.type -notin @('file','submit','checkbox','radio')) { $campos[$a.name] = [string]$a.value }
    }
    foreach ($m in [regex]::Matches($html, '(?s)<textarea\b([^>]*)>(.*?)</textarea>')) {
        $a = Atributos $m.Groups[1].Value
        if ($a.name) { $campos[$a.name] = Decodificar $m.Groups[2].Value }
    }
    foreach ($m in [regex]::Matches($html, '(?s)<select\b([^>]*)>(.*?)</select>')) {
        $a = Atributos $m.Groups[1].Value
        $seleccion = @()
        foreach ($op in [regex]::Matches($m.Groups[2].Value, '<option\b[^>]*>')) {
            if ($op.Value -match '\bselected(?:\s|=|>)') { $seleccion += (Atributos $op.Value).value }
        }
        if ($a.name) { $campos[$a.name] = $seleccion }
    }
    return $campos
}
$login = Invoke-WebRequest "$raiz/login" -SessionVariable sesion
$csrf = (Campos $login.Content)._csrf
if (-not $csrf) { throw 'No se encontró el token CSRF del acceso.' }
$acceso = Invoke-WebRequest "$raiz/login" -WebSession $sesion -Method Post -Body @{
    username = $Credencial.UserName; password = $Credencial.GetNetworkCredential().Password; _csrf = $csrf
}
if ($acceso.BaseResponse.RequestMessage.RequestUri.AbsolutePath -eq '/login') { throw 'No se pudo iniciar sesión como gestor.' }
$manejador = [Net.Http.HttpClientHandler]::new()
$manejador.CookieContainer = $sesion.Cookies
$cliente = [Net.Http.HttpClient]::new($manejador)
try {
    foreach ($foto in $manifest.fotografias) {
        $busqueda = [Uri]::EscapeDataString($foto.nombre_cientifico)
        $lista = Invoke-WebRequest "$raiz/gestor/plantas/catalogo?q=$busqueda" -WebSession $sesion
        $ids = @([regex]::Matches($lista.Content, 'href="/gestor/plantas/catalogo/(\d+)"') | ForEach-Object { $_.Groups[1].Value } | Select-Object -Unique)
        if ($ids.Count -ne 1) { throw "Se esperaba una única ficha para $($foto.nombre_cientifico). Revisa la carga del bloque $Bloque." }
        $urlFicha = "$raiz/gestor/plantas/catalogo/$($ids[0])"
        $existente = Invoke-WebRequest "$urlFicha/foto" -WebSession $sesion -SkipHttpErrorCheck
        if ($existente.StatusCode -eq 200 -and $existente.Headers.'Content-Type' -match '^image/') {
            Write-Host "Conservada: $($foto.nombre_cientifico)"; continue
        }
        if ($existente.StatusCode -ne 404) { throw 'Respuesta inesperada al consultar la fotografía.' }
        $edicion = Invoke-WebRequest "$urlFicha/editar" -WebSession $sesion
        $campos = Campos $edicion.Content
        if ($campos.nombreCientifico -ne $foto.nombre_cientifico -or -not $campos._csrf) { throw 'La ficha no coincide o falta el token CSRF.' }
        $campos.fotoFuente = $foto.atribucion
        $multipart = [Net.Http.MultipartFormDataContent]::new()
        try {
            foreach ($nombre in $campos.Keys) {
                foreach ($valor in @($campos[$nombre])) { $multipart.Add([Net.Http.StringContent]::new([string]$valor), $nombre) }
            }
            $contenido = [Net.Http.ByteArrayContent]::new([IO.File]::ReadAllBytes((Join-Path $carpetaFotos $foto.archivo)))
            $contenido.Headers.ContentType = [Net.Http.Headers.MediaTypeHeaderValue]::new('image/jpeg')
            $multipart.Add($contenido, 'foto', $foto.archivo)
            $respuesta = $cliente.PostAsync($urlFicha, $multipart).GetAwaiter().GetResult()
            $respuesta.EnsureSuccessStatusCode() | Out-Null
            if ($respuesta.RequestMessage.RequestUri.AbsolutePath -ne "/gestor/plantas/catalogo/$($ids[0])") { throw 'No se confirmó el guardado de la ficha.' }
            $pagina = $respuesta.Content.ReadAsStringAsync().GetAwaiter().GetResult()
            if ($pagina -match 'Revisa los datos:') { throw 'El formulario rechazó la fotografía o sus datos.' }
        } finally { $multipart.Dispose() }
        $bytes = $cliente.GetByteArrayAsync("$urlFicha/foto").GetAwaiter().GetResult()
        $hash = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($bytes))
        if ($hash -ne $foto.sha256) { throw 'La fotografía recuperada no coincide con el archivo.' }
        Write-Host "Adjuntada y comprobada: $($foto.nombre_cientifico)"
    }
} finally { $cliente.Dispose(); $Credencial = $null }
