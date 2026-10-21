-- Migración: agregar columna motivo_cancelacion a la tabla citas
-- Se puede ejecutar varias veces sin error (IF NOT EXISTS no aplica a ALTER, se usa IGNORE de forma implícita al verificar)
ALTER TABLE citas ADD COLUMN motivo_cancelacion VARCHAR(200) NULL;
