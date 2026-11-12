
-- V4: Aislamiento de datos por emprendimiento
-- Ejecutar manualmente en PostgreSQL.
-- Requiere respaldo previo y Spring Boot detenido.
-- No elimina ni reasigna registros antiguos.

BEGIN;

-- 1. Clientes
ALTER TABLE public.clientes
    ADD COLUMN IF NOT EXISTS emprendimiento_id BIGINT;

-- 2. Productos
ALTER TABLE public.productos
    ADD COLUMN IF NOT EXISTS emprendimiento_id BIGINT;

-- 3. Gastos
ALTER TABLE public.gastos
    ADD COLUMN IF NOT EXISTS emprendimiento_id BIGINT;

-- 4. Pedidos
ALTER TABLE public.pedidos
    ADD COLUMN IF NOT EXISTS emprendimiento_id BIGINT;

ALTER TABLE public.pedidos
    ADD COLUMN IF NOT EXISTS modalidad_entrega VARCHAR(255);

-- Los pedidos antiguos utilizaban entrega a domicilio.
UPDATE public.pedidos
SET modalidad_entrega = 'ENVIO_DOMICILIO'
WHERE modalidad_entrega IS NULL;

ALTER TABLE public.pedidos
    ALTER COLUMN modalidad_entrega SET NOT NULL;

-- La recogida en el negocio no necesita direccion.
ALTER TABLE public.pedidos
    ALTER COLUMN direccion_entrega_id DROP NOT NULL;

-- 5. Claves foraneas: solo si no existe una
-- equivalente para la misma columna.

DO $$
DECLARE
    tabla TEXT;
    existe_fk BOOLEAN;
BEGIN
    FOREACH tabla IN ARRAY ARRAY[
        'clientes', 'productos', 'gastos', 'pedidos'
    ]
    LOOP
        SELECT EXISTS (
            SELECT 1
            FROM pg_constraint c
            JOIN pg_class t ON t.oid = c.conrelid
            JOIN pg_namespace n ON n.oid = t.relnamespace
            JOIN pg_attribute a
                ON a.attrelid = t.oid
               AND a.attnum = ANY(c.conkey)
            WHERE n.nspname = 'public'
              AND t.relname = tabla
              AND c.contype = 'f'
              AND c.confrelid = 'public.emprendimientos'::regclass
              AND a.attname = 'emprendimiento_id'
              AND array_length(c.conkey, 1) = 1
        ) INTO existe_fk;

        IF NOT existe_fk THEN
            EXECUTE format(
                'ALTER TABLE public.%I
                 ADD CONSTRAINT %I
                 FOREIGN KEY (emprendimiento_id)
                 REFERENCES public.emprendimientos(id)',
                tabla,
                'fk_' || tabla || '_emprendimiento'
            );
        END IF;
    END LOOP;
END $$;

-- 6. Indices para consultas filtradas.
CREATE INDEX IF NOT EXISTS idx_clientes_emprendimiento
    ON public.clientes(emprendimiento_id);

CREATE INDEX IF NOT EXISTS idx_productos_emprendimiento
    ON public.productos(emprendimiento_id);

CREATE INDEX IF NOT EXISTS idx_gastos_emprendimiento
    ON public.gastos(emprendimiento_id);

CREATE INDEX IF NOT EXISTS idx_pedidos_emprendimiento
    ON public.pedidos(emprendimiento_id);

COMMIT;
