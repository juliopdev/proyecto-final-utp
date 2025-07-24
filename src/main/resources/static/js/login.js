// login.js - Fixed Version para trabajar con PostgreSQL normalizado
document.addEventListener("DOMContentLoaded", () => {
  const loginPage = document.querySelector(".login-page");
  const API_URL = "https://hamburguer-xmx8.onrender.com/api";
  
  let currentTemplate = 'login'; // 'login' o 'register'

  function mostrarVista(templateId) {
    const template = document.getElementById(templateId + '-template');
    if (template) {
      loginPage.innerHTML = "";
      loginPage.appendChild(template.content.cloneNode(true));
      currentTemplate = templateId;
      attachFormListeners();
      configurarValidacionTiempoReal();
    } else {
      console.error(`Template ${templateId} no encontrado`);
    }
  }

  function mostrarError(formId, mensaje) {
    limpiarErrores(formId);
    
    const form = document.getElementById(formId);
    if (!form) return;

    let errorDiv = form.querySelector(".error-message");
    if (!errorDiv) {
      errorDiv = document.createElement("div");
      errorDiv.className = "error-message alert alert-danger";
      errorDiv.style.marginTop = "15px";
      form.appendChild(errorDiv);
    }
    
    errorDiv.innerHTML = `<i class="bi bi-exclamation-triangle me-2"></i>${mensaje}`;
    
    // Auto-ocultar después de 5 segundos
    setTimeout(() => {
      if (errorDiv.parentNode) {
        errorDiv.style.opacity = '0';
        setTimeout(() => {
          if (errorDiv.parentNode) {
            errorDiv.remove();
          }
        }, 300);
      }
    }, 5000);
  }

  function mostrarExito(formId, mensaje) {
    limpiarErrores(formId);
    
    const form = document.getElementById(formId);
    if (!form) return;

    const successDiv = document.createElement("div");
    successDiv.className = "success-message alert alert-success";
    successDiv.style.marginTop = "15px";
    successDiv.innerHTML = `<i class="bi bi-check-circle me-2"></i>${mensaje}`;
    
    form.appendChild(successDiv);
    
    setTimeout(() => {
      if (successDiv.parentNode) {
        successDiv.style.opacity = '0';
        setTimeout(() => {
          if (successDiv.parentNode) {
            successDiv.remove();
          }
        }, 300);
      }
    }, 3000);
  }

  function limpiarErrores(formId) {
    const form = document.getElementById(formId);
    if (!form) return;

    const errorMessages = form.querySelectorAll(".error-message, .success-message");
    errorMessages.forEach(msg => msg.remove());
    
    // Limpiar clases de validación
    const inputs = form.querySelectorAll('.form-control');
    inputs.forEach(input => {
      input.classList.remove('is-invalid', 'is-valid');
    });
  }

  async function validarInicioSesion(event) {
    event.preventDefault();
    
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    
    const email = document.getElementById("usuario").value.trim();
    const password = document.getElementById("contrasena").value.trim();

    // Validaciones del frontend
    if (!email || !password) {
      mostrarError("formulario-login", "Por favor, completa todos los campos.");
      return;
    }

    if (!validarEmail(email)) {
      mostrarError("formulario-login", "Por favor, ingresa un email válido.");
      return;
    }

    // Deshabilitar botón y mostrar carga
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Iniciando sesión...';

    try {
      const response = await fetch(`${API_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.msg || data.error || "Error en el servidor.");
      }

      if (data.success && data.token) {
        // Guardar token
        localStorage.setItem("token", data.token);

        // Obtener información completa del usuario
        try {
          const userResponse = await fetch(`${API_URL}/auth/me`, {
            headers: { 
              "Content-Type": "application/json",
              "Authorization": `Bearer ${data.token}` 
            },
          });
          
          const userData = await userResponse.json();
          
          if (userData.success && userData.user) {
            const userDataNormalized = {
              id_usuario: userData.user.id_usuario,
              nombre: userData.user.nombre,
              email: userData.user.email,
              telefono: userData.user.telefono,
              direccion: userData.user.direccion,
              rol: userData.user.rol, // Este viene del JOIN con la tabla Roles
              fecha_creacion: userData.user.fecha_creacion
            };
            
            localStorage.setItem("user", JSON.stringify(userDataNormalized));
            
            mostrarExito("formulario-login", "¡Inicio de sesión exitoso! Redirigiendo...");
            
            setTimeout(() => {
              // Redirigir según el rol
              if (userDataNormalized.rol === 'admin') {
                window.location.href = "/admin.html";
              } else {
                window.location.href = "/index.html";
              }
            }, 1500);
          } else {
            throw new Error("No se pudo obtener la información del usuario.");
          }
        } catch (userError) {
          console.error("Error al obtener datos del usuario:", userError);
        }
      } else {
        throw new Error(data.msg || "Credenciales inválidas.");
      }
    } catch (error) {
      console.error("Error en el inicio de sesión:", error.message);
      mostrarError("formulario-login", error.message);
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalText;
    }
  }

  async function validarRegistro(event) {
    event.preventDefault();

    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;

    const nombre = document.getElementById("nombreRegistro").value.trim();
    const apellidos = document.getElementById("apellidosRegistro").value.trim();
    const email = document.getElementById("emailRegistro").value.trim();
    const password = document.getElementById("passwordRegistro").value.trim();
    const passwordConfirm = document.getElementById("passwordConfirmRegistro")?.value.trim();
    const telefono = document.getElementById("telefonoRegistro")?.value.trim();
    const direccion = document.getElementById("direccionRegistro")?.value.trim();

    // Validaciones del frontend
    if (!nombre || !apellidos || !email || !password) {
      mostrarError("registroForm", "Nombre, apellidos, email y contraseña son obligatorios.");
      return;
    }

    if (!validarEmail(email)) {
      mostrarError("registroForm", "Por favor, ingresa un email válido.");
      return;
    }

    if (password.length < 6) {
      mostrarError("registroForm", "La contraseña debe tener al menos 6 caracteres.");
      return;
    }

    if (passwordConfirm && password !== passwordConfirm) {
      mostrarError("registroForm", "Las contraseñas no coinciden.");
      return;
    }

    if (telefono && !/^[0-9]{9}$/.test(telefono)) {
      mostrarError("registroForm", "El teléfono debe tener 9 dígitos numéricos.");
      return;
    }

    // Deshabilitar botón y mostrar carga
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Registrando...';

    try {
      const nombreCompleto = `${nombre} ${apellidos}`.trim();

      const requestBody = {
        nombre: nombreCompleto,
        email,
        password
      };
      
      if (telefono) requestBody.telefono = telefono;
      if (direccion) requestBody.direccion = direccion;

      const response = await fetch(`${API_URL}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.msg || data.error || "Error en el registro.");
      }

      if (data.success) {
        mostrarExito("registroForm", "¡Registro exitoso! Redirigiendo al login...");
        
        setTimeout(() => {
          mostrarVista("login");
          // Pre-llenar el email en el login
          setTimeout(() => {
            const emailInput = document.getElementById("usuario");
            if (emailInput) emailInput.value = email;
          }, 100);
        }, 2000);
      } else {
        throw new Error(data.msg || "Error en el registro.");
      }
    } catch (error) {
      console.error("Error en el registro:", error.message);
      mostrarError("registroForm", error.message);
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalText;
    }
  }

  function validarEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  function configurarValidacionTiempoReal() {
    // Validación para email
    const emailInputs = document.querySelectorAll('input[type="email"]');
    emailInputs.forEach(input => {
      input.addEventListener('blur', () => {
        if (input.value.trim() && !validarEmail(input.value.trim())) {
          input.classList.add('is-invalid');
          mostrarTooltipError(input, 'Email inválido');
        } else if (input.value.trim()) {
          input.classList.remove('is-invalid');
          input.classList.add('is-valid');
          ocultarTooltipError(input);
        }
      });

      input.addEventListener('input', () => {
        if (input.classList.contains('is-invalid') && validarEmail(input.value.trim())) {
          input.classList.remove('is-invalid');
          input.classList.add('is-valid');
          ocultarTooltipError(input);
        }
      });
    });

    // Validación para contraseña
    const passwordInputs = document.querySelectorAll('input[type="password"]');
    passwordInputs.forEach(input => {
      if (input.id === 'passwordRegistro') {
        input.addEventListener('blur', () => {
          if (input.value.length > 0 && input.value.length < 6) {
            input.classList.add('is-invalid');
            mostrarTooltipError(input, 'Mínimo 6 caracteres');
          } else if (input.value.length >= 6) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            ocultarTooltipError(input);
          }
        });
      }
    });

    // Validación para confirmación de contraseña
    const passwordConfirm = document.getElementById('passwordConfirmRegistro');
    if (passwordConfirm) {
      passwordConfirm.addEventListener('blur', () => {
        const password = document.getElementById('passwordRegistro');
        if (passwordConfirm.value && password && passwordConfirm.value !== password.value) {
          passwordConfirm.classList.add('is-invalid');
          mostrarTooltipError(passwordConfirm, 'Las contraseñas no coinciden');
        } else if (passwordConfirm.value && password && passwordConfirm.value === password.value) {
          passwordConfirm.classList.remove('is-invalid');
          passwordConfirm.classList.add('is-valid');
          ocultarTooltipError(passwordConfirm);
        }
      });
    }

    // Validación para teléfono
    const telefonoInput = document.getElementById('telefonoRegistro');
    if (telefonoInput) {
      telefonoInput.addEventListener('input', (e) => {
        // Solo permitir números
        e.target.value = e.target.value.replace(/\D/g, '').slice(0, 9);
      });

      telefonoInput.addEventListener('blur', () => {
        if (telefonoInput.value && telefonoInput.value.length !== 9) {
          telefonoInput.classList.add('is-invalid');
          mostrarTooltipError(telefonoInput, 'Debe tener 9 dígitos');
        } else if (telefonoInput.value.length === 9) {
          telefonoInput.classList.remove('is-invalid');
          telefonoInput.classList.add('is-valid');
          ocultarTooltipError(telefonoInput);
        }
      });
    }
  }

  function mostrarTooltipError(input, mensaje) {
    ocultarTooltipError(input); // Limpiar tooltip existente
    
    const tooltip = document.createElement('div');
    tooltip.className = 'invalid-tooltip';
    tooltip.textContent = mensaje;
    tooltip.style.cssText = `
      position: absolute;
      top: 100%;
      left: 0;
      background: #dc3545;
      color: white;
      padding: 5px 10px;
      border-radius: 4px;
      font-size: 12px;
      z-index: 1000;
      margin-top: 5px;
    `;
    
    input.parentElement.style.position = 'relative';
    input.parentElement.appendChild(tooltip);
  }

  function ocultarTooltipError(input) {
    const tooltip = input.parentElement.querySelector('.invalid-tooltip');
    if (tooltip) {
      tooltip.remove();
    }
  }

  function attachFormListeners() {
    const loginForm = document.getElementById("formulario-login");
    if (loginForm) {
      loginForm.addEventListener("submit", validarInicioSesion);
    }

    const registroForm = document.getElementById("registroForm");
    if (registroForm) {
      registroForm.addEventListener("submit", validarRegistro);
    }

    const mostrarRegistroBtn = document.getElementById("mostrar-registro");
    if (mostrarRegistroBtn) {
      mostrarRegistroBtn.addEventListener("click", (e) => {
        e.preventDefault();
        mostrarVista("registro");
      });
    }

    const mostrarLoginBtn = document.getElementById("mostrar-login");
    if (mostrarLoginBtn) {
      mostrarLoginBtn.addEventListener("click", (e) => {
        e.preventDefault();
        mostrarVista("login");
      });
    }

    // Configurar toggle para mostrar/ocultar contraseña
    configurarTogglePassword();
  }

  function configurarTogglePassword() {
    const toggleButtons = document.querySelectorAll('.toggle-password');
    toggleButtons.forEach(button => {
      button.addEventListener('click', () => {
        const input = button.previousElementSibling;
        const icon = button.querySelector('i');
        
        if (input.type === 'password') {
          input.type = 'text';
          icon.className = 'bi bi-eye-slash';
        } else {
          input.type = 'password';
          icon.className = 'bi bi-eye';
        }
      });
    });
  }

  // Verificar si ya hay una sesión activa
  function verificarSesionActiva() {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    
    if (token && user) {
      // Verificar si el token sigue siendo válido
      fetch(`${API_URL}/auth/verify`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          // Token válido, redirigir
          const userData = JSON.parse(user);
          if (userData.rol === 'admin') {
            window.location.href = '/admin.html';
          } else {
            window.location.href = '/index.html';
          }
        } else {
          // Token inválido, limpiar storage
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        }
      })
      .catch(error => {
        console.error('Error verificando token:', error);
        // En caso de error, limpiar storage por seguridad
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      });
    }
  }

  // Funcionalidad de "Recordar sesión"
  function configurarRecordarSesion() {
    const recordarCheckbox = document.getElementById('recordar-sesion');
    if (recordarCheckbox) {
      // Verificar si hay preferencia guardada
      const recordarPreferencia = localStorage.getItem('recordar-sesion');
      if (recordarPreferencia === 'true') {
        recordarCheckbox.checked = true;
        
        // Pre-llenar email si está guardado
        const emailGuardado = localStorage.getItem('email-guardado');
        if (emailGuardado) {
          const emailInput = document.getElementById('usuario');
          if (emailInput) emailInput.value = emailGuardado;
        }
      }

      recordarCheckbox.addEventListener('change', () => {
        localStorage.setItem('recordar-sesion', recordarCheckbox.checked);
        
        if (!recordarCheckbox.checked) {
          localStorage.removeItem('email-guardado');
        }
      });
    }
  }

  // Funcionalidad de recuperación de contraseña
  // function configurarRecuperacionPassword() {
  //   const recuperarLink = document.getElementById('recuperar-password');
  //   if (recuperarLink) {
  //     recuperarLink.addEventListener('click', (e) => {
  //       e.preventDefault();
  //       // mostrarModalRecuperacion();
  //     });
  //   }
  // }

  // function mostrarModalRecuperacion() {
  //   const modal = document.createElement('div');
  //   modal.className = 'modal fade';
  //   modal.innerHTML = `
  //     <div class="modal-dialog">
  //       <div class="modal-content">
  //         <div class="modal-header">
  //           <h5 class="modal-title">Recuperar Contraseña</h5>
  //           <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
  //         </div>
  //         <div class="modal-body">
  //           <form id="recuperar-form">
  //             <div class="mb-3">
  //               <label for="email-recuperacion" class="form-label">Email registrado</label>
  //               <input type="email" class="form-control" id="email-recuperacion" required>
  //               <div class="form-text">Te enviaremos un enlace para restablecer tu contraseña.</div>
  //             </div>
  //             <button type="submit" class="btn btn-primary w-100">Enviar Enlace</button>
  //           </form>
  //         </div>
  //       </div>
  //     </div>
  //   `;

  //   document.body.appendChild(modal);
  //   const bsModal = new bootstrap.Modal(modal);
  //   bsModal.show();

  //   // Configurar formulario de recuperación
  //   const form = modal.querySelector('#recuperar-form');
  //   form.addEventListener('submit', async (e) => {
  //     e.preventDefault();
      
  //     const email = document.getElementById('email-recuperacion').value.trim();
  //     const submitBtn = form.querySelector('button[type="submit"]');
  //     const originalText = submitBtn.textContent;
      
  //     submitBtn.disabled = true;
  //     submitBtn.textContent = 'Enviando...';

  //     try {
  //       const response = await fetch(`${API_URL}/auth/forgot-password`, {
  //         method: 'POST',
  //         headers: { 'Content-Type': 'application/json' },
  //         body: JSON.stringify({ email })
  //       });

  //       const data = await response.json();

  //       if (data.success) {
  //         modal.querySelector('.modal-body').innerHTML = `
  //           <div class="alert alert-success">
  //             <i class="bi bi-check-circle me-2"></i>
  //             Se ha enviado un enlace de recuperación a tu email.
  //           </div>
  //         `;
  //         setTimeout(() => bsModal.hide(), 3000);
  //       } else {
  //         throw new Error(data.msg || 'Error al enviar el enlace');
  //       }
  //     } catch (error) {
  //       modal.querySelector('.modal-body').innerHTML += `
  //         <div class="alert alert-danger mt-3">
  //           <i class="bi bi-exclamation-triangle me-2"></i>
  //           ${error.message}
  //         </div>
  //       `;
  //     } finally {
  //       submitBtn.disabled = false;
  //       submitBtn.textContent = originalText;
  //     }
  //   });

  //   modal.addEventListener('hidden.bs.modal', () => {
  //     modal.remove();
  //   });
  // }

  // Demo rápido para testing
  function configurarDemoRapido() {
    const demoBtn = document.getElementById('demo-rapido');
    if (demoBtn) {
      demoBtn.addEventListener('click', () => {
        const emailInput = document.getElementById('usuario');
        const passwordInput = document.getElementById('contrasena');
        
        if (emailInput && passwordInput) {
          emailInput.value = 'test@test.com';
          passwordInput.value = 'test123';
          
          // Simular submit del formulario
          const form = document.getElementById('formulario-login');
          if (form) {
            form.dispatchEvent(new Event('submit'));
          }
        }
      });
    }
  }

  // Tema oscuro/claro
  function initTheme() {
    const icono = document.getElementById("iconoTema");
    if (localStorage.getItem("tema") === "oscuro") {
      document.body.classList.add("bg-dark", "text-light");
      if (icono) icono.textContent = "🌙";
    }
  }

  window.cambiarTema = function() {
    const body = document.body;
    const icono = document.getElementById("iconoTema");
    body.classList.toggle("bg-dark");
    body.classList.toggle("text-light");
    const esOscuro = body.classList.contains("bg-dark");
    if (icono) icono.textContent = esOscuro ? "🌙" : "☀️";
    localStorage.setItem("tema", esOscuro ? "oscuro" : "claro");
  };

  // Inicialización
  function init() {
    initTheme();
    verificarSesionActiva();
    mostrarVista("login");
    
    setTimeout(() => {
      configurarRecordarSesion();
      // configurarRecuperacionPassword();
      configurarDemoRapido();
    }, 100);

    console.log('Página de login inicializada correctamente');
  }

  // Estilos adicionales para mejorar la UX
  const loginStyles = document.createElement('style');
  loginStyles.textContent = `
    .login-page {
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }

    .login-container {
      background: white;
      border-radius: 20px;
      box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
      overflow: hidden;
      max-width: 400px;
      width: 100%;
      animation: slideIn 0.6s ease-out;
    }

    @keyframes slideIn {
      from {
        opacity: 0;
        transform: translateY(30px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .login-header {
      background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
      color: white;
      padding: 30px;
      text-align: center;
    }

    .login-header h2 {
      margin: 0;
      font-weight: bold;
    }

    .login-header p {
      margin: 10px 0 0 0;
      opacity: 0.9;
    }

    .login-body {
      padding: 30px;
    }

    .form-control {
      border: 2px solid #e9ecef;
      border-radius: 10px;
      padding: 12px 15px;
      transition: all 0.3s ease;
    }

    .form-control:focus {
      border-color: #ff6b35;
      box-shadow: 0 0 0 0.2rem rgba(255, 107, 53, 0.25);
    }

    .form-control.is-valid {
      border-color: #28a745;
    }

    .form-control.is-invalid {
      border-color: #dc3545;
    }

    .input-group {
      position: relative;
    }

    .toggle-password {
      position: absolute;
      right: 10px;
      top: 50%;
      transform: translateY(-50%);
      background: none;
      border: none;
      color: #6c757d;
      cursor: pointer;
      z-index: 10;
    }

    .toggle-password:hover {
      color: #ff6b35;
    }

    .btn-primary {
      background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
      border: none;
      border-radius: 10px;
      padding: 12px;
      font-weight: 600;
      transition: all 0.3s ease;
    }

    .btn-primary:hover {
      background: linear-gradient(135deg, #e55a2b 0%, #e0841a 100%);
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(255, 107, 53, 0.4);
    }

    .btn-primary:disabled {
      background: #6c757d;
      transform: none;
      box-shadow: none;
    }

    .btn-outline-secondary {
      border: 2px solid #e9ecef;
      color: #6c757d;
      border-radius: 10px;
      padding: 10px;
      transition: all 0.3s ease;
    }

    .btn-outline-secondary:hover {
      background: #ff6b35;
      border-color: #ff6b35;
      color: white;
    }

    .form-check-input:checked {
      background-color: #ff6b35;
      border-color: #ff6b35;
    }

    .error-message, .success-message {
      border-radius: 10px;
      animation: fadeIn 0.3s ease-out;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(-10px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .login-footer {
      background: #f8f9fa;
      padding: 20px 30px;
      text-align: center;
      border-top: 1px solid #e9ecef;
    }

    .login-footer a {
      color: #ff6b35;
      text-decoration: none;
      font-weight: 500;
    }

    .login-footer a:hover {
      text-decoration: underline;
    }

    .demo-section {
      background: #e3f2fd;
      border: 1px solid #bbdefb;
      border-radius: 10px;
      padding: 15px;
      margin-bottom: 20px;
      text-align: center;
    }

    .demo-section small {
      color: #1976d2;
      font-weight: 500;
    }

    @media (max-width: 480px) {
      .login-page {
        padding: 10px;
      }
      
      .login-header, .login-body, .login-footer {
        padding: 20px;
      }
    }

    /* Tema oscuro */
    .bg-dark .login-container {
      background: #2d3748;
      color: white;
    }

    .bg-dark .form-control {
      background: #4a5568;
      border-color: #4a5568;
      color: white;
    }

    .bg-dark .form-control:focus {
      background: #4a5568;
      border-color: #ff6b35;
      color: white;
    }

    .bg-dark .login-footer {
      background: #1a202c;
      border-color: #4a5568;
    }

    .bg-dark .demo-section {
      background: #2d3748;
      border-color: #4a5568;
    }
  `;
  document.head.appendChild(loginStyles);

  // Inicializar
  init();
});