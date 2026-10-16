## Aplicación de constraints

El archivo migrations/V1__constraints_usuarios.sql
contiene las restricciones de integridad para usuarios.

IMPORTANTE:
- El script se ejecuta manualmente desde pgAdmin.
- No se ejecuta automáticamente al iniciar Spring Boot.
- Debe ejecutarse una sola vez por base de datos.
- No debe ejecutarse si las restricciones ya existen.
- Los datos existentes deben cumplir las restricciones.
- Imaginen que tienen que imlementarlos bloque por bloque y darle a boton de run.

## Procedimiento

1. Configurar la conexión a PostgreSQL.
2. Iniciar Spring Boot para crear la tabla usuarios,
   si todavía no existe.
3. Detener Spring Boot.
4. Revisar que no existan datos nulos, vacíos o
   correos duplicados.
5. Abrir Query Tool en pgAdmin.
6. Ejecutar V1__constraints_usuarios.sql.
7. Verificar que las restricciones fueron creadas.

## Credenciales

La contraseña de PostgreSQL se configura mediante
la variable de entorno DB_PASSWORD.

Nunca almacenar contraseñas personales en GitHub.

## Pendiente

Automatizar las migraciones mediante Flyway.