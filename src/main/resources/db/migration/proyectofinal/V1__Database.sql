-- =====================================================
-- TABLAS POSTGRESQL
-- =====================================================

-- Tabla de Roles
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre_rol VARCHAR(50) UNIQUE NOT NULL
);

-- Tablas de configuración
CREATE TABLE documentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tipo_documento VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE comprobantes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tipo_comprobante VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE estados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre_estado VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE metodos_entrega (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre_metodo VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE metodos_pago (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre_metodo VARCHAR(50) UNIQUE NOT NULL
);

-- Tabla clientes 
CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(15),
    direccion TEXT,
    id_rol UUID NOT NULL,
    dni VARCHAR(8),
    ruc VARCHAR(11),
    CONSTRAINT fk_cliente_rol FOREIGN KEY (id_rol) REFERENCES roles (id),
    CONSTRAINT chk_dni_ruc CHECK (dni IS NOT NULL OR ruc IS NOT NULL),
    CONSTRAINT chk_dni_length CHECK (LENGTH(dni) = 8 OR dni IS NULL),
    CONSTRAINT chk_ruc_length CHECK (LENGTH(ruc) = 11 OR ruc IS NULL),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_telefono_format CHECK (telefono ~* '^[0-9+\-\s()]+$' OR telefono IS NULL)
);

-- Tabla de Empleados 
CREATE TABLE empleados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_usuario UUID NOT NULL,
    CONSTRAINT fk_empleado_usuario FOREIGN KEY (id_usuario) REFERENCES clientes (id)
);

-- Tabla categorias 
CREATE TABLE categorias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre_categoria VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    imagen_url VARCHAR(255),
    CONSTRAINT chk_nombre_categoria_not_empty CHECK (TRIM(nombre_categoria) != '')
);

-- Tabla productos
CREATE TABLE productos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    stock INTEGER DEFAULT 0,
    precio_unitario DECIMAL(8,2) NOT NULL,
    id_categoria UUID,
    imagen_url VARCHAR(255),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categorias (id),
    CONSTRAINT chk_precio_positivo CHECK (precio_unitario > 0),
    CONSTRAINT chk_stock_no_negativo CHECK (stock >= 0),
    CONSTRAINT chk_nombre_producto_not_empty CHECK (TRIM(nombre) != '')
);

-- Tabla pedidos
CREATE TABLE pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_cliente UUID,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) DEFAULT 'pendiente',
    tipo_entrega VARCHAR(20) DEFAULT 'delivery',
    direccion_entrega TEXT,
    tiempo_estimado INTEGER DEFAULT 30,
    metodo_pago VARCHAR(30),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES clientes (id),
    CONSTRAINT chk_total_positivo CHECK (total > 0),
    CONSTRAINT chk_tiempo_estimado_positivo CHECK (tiempo_estimado > 0),
    CONSTRAINT chk_estado_valido CHECK (estado IN ('pendiente', 'confirmado', 'en_preparacion', 'en_camino', 'entregado', 'cancelado')),
    CONSTRAINT chk_tipo_entrega_valido CHECK (tipo_entrega IN ('delivery', 'recojo', 'mesa')),
    CONSTRAINT chk_direccion_delivery CHECK (
        (tipo_entrega = 'delivery' AND direccion_entrega IS NOT NULL AND TRIM(direccion_entrega) != '') 
        OR tipo_entrega != 'delivery'
    )
);

-- Tabla detalle_pedido
CREATE TABLE detalle_pedido (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_pedido UUID,
    id_producto UUID,
    cantidad INTEGER NOT NULL,
    precio_unitario DECIMAL(8,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos (id),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES productos (id),
    CONSTRAINT chk_cantidad_positiva CHECK (cantidad > 0),
    CONSTRAINT chk_precio_unitario_positivo CHECK (precio_unitario > 0),
    CONSTRAINT chk_subtotal_positivo CHECK (subtotal > 0),
    CONSTRAINT chk_subtotal_calculo CHECK (subtotal = cantidad * precio_unitario)
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_clientes_email ON clientes(email);
CREATE INDEX idx_clientes_rol ON clientes(id_rol);
CREATE INDEX idx_pedidos_cliente ON pedidos(id_cliente);
CREATE INDEX idx_pedidos_fecha_creacion ON pedidos(fecha_creacion);
CREATE INDEX idx_pedidos_estado ON pedidos(estado);
CREATE INDEX idx_detalle_pedido ON detalle_pedido(id_pedido);
CREATE INDEX idx_detalle_producto ON detalle_pedido(id_producto);
CREATE INDEX idx_productos_categoria ON productos(id_categoria);
CREATE INDEX idx_empleados_usuario ON empleados(id_usuario);