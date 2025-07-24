// menu.js - Fixed Version para trabajar con PostgreSQL normalizado
document.addEventListener('DOMContentLoaded', async () => {
    // Contenedores principales del DOM
    const categoriasContainer = document.getElementById("categorias-container");
    const productosContainer = document.getElementById("productos-container");
    const API_URL = 'https://hamburguer-xmx8.onrender.com/api';

    let categories = [];
    let products = [];

    // Inicializa el menú: carga categorías y productos, y configura el swiper
    async function initMenu() {
        try {
            const token = localStorage.getItem('token');
            const headers = {
                'Content-Type': 'application/json',
                ...(token && { 'Authorization': `Bearer ${token}` })
            };

            // Cargar categorías
            const catResponse = await fetch(`${API_URL}/categories`, { headers });
            const catData = await catResponse.json();
            
            if (catData.success && catData.data) {
                categories = catData.data.map(cat => ({
                    _id: cat.id_categoria,
                    nombre: cat.nombre,
                    icono: cat.icono,
                    descripcion: cat.descripcion,
                    activo: cat.activo
                }));
                renderCategoriasSwiper();
                initSwiperCategorias();
            } else {
                throw new Error('Error al cargar categorías');
            }

            // Cargar productos iniciales (todos)
            await renderProductos('todo', document.querySelector("#categorias-container button.active"));

        } catch (error) {
            console.error("Error al cargar el menú:", error.message);
            categoriasContainer.innerHTML = "<p>Error al cargar el menú. Intenta recargar la página.</p>";
        }
    }

    // Renderiza los productos según la categoría seleccionada
    async function renderProductos(categoryId, btn) {
        const allBtns = document.querySelectorAll("#categorias-container .swiper-slide button");
        allBtns.forEach(b => b.classList.remove("active"));
        if (btn) btn.classList.add("active");

        productosContainer.innerHTML = "<h4>Cargando productos...</h4>";

        try {
            const token = localStorage.getItem('token');
            const headers = {
                'Content-Type': 'application/json',
                ...(token && { 'Authorization': `Bearer ${token}` })
            };

            let url = `${API_URL}/products`;
            if (categoryId !== 'todo') {
                url = `${API_URL}/products?categoria=${categoryId}`;
            }
            
            const response = await fetch(url, { headers });
            const prodData = await response.json();

            if (prodData.success && prodData.data) {
                // Normalizar productos para compatibilidad con el frontend
                products = prodData.data.map(producto => ({
                    _id: producto.id_producto,
                    nombre: producto.nombre,
                    descripcion: producto.descripcion,
                    precio: parseFloat(producto.precio),
                    imagen: producto.imagen,
                    stock: producto.stock,
                    activo: producto.activo,
                    categoria: {
                        _id: producto.categoria.id_categoria,
                        nombre: producto.categoria.nombre,
                        icono: producto.categoria.icono
                    }
                }));

                productosContainer.innerHTML = "";
                
                if (products.length === 0) {
                    productosContainer.innerHTML = "<p>No hay productos disponibles en esta categoría.</p>";
                    return;
                }

                // Crear tarjetas de productos
                products.forEach(producto => {
                    const card = document.createElement("div");
                    card.className = "producto galeria-item";
                    
                    // Verificar disponibilidad
                    const disponible = producto.activo && producto.stock > 0;
                    const claseDisponibilidad = disponible ? '' : ' producto-agotado';
                    
                    card.innerHTML = `
                        <div class="producto-imagen-container">
                            <img src="${producto.imagen}" 
                                 alt="${producto.nombre}" 
                                 onerror="this.onerror=null;this.src='https://placehold.co/600x400/CCCCCC/FFFFFF?text=${encodeURIComponent(producto.nombre)}';">
                            ${!disponible ? '<div class="overlay-agotado">Agotado</div>' : ''}
                        </div>
                        <div class="producto-info">
                            <h3>${producto.nombre}</h3>
                            <p class="descripcion">${producto.descripcion}</p>
                            <p class="precio">S/ ${producto.precio.toFixed(2)}</p>
                            ${producto.stock <= 5 && producto.stock > 0 ? 
                                `<p class="stock-bajo">¡Últimas ${producto.stock} unidades!</p>` : ''}
                        </div>
                    `;
                    
                    card.classList.add(claseDisponibilidad);
                    
                    if (disponible) {
                        card.addEventListener("click", () => showProductModal(producto));
                        card.style.cursor = 'pointer';
                    }
                    
                    productosContainer.appendChild(card);
                });
            } else {
                productosContainer.innerHTML = "<p>Error al cargar productos.</p>";
            }
        } catch (error) {
            console.error('Error al obtener productos:', error.message);
            productosContainer.innerHTML = "<p>Error de conexión. Verifica tu conexión a internet.</p>";
        }
    }
    
    // Muestra el modal con la información del producto seleccionado
    function showProductModal(producto) {
        document.getElementById("modalNombre").textContent = producto.nombre;
        document.getElementById("modalDesc").textContent = producto.descripcion;
        document.getElementById("modalPrecio").textContent = producto.precio.toFixed(2);
        document.getElementById("modalImg").src = producto.imagen;
        document.getElementById("modalImg").alt = producto.nombre;
        
        // Mostrar información adicional si está disponible
        const modalExtra = document.getElementById("modalExtra");
        if (modalExtra) {
            let extraInfo = [];
            if (producto.stock && producto.stock <= 10) {
                extraInfo.push(`Stock disponible: ${producto.stock}`);
            }
            modalExtra.innerHTML = extraInfo.length > 0 ? extraInfo.join('<br>') : '';
        }
        
        const modal = document.getElementById("modalProducto");
        modal.classList.add("show");

        document.getElementById("btnAgregar").onclick = () => addToCart(producto);
        document.getElementById("btnSalir").onclick = () => modal.classList.remove("show");
        
        // Cerrar modal al hacer clic fuera
        modal.onclick = (e) => {
            if (e.target === modal) {
                modal.classList.remove("show");
            }
        };
    }

    // Agrega el producto seleccionado al carrito en localStorage
    function addToCart(producto) {
        const user = JSON.parse(localStorage.getItem("user"));
        const token = localStorage.getItem("token");
        
        // Verificar si es administrador
        const isAdmin = token && user && user.email === 'test@test.com';
        if (isAdmin) {
            alert('Modo administrador: No puedes agregar productos al carrito.');
            return;
        }

        // Verificar disponibilidad
        if (!producto.activo || producto.stock <= 0) {
            alert('Este producto no está disponible actualmente.');
            return;
        }

        let carrito = JSON.parse(localStorage.getItem("carrito")) || [];
        const existente = carrito.find(p => p._id === producto._id);

        if (existente) {
            // Verificar si hay suficiente stock
            if (existente.cantidad >= producto.stock) {
                alert(`Solo tenemos ${producto.stock} unidades disponibles de ${producto.nombre}.`);
                return;
            }
            existente.cantidad += 1;
        } else {
            const nuevoProducto = {
                _id: producto._id,
                nombre: producto.nombre,
                precio: producto.precio,
                imagen: producto.imagen,
                cantidad: 1,
                stock_disponible: producto.stock
            };
            carrito.push(nuevoProducto);
        }

        localStorage.setItem("carrito", JSON.stringify(carrito));
        
        // Mostrar confirmación mejorada
        const mensaje = `✅ ${producto.nombre} agregado al carrito`;
        mostrarNotificacion(mensaje, 'success');
        
        document.getElementById("modalProducto").classList.remove("show");
        
        // Actualizar contador de carrito si existe
        actualizarContadorCarrito();
    }

    // Función para mostrar notificaciones
    function mostrarNotificacion(mensaje, tipo = 'info') {
        // Crear elemento de notificación
        const notificacion = document.createElement('div');
        notificacion.className = `notificacion notificacion-${tipo}`;
        notificacion.textContent = mensaje;
        notificacion.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${tipo === 'success' ? '#28a745' : '#007bff'};
            color: white;
            padding: 15px 20px;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            z-index: 10000;
            max-width: 300px;
            animation: slideIn 0.3s ease-out;
        `;
        
        document.body.appendChild(notificacion);
        
        // Remover después de 3 segundos
        setTimeout(() => {
            notificacion.style.animation = 'slideOut 0.3s ease-in';
            setTimeout(() => {
                if (notificacion.parentNode) {
                    notificacion.parentNode.removeChild(notificacion);
                }
            }, 300);
        }, 3000);
    }

    // Función para actualizar contador de carrito
    function actualizarContadorCarrito() {
        const carrito = JSON.parse(localStorage.getItem("carrito")) || [];
        const contador = carrito.reduce((sum, item) => sum + item.cantidad, 0);
        
        const contadorElement = document.getElementById("carrito-contador");
        if (contadorElement) {
            contadorElement.textContent = contador;
            contadorElement.style.display = contador > 0 ? 'block' : 'none';
        }
    }

    // Renderiza las categorías en el swiper
    function renderCategoriasSwiper() {
        const categoriasWrapper = document.querySelector("#categorias-container .swiper-wrapper");
        if (!categoriasWrapper) {
            console.error("No se encontró el contenedor de categorías");
            return;
        }

        categoriasWrapper.innerHTML = "";

        // Botón para ver todos los productos
        const slideTodo = document.createElement("div");
        slideTodo.classList.add("swiper-slide");
        const btnTodo = document.createElement('button');
        btnTodo.className = "categoria-btn active";
        btnTodo.innerHTML = "🍽️ Ver Todo";
        btnTodo.setAttribute('data-category-id', 'todo');
        btnTodo.onclick = () => renderProductos("todo", btnTodo);
        slideTodo.appendChild(btnTodo);
        categoriasWrapper.appendChild(slideTodo);

        // Botones para cada categoría activa
        categories
            .filter(categoria => categoria.activo)
            .forEach(categoria => {
                const slide = document.createElement("div");
                slide.classList.add("swiper-slide");
                const btnCat = document.createElement('button');
                btnCat.className = "categoria-btn";
                btnCat.innerHTML = `${categoria.icono} ${categoria.nombre}`;
                btnCat.setAttribute('data-category-id', categoria._id);
                btnCat.onclick = () => renderProductos(categoria._id, btnCat);
                slide.appendChild(btnCat);
                categoriasWrapper.appendChild(slide);
            });
    }

    // Inicializa el swiper de categorías
    function initSwiperCategorias() {
        return new Swiper("#categorias-container", {
            slidesPerView: "auto",
            spaceBetween: 20,
            loop: true,
            speed: 600,
            navigation: {
                nextEl: ".swiper-button-next",
                prevEl: ".swiper-button-prev",
            },
            centeredSlides: false,
            watchOverflow: false,
            loopAdditionalSlides: 1,
            // Configuración responsive
            breakpoints: {
                320: {
                    slidesPerView: 2,
                    spaceBetween: 10
                },
                640: {
                    slidesPerView: 3,
                    spaceBetween: 15
                },
                768: {
                    slidesPerView: 4,
                    spaceBetween: 20
                },
                1024: {
                    slidesPerView: "auto",
                    spaceBetween: 20
                }
            }
        });
    }

    // Renderiza los botones de autenticación según el estado del usuario
    // function renderAuthButtons() {
    //     const authButtons = document.getElementById('auth-buttons');
    //     if (!authButtons) return;

    //     const user = JSON.parse(localStorage.getItem('user'));
    //     const token = localStorage.getItem('token');
        
    //     authButtons.innerHTML = '';

    //     if (token && user && user.email === 'test@test.com' && user.rol === 'admin') {
    //         // Modo administrador
    //         authButtons.innerHTML = `
    //             <div class="registro">
    //                 <a href="admin.html" class="admin-btn">
    //                     <i class="bi bi-basket-fill"></i> Gestionar Productos
    //                 </a>
    //             </div>
    //             <div class="registro">
    //                 <a href="admin-orders.html" class="admin-btn">
    //                     <i class="bi bi-list-check"></i> Ver Órdenes
    //                 </a>
    //             </div>
    //             <div class="registro">
    //                 <a href="#" onclick="logout()" class="admin-btn">
    //                     <i class="bi bi-box-arrow-right"></i> Salir
    //                 </a>
    //             </div>
    //         `;
    //     } else if (token && user) {
    //         // Usuario común logeado
    //         authButtons.innerHTML = `
    //             <div class="registro">
    //                 <span class="user-welcome">Hola, ${user.nombre.split(' ')[0]}</span>
    //             </div>
    //             <div class="registro">
    //                 <a href="#" onclick="logout()">Cerrar sesión</a>
    //             </div>
    //             <div class="carrito">
    //                 <a href="carrito.html">
    //                     <img src="Icon/carrito-de-compras.png" alt="Carrito">
    //                     <span id="carrito-contador" class="carrito-contador"></span>
    //                 </a>
    //             </div>
    //         `;
    //     } else {
    //         // No autenticado
    //         authButtons.innerHTML = `
    //             <div class="registro">
    //                 <a href="login.html">Inicia sesión <img src="assets/icon/iniciar_sesion.png" alt=""></a>
    //             </div>
    //             <div class="carrito">
    //                 <a href="carrito.html">
    //                     <img src="assets/icon/carrito-de-compras.png" alt="Carrito">
    //                     <span id="carrito-contador" class="carrito-contador"></span>
    //                 </a>
    //             </div>
    //         `;
    //     }

    //     // Actualizar contador de carrito después de renderizar
    //     setTimeout(actualizarContadorCarrito, 100);
    // }

    // Aplicar tema guardado al cargar la página
    const icono = document.getElementById('iconoTema');
    if (localStorage.getItem('tema') === 'oscuro') {
        document.body.classList.add('bg-dark', 'text-light');
        if (icono) icono.textContent = '🌙';
    }

    // Inicializar menú y renderizar botones
    await initMenu();
    // renderAuthButtons();

    // Añadir estilos CSS para las animaciones y mejoras
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideIn {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        
        @keyframes slideOut {
            from { transform: translateX(0); opacity: 1; }
            to { transform: translateX(100%); opacity: 0; }
        }
        
        .producto-agotado {
            opacity: 0.6;
            cursor: not-allowed !important;
        }
        
        .producto-imagen-container {
            position: relative;
            overflow: hidden;
        }
        
        .overlay-agotado {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0,0,0,0.7);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            font-size: 1.2em;
        }
        
        .stock-bajo {
            color: #ff6b35;
            font-weight: bold;
            font-size: 0.9em;
            margin-top: 5px;
        }
        
        .carrito-contador {
            position: absolute;
            top: -8px;
            right: -8px;
            background: #ff6b35;
            color: white;
            border-radius: 50%;
            width: 20px;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            font-weight: bold;
            display: none;
        }
        
        .user-welcome {
            color: #ff6b35;
            font-weight: bold;
        }
        
        .categoria-btn {
            transition: all 0.3s ease;
            border: 2px solid transparent;
            background: rgba(255, 107, 53, 0.1);
            border-radius: 25px;
            padding: 10px 20px;
            font-weight: 500;
        }
        
        .categoria-btn:hover {
            background: rgba(255, 107, 53, 0.2);
            transform: translateY(-2px);
        }
        
        .categoria-btn.active {
            background: #ff6b35;
            color: white;
            border-color: #ff6b35;
        }
    `;
    document.head.appendChild(style);
});

// --- Funciones globales ---

// Cambia entre el tema claro y oscuro de la aplicación
function cambiarTema() {
    const body = document.body;
    const icono = document.getElementById('iconoTema');
    
    body.classList.toggle('bg-dark');
    body.classList.toggle('text-light');
    
    const esOscuro = body.classList.contains('bg-dark');
    if (icono) icono.textContent = esOscuro ? '🌙' : '☀️';
    
    localStorage.setItem('tema', esOscuro ? 'oscuro' : 'claro');
}

// Cierra la sesión del usuario y redirige al inicio
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('carrito'); // Limpiar carrito al salir
    window.location.href = 'index.html';
}

// Función para manejar errores de red de forma global
function handleNetworkError(error) {
    console.error('Error de red:', error);
    
    if (navigator.onLine) {
        mostrarNotificacion('Error de conexión con el servidor. Intenta más tarde.', 'error');
    } else {
        mostrarNotificacion('Sin conexión a internet. Verifica tu conexión.', 'error');
    }
}