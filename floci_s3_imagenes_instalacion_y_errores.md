# Floci 2.2.0 en Windows: instalación y almacenamiento de imágenes con S3

## 1. Objetivo

Registro de la instalación funcional de Floci 2.2.0 en Windows con Docker Desktop, apertura de la consola web, configuración de AWS CLI y pruebas de creación de un bucket y carga/descarga de una imagen. Incluye los errores encontrados y sus soluciones.

> Entorno local de desarrollo y pruebas. No se configuró una cuenta real de AWS.

## 2. Entorno

- Windows y PowerShell.
- Docker Desktop / Docker Engine 29.8.0.
- Docker Compose 5.5.1.
- Floci 2.2.0 Community.
- Directorio: `C:\dev\floci`.
- Endpoint de servicios: `http://localhost:4566`.
- Consola web: `http://localhost:4500/console/aws`.
- Región: `us-east-1`.
- Cuenta local del emulador: `000000000000`.
- Bucket: `imagenes-app`.
- Objeto: `imagenes/imagen-prueba.jpeg`.
- Tamaño original y descargado: `79068` bytes.

## 3. Arquitectura

```text
Windows / PowerShell
   |
   | AWS CLI + credenciales locales de prueba
   v
Floci endpoint: http://localhost:4566
   |
   +-- Bucket: imagenes-app
          |
          +-- Objeto: imagenes/imagen-prueba.jpeg

Consola web: http://localhost:4500/console/aws
Datos persistentes configurados en: C:\dev\floci\data
```

El bucket es un contenedor lógico. `imagenes/` forma parte de la clave del objeto y no tiene que corresponder a una carpeta física del mismo nombre en Windows.

## 4. Verificar Docker

En PowerShell:

```powershell
docker --version
docker compose version
docker info
```

`docker info` debe mostrar información del servidor. Si falla la conexión con el motor, abre Docker Desktop, espera a que termine de iniciar y vuelve a intentarlo.

## 5. Crear el directorio de trabajo

```powershell
New-Item -ItemType Directory -Force -Path "C:\dev\floci"
Set-Location "C:\dev\floci"
Get-Location
```

Ejecuta Docker Compose desde esta carpeta para usar el archivo correcto.

## 6. Crear `compose.yaml`

Ruta: `C:\dev\floci\compose.yaml`.

```yaml
services:
  floci:
    image: floci/floci:latest
    container_name: floci
    ports:
      - "4566:4566"
    volumes:
      - ./data:/app/data
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      FLOCI_STORAGE_MODE: persistent
      FLOCI_STORAGE_PERSISTENT_PATH: /app/data
```

Para crearlo desde PowerShell:

```powershell
Set-Location "C:\dev\floci"

@'
services:
  floci:
    image: floci/floci:latest
    container_name: floci
    ports:
      - "4566:4566"
    volumes:
      - ./data:/app/data
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      FLOCI_STORAGE_MODE: persistent
      FLOCI_STORAGE_PERSISTENT_PATH: /app/data
'@ | Set-Content -Path ".\compose.yaml" -Encoding utf8
```

Validar la configuración:

```powershell
docker compose config
```

### Qué significa la configuración

- `image`: imagen de Floci.
- `container_name`: nombre del contenedor.
- `4566:4566`: publica el endpoint local.
- `./data:/app/data`: enlaza `C:\dev\floci\data` con el almacenamiento interno.
- `/var/run/docker.sock`: permite a Floci comunicarse con Docker para funciones que lo necesiten.
- `FLOCI_STORAGE_MODE: persistent`: activa almacenamiento persistente.
- `FLOCI_STORAGE_PERSISTENT_PATH`: ruta dentro del contenedor.

## 7. Descargar e iniciar Floci

```powershell
Set-Location "C:\dev\floci"
docker pull floci/floci:latest
docker compose up -d
docker compose ps
```

El contenedor `floci` apareció como `Up` y `healthy`.

Consultar registros:

```powershell
docker compose logs --tail 100 floci
```

Se observó Floci 2.2.0, `AWS Local Emulator Ready`, región `us-east-1`, cuenta `000000000000` y ruta persistente `/app/data`.

## 8. Verificar el endpoint

```powershell
Test-NetConnection localhost -Port 4566
```

Resultado esperado:

```text
TcpTestSucceeded : True
```

Abrir la página principal del runtime:

```powershell
Start-Process "http://localhost:4566"
```

La página mostró estado `ready`, versión `2.2.0 community` y la lista de servicios anunciados.

## 9. Abrir la consola web

```powershell
Start-Process "http://localhost:4566/_floci/ui"
```

La consola se abrió finalmente en:

```text
http://localhost:4500/console/aws
```

En la captura se observó el estado `Connected`, runtime alcanzable y el menú lateral de servicios AWS.

Para explorar Storage:
1. Busca la sección **STORAGE** en el menú lateral.
2. Selecciona **Storage**, no **Table Storage**.
3. Comprueba si la versión de la interfaz ofrece opciones de S3, buckets u objetos.

La consola quedó abierta y conectada, pero la navegación exacta hasta una vista de buckets aún no se ha confirmado. Si la interfaz no ofrece un explorador S3, usa AWS CLI.

## 10. Instalar AWS CLI 2

Se ejecutó el instalador oficial:

```powershell
irm https://awscli.amazonaws.com/v2/install.ps1 | iex
```

El instalador informó que AWS CLI 2.37.12 quedó instalado en:

```text
C:\Users\usuario\AppData\Local\Programs\Amazon\AWSCLIV2
```

### Error 1: `aws` no se reconoce

Al ejecutar `aws --version`, PowerShell indicó que `aws` no se reconocía.

**Causa:** el ejecutable existía, pero la carpeta no estaba en el `PATH` de la sesión.

Se comprobó el ejecutable:

```powershell
$awsPath = "$env:LOCALAPPDATA\Programs\Amazon\AWSCLIV2"
Test-Path "$awsPath\aws.exe"
```

Resultado: `True`.

Se ejecutó mediante la ruta completa:

```powershell
& "$env:LOCALAPPDATA\Programs\Amazon\AWSCLIV2\aws.exe" --version
```

Resultado confirmado:

```text
aws-cli/2.37.12 Python/3.14.6 Windows/10 script-exe/AMD64
```

Se agregó la carpeta al `PATH` de usuario y a la sesión actual:

```powershell
$awsPath = "$env:LOCALAPPDATA\Programs\Amazon\AWSCLIV2"
$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
$paths = @($userPath -split ";" | Where-Object { $_ })

if ($paths -notcontains $awsPath) {
    $newPath = (@($paths) + $awsPath) -join ";"
    [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
}

if (($env:Path -split ";") -notcontains $awsPath) {
    $env:Path += ";$awsPath"
}

aws --version
```

La última ejecución devolvió `aws-cli/2.37.12`.

## 11. Configurar credenciales locales

En la misma ventana de PowerShell:

```powershell
$env:AWS_ACCESS_KEY_ID = "test"
$env:AWS_SECRET_ACCESS_KEY = "test"
$env:AWS_DEFAULT_REGION = "us-east-1"
```

Comprobar sin exponer credenciales reales:

```powershell
Write-Output "Access key: $env:AWS_ACCESS_KEY_ID"
Write-Output "Region: $env:AWS_DEFAULT_REGION"
```

Son variables temporales: se pierden al cerrar PowerShell. `test` son credenciales ficticias para desarrollo local y no deben usarse contra AWS real.

## 12. Error 2: `NoCredentials`

Se ejecutó:

```powershell
aws s3 ls --endpoint-url http://localhost:4566
```

Y se obtuvo:

```text
An error occurred (NoCredentials): Unable to locate credentials.
```

**Causa:** AWS CLI no tenía credenciales disponibles en esa sesión.

**Solución:** definir `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` y `AWS_DEFAULT_REGION` como se indica en la sección anterior y repetir:

```powershell
aws s3 ls --endpoint-url http://localhost:4566
```

Después de configurar las variables, el comando terminó sin error. La lista estaba vacía porque todavía no había buckets; eso era normal.

## 13. Crear el bucket

```powershell
aws s3 mb s3://imagenes-app --endpoint-url http://localhost:4566
```

Salida confirmada:

```text
make_bucket: imagenes-app
```

Verificar:

```powershell
aws s3 ls --endpoint-url http://localhost:4566
```

Salida observada:

```text
2026-10-12 00:19:24 imagenes-app
```

## 14. Preparar la imagen local

Archivo de prueba:

```text
C:\dev\floci\imagen-prueba.jpeg
```

Comprobar:

```powershell
Get-Item .\imagen-prueba.jpeg
```

Tamaño observado: `79068` bytes.

## 15. Subir la imagen

```powershell
aws s3 cp .\imagen-prueba.jpeg s3://imagenes-app/imagenes/imagen-prueba.jpeg --content-type image/jpeg --endpoint-url http://localhost:4566
```

Salida confirmada:

```text
upload: .\imagen-prueba.jpeg to s3://imagenes-app/imagenes/imagen-prueba.jpeg
```

El parámetro `--content-type image/jpeg` asigna el tipo MIME del objeto.

## 16. Verificar el objeto

```powershell
aws s3 ls s3://imagenes-app/imagenes/ --endpoint-url http://localhost:4566
```

Salida observada:

```text
2026-10-12 00:22:47      79068 imagen-prueba.jpeg
```

Para consultar metadatos:

```powershell
aws s3api head-object `
  --bucket imagenes-app `
  --key imagenes/imagen-prueba.jpeg `
  --endpoint-url http://localhost:4566
```

## 17. Descargar la imagen

```powershell
aws s3 cp s3://imagenes-app/imagenes/imagen-prueba.jpeg .\imagen-descargada.jpeg --endpoint-url http://localhost:4566
```

Salida confirmada:

```text
download: s3://imagenes-app/imagenes/imagen-prueba.jpeg to .\imagen-descargada.jpeg
```

Comparar los tamaños:

```powershell
Get-Item .\imagen-prueba.jpeg, .\imagen-descargada.jpeg |
    Select-Object Name, Length
```

Resultado observado:

```text
Name                   Length
----                   ------
imagen-prueba.jpeg      79068
imagen-descargada.jpeg  79068
```

Abrir la copia descargada:

```powershell
Start-Process .\imagen-descargada.jpeg
```

Esto verificó el ciclo básico de subida y descarga.

## 18. Comandos útiles

Listar buckets:

```powershell
aws s3 ls --endpoint-url http://localhost:4566
```

Listar todos los objetos:

```powershell
aws s3 ls s3://imagenes-app/ --recursive --endpoint-url http://localhost:4566
```

Consultar metadatos:

```powershell
aws s3api head-object --bucket imagenes-app --key imagenes/imagen-prueba.jpeg --endpoint-url http://localhost:4566
```

Consultar estado de Floci:

```powershell
Set-Location "C:\dev\floci"
docker compose ps
```

Ver registros:

```powershell
docker compose logs -f floci
```

## 19. Detener y volver a iniciar

Detener:

```powershell
Set-Location "C:\dev\floci"
docker compose down
```

Iniciar:

```powershell
Set-Location "C:\dev\floci"
docker compose up -d
docker compose ps
```

La configuración enlaza los datos con `C:\dev\floci\data`. Para verificar persistencia después del reinicio:

```powershell
aws s3 ls s3://imagenes-app/ --recursive --endpoint-url http://localhost:4566
```

La persistencia tras un reinicio completo aún debe comprobarse explícitamente. No borres `C:\dev\floci\data` si deseas conservar los datos.

## 20. Estado final

| Elemento | Estado |
|---|---|
| Docker Desktop / Engine | Operativo |
| Imagen de Floci | Descargada |
| Floci 2.2.0 | Contenedor `healthy` |
| Endpoint `localhost:4566` | Accesible |
| Consola `localhost:4500/console/aws` | Abierta y conectada |
| AWS CLI 2.37.12 | Instalado y accesible |
| Credenciales de prueba | Configuradas en la sesión utilizada |
| Bucket `imagenes-app` | Creado |
| Subida de imagen | Correcta |
| Descarga de imagen | Correcta |
| Tamaño original y descargado | Ambos `79068` bytes |
| Persistencia tras reinicio | Pendiente de prueba explícita |
| Exploración de objetos S3 desde la UI | Pendiente de confirmar |

## 21. Resumen de errores y soluciones

| Error | Causa | Solución |
|---|---|---|
| `aws` no se reconoce | AWS CLI no estaba en el `PATH` de la sesión | Verificar `aws.exe`, añadir la carpeta al `PATH` y comprobar con `aws --version` |
| `NoCredentials` | AWS CLI no tenía credenciales en la sesión | Definir las tres variables de entorno de prueba |
| Lista S3 vacía | El bucket aún no se había creado | Crear `imagenes-app` con `aws s3 mb` |
| No encontrar una carpeta física con el nombre del objeto | S3 guarda objetos como claves y Floci puede usar almacenamiento interno | Consultar el bucket con AWS CLI; no asumir que cada objeto existe como archivo visible en `data` |
| Duda entre endpoint y consola | Runtime y UI usan puertos distintos | API: `localhost:4566`; consola observada: `localhost:4500/console/aws` |

## 22. Próximos pasos

Antes de implementar el servicio Java:

1. Comprobar explícitamente la persistencia tras `docker compose down` y `docker compose up -d`.
2. Explorar Storage en la consola y confirmar si permite navegar buckets y objetos.
3. Integrar el bucket con Java 21 y Spring Boot mediante AWS SDK for Java 2.x.
4. Mantener credenciales y endpoint local en la configuración de desarrollo. Para AWS real, usar credenciales y políticas adecuadas; nunca usar las credenciales ficticias `test`.

## 23. Referencias

- Floci: https://floci.com/
- Documentación de Floci: https://docs.floci.com/
- Instalación oficial de AWS CLI: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html
