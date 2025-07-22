-- Migración V1: Tablas adicionales para el sistema de usuario mejorado
-- Autor: Julio Pariona

-- Índices adicionales para optimización de consultas en tabla Clientes
CREATE INDEX IF NOT EXISTS idx_clientes_email ON Clientes(Correo);
CREATE INDEX IF NOT EXISTS idx_clientes_telefono ON Clientes(Telefono);
CREATE INDEX IF NOT EXISTS idx_clientes_rol ON Clientes(ID_Rol);

-- Agregar campos adicionales a la tabla Clientes para mejorar funcionalidad
DO $$
BEGIN
    -- Verificar y agregar columnas de auditoría
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'clientes' AND column_name = 'created_at') THEN
        ALTER TABLE Clientes ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'clientes' AND column_name = 'updated_at') THEN
        ALTER TABLE Clientes ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'clientes' AND column_name = 'deleted_at') THEN
        ALTER TABLE Clientes ADD COLUMN deleted_at TIMESTAMP NULL;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'clientes' AND column_name = 'last_login') THEN
        ALTER TABLE Clientes ADD COLUMN last_login TIMESTAMP NULL;
    END IF;
    
    -- Verificar y agregar campos de estado de cuenta
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'clientes' AND column_name = 'is_enabled') THEN
        ALTER TABLE Clientes ADD COLUMN is_enabled BOOLEAN DEFAULT TRUE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'clientes' AND column_name = 'is_locked') THEN
        ALTER TABLE Clientes ADD COLUMN is_locked BOOLEAN DEFAULT FALSE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'clientes' AND column_name = 'is_verified') THEN
        ALTER TABLE Clientes ADD COLUMN is_verified BOOLEAN DEFAULT FALSE;
    END IF;
    
    -- Campo para integración con MongoDB
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'clientes' AND column_name = 'mongo_user_id') THEN
        ALTER TABLE Clientes ADD COLUMN mongo_user_id VARCHAR(24) NULL;
    END IF;
END $$;

-- Crear índices adicionales para optimización
CREATE INDEX IF NOT EXISTS idx_clientes_created_at ON Clientes(created_at);
CREATE INDEX IF NOT EXISTS idx_clientes_deleted_at ON Clientes(deleted_at);
CREATE INDEX IF NOT EXISTS idx_clientes_enabled ON Clientes(is_enabled);
CREATE INDEX IF NOT EXISTS idx_clientes_mongo_id ON Clientes(mongo_user_id);

-- Tabla para tokens de verificación y reset de contraseñas
CREATE TABLE IF NOT EXISTS user_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(50) NOT NULL CHECK (token_type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'REMEMBER_ME')),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Clientes(ID_Cliente) ON DELETE CASCADE
);

-- Índices para tabla user_tokens
CREATE INDEX IF NOT EXISTS idx_user_tokens_user_id ON user_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_user_tokens_token ON user_tokens(token);
CREATE INDEX IF NOT EXISTS idx_user_tokens_type ON user_tokens(token_type);
CREATE INDEX IF NOT EXISTS idx_user_tokens_expires ON user_tokens(expires_at);

-- Tabla para remember-me tokens persistentes (requerida por Spring Security)
CREATE TABLE IF NOT EXISTS persistent_logins (
    username VARCHAR(100) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

-- Tabla para sesiones de usuario (opcional, para tracking)
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES Clientes(ID_Cliente) ON DELETE CASCADE
);

-- Índices para user_sessions
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_session_id ON user_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_active ON user_sessions(is_active);

-- Mejorar campos existentes en tabla Productos
DO $$
BEGIN
    -- Agregar campos de auditoría a Productos
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'productos' AND column_name = 'created_at') THEN
        ALTER TABLE Productos ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'productos' AND column_name = 'updated_at') THEN
        ALTER TABLE Productos ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'productos' AND column_name = 'deleted_at') THEN
        ALTER TABLE Productos ADD COLUMN deleted_at TIMESTAMP NULL;
    END IF;
    
    -- Agregar campos adicionales para productos
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'productos' AND column_name = 'is_active') THEN
        ALTER TABLE Productos ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'productos' AND column_name = 'is_featured') THEN
        ALTER TABLE Productos ADD COLUMN is_featured BOOLEAN DEFAULT FALSE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'productos' AND column_name = 'slug') THEN
        ALTER TABLE Productos ADD COLUMN slug VARCHAR(150) UNIQUE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'productos' AND column_name = 'meta_description') THEN
        ALTER TABLE Productos ADD COLUMN meta_description VARCHAR(160);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'productos' AND column_name = 'mongo_product_id') THEN
        ALTER TABLE Productos ADD COLUMN mongo_product_id VARCHAR(24);
    END IF;
END $;

-- Índices adicionales para Productos
CREATE INDEX IF NOT EXISTS idx_productos_active ON Productos(is_active);
CREATE INDEX IF NOT EXISTS idx_productos_featured ON Productos(is_featured);
CREATE INDEX IF NOT EXISTS idx_productos_slug ON Productos(slug);
CREATE INDEX IF NOT EXISTS idx_productos_created_at ON Productos(created_at);

-- Mejorar tabla Categorias
DO $
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'categorias' AND column_name = 'descripcion') THEN
        ALTER TABLE Categorias ADD COLUMN descripcion VARCHAR(500);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'categorias' AND column_name = 'slug') THEN
        ALTER TABLE Categorias ADD COLUMN slug VARCHAR(100) UNIQUE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'categorias' AND column_name = 'is_active') THEN
        ALTER TABLE Categorias ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'categorias' AND column_name = 'sort_order') THEN
        ALTER TABLE Categorias ADD COLUMN sort_order INTEGER DEFAULT 0;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'categorias' AND column_name = 'created_at') THEN
        ALTER TABLE Categorias ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'categorias' AND column_name = 'updated_at') THEN
        ALTER TABLE Categorias ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'categorias' AND column_name = 'deleted_at') THEN
        ALTER TABLE Categorias ADD COLUMN deleted_at TIMESTAMP NULL;
    END IF;
END $;

-- Índices para Categorias
CREATE INDEX IF NOT EXISTS idx_categorias_active ON Categorias(is_active);
CREATE INDEX IF NOT EXISTS idx_categorias_sort_order ON Categorias(sort_order);
CREATE INDEX IF NOT EXISTS idx_categorias_slug ON Categorias(slug);

-- Función para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

-- Triggers para actualizar updated_at automáticamente
DROP TRIGGER IF EXISTS update_clientes_updated_at ON Clientes;
CREATE TRIGGER update_clientes_updated_at
    BEFORE UPDATE ON Clientes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_productos_updated_at ON Productos;
CREATE TRIGGER update_productos_updated_at
    BEFORE UPDATE ON Productos
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_categorias_updated_at ON Categorias;
CREATE TRIGGER update_categorias_updated_at
    BEFORE UPDATE ON Categorias
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Función para generar slugs automáticamente
CREATE OR REPLACE FUNCTION generate_slug(input_text TEXT)
RETURNS TEXT AS $
BEGIN
    RETURN lower(
        regexp_replace(
            regexp_replace(
                regexp_replace(input_text, '[áàâãä]', 'a', 'g'),
                '[éèêë]', 'e', 'g'
            ),
            '[íìîï]', 'i', 'g'
        )
    );
END;
$ LANGUAGE plpgsql;

-- Función para generar slug de producto
CREATE OR REPLACE FUNCTION generate_product_slug()
RETURNS TRIGGER AS $
BEGIN
    IF NEW.slug IS NULL OR NEW.slug = '' THEN
        NEW.slug := lower(
            regexp_replace(
                regexp_replace(NEW.Producto, '[^a-zA-Z0-9\s-]', '', 'g'),
                '\s+', '-', 'g'
            )
        );
    END IF;
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

-- Función para generar slug de categoría
CREATE OR REPLACE FUNCTION generate_category_slug()
RETURNS TRIGGER AS $
BEGIN
    IF NEW.slug IS NULL OR NEW.slug = '' THEN
        NEW.slug := lower(
            regexp_replace(
                regexp_replace(NEW.Categoria, '[^a-zA-Z0-9\s-]', '', 'g'),
                '\s+', '-', 'g'
            )
        );
    END IF;
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

-- Triggers para generar slugs automáticamente
DROP TRIGGER IF EXISTS generate_product_slug_trigger ON Productos;
CREATE TRIGGER generate_product_slug_trigger
    BEFORE INSERT OR UPDATE ON Productos
    FOR EACH ROW
    EXECUTE FUNCTION generate_product_slug();

DROP TRIGGER IF EXISTS generate_category_slug_trigger ON Categorias;
CREATE TRIGGER generate_category_slug_trigger
    BEFORE INSERT OR UPDATE ON Categorias
    FOR EACH ROW
    EXECUTE FUNCTION generate_category_slug();

-- Función para limpiar tokens expirados
CREATE OR REPLACE FUNCTION cleanup_expired_tokens()
RETURNS void AS $
BEGIN
    DELETE FROM user_tokens 
    WHERE expires_at < CURRENT_TIMESTAMP 
    AND used_at IS NULL;
    
    DELETE FROM persistent_logins 
    WHERE last_used < CURRENT_TIMESTAMP - INTERVAL '30 days';
    
    -- Marcar sesiones inactivas
    UPDATE user_sessions 
    SET is_active = FALSE 
    WHERE last_activity < CURRENT_TIMESTAMP - INTERVAL '24 hours' 
    AND is_active = TRUE;
END;
$ LANGUAGE plpgsql;

-- Insertar datos de prueba mejorados (solo en desarrollo)
DO $
BEGIN
    -- Insertar más roles si no existen
    INSERT INTO Rol (ID_Rol, Rol) VALUES (3, 'Repartidor') ON CONFLICT (ID_Rol) DO NOTHING;
    INSERT INTO Rol (ID_Rol, Rol) VALUES (4, 'Vendedor') ON CONFLICT (ID_Rol) DO NOTHING;
    
    -- Insertar categorías de ejemplo si no existen
    INSERT INTO Categorias (Categoria, Icono, descripcion, is_active, sort_order) VALUES 
    ('Bebidas', '/img/icons/bebidas.svg', 'Bebidas frías y calientes', TRUE, 1),
    ('Comida Rápida', '/img/icons/comida-rapida.svg', 'Hamburguesas, pizzas y más', TRUE, 2),
    ('Postres', '/img/icons/postres.svg', 'Dulces y postres caseros', TRUE, 3),
    ('Ensaladas', '/img/icons/ensaladas.svg', 'Opciones saludables y frescas', TRUE, 4)
    ON CONFLICT (Categoria) DO NOTHING;
    
    -- Actualizar productos existentes con campos nuevos si no tienen valores
    UPDATE Productos SET 
        is_active = TRUE,
        is_featured = FALSE,
        created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
        updated_at = CURRENT_TIMESTAMP
    WHERE is_active IS NULL;
    
    -- Actualizar categorías existentes
    UPDATE Categorias SET 
        is_active = TRUE,
        sort_order = COALESCE(sort_order, 0),
        created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
        updated_at = CURRENT_TIMESTAMP
    WHERE is_active IS NULL;
    
    -- Actualizar clientes existentes
    UPDATE Clientes SET 
        is_enabled = TRUE,
        is_locked = FALSE,
        is_verified = FALSE,
        created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
        updated_at = CURRENT_TIMESTAMP
    WHERE is_enabled IS NULL;
END $;

-- Crear vista para usuarios activos (útil para consultas frecuentes)
CREATE OR REPLACE VIEW active_users AS
SELECT 
    c.ID_Cliente as id,
    c.Nombre as first_name,
    c.Apellido as last_name,
    c.Correo as email,
    c.Telefono as phone,
    c.Fecha_Nacimiento as birth_date,
    c.Dirección as address,
    r.Rol as role_name,
    c.is_verified,
    c.created_at,
    c.last_login,
    c.mongo_user_id
FROM Clientes c
JOIN Rol r ON c.ID_Rol = r.ID_Rol
WHERE c.deleted_at IS NULL 
AND c.is_enabled = TRUE;

-- Crear vista para productos activos
CREATE OR REPLACE VIEW active_products AS
SELECT 
    p.ID_Producto as id,
    p.Producto as name,
    p.Precio as price,
    p.Stock as stock,
    p.Descripcion as description,
    p.Imagen as image_url,
    p.slug,
    p.is_featured,
    c.Categoria as category_name,
    c.slug as category_slug,
    p.created_at,
    p.updated_at
FROM Productos p
JOIN Categorias c ON p.ID_Categoria = c.ID_Categoria
WHERE p.deleted_at IS NULL 
AND p.is_active = TRUE 
AND c.is_active = TRUE;

-- Comentarios para documentación
COMMENT ON TABLE user_tokens IS 'Tabla para almacenar tokens de verificación de email y reset de contraseñas';
COMMENT ON TABLE persistent_logins IS 'Tabla requerida por Spring Security para Remember-Me persistente';
COMMENT ON TABLE user_sessions IS 'Tabla para tracking de sesiones de usuario';
COMMENT ON VIEW active_users IS 'Vista para usuarios activos con información de rol';
COMMENT ON VIEW active_products IS 'Vista para productos activos con información de categoría';

-- Crear función para estadísticas de usuarios
CREATE OR REPLACE FUNCTION get_user_stats()
RETURNS TABLE(
    total_users BIGINT,
    active_users BIGINT,
    verified_users BIGINT,
    users_today BIGINT,
    users_this_month BIGINT
) AS $
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*) FROM Clientes WHERE deleted_at IS NULL),
        (SELECT COUNT(*) FROM Clientes WHERE deleted_at IS NULL AND is_enabled = TRUE),
        (SELECT COUNT(*) FROM Clientes WHERE deleted_at IS NULL AND is_verified = TRUE),
        (SELECT COUNT(*) FROM Clientes WHERE DATE(created_at) = CURRENT_DATE AND deleted_at IS NULL),
        (SELECT COUNT(*) FROM Clientes WHERE created_at >= DATE_TRUNC('month', CURRENT_DATE) AND deleted_at IS NULL);
END;
$ LANGUAGE plpgsql;