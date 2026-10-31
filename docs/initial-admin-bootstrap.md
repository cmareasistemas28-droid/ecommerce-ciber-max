# Bootstrap del primer administrador

## Qué hace

El bootstrap crea una sola vez un `Usuario` local con rol `ADMINISTRADOR` y
su `Empleado` asociado. Se ejecuta mediante `ApplicationRunner`, no mediante
un endpoint HTTP.

Está desactivado por defecto:

```properties
ADMIN_BOOTSTRAP_ENABLED=false
```

La aplicación guarda el estado en `admin_bootstrap_control`. Por eso reiniciar
el servidor, borrar al administrador posteriormente o volver a activar la
variable no crea otra cuenta ni restablece contraseñas.

## Base de datos nueva

Flyway ejecuta automáticamente:

- `V1__initial_schema.sql`: tablas actuales de usuarios y empleados.
- `V2__admin_bootstrap_control.sql`: registro singleton de control.

Configura PostgreSQL y las variables obligatorias, por ejemplo en el entorno
del proceso (no en el repositorio):

```text
ADMIN_BOOTSTRAP_ENABLED=true
ADMIN_INITIAL_EMAIL=admin@dominio.example
ADMIN_INITIAL_PASSWORD=<frase-de-al-menos-12-caracteres-con-un-numero>
ADMIN_INITIAL_NOMBRES=Nombre
ADMIN_INITIAL_APELLIDOS=Apellido
ADMIN_INITIAL_DNI=12345678
ADMIN_INITIAL_FECHA_NACIMIENTO=1990-01-01
```

`ADMIN_INITIAL_TELEFONO`, `ADMIN_INITIAL_DIRECCION` y
`ADMIN_INITIAL_REFERENCIA` son opcionales.

Ejecuta la aplicación una única vez:

```powershell
$env:ADMIN_BOOTSTRAP_ENABLED = "true"
mvn.cmd spring-boot:run
```

Cuando aparezca el mensaje de bootstrap completado, detén la aplicación y
desactiva la variable antes del arranque normal:

```powershell
$env:ADMIN_BOOTSTRAP_ENABLED = "false"
mvn.cmd spring-boot:run
```

También se puede usar el JAR generado con `java -jar` y las mismas variables.
Las credenciales nunca se imprimen en los logs.

## Base de datos existente

No uses `clean`, no borres tablas y no marques automáticamente una base de
datos no inspeccionada. Como actualmente no hay historial Flyway, verifica
primero que las tablas existentes correspondan al esquema de `V1`. Después,
ejecuta una sola vez el comando `baseline` de Flyway con las credenciales
inyectadas por el entorno:

```text
flyway -url=<URL_POSTGRES> -user=<USUARIO> -password=<SECRETO> baseline -baselineVersion=1
```

Luego inicia la aplicación normalmente. Flyway aplicará `V2`, que crea solo la
tabla de control. Si el esquema existente no coincide con `V1`, prepara una
migración específica antes de activar `ddl-auto=validate` en producción.

En producción `ddl-auto` está configurado como `validate`; Flyway es el dueño
del esquema. En desarrollo se conserva `update` para la compatibilidad con el
flujo local actual, pero las migraciones siguen ejecutándose.

## Primer acceso y administración posterior

Después del bootstrap, inicia sesión en:

```text
http://localhost:8080/admin/login
```

El registro público de empleados, si está habilitado, siempre asigna `VENDEDOR`
y nunca puede escalarse a `ADMINISTRADOR`. Los siguientes administradores deben
crearse desde una función administrativa autenticada o mediante un procedimiento
de recuperación operativo separado y auditado. El bootstrap no debe reutilizarse
para recuperar contraseñas, reactivar cuentas ni crear administradores nuevos.
