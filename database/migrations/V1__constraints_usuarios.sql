-- Los datos existentes deben cumplir estas restricciones.

-- 1. Campos obligatorios

ALTER TABLE public.usuarios
    ALTER COLUMN nombre SET NOT NULL,
    ALTER COLUMN apellido SET NOT NULL,
    ALTER COLUMN correo SET NOT NULL;

-- 2. Correo unico

ALTER TABLE public.usuarios
    ADD CONSTRAINT uk_usuarios_correo
    UNIQUE (correo);

-- 3. Nombre no vacio

ALTER TABLE public.usuarios
    ADD CONSTRAINT chk_usuarios_nombre
    CHECK (LENGTH(TRIM(nombre)) > 0);

-- 4. Apellido no vacio

ALTER TABLE public.usuarios
    ADD CONSTRAINT chk_usuarios_apellido
    CHECK (LENGTH(TRIM(apellido)) > 0);

-- 5. Correo no vacio

ALTER TABLE public.usuarios
    ADD CONSTRAINT chk_usuarios_correo
    CHECK (LENGTH(TRIM(correo)) > 0);


-- 6. Evitar correos duplicados ignorando mayusculas y espacios al inicio y final.

    CREATE UNIQUE INDEX ux_usuarios_correo_normalizado
    ON public.usuarios (LOWER(TRIM(correo)));