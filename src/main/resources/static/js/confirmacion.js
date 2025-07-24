// confirmacion.js - Fixed Version para trabajar con PostgreSQL normalizado
document.addEventListener("DOMContentLoaded", () => {
  // Elementos del DOM
  const pedidoGrid = document.getElementById("pedido-grid");
  const mensajeEntrega = document.getElementById("mensaje-entrega");
  const clienteNombre = document.getElementById("cliente-nombre");
  const comprobanteElement = document.getElementById("comprobante");
  const subtotalElement = document.getElementById("subtotal");
  const deliveryCostElement = document.getElementById("delivery-cost");
  const totalElement = document.getElementById("total");
  const confirmacionHeader = document.getElementById("confirmacion-header");
  const numeroOrdenElement = document.getElementById("numero-orden");
  const fechaPedidoElement = document.getElementById("fecha-pedido");
  const estadoPedidoElement = document.getElementById("estado-pedido");

  // Recuperar datos del último pedido
  const ultimoPedido = JSON.parse(localStorage.getItem("ultimo_pedido")) || {};

  function mostrarResumen() {
    // Verificar que existen datos del pedido
    if (!ultimoPedido.items || ultimoPedido.items.length === 0) {
      mostrarError("No se encontraron datos del pedido. Es posible que haya expirado la sesión.");
      return;
    }

    try {
      // Actualizar encabezado
      if (confirmacionHeader) {
        const nombreCompleto = `${ultimoPedido.cliente.nombres} ${ultimoPedido.cliente.apellidos}`;
        confirmacionHeader.textContent = `¡PEDIDO CONFIRMADO, ${nombreCompleto.toUpperCase()}!`;
      }

      // Mostrar número de orden si existe
      if (numeroOrdenElement && ultimoPedido.numero_orden) {
        numeroOrdenElement.textContent = ultimoPedido.numero_orden;
      }

      // Mostrar fecha del pedido
      if (fechaPedidoElement) {
        const fechaPedido = ultimoPedido.fecha_pedido ? 
          new Date(ultimoPedido.fecha_pedido) : 
          new Date();
        fechaPedidoElement.textContent = fechaPedido.toLocaleString('es-PE', {
          dateStyle: 'full',
          timeStyle: 'short'
        });
      }

      // Mostrar estado del pedido
      if (estadoPedidoElement) {
        estadoPedidoElement.textContent = ultimoPedido.estado || 'Pendiente';
        estadoPedidoElement.className = `badge bg-${getEstadoColor(ultimoPedido.estado || 'Pendiente')}`;
      }

      // Renderizar items del pedido
      renderizarItems();

      // Mostrar información del cliente
      mostrarInfoCliente();

      // Mostrar totales
      mostrarTotales();

      // Mostrar mensaje de entrega
      mostrarMensajeEntrega();

      // Configurar acciones adicionales
      configurarAcciones();

    } catch (error) {
      console.error("Error al mostrar resumen:", error);
      mostrarError("Hubo un error al mostrar los detalles del pedido.");
    }
  }

  function renderizarItems() {
    if (!pedidoGrid) return;

    pedidoGrid.innerHTML = "";

    ultimoPedido.items.forEach((item, index) => {
      const itemElement = document.createElement("div");
      itemElement.className = "col-md-6 col-lg-4 mb-4";
      
      const subtotalItem = item.subtotal || (item.cantidad * item.precio_unitario) || (item.cantidad * item.precio);
      
      itemElement.innerHTML = `
        <div class="card h-100 shadow-sm item-card" style="animation: fadeInUp 0.6s ease-out ${index * 0.1}s both;">
          <div class="card-body text-center">
            <div class="item-icon mb-3">
              <i class="bi bi-${getItemIcon(item.nombre_producto || item.nombre)} fs-1 text-primary"></i>
            </div>
            <h5 class="card-title fw-bold">${item.nombre_producto || item.nombre}</h5>
            <div class="item-details">
              <p class="card-text mb-1">
                <span class="text-muted">Precio unitario:</span> 
                <span class="fw-bold">S/ ${(item.precio_unitario || item.precio).toFixed(2)}</span>
              </p>
              <p class="card-text mb-1">
                <span class="text-muted">Cantidad:</span> 
                <span class="badge bg-secondary">${item.cantidad}</span>
              </p>
              <p class="card-text">
                <span class="text-muted">Subtotal:</span> 
                <span class="fw-bold text-success">S/ ${subtotalItem.toFixed(2)}</span>
              </p>
            </div>
          </div>
        </div>
      `;
      pedidoGrid.appendChild(itemElement);
    });
  }

  function mostrarInfoCliente() {
    if (clienteNombre) {
      clienteNombre.textContent = `${ultimoPedido.cliente.nombres} ${ultimoPedido.cliente.apellidos}`;
    }

    if (comprobanteElement) {
      const tipoComprobante = ultimoPedido.cliente.comprobante || 'boleta';
      const documento = tipoComprobante === 'boleta' ? 
        `Boleta (DNI: ${ultimoPedido.cliente.dni})` : 
        `Factura (RUC: ${ultimoPedido.cliente.ruc})`;
      comprobanteElement.textContent = documento;
    }

    // Mostrar información adicional del cliente si existe el contenedor
    const infoClienteContainer = document.getElementById("info-cliente-adicional");
    if (infoClienteContainer) {
      infoClienteContainer.innerHTML = `
        <div class="row">
          <div class="col-md-6">
            <strong>Email:</strong> ${ultimoPedido.cliente.email || 'No especificado'}
          </div>
          <div class="col-md-6">
            <strong>Teléfono:</strong> ${ultimoPedido.cliente.telefono || 'No especificado'}
          </div>
        </div>
      `;
    }
  }

  function mostrarTotales() {
    const subtotal = ultimoPedido.subtotal || 
      ultimoPedido.items.reduce((sum, item) => sum + (item.subtotal || (item.cantidad * (item.precio_unitario || item.precio))), 0);
    const deliveryCost = ultimoPedido.deliveryCost || ultimoPedido.costo_delivery || 0;
    const total = ultimoPedido.total || (subtotal + deliveryCost);

    if (subtotalElement) subtotalElement.textContent = subtotal.toFixed(2);
    if (deliveryCostElement) deliveryCostElement.textContent = deliveryCost.toFixed(2);
    if (totalElement) totalElement.textContent = total.toFixed(2);

    // Resaltar el costo de delivery si es mayor a 0
    if (deliveryCostElement && deliveryCost > 0) {
      deliveryCostElement.parentElement.classList.add('text-warning');
    }
  }

  function mostrarMensajeEntrega() {
    if (!mensajeEntrega) return;

    const metodoEntrega = ultimoPedido.entrega?.metodo || 'recoger';
    let mensaje = '';
    let tiempoEstimado = '';
    let icono = '';

    if (metodoEntrega === 'delivery') {
      mensaje = "Tu pedido llegará en aproximadamente 25-40 minutos.";
      tiempoEstimado = "25-40 minutos";
      icono = "🚗";
      
      // Mostrar dirección si existe
      if (ultimoPedido.entrega?.direccion) {
        mensaje += ` <br><strong>Dirección:</strong> ${ultimoPedido.entrega.direccion}`;
        if (ultimoPedido.entrega?.referencia) {
          mensaje += ` <br><strong>Referencia:</strong> ${ultimoPedido.entrega.referencia}`;
        }
      }
    } else {
      mensaje = "Tu pedido estará listo para recojo en aproximadamente 15-25 minutos.";
      tiempoEstimado = "15-25 minutos";
      icono = "🏪";
    }

    mensajeEntrega.innerHTML = `
      <div class="alert alert-info d-flex align-items-center">
        <div class="me-3" style="font-size: 2rem;">${icono}</div>
        <div>
          <strong>Información de entrega:</strong><br>
          ${mensaje}
        </div>
      </div>
    `;

    // Agregar contador de tiempo estimado si es posible
    const fechaPedido = ultimoPedido.fecha_pedido ? new Date(ultimoPedido.fecha_pedido) : new Date();
    const tiempoMinutos = metodoEntrega === 'delivery' ? 40 : 25;
    const fechaEstimada = new Date(fechaPedido.getTime() + tiempoMinutos * 60000);
    
    const contadorElement = document.getElementById("contador-tiempo");
    if (contadorElement) {
      iniciarContador(fechaEstimada, contadorElement);
    }
  }

  function configurarAcciones() {
    // Botón para seguir comprando
    const btnSeguirComprando = document.getElementById("btn-seguir-comprando");
    if (btnSeguirComprando) {
      btnSeguirComprando.onclick = () => {
        window.location.href = "menu-completo.html";
      };
    }

    // Botón para ver historial de pedidos
    const btnHistorial = document.getElementById("btn-historial");
    if (btnHistorial) {
      btnHistorial.onclick = () => {
        window.location.href = "mis-pedidos.html";
      };
    }

    // Botón para compartir pedido
    const btnCompartir = document.getElementById("btn-compartir");
    if (btnCompartir) {
      btnCompartir.onclick = compartirPedido;
    }

    // Botón para descargar comprobante
    const btnDescargar = document.getElementById("btn-descargar");
    if (btnDescargar) {
      btnDescargar.onclick = descargarComprobante;
    }
  }

  function iniciarContador(fechaObjetivo, elemento) {
    function actualizarContador() {
      const ahora = new Date();
      const diferencia = fechaObjetivo - ahora;

      if (diferencia > 0) {
        const minutos = Math.floor(diferencia / 60000);
        const segundos = Math.floor((diferencia % 60000) / 1000);
        elemento.textContent = `Tiempo estimado: ${minutos}:${segundos.toString().padStart(2, '0')}`;
      } else {
        elemento.textContent = "¡Tu pedido debería estar listo!";
        elemento.classList.add('text-success', 'fw-bold');
      }
    }

    actualizarContador();
    setInterval(actualizarContador, 1000);
  }

  function compartirPedido() {
    if (navigator.share) {
      navigator.share({
        title: 'Mi Pedido - Hamburguesería',
        text: `¡Acabo de hacer un pedido! Número: ${ultimoPedido.numero_orden || 'N/A'}`,
        url: window.location.href
      });
    } else {
      // Fallback: copiar al portapapeles
      const texto = `¡Acabo de hacer un pedido en la hamburguesería! Número: ${ultimoPedido.numero_orden || 'N/A'}`;
      navigator.clipboard.writeText(texto).then(() => {
        mostrarNotificacion('Información del pedido copiada al portapapeles', 'success');
      });
    }
  }

  function descargarComprobante() {
    // Crear contenido del comprobante
    const contenido = generarContenidoComprobante();
    const blob = new Blob([contenido], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    
    const a = document.createElement('a');
    a.href = url;
    a.download = `comprobante-${ultimoPedido.numero_orden || 'pedido'}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  function generarContenidoComprobante() {
    return `
COMPROBANTE DE PEDIDO
=====================

Número de Orden: ${ultimoPedido.numero_orden || 'N/A'}
Fecha: ${ultimoPedido.fecha_pedido ? new Date(ultimoPedido.fecha_pedido).toLocaleString('es-PE') : new Date().toLocaleString('es-PE')}
Estado: ${ultimoPedido.estado || 'Pendiente'}

DATOS DEL CLIENTE
-----------------
Nombre: ${ultimoPedido.cliente.nombres} ${ultimoPedido.cliente.apellidos}
Email: ${ultimoPedido.cliente.email || 'No especificado'}
Teléfono: ${ultimoPedido.cliente.telefono || 'No especificado'}
Documento: ${ultimoPedido.cliente.comprobante === 'boleta' ? `DNI: ${ultimoPedido.cliente.dni}` : `RUC: ${ultimoPedido.cliente.ruc}`}

ITEMS DEL PEDIDO
----------------
${ultimoPedido.items.map(item => `${item.nombre_producto || item.nombre} x${item.cantidad} - S/ ${((item.subtotal || (item.cantidad * (item.precio_unitario || item.precio)))).toFixed(2)}`).join('\n')}

TOTALES
-------
Subtotal: S/ ${(ultimoPedido.subtotal || ultimoPedido.items.reduce((sum, item) => sum + (item.subtotal || (item.cantidad * (item.precio_unitario || item.precio))), 0)).toFixed(2)}
Delivery: S/ ${(ultimoPedido.deliveryCost || ultimoPedido.costo_delivery || 0).toFixed(2)}
Total: S/ ${(ultimoPedido.total || 0).toFixed(2)}

ENTREGA
-------
Método: ${ultimoPedido.entrega?.metodo === 'delivery' ? 'Delivery' : 'Recojo en tienda'}
${ultimoPedido.entrega?.direccion ? `Dirección: ${ultimoPedido.entrega.direccion}` : ''}
${ultimoPedido.entrega?.referencia ? `Referencia: ${ultimoPedido.entrega.referencia}` : ''}

¡Gracias por tu preferencia!
    `.trim();
  }

  function getItemIcon(nombreItem) {
    const nombre = nombreItem.toLowerCase();
    if (nombre.includes('hamburguesa') || nombre.includes('burger')) return 'burger';
    if (nombre.includes('alitas') || nombre.includes('alita')) return 'drumstick-bite';
    if (nombre.includes('pollo')) return 'egg-fried';
    if (nombre.includes('bebida') || nombre.includes('refresco') || nombre.includes('jugo')) return 'cup-straw';
    if (nombre.includes('papa') || nombre.includes('papas')) return 'potato';
    if (nombre.includes('ensalada')) return 'salad';
    if (nombre.includes('postre') || nombre.includes('helado')) return 'ice-cream';
    return 'food-croissant'; // Icono por defecto
  }

  function getEstadoColor(estado) {
    switch (estado.toLowerCase()) {
      case 'pendiente': return 'warning';
      case 'confirmado': return 'info';
      case 'en preparación': return 'primary';
      case 'en camino': return 'secondary';
      case 'entregado': return 'success';
      case 'cancelado': return 'danger';
      default: return 'secondary';
    }
  }

  function mostrarError(mensaje) {
    if (pedidoGrid) {
      pedidoGrid.innerHTML = `
        <div class="col-12">
          <div class="alert alert-warning text-center">
            <i class="bi bi-exclamation-triangle fs-1 text-warning mb-3"></i>
            <h4>¡Oops!</h4>
            <p>${mensaje}</p>
            <button class="btn btn-primary" onclick="window.location.href='menu-completo.html'">
              Ir al Menú
            </button>
          </div>
        </div>
      `;
    }

    if (confirmacionHeader) {
      confirmacionHeader.textContent = "ERROR AL CARGAR PEDIDO";
    }
  }

  function mostrarNotificacion(mensaje, tipo = 'info') {
    const notification = document.createElement('div');
    notification.className = `alert alert-${tipo === 'error' ? 'danger' : tipo} alert-dismissible fade show position-fixed`;
    notification.style.cssText = `
      top: 20px;
      right: 20px;
      z-index: 9999;
      max-width: 350px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    `;
    notification.innerHTML = `
      <i class="bi bi-${getNotificationIcon(tipo)} me-2"></i>
      ${mensaje}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
      if (notification.parentNode) {
        notification.remove();
      }
    }, 5000);
  }

  function getNotificationIcon(tipo) {
    switch (tipo) {
      case 'success': return 'check-circle-fill';
      case 'error': return 'exclamation-triangle-fill';
      case 'warning': return 'exclamation-triangle-fill';
      default: return 'info-circle-fill';
    }
  }

  // --- Funcionalidad de Tema (Claro/Oscuro) ---
  function initTheme() {
    const icono = document.getElementById("iconoTema");
    if (localStorage.getItem("tema") === "oscuro") {
      document.body.classList.add("bg-dark", "text-light");
      if (icono) icono.textContent = "🌙";
    }
  }

  window.cambiarTema = function () {
    const body = document.body;
    const icono = document.getElementById("iconoTema");
    body.classList.toggle("bg-dark");
    body.classList.toggle("text-light");
    const esOscuro = body.classList.contains("bg-dark");
    if (icono) icono.textContent = esOscuro ? "🌙" : "☀️";
    localStorage.setItem("tema", esOscuro ? "oscuro" : "claro");
  };

  // Funcionalidad de seguimiento del pedido
  function configurarSeguimiento() {
    const numeroOrden = ultimoPedido.numero_orden;
    if (!numeroOrden) return;

    // Simular actualizaciones de estado (en un entorno real, esto vendría del backend)
    const seguimientoContainer = document.getElementById("seguimiento-pedido");
    if (seguimientoContainer) {
      seguimientoContainer.innerHTML = `
        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">
              <i class="bi bi-geo-alt me-2"></i>
              Seguimiento del Pedido
            </h5>
          </div>
          <div class="card-body">
            <div class="timeline">
              <div class="timeline-item active">
                <div class="timeline-icon bg-success">
                  <i class="bi bi-check"></i>
                </div>
                <div class="timeline-content">
                  <h6>Pedido Confirmado</h6>
                  <small class="text-muted">${new Date().toLocaleTimeString('es-PE')}</small>
                </div>
              </div>
              <div class="timeline-item">
                <div class="timeline-icon bg-secondary">
                  <i class="bi bi-clock"></i>
                </div>
                <div class="timeline-content">
                  <h6>En Preparación</h6>
                  <small class="text-muted">Pendiente...</small>
                </div>
              </div>
              <div class="timeline-item">
                <div class="timeline-icon bg-secondary">
                  <i class="bi bi-truck"></i>
                </div>
                <div class="timeline-content">
                  <h6>${ultimoPedido.entrega?.metodo === 'delivery' ? 'En Camino' : 'Listo para Recojo'}</h6>
                  <small class="text-muted">Pendiente...</small>
                </div>
              </div>
              <div class="timeline-item">
                <div class="timeline-icon bg-secondary">
                  <i class="bi bi-check-circle"></i>
                </div>
                <div class="timeline-content">
                  <h6>Entregado</h6>
                  <small class="text-muted">Pendiente...</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      `;
    }
  }

  // Botón para calificar la experiencia
  function configurarCalificacion() {
    const calificacionContainer = document.getElementById("calificacion-container");
    if (calificacionContainer) {
      calificacionContainer.innerHTML = `
        <div class="card mt-4">
          <div class="card-body text-center">
            <h5>¿Cómo estuvo tu experiencia?</h5>
            <p class="text-muted">Tu opinión nos ayuda a mejorar</p>
            <div class="rating-stars mb-3">
              ${Array.from({length: 5}, (_, i) => `
                <span class="star" data-rating="${i + 1}">
                  <i class="bi bi-star"></i>
                </span>
              `).join('')}
            </div>
            <button class="btn btn-outline-primary" onclick="abrirModalCalificacion()">
              Dejar Reseña
            </button>
          </div>
        </div>
      `;

      // Configurar interactividad de estrellas
      const stars = calificacionContainer.querySelectorAll('.star');
      stars.forEach(star => {
        star.addEventListener('click', () => {
          const rating = parseInt(star.dataset.rating);
          actualizarEstrellas(stars, rating);
        });

        star.addEventListener('mouseenter', () => {
          const rating = parseInt(star.dataset.rating);
          actualizarEstrellas(stars, rating, true);
        });
      });

      calificacionContainer.addEventListener('mouseleave', () => {
        actualizarEstrellas(stars, 0);
      });
    }
  }

  function actualizarEstrellas(stars, rating, hover = false) {
    stars.forEach((star, index) => {
      const icon = star.querySelector('i');
      if (index < rating) {
        icon.className = hover ? 'bi bi-star-fill text-warning' : 'bi bi-star-fill text-primary';
      } else {
        icon.className = 'bi bi-star';
      }
    });
  }

  function abrirModalCalificacion() {
    // Implementar modal de calificación
    mostrarNotificacion('Funcionalidad de calificación próximamente disponible', 'info');
  }

  // Limpiar datos del pedido después de un tiempo
  function programarLimpiezaDatos() {
    // Limpiar datos del último pedido después de 24 horas
    setTimeout(() => {
      localStorage.removeItem("ultimo_pedido");
    }, 24 * 60 * 60 * 1000); // 24 horas
  }

  // Inicialización
  function init() {
    initTheme();
    mostrarResumen();
    configurarSeguimiento();
    configurarCalificacion();
    programarLimpiezaDatos();
    
    // Agregar efecto de confeti si la librería está disponible
    if (typeof confetti === 'function') {
      confetti({
        particleCount: 100,
        spread: 70,
        origin: { y: 0.6 }
      });
    }

    console.log('Página de confirmación inicializada correctamente');
  }

  // Estilos adicionales para la página de confirmación
  const confirmacionStyles = document.createElement('style');
  confirmacionStyles.textContent = `
    @keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(30px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .item-card {
      transition: transform 0.2s ease, box-shadow 0.2s ease;
      border: none;
      border-radius: 15px;
    }

    .item-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 25px rgba(0,0,0,0.15);
    }

    .item-icon {
      background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(247, 147, 30, 0.1) 100%);
      border-radius: 50%;
      width: 80px;
      height: 80px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .item-details {
      background: #f8f9fa;
      border-radius: 10px;
      padding: 15px;
      margin-top: 15px;
    }

    .timeline {
      position: relative;
      padding-left: 30px;
    }

    .timeline::before {
      content: '';
      position: absolute;
      left: 15px;
      top: 0;
      bottom: 0;
      width: 2px;
      background: #e9ecef;
    }

    .timeline-item {
      position: relative;
      margin-bottom: 30px;
    }

    .timeline-icon {
      position: absolute;
      left: -22px;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 14px;
    }

    .timeline-item.active .timeline-icon {
      background: #28a745 !important;
    }

    .timeline-content h6 {
      margin: 0;
      font-weight: 600;
    }

    .rating-stars .star {
      cursor: pointer;
      font-size: 1.5rem;
      margin: 0 5px;
      transition: transform 0.2s ease;
    }

    .rating-stars .star:hover {
      transform: scale(1.2);
    }

    .btn-group-actions {
      gap: 10px;
    }

    .btn-group-actions .btn {
      border-radius: 25px;
      padding: 10px 20px;
      font-weight: 500;
      transition: all 0.3s ease;
    }

    .btn-group-actions .btn:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0,0,0,0.15);
    }

    .alert-info {
      border-left: 4px solid #17a2b8;
      background: linear-gradient(135deg, rgba(23, 162, 184, 0.1) 0%, rgba(23, 162, 184, 0.05) 100%);
    }

    .card-header {
      background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
      border-radius: 15px 15px 0 0 !important;
    }

    .confirmation-hero {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 40px 0;
      border-radius: 20px;
      margin-bottom: 30px;
      text-align: center;
    }

    .confirmation-hero h1 {
      font-size: 2.5rem;
      font-weight: bold;
      margin-bottom: 10px;
    }

    .confirmation-hero .lead {
      font-size: 1.2rem;
      opacity: 0.9;
    }

    .order-summary {
      background: white;
      border-radius: 15px;
      padding: 25px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      margin-bottom: 30px;
    }

    .total-section {
      background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
      color: white;
      border-radius: 15px;
      padding: 20px;
      text-align: center;
    }

    .total-section h3 {
      margin: 0;
      font-weight: bold;
    }

    @media (max-width: 768px) {
      .confirmation-hero h1 {
        font-size: 1.8rem;
      }
      
      .btn-group-actions {
        flex-direction: column;
      }
      
      .btn-group-actions .btn {
        width: 100%;
        margin-bottom: 10px;
      }
    }
  `;
  document.head.appendChild(confirmacionStyles);

  // Inicializar la página
  init();
});