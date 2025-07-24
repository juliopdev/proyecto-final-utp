-- Tabla reservas_stock
CREATE TABLE reservas_stock (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_producto UUID NOT NULL,
    id_cliente UUID NOT NULL,
    cantidad_reservada INTEGER NOT NULL,
    estado VARCHAR(20) DEFAULT 'activa',
    fecha_expiracion TIMESTAMP NOT NULL,
    CONSTRAINT fk_reserva_producto FOREIGN KEY (id_producto) REFERENCES productos (id),
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES clientes (id)
);