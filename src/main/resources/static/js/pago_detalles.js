// pago_detalles.js - Fixed Version para trabajar con PostgreSQL normalizado
document.addEventListener("DOMContentLoaded", () => {
  const API_URL = "https://hamburguer-xmx8.onrender.com/api";

  // Elementos del DOM
  const subtotalElement = document.getElementById("subtotal");
  const deliveryCostElement = document.getElementById("delivery-cost");
  const totalElement = document.getElementById("total");
  const btnProcesar = document.getElementById("btn-procesar");

  // Opciones de comprobante
  const boletaRadio = document.getElementById("boleta");
  const facturaRadio = document.getElementById("factura");
  const dniContainer = document.getElementById("dni-container");
  const rucContainer = document.getElementById("ruc-container");

  // Opciones de entrega
  const deliveryRadio = document.getElementById("delivery");
  const recogerRadio = document.getElementById("recoger");
  const direccionContainer = document.getElementById("direccion-container");
  const referenciaContainer = document.getElementById("referencia-container");

  // Opciones de pago
  const tarjetaRadio = document.getElementById("tarjeta");
  const yapeRadio = document.getElementById("yape");
  const tarjetaContainer = document.getElementById("tarjeta-container");
  const fechaVencimientoContainer = document.getElementById("fecha-vencimiento-container");
  const titularContainer = document.getElementById("titular-container");
  const cvvContainer = document.getElementById("cvv-container");
  const yapeNumeroContainer = document.getElementById("yape-numero-container");
  const yapeCodigoContainer = document.getElementById("yape-codigo-container");

  // Barra de progreso
  const progressBar = document.getElementById("progress-bar");

  // Campos de entrada
  const dniInput = document.getElementById("dni");
  const rucInput = document.getElementById("ruc");
  const numeroTarjetaInput = document.getElementById("numero-tarjeta");
  const fechaVencimientoInput = document.getElementById("fecha-vencimiento");
  const telefonoInput = document.getElementById("telefono");

  // Variables de estado
  let carrito = JSON.parse(localStorage.getItem("carrito")) || [];
  let currentStep = 1;
  let deliveryCost = 5.00;
  let metodosEntrega = [];
  let metodosPago = [];
  let tiposComprobante = [];

  // Cargar datos de configuración del backend
  async function cargarConfiguracion() {
    try {
      const token = localStorage.getItem("token");
      const headers = {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      };

      // Cargar métodos de entrega, pago y tipos de comprobante
      const [entregaRes, pagoRes, comprobanteRes] = await Promise.all([
        fetch(`${API_URL}/config/metodos-entrega`, { headers }),
        fetch(`${API_URL}/config/metodos-pago`, { headers }),
        fetch(`${API_URL}/config/tipos-comprobante`, { headers })
      ]);

      if (entregaRes.ok) {
        const entregaData = await entregaRes.json();
        metodosEntrega = entregaData.data || [
          { id_metodo_entrega: 'delivery', nombre_metodo: 'delivery' },
          { id_metodo_entrega: 'recoger', nombre_metodo: 'recoger' }
        ];
      }

      if (pagoRes.ok) {
        const pagoData = await pagoRes.json();
        metodosPago = pagoData.data || [
          { id_metodo_pago: 'tarjeta', nombre_metodo: 'tarjeta' },
          { id_metodo_pago: 'yape', nombre_metodo: 'yape' }
        ];
      }

      if (comprobanteRes.ok) {
        const comprobanteData = await comprobanteRes.json();
        tiposComprobante = comprobanteData.data || [
          { id_comprobante: 'boleta', tipo_comprobante: 'boleta' },
          { id_comprobante: 'factura', tipo_comprobante: 'factura' }
        ];
      }
    } catch (error) {
      console.warn('Error al cargar configuración, usando valores por defecto:', error);
    }
  }

  async function fetchUserData() {
    const token = localStorage.getItem("token");
    const userData = JSON.parse(localStorage.getItem("user"));

    // Hacer campos editables
    const campos = ['nombre', 'apellido', 'correo', 'telefono'];
    campos.forEach(campo => {
      const element = document.getElementById(campo);
      if (element) element.readOnly = false;
    });

    // Precargar desde localStorage si existe
    if (userData) {
      const nombres = userData.nombre.split(" ");
      document.getElementById("nombre").value = nombres[0] || "";
      document.getElementById("apellido").value = nombres.slice(1).join(" ") || "";
      document.getElementById("correo").value = userData.email || "";
      document.getElementById("telefono").value = userData.telefono || "";
      return;
    }

    // Si no hay datos en localStorage, obtener de la API
    if (token) {
      try {
        const response = await fetch(`${API_URL}/auth/me`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        });

        const data = await response.json();
        
        if (data.success && data.user) {
          const nombres = (data.user.nombre || "").split(" ");
          document.getElementById("nombre").value = nombres[0] || "";
          document.getElementById("apellido").value = nombres.slice(1).join(" ") || "";
          document.getElementById("correo").value = data.user.email || "";
          document.getElementById("telefono").value = data.user.telefono || "";
        }
      } catch (error) {
        console.error("Error fetching user data:", error);
      }
    }
  }

  function updateTotal() {
    const subtotal = carrito.reduce((sum, item) => sum + item.precio * item.cantidad, 0);
    const costoDelivery = deliveryRadio.checked ? deliveryCost : 0;
    const total = subtotal + costoDelivery;

    subtotalElement.textContent = subtotal.toFixed(2);
    deliveryCostElement.textContent = costoDelivery.toFixed(2);
    totalElement.textContent = total.toFixed(2);
  }

  function toggleComprobanteFields() {
    if (boletaRadio.checked) {
      dniContainer.classList.remove("d-none");
      rucContainer.classList.add("d-none");
      dniInput.required = true;
      rucInput.required = false;
    } else {
      dniContainer.classList.add("d-none");
      rucContainer.classList.remove("d-none");
      dniInput.required = false;
      rucInput.required = true;
    }
  }

  function toggleEntregaFields() {
    if (deliveryRadio.checked) {
      direccionContainer.classList.remove("d-none");
      referenciaContainer.classList.remove("d-none");
      document.getElementById("direccion").required = true;
      document.getElementById("referencia").required = true;
    } else {
      direccionContainer.classList.add("d-none");
      referenciaContainer.classList.add("d-none");
      document.getElementById("direccion").required = false;
      document.getElementById("referencia").required = false;
    }
    updateTotal();
  }

  function togglePagoFields() {
    if (tarjetaRadio.checked) {
      // Mostrar campos de tarjeta
      [tarjetaContainer, fechaVencimientoContainer, titularContainer, cvvContainer].forEach(
        container => container.classList.remove("d-none")
      );
      // Ocultar campos de Yape
      [yapeNumeroContainer, yapeCodigoContainer].forEach(
        container => container.classList.add("d-none")
      );
      
      // Configurar requerimientos
      numeroTarjetaInput.required = true;
      fechaVencimientoInput.required = true;
      document.getElementById("titular").required = true;
      document.getElementById("cvv").required = true;
      document.getElementById("yape-numero").required = false;
      document.getElementById("yape-codigo").required = false;
    } else {
      // Ocultar campos de tarjeta
      [tarjetaContainer, fechaVencimientoContainer, titularContainer, cvvContainer].forEach(
        container => container.classList.add("d-none")
      );
      // Mostrar campos de Yape
      [yapeNumeroContainer, yapeCodigoContainer].forEach(
        container => container.classList.remove("d-none")
      );
      
      // Configurar requerimientos
      numeroTarjetaInput.required = false;
      fechaVencimientoInput.required = false;
      document.getElementById("titular").required = false;
      document.getElementById("cvv").required = false;
      document.getElementById("yape-numero").required = true;
      document.getElementById("yape-codigo").required = true;
    }
  }

  function validateStep(step) {
    const inputs = document.querySelectorAll(`#step-${step} input[required]`);
    let isValid = true;

    for (let input of inputs) {
      if (!input.value.trim()) {
        input.classList.add("is-invalid");
        isValid = false;
      } else {
        input.classList.remove("is-invalid");
      }

      // Validaciones específicas
      if (input.id === "dni" && input.value && !/^[0-9]{8}$/.test(input.value)) {
        input.classList.add("is-invalid");
        input.setCustomValidity("DNI debe tener 8 dígitos numéricos");
        isValid = false;
      } else if (input.id === "ruc" && input.value && !/^[0-9]{11}$/.test(input.value)) {
        input.classList.add("is-invalid");
        input.setCustomValidity("RUC debe tener 11 dígitos numéricos");
        isValid = false;
      } else if (input.id === "numero-tarjeta" && input.value && !/^[0-9]{16}$/.test(input.value)) {
        input.classList.add("is-invalid");
        input.setCustomValidity("Número de tarjeta debe tener 16 dígitos numéricos");
        isValid = false;
      } else if (input.id === "telefono" && input.value && !/^[0-9]{9}$/.test(input.value)) {
        input.classList.add("is-invalid");
        input.setCustomValidity("Teléfono debe tener 9 dígitos numéricos");
        isValid = false;
      } else if (input.id === "fecha-vencimiento" && input.value) {
        const match = input.value.match(/^(0[1-9]|1[0-2])\/([0-9]{2})$/);
        if (!match) {
          input.classList.add("is-invalid");
          input.setCustomValidity("Fecha de vencimiento debe ser MM/YY (ej. 07/25)");
          isValid = false;
        } else {
          const [_, month, year] = match;
          const inputDate = new Date(`20${year}-${month}-01`);
          const currentDate = new Date();
          if (inputDate < currentDate) {
            input.classList.add("is-invalid");
            input.setCustomValidity("Fecha de vencimiento debe ser posterior a la fecha actual");
            isValid = false;
          } else {
            input.classList.remove("is-invalid");
            input.setCustomValidity("");
          }
        }
      } else {
        input.classList.remove("is-invalid");
        input.setCustomValidity("");
      }
    }
    return isValid;
  }

  window.nextStep = function (step) {
    if (validateStep(step)) {
      document.getElementById(`step-${step}`).classList.remove("active");
      document.getElementById(`step-${step}`).classList.add("d-none");
      currentStep++;
      document.getElementById(`step-${currentStep}`).classList.remove("d-none");
      document.getElementById(`step-${currentStep}`).classList.add("active");
      progressBar.style.width = `${(currentStep / 3) * 100}%`;
      progressBar.textContent = `Paso ${currentStep} de 3`;
    } else {
      showNotification("Por favor, completa todos los campos requeridos correctamente.", 'error');
    }
  };

  window.prevStep = function (step) {
    document.getElementById(`step-${step}`).classList.remove("active");
    document.getElementById(`step-${step}`).classList.add("d-none");
    currentStep--;
    document.getElementById(`step-${currentStep}`).classList.remove("d-none");
    document.getElementById(`step-${currentStep}`).classList.add("active");
    progressBar.style.width = `${(currentStep / 3) * 100}%`;
    progressBar.textContent = `Paso ${currentStep} de 3`;
  };

  async function procesarPago() {
    if (!validateStep(3)) {
      showNotification("Por favor, completa todos los campos requeridos correctamente.", 'error');
      return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      showNotification("Necesitas iniciar sesión para confirmar tu pedido.", 'error');
      setTimeout(() => {
        window.location.href = "login.html";
      }, 2000);
      return;
    }

    if (carrito.length === 0) {
      showNotification("Tu carrito está vacío.", 'error');
      return;
    }

    // Preparar datos de la orden según la nueva estructura
    const orderData = {
      // Datos del cliente
      cliente_nombres: document.getElementById("nombre").value,
      cliente_apellidos: document.getElementById("apellido").value,
      cliente_email: document.getElementById("correo").value,
      cliente_telefono: document.getElementById("telefono").value,
      cliente_dni: boletaRadio.checked ? dniInput.value : null,
      cliente_ruc: facturaRadio.checked ? rucInput.value : null,

      // Tipo de comprobante
      tipo_comprobante: boletaRadio.checked ? "boleta" : "factura",

      // Datos de entrega
      metodo_entrega: deliveryRadio.checked ? "delivery" : "recoger",
      entrega_direccion: deliveryRadio.checked ? document.getElementById("direccion").value : null,
      entrega_referencia: deliveryRadio.checked ? document.getElementById("referencia").value : null,

      // Datos de pago
      metodo_pago: tarjetaRadio.checked ? "tarjeta" : "yape",
      pago_detalles: tarjetaRadio.checked ? {
        numero_tarjeta_masked: "**** **** **** " + numeroTarjetaInput.value.slice(-4),
        fecha_vencimiento: fechaVencimientoInput.value,
        titular: document.getElementById("titular").value,
        // CVV no se almacena por seguridad
      } : {
        yape_numero: document.getElementById("yape-numero").value,
        codigo_confirmacion: document.getElementById("yape-codigo").value
      },

      // Items del pedido
      items: carrito.map((item) => ({
        id_producto: item._id,
        nombre_producto: item.nombre,
        cantidad: item.cantidad,
        precio_unitario: item.precio,
        subtotal: item.precio * item.cantidad,
      })),

      // Totales
      subtotal: carrito.reduce((sum, item) => sum + item.precio * item.cantidad, 0),
      costo_delivery: deliveryRadio.checked ? deliveryCost : 0,
      total: carrito.reduce((sum, item) => sum + item.precio * item.cantidad, 0) + 
             (deliveryRadio.checked ? deliveryCost : 0),
    };

    btnProcesar.disabled = true;
    btnProcesar.innerHTML = '<i class="bi bi-hourglass-split"></i> Procesando...';

    try {
      const response = await fetch(`${API_URL}/orders`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(orderData),
      });

      const data = await response.json();

      if (data.success) {
        // Guardar detalles del pedido para la página de confirmación
        const pedidoConfirmacion = {
          numero_orden: data.orden.numero_orden,
          cliente: {
            nombres: orderData.cliente_nombres,
            apellidos: orderData.cliente_apellidos,
            email: orderData.cliente_email,
            telefono: orderData.cliente_telefono,
            comprobante: orderData.tipo_comprobante,
            dni: orderData.cliente_dni,
            ruc: orderData.cliente_ruc
          },
          entrega: {
            metodo: orderData.metodo_entrega,
            direccion: orderData.entrega_direccion,
            referencia: orderData.entrega_referencia
          },
          items: orderData.items,
          subtotal: orderData.subtotal,
          deliveryCost: orderData.costo_delivery,
          total: orderData.total,
          fecha_pedido: new Date().toISOString(),
          estado: 'Pendiente'
        };

        localStorage.setItem("ultimo_pedido", JSON.stringify(pedidoConfirmacion));
        localStorage.removeItem("carrito");
        
        showNotification("¡Pedido procesado exitosamente!", 'success');
        
        setTimeout(() => {
          window.location.href = "confirmacion.html";
        }, 1500);
      } else {
        throw new Error(data.msg || 'Error al procesar el pedido');
      }
    } catch (error) {
      console.error("Error al procesar pago:", error);
      showNotification("Hubo un error al procesar tu pedido: " + error.message, 'error');
      btnProcesar.disabled = false;
      btnProcesar.innerHTML = '<i class="bi bi-credit-card"></i> Procesar Pago';
    }
  }

  // Función para mostrar notificaciones
  function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type === 'error' ? 'danger' : type} alert-dismissible fade show position-fixed`;
    notification.style.cssText = `
      top: 20px;
      right: 20px;
      z-index: 9999;
      max-width: 350px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    `;
    notification.innerHTML = `
      <i class="bi bi-${getNotificationIcon(type)} me-2"></i>
      ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    document.body.appendChild(notification);

    // Auto-remove after 5 seconds
    setTimeout(() => {
      if (notification.parentNode) {
        notification.remove();
      }
    }, 5000);
  }

  function getNotificationIcon(type) {
    switch (type) {
      case 'success': return 'check-circle-fill';
      case 'error': return 'exclamation-triangle-fill';
      case 'warning': return 'exclamation-triangle-fill';
      default: return 'info-circle-fill';
    }
  }

  // Validaciones en tiempo real
  function setupRealTimeValidation() {
    dniInput.addEventListener("input", () => {
      if (dniInput.value && !/^[0-9]{8}$/.test(dniInput.value)) {
        dniInput.classList.add("is-invalid");
        dniInput.setCustomValidity("DNI debe tener 8 dígitos numéricos");
      } else {
        dniInput.classList.remove("is-invalid");
        dniInput.setCustomValidity("");
      }
    });

    rucInput.addEventListener("input", () => {
      if (rucInput.value && !/^[0-9]{11}$/.test(rucInput.value)) {
        rucInput.classList.add("is-invalid");
        rucInput.setCustomValidity("RUC debe tener 11 dígitos numéricos");
      } else {
        rucInput.classList.remove("is-invalid");
        rucInput.setCustomValidity("");
      }
    });

    numeroTarjetaInput.addEventListener("input", (e) => {
      // Formatear número de tarjeta
      let value = e.target.value.replace(/\D/g, '');
      value = value.replace(/(\d{4})(?=\d)/g, '$1 ');
      e.target.value = value;

      const cleanValue = value.replace(/\s/g, '');
      if (cleanValue && cleanValue.length !== 16) {
        numeroTarjetaInput.classList.add("is-invalid");
        numeroTarjetaInput.setCustomValidity("Número de tarjeta debe tener 16 dígitos");
      } else {
        numeroTarjetaInput.classList.remove("is-invalid");
        numeroTarjetaInput.setCustomValidity("");
      }
    });

    telefonoInput.addEventListener("input", () => {
      if (telefonoInput.value && !/^[0-9]{9}$/.test(telefonoInput.value)) {
        telefonoInput.classList.add("is-invalid");
        telefonoInput.setCustomValidity("Teléfono debe tener 9 dígitos numéricos");
      } else {
        telefonoInput.classList.remove("is-invalid");
        telefonoInput.setCustomValidity("");
      }
    });

    fechaVencimientoInput.addEventListener("input", (e) => {
      let value = e.target.value.replace(/[^0-9]/g, "");
      if (value.length >= 2) {
        const month = value.slice(0, 2);
        if (parseInt(month) >= 1 && parseInt(month) <= 12) {
          value = `${month}/${value.slice(2)}`;
          e.target.value = value;
        } else {
          fechaVencimientoInput.classList.add("is-invalid");
          fechaVencimientoInput.setCustomValidity("Mes debe ser entre 01 y 12");
          return;
        }
      } else {
        e.target.value = value;
      }

      const match = value.match(/^(0[1-9]|1[0-2])\/([0-9]{2})$/);
      if (!match && value.length >= 5) {
        fechaVencimientoInput.classList.add("is-invalid");
        fechaVencimientoInput.setCustomValidity("Fecha de vencimiento debe ser MM/YY");
      } else if (match) {
        const [_, month, year] = match;
        const inputDate = new Date(`20${year}-${month}-01`);
        const currentDate = new Date();
        if (inputDate < currentDate) {
          fechaVencimientoInput.classList.add("is-invalid");
          fechaVencimientoInput.setCustomValidity("Fecha de vencimiento debe ser posterior a la fecha actual");
        } else {
          fechaVencimientoInput.classList.remove("is-invalid");
          fechaVencimientoInput.setCustomValidity("");
        }
      } else {
        fechaVencimientoInput.classList.remove("is-invalid");
        fechaVencimientoInput.setCustomValidity("");
      }
    });

    // Validación de CVV
    const cvvInput = document.getElementById("cvv");
    cvvInput.addEventListener("input", (e) => {
      e.target.value = e.target.value.replace(/\D/g, '').slice(0, 4);
      
      if (e.target.value && (e.target.value.length < 3 || e.target.value.length > 4)) {
        cvvInput.classList.add("is-invalid");
        cvvInput.setCustomValidity("CVV debe tener 3 o 4 dígitos");
      } else {
        cvvInput.classList.remove("is-invalid");
        cvvInput.setCustomValidity("");
      }
    });

    // Validación de código Yape
    const yapeCodigoInput = document.getElementById("yape-codigo");
    yapeCodigoInput.addEventListener("input", (e) => {
      e.target.value = e.target.value.replace(/\D/g, '').slice(0, 6);
      
      if (e.target.value && e.target.value.length !== 6) {
        yapeCodigoInput.classList.add("is-invalid");
        yapeCodigoInput.setCustomValidity("Código de confirmación debe tener 6 dígitos");
      } else {
        yapeCodigoInput.classList.remove("is-invalid");
        yapeCodigoInput.setCustomValidity("");
      }
    });
  }

  // Event listeners para toggles
  function setupEventListeners() {
    boletaRadio.addEventListener("change", toggleComprobanteFields);
    facturaRadio.addEventListener("change", toggleComprobanteFields);
    deliveryRadio.addEventListener("change", toggleEntregaFields);
    recogerRadio.addEventListener("change", toggleEntregaFields);
    tarjetaRadio.addEventListener("change", togglePagoFields);
    yapeRadio.addEventListener("change", togglePagoFields);
    btnProcesar.addEventListener("click", procesarPago);
  }

  // Verificar que el carrito no esté vacío
  function verificarCarrito() {
    if (carrito.length === 0) {
      showNotification("Tu carrito está vacío. Serás redirigido al menú.", 'warning');
      setTimeout(() => {
        window.location.href = "menu-completo.html";
      }, 3000);
      return false;
    }
    return true;
  }

  // Mostrar resumen del carrito
  function mostrarResumenCarrito() {
    const resumenContainer = document.getElementById("resumen-carrito");
    if (resumenContainer) {
      resumenContainer.innerHTML = "";
      
      carrito.forEach(item => {
        const itemElement = document.createElement("div");
        itemElement.className = "d-flex justify-content-between align-items-center mb-2 p-2 border rounded";
        itemElement.innerHTML = `
          <div class="d-flex align-items-center">
            <img src="${item.imagen}" alt="${item.nombre}" 
                 style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px;" 
                 class="me-2">
            <div>
              <div class="fw-bold">${item.nombre}</div>
              <small class="text-muted">Cantidad: ${item.cantidad}</small>
            </div>
          </div>
          <div class="fw-bold">S/ ${(item.precio * item.cantidad).toFixed(2)}</div>
        `;
        resumenContainer.appendChild(itemElement);
      });
    }
  }

  // Tema
  function initTheme() {
    const icono = document.getElementById('iconoTema');
    if (localStorage.getItem('tema') === 'oscuro') {
      document.body.classList.add('bg-dark', 'text-light');
      if (icono) icono.textContent = '🌙';
    }
  }

  window.cambiarTema = function() {
    const body = document.body;
    const icono = document.getElementById('iconoTema');
    body.classList.toggle('bg-dark');
    body.classList.toggle('text-light');
    const esOscuro = body.classList.contains('bg-dark');
    if (icono) icono.textContent = esOscuro ? '🌙' : '☀️';
    localStorage.setItem('tema', esOscuro ? 'oscuro' : 'claro');
  };

  // Inicialización
  async function init() {
    if (!verificarCarrito()) return;
    
    initTheme();
    await cargarConfiguracion();
    await fetchUserData();
    updateTotal();
    toggleComprobanteFields();
    toggleEntregaFields();
    togglePagoFields();
    setupEventListeners();
    setupRealTimeValidation();
    mostrarResumenCarrito();
    
    console.log('Página de pago inicializada correctamente');
  }

  // Estilos adicionales
  const pagoStyles = document.createElement('style');
  pagoStyles.textContent = `
    .step-container {
      min-height: 400px;
    }
    
    .progress {
      height: 8px;
      border-radius: 4px;
    }
    
    .form-control.is-invalid {
      border-color: #dc3545;
      animation: shake 0.5s ease-in-out;
    }
    
    @keyframes shake {
      0%, 100% { transform: translateX(0); }
      25% { transform: translateX(-5px); }
      75% { transform: translateX(5px); }
    }
    
    .btn-primary {
      background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
      border: none;
    }
    
    .btn-primary:hover {
      background: linear-gradient(135deg, #e55a2b 0%, #e0841a 100%);
      transform: translateY(-1px);
    }
    
    .payment-option {
      transition: all 0.3s ease;
      border: 2px solid transparent;
      border-radius: 8px;
      padding: 15px;
    }
    
    .payment-option:hover {
      border-color: #ff6b35;
      background-color: rgba(255, 107, 53, 0.05);
    }
    
    .form-check-input:checked {
      background-color: #ff6b35;
      border-color: #ff6b35;
    }
    
    .card {
      border: none;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      border-radius: 15px;
    }
    
    .card-header {
      background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
      color: white;
      border-radius: 15px 15px 0 0 !important;
    }
    
    .step-indicator {
      background: #f8f9fa;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
    }
    
    .step-indicator.active {
      background: #ff6b35;
      color: white;
    }
    
    .step-indicator.completed {
      background: #28a745;
      color: white;
    }
  `;
  document.head.appendChild(pagoStyles);

  // Inicializar cuando el DOM esté listo
  init();
});