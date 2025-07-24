
-- =====================================================
-- DATOS INICIALES
-- =====================================================

-- Roles
INSERT INTO roles (nombre_rol) VALUES 
('ADMIN'),
('CLIENTE'),
('EMPLEADO'),
('REPARTIDOR')
ON CONFLICT (nombre_rol) DO NOTHING;

-- Tipos de documento
INSERT INTO documentos (tipo_documento) VALUES 
('DNI'),
('RUC')
ON CONFLICT (tipo_documento) DO NOTHING;

-- Tipos de comprobante
INSERT INTO comprobantes (tipo_comprobante) VALUES 
('boleta'),
('factura')
ON CONFLICT (tipo_comprobante) DO NOTHING;

-- Estados de órdenes
INSERT INTO estados (nombre_estado) VALUES 
('Pendiente'),
('Confirmado'),
('En Preparación'),
('En Camino'),
('Entregado'),
('Cancelado')
ON CONFLICT (nombre_estado) DO NOTHING;

-- Métodos de entrega
INSERT INTO metodos_entrega (nombre_metodo) VALUES 
('delivery'),
('recojo'),
('mesa')
ON CONFLICT (nombre_metodo) DO NOTHING;

-- Métodos de pago
INSERT INTO metodos_pago (nombre_metodo) VALUES 
('tarjeta'),
('yape'),
('efectivo')
ON CONFLICT (nombre_metodo) DO NOTHING;

-- Categorias
INSERT INTO categorias (nombre_categoria, descripcion, imagen_url) VALUES
('HAMBURGUESA', 'Nuestras mejores hamburguesas artesanales.', 'Hamburguesa_SinFondo.png'),
('ALITAS', 'Alitas de pollo crujientes con tu salsa favorita.', 'Alitas_SinFondo.png'),
('POLLO', 'Jugoso pollo a la brasa y broaster.', 'Pollo_SinFondo.png'),
('PAPAS', 'Papas fritas clásicas, perfectas para acompañar.', 'Papas_SinFondo.png')
ON CONFLICT (nombre_categoria) DO NOTHING;