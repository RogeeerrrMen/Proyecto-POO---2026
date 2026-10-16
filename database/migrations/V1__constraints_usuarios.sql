
-- REQUISITOS:
-- 1. La tabla public.usuarios debe existir.
-- 2. Los datos existentes deben cumplir las reglas.
-- 3. Ejecutar una sola vez por base de datos.
-- 4. No ejecutar en una base donde ya existan
--    estos constraints.

BEGIN;

-- Campos obligatorios
ALTER TABLE public.usuarios
    ALTER COLUMN nombre SET NOT NULL,
    ALTER COLUMN apellido SET NOT NULL,
    ALTER COLUMN correo SET NOT NULL;

-- Correo unico
ALTER TABLE public.usuarios
    ADD CONSTRAINT uk_usuarios_correo
    UNIQUE (correo);

-- Nombre no vacio
ALTER TABLE public.usuarios
    ADD CONSTRAINT chk_usuarios_nombre
    CHECK (LENGTH(TRIM(nombre)) > 0);

-- Apellido no vacio
ALTER TABLE public.usuarios
    ADD CONSTRAINT chk_usuarios_apellido
    CHECK (LENGTH(TRIM(apellido)) > 0);

-- Correo no vacio
ALTER TABLE public.usuarios
    ADD CONSTRAINT chk_usuarios_correo
    CHECK (LENGTH(TRIM(correo)) > 0);

-- Correo unico ignorando mayusculas y espacios
CREATE UNIQUE INDEX ux_usuarios_correo_normalizado
ON public.usuarios (LOWER(TRIM(correo)));

COMMIT;
