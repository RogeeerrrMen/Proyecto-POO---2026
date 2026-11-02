-- INSTRUCCIONES
-- Este archivo se ejecuta manualmente; no se ejecuta al iniciar Spring Boot.
--
-- Usarlo únicamente si las fechas de pedidos todavía son de tipo timestamp.
-- Si la base ya fue convertida o es nueva con el modelo actualizado,
-- no ejecutar este archivo.
--
-- 1. Descargar los cambios del repositorio y mantener Spring Boot detenido.
-- 2. Abrir pgAdmin y seleccionar la base de datos ProyectoPOO2026.
-- 3. Hacer clic derecho sobre la base y abrir Query Tool.
-- 4. Presionar Ctrl + O y seleccionar este archivo desde:
--    database/migrations/V2__separar_fechas_horas_pedidos.sql
-- 5. Sin seleccionar una parte del texto, ejecutar el archivo completo
--    con F5 una sola vez.
-- 6. Si termina correctamente, iniciar Spring Boot.
--
-- El script separa las fechas y horas conservando los datos existentes.
-- Si aparece un error, ejecutar ROLLBACK; por separado y revisar la causa
-- antes de volver a intentarlo.

BEGIN;

ALTER TABLE public.pedidos
    ADD COLUMN hora_preparacion time without time zone,
    ADD COLUMN hora_envio time without time zone,
    ADD COLUMN hora_entrega time without time zone;

UPDATE public.pedidos
SET hora_preparacion = fecha_preparacion::time,
    hora_envio = fecha_envio::time,
    hora_entrega = fecha_entrega::time;

ALTER TABLE public.pedidos
    ALTER COLUMN fecha_preparacion TYPE date
        USING fecha_preparacion::date,
    ALTER COLUMN fecha_envio TYPE date
        USING fecha_envio::date,
    ALTER COLUMN fecha_entrega TYPE date
        USING fecha_entrega::date;

COMMIT;