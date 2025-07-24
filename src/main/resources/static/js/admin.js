// admin.js - Fixed Version para trabajar con PostgreSQL normalizado
document.addEventListener('DOMContentLoaded', () => {
  const API_URL = 'https://hamburguer-xmx8.onrender.com/api';
  let categories = [];
  let products = [];
  let editingProductId = null;
  let editingCategoryId = null;

  // Obtiene el token y los datos del usuario del localStorage
  const token = localStorage.getItem('token');
  const user = JSON.parse(localStorage.getItem('user'));

  // --- Validación de Acceso (Solo Admin) ---
  if (!token || !user || user.email !== 'test@test.com' || user.rol !== 'admin') {
    alert('Acceso denegado. Solo administradores pueden acceder a esta sección.');
    window.location.href = 'login.html';
    return;
  }

  // Configurar headers de autorización
  const authHeader = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  };

  // --- Funciones de Carga Inicial ---
  async function fetchData() {
    showLoading(true);
    try {
      const [catRes, prodRes] = await Promise.all([
        fetch(`${API_URL}/categories`, { headers: authHeader }),
        fetch(`${API_URL}/products`, { headers: authHeader })
      ]);

      const catData = await catRes.json();
      const prodData = await prodRes.json();

      if (catData.success) {
        // Normalizar categorías para compatibilidad
        categories = catData.data.map(cat => ({
          _id: cat.id_categoria,
          nombre: cat.nombre,
          icono: cat.icono,
          descripcion: cat.descripcion,
          activo: cat.activo,
          orden_display: cat.orden_display
        }));
      } else {
        throw new Error('Error al cargar categorías: ' + catData.msg);
      }

      if (prodData.success) {
        // Normalizar productos para compatibilidad
        products = prodData.data.map(prod => ({
          _id: prod.id_producto,
          nombre: prod.nombre,
          descripcion: prod.descripcion,
          precio: parseFloat(prod.precio),
          imagen: prod.imagen,
          stock: prod.stock,
          activo: prod.activo,
          categoria: {
            _id: prod.categoria.id_categoria,
            nombre: prod.categoria.nombre,
            icono: prod.categoria.icono
          }
        }));
      } else {
        throw new Error('Error al cargar productos: ' + prodData.msg);
      }

      renderAll();
      showNotification('Datos cargados correctamente', 'success');
    } catch (error) {
      console.error('Error fetching data:', error);
      showNotification('Error al cargar datos: ' + error.message, 'error');
    } finally {
      showLoading(false);
    }
  }

  function renderAll() {
    renderCategoryList();
    renderProductTable();
    updateCategoryDropdown();
    updateDashboardStats();
  }

  // --- Gestión de Productos ---
  function renderProductTable() {
    const tbody = document.getElementById('foodTableBody');
    tbody.innerHTML = '';

    if (products.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-center">No hay productos registrados</td></tr>';
      return;
    }

    products.forEach(product => {
      const tr = document.createElement('tr');
      tr.className = product.activo ? '' : 'table-secondary';
      
      const estadoColor = product.activo ? 'success' : 'secondary';
      const estadoTexto = product.activo ? 'Activo' : 'Inactivo';
      const stockColor = product.stock > 10 ? 'success' : product.stock > 0 ? 'warning' : 'danger';
      
      tr.innerHTML = `
        <td>
          <img src="${product.imagen}" alt="${product.nombre}" 
               class="product-thumb" 
               onerror="this.src='https://placehold.co/60x40/CCCCCC/FFFFFF?text=IMG'"
               style="width:60px;height:40px;object-fit:cover;border-radius:4px;">
        </td>
        <td>
          <div class="fw-bold">${product.nombre}</div>
          <small class="text-muted">${product.categoria.nombre}</small>
        </td>
        <td>
          <small class="text-muted">${product.descripcion.substring(0, 50)}${product.descripcion.length > 50 ? '...' : ''}</small>
        </td>
        <td>
          <span class="fw-bold">S/ ${product.precio.toFixed(2)}</span>
        </td>
        <td>
          <span class="badge bg-${stockColor}">${product.stock} unidades</span>
        </td>
        <td>
          <span class="badge bg-${estadoColor}">${estadoTexto}</span>
        </td>
        <td>
          <div class="btn-group" role="group">
            <button class="btn btn-sm btn-outline-warning" onclick="editProduct('${product._id}')" title="Editar">
              <i class="bi bi-pencil"></i>
            </button>
            <button class="btn btn-sm btn-outline-${product.activo ? 'secondary' : 'success'}" 
                    onclick="toggleProductStatus('${product._id}')" 
                    title="${product.activo ? 'Desactivar' : 'Activar'}">
              <i class="bi bi-${product.activo ? 'eye-slash' : 'eye'}"></i>
            </button>
            <button class="btn btn-sm btn-outline-danger" onclick="deleteProduct('${product._id}')" title="Eliminar">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </td>
      `;
      tbody.appendChild(tr);
    });
  }

  async function handleProductFormSubmit(e) {
    e.preventDefault();
    
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Guardando...';

    const productData = {
      nombre: document.getElementById('foodName').value.trim(),
      descripcion: document.getElementById('foodDesc').value.trim(),
      precio: parseFloat(document.getElementById('foodPrice').value),
      imagen: document.getElementById('foodImage').value.trim(),
      id_categoria: document.getElementById('foodCategory').value,
      stock: parseInt(document.getElementById('foodStock').value) || 0,
      activo: document.getElementById('foodActive').checked
    };

    // Validaciones
    if (!productData.nombre || !productData.precio || !productData.id_categoria) {
      showNotification('Nombre, precio y categoría son obligatorios.', 'error');
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalText;
      return;
    }

    if (productData.precio <= 0) {
      showNotification('El precio debe ser mayor a 0.', 'error');
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalText;
      return;
    }

    try {
      let response;
      if (editingProductId) {
        response = await fetch(`${API_URL}/products/${editingProductId}`, {
          method: 'PUT',
          headers: authHeader,
          body: JSON.stringify(productData)
        });
      } else {
        response = await fetch(`${API_URL}/products`, {
          method: 'POST',
          headers: authHeader,
          body: JSON.stringify(productData)
        });
      }

      const result = await response.json();
      
      if (result.success) {
        await fetchData();
        resetProductForm();
        showNotification(
          editingProductId ? 'Producto actualizado correctamente' : 'Producto creado correctamente', 
          'success'
        );
      } else {
        throw new Error(result.msg || 'Error desconocido');
      }
    } catch (error) {
      console.error('Error saving product:', error);
      showNotification('Error al guardar: ' + error.message, 'error');
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalText;
    }
  }

  async function deleteProduct(id) {
    const product = products.find(p => p._id === id);
    if (!product) return;

    const confirmed = await showConfirmDialog(
      '¿Eliminar producto?',
      `¿Estás seguro de que deseas eliminar "${product.nombre}"? Esta acción no se puede deshacer.`
    );

    if (confirmed) {
      try {
        const response = await fetch(`${API_URL}/products/${id}`, {
          method: 'DELETE',
          headers: authHeader
        });
        
        const result = await response.json();
        
        if (result.success) {
          await fetchData();
          showNotification('Producto eliminado correctamente', 'success');
        } else {
          throw new Error(result.msg || 'Error al eliminar');
        }
      } catch (error) {
        console.error('Error deleting product:', error);
        showNotification('Error al eliminar: ' + error.message, 'error');
      }
    }
  }

  async function toggleProductStatus(id) {
    const product = products.find(p => p._id === id);
    if (!product) return;

    try {
      const response = await fetch(`${API_URL}/products/${id}`, {
        method: 'PUT',
        headers: authHeader,
        body: JSON.stringify({ activo: !product.activo })
      });
      
      const result = await response.json();
      
      if (result.success) {
        await fetchData();
        showNotification(
          `Producto ${!product.activo ? 'activado' : 'desactivado'} correctamente`, 
          'success'
        );
      } else {
        throw new Error(result.msg || 'Error al cambiar estado');
      }
    } catch (error) {
      console.error('Error toggling product status:', error);
      showNotification('Error al cambiar estado: ' + error.message, 'error');
    }
  }

  function populateProductForm(productId) {
    const product = products.find(p => p._id === productId);
    if (!product) return;

    document.getElementById('foodName').value = product.nombre;
    document.getElementById('foodDesc').value = product.descripcion;
    document.getElementById('foodPrice').value = product.precio;
    document.getElementById('foodImage').value = product.imagen;
    document.getElementById('foodCategory').value = product.categoria._id;
    document.getElementById('foodStock').value = product.stock;
    document.getElementById('foodActive').checked = product.activo;
    
    editingProductId = product._id;
    
    const submitBtn = document.querySelector('#foodForm button[type="submit"]');
    submitBtn.innerHTML = '<i class="bi bi-pencil-fill"></i> Actualizar Producto';
    
    document.getElementById('cancelBtn').style.display = 'inline-block';
    
    // Scroll al formulario
    document.getElementById('foodForm').scrollIntoView({ behavior: 'smooth' });
  }

  function resetProductForm() {
    document.getElementById('foodForm').reset();
    editingProductId = null;
    
    const submitBtn = document.querySelector('#foodForm button[type="submit"]');
    submitBtn.innerHTML = '<i class="bi bi-plus-circle"></i> Agregar Producto';
    
    document.getElementById('cancelBtn').style.display = 'none';
  }

  // --- Gestión de Categorías ---
  function renderCategoryList() {
    const ul = document.getElementById('category-list-ul');
    ul.innerHTML = '';

    if (categories.length === 0) {
      ul.innerHTML = '<li class="list-group-item text-center">No hay categorías registradas</li>';
      return;
    }

    // Ordenar categorías por orden_display
    const sortedCategories = [...categories].sort((a, b) => (a.orden_display || 0) - (b.orden_display || 0));

    sortedCategories.forEach(cat => {
      const li = document.createElement('li');
      li.className = `list-group-item d-flex justify-content-between align-items-center ${!cat.activo ? 'list-group-item-secondary' : ''}`;
      
      const productCount = products.filter(p => p.categoria._id === cat._id).length;
      
      li.innerHTML = `
        <div class="d-flex align-items-center">
          <span class="me-3" style="font-size: 1.5em;">${cat.icono}</span>
          <div>
            <div class="fw-bold">${cat.nombre}</div>
            <small class="text-muted">${productCount} productos</small>
          </div>
        </div>
        <div class="btn-group" role="group">
          <button class="btn btn-sm btn-outline-warning" onclick="editCategory('${cat._id}')" title="Editar">
            <i class="bi bi-pencil"></i>
          </button>
          <button class="btn btn-sm btn-outline-${cat.activo ? 'secondary' : 'success'}" 
                  onclick="toggleCategoryStatus('${cat._id}')" 
                  title="${cat.activo ? 'Desactivar' : 'Activar'}">
            <i class="bi bi-${cat.activo ? 'eye-slash' : 'eye'}"></i>
          </button>
          <button class="btn btn-sm btn-outline-danger" onclick="deleteCategory('${cat._id}')" title="Eliminar">
            <i class="bi bi-trash"></i>
          </button>
        </div>
      `;
      ul.appendChild(li);
    });
  }

  async function handleCategoryFormSubmit(e) {
    e.preventDefault();
    
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Guardando...';

    const categoryData = {
      nombre: document.getElementById('categoryName').value.trim(),
      icono: document.getElementById('categoryIcon').value.trim(),
      descripcion: document.getElementById('categoryDesc').value.trim(),
      orden_display: parseInt(document.getElementById('categoryOrder').value) || 0,
      activo: document.getElementById('categoryActive').checked
    };

    if (!categoryData.nombre || !categoryData.icono) {
      showNotification('Nombre e ícono son obligatorios.', 'error');
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalText;
      return;
    }

    try {
      let response;
      if (editingCategoryId) {
        response = await fetch(`${API_URL}/categories/${editingCategoryId}`, {
          method: 'PUT',
          headers: authHeader,
          body: JSON.stringify(categoryData)
        });
      } else {
        response = await fetch(`${API_URL}/categories`, {
          method: 'POST',
          headers: authHeader,
          body: JSON.stringify(categoryData)
        });
      }

      const result = await response.json();
      
      if (result.success) {
        await fetchData();
        resetCategoryForm();
        showNotification(
          editingCategoryId ? 'Categoría actualizada correctamente' : 'Categoría creada correctamente', 
          'success'
        );
      } else {
        throw new Error(result.msg || 'Error desconocido');
      }
    } catch (error) {
      console.error('Error saving category:', error);
      showNotification('Error al guardar: ' + error.message, 'error');
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalText;
    }
  }

  async function deleteCategory(id) {
    const category = categories.find(c => c._id === id);
    if (!category) return;

    const productCount = products.filter(p => p.categoria._id === id).length;
    
    let message = `¿Estás seguro de que deseas eliminar la categoría "${category.nombre}"?`;
    if (productCount > 0) {
      message += `\n\nEsta categoría tiene ${productCount} producto(s) asociado(s) que también serán eliminados.`;
    }

    const confirmed = await showConfirmDialog('¿Eliminar categoría?', message);

    if (confirmed) {
      try {
        const response = await fetch(`${API_URL}/categories/${id}`, {
          method: 'DELETE',
          headers: authHeader
        });
        
        const result = await response.json();
        
        if (result.success) {
          await fetchData();
          showNotification('Categoría eliminada correctamente', 'success');
        } else {
          throw new Error(result.msg || 'Error al eliminar');
        }
      } catch (error) {
        console.error('Error deleting category:', error);
        showNotification('Error al eliminar: ' + error.message, 'error');
      }
    }
  }

  async function toggleCategoryStatus(id) {
    const category = categories.find(c => c._id === id);
    if (!category) return;

    try {
      const response = await fetch(`${API_URL}/categories/${id}`, {
        method: 'PUT',
        headers: authHeader,
        body: JSON.stringify({ activo: !category.activo })
      });
      
      const result = await response.json();
      
      if (result.success) {
        await fetchData();
        showNotification(
          `Categoría ${!category.activo ? 'activada' : 'desactivada'} correctamente`, 
          'success'
        );
      } else {
        throw new Error(result.msg || 'Error al cambiar estado');
      }
    } catch (error) {
      console.error('Error toggling category status:', error);
      showNotification('Error al cambiar estado: ' + error.message, 'error');
    }
  }

  function populateCategoryForm(categoryId) {
    const category = categories.find(c => c._id === categoryId);
    if (!category) return;

    document.getElementById('categoryName').value = category.nombre;
    document.getElementById('categoryIcon').value = category.icono;
    document.getElementById('categoryDesc').value = category.descripcion || '';
    document.getElementById('categoryOrder').value = category.orden_display || 0;
    document.getElementById('categoryActive').checked = category.activo;
    
    editingCategoryId = category._id;
    
    const submitBtn = document.querySelector('#categoryForm button[type="submit"]');
    submitBtn.innerHTML = '<i class="bi bi-pencil-fill"></i> Actualizar Categoría';
    
    document.getElementById('cancelCategoryBtn').style.display = 'inline-block';
    
    // Scroll al formulario
    document.getElementById('categoryForm').scrollIntoView({ behavior: 'smooth' });
  }

  function resetCategoryForm() {
    document.getElementById('categoryForm').reset();
    editingCategoryId = null;
    
    const submitBtn = document.querySelector('#categoryForm button[type="submit"]');
    submitBtn.innerHTML = '<i class="bi bi-plus-circle"></i> Agregar Categoría';
    
    document.getElementById('cancelCategoryBtn').style.display = 'none';
  }

  function updateCategoryDropdown() {
    const categorySelect = document.getElementById('foodCategory');
    categorySelect.innerHTML = '<option value="" disabled selected>Seleccionar categoría</option>';
    
    categories
      .filter(cat => cat.activo)
      .sort((a, b) => (a.orden_display || 0) - (b.orden_display || 0))
      .forEach(cat => {
        const option = document.createElement('option');
        option.value = cat._id;
        option.textContent = `${cat.icono} ${cat.nombre}`;
        categorySelect.appendChild(option);
      });
  }

  // --- Dashboard y Estadísticas ---
  function updateDashboardStats() {
    // Estadísticas generales
    const totalProducts = products.length;
    const activeProducts = products.filter(p => p.activo).length;
    const totalCategories = categories.length;
    const lowStockProducts = products.filter(p => p.stock <= 5 && p.activo).length;

    // Actualizar elementos del DOM si existen
    updateStatElement('total-products', totalProducts);
    updateStatElement('active-products', activeProducts);
    updateStatElement('total-categories', totalCategories);
    updateStatElement('low-stock-products', lowStockProducts);

    // Productos más caros y más baratos
    if (products.length > 0) {
      const sortedByPrice = [...products].sort((a, b) => b.precio - a.precio);
      updateStatElement('most-expensive', `S/ ${sortedByPrice[0]?.precio.toFixed(2)} (${sortedByPrice[0]?.nombre})`);
      updateStatElement('least-expensive', `S/ ${sortedByPrice[sortedByPrice.length - 1]?.precio.toFixed(2)} (${sortedByPrice[sortedByPrice.length - 1]?.nombre})`);
    }

    // Alertas de stock bajo
    if (lowStockProducts > 0) {
      showNotification(`${lowStockProducts} producto(s) con stock bajo`, 'warning');
    }
  }

  function updateStatElement(id, value) {
    const element = document.getElementById(id);
    if (element) {
      element.textContent = value;
    }
  }

  // --- Funciones de UI y Utilidades ---
  function showLoading(show) {
    const loader = document.getElementById('loading-spinner');
    if (loader) {
      loader.style.display = show ? 'block' : 'none';
    }
  }

  function showNotification(message, type = 'info') {
    // Crear notificación toast
    const toastContainer = document.getElementById('toast-container') || createToastContainer();
    
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type === 'error' ? 'danger' : type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
      <div class="d-flex">
        <div class="toast-body">
          <i class="bi bi-${getToastIcon(type)} me-2"></i>
          ${message}
        </div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
      </div>
    `;

    toastContainer.appendChild(toast);
    
    // Mostrar toast usando Bootstrap
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();

    // Remover del DOM después de que se oculte
    toast.addEventListener('hidden.bs.toast', () => {
      toast.remove();
    });
  }

  function createToastContainer() {
    const container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container position-fixed top-0 end-0 p-3';
    container.style.zIndex = '9999';
    document.body.appendChild(container);
    return container;
  }

  function getToastIcon(type) {
    switch (type) {
      case 'success': return 'check-circle';
      case 'error': return 'exclamation-triangle';
      case 'warning': return 'exclamation-triangle';
      default: return 'info-circle';
    }
  }

  function showConfirmDialog(title, message) {
    return new Promise((resolve) => {
      // Crear modal de confirmación
      const modal = document.createElement('div');
      modal.className = 'modal fade';
      modal.innerHTML = `
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">${title}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <p>${message.replace(/\n/g, '<br>')}</p>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
              <button type="button" class="btn btn-danger" id="confirm-btn">Confirmar</button>
            </div>
          </div>
        </div>
      `;

      document.body.appendChild(modal);
      
      const bsModal = new bootstrap.Modal(modal);
      bsModal.show();

      modal.querySelector('#confirm-btn').addEventListener('click', () => {
        bsModal.hide();
        resolve(true);
      });

      modal.addEventListener('hidden.bs.modal', () => {
        modal.remove();
        resolve(false);
      });
    });
  }

  // --- Funciones Globales (expuestas al window) ---
  window.editProduct = populateProductForm;
  window.deleteProduct = deleteProduct;
  window.toggleProductStatus = toggleProductStatus;
  window.editCategory = populateCategoryForm;
  window.deleteCategory = deleteCategory;
  window.toggleCategoryStatus = toggleCategoryStatus;

  // --- Event Listeners ---
  document.getElementById('foodForm').addEventListener('submit', handleProductFormSubmit);
  document.getElementById('cancelBtn').addEventListener('click', resetProductForm);
  document.getElementById('categoryForm').addEventListener('submit', handleCategoryFormSubmit);
  document.getElementById('cancelCategoryBtn').addEventListener('click', resetCategoryForm);

  // Preview de imagen del producto
  document.getElementById('foodImage').addEventListener('input', (e) => {
    const preview = document.getElementById('image-preview');
    if (preview) {
      const url = e.target.value.trim();
      if (url) {
        preview.src = url;
        preview.style.display = 'block';
        preview.onerror = () => {
          preview.style.display = 'none';
        };
      } else {
        preview.style.display = 'none';
      }
    }
  });

  // Filtros y búsqueda
  document.getElementById('product-search')?.addEventListener('input', (e) => {
    const searchTerm = e.target.value.toLowerCase();
    filterProducts(searchTerm);
  });

  document.getElementById('category-filter')?.addEventListener('change', (e) => {
    const categoryId = e.target.value;
    filterProductsByCategory(categoryId);
  });

  function filterProducts(searchTerm) {
    const filteredProducts = products.filter(product => 
      product.nombre.toLowerCase().includes(searchTerm) ||
      product.descripcion.toLowerCase().includes(searchTerm)
    );
    renderFilteredProducts(filteredProducts);
  }

  function filterProductsByCategory(categoryId) {
    if (!categoryId) {
      renderProductTable();
      return;
    }
    
    const filteredProducts = products.filter(product => 
      product.categoria._id === categoryId
    );
    renderFilteredProducts(filteredProducts);
  }

  function renderFilteredProducts(filteredProducts) {
    const tbody = document.getElementById('foodTableBody');
    tbody.innerHTML = '';

    if (filteredProducts.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-center">No se encontraron productos</td></tr>';
      return;
    }

    // Usar la misma lógica de renderProductTable pero con productos filtrados
    const originalProducts = products;
    products = filteredProducts;
    renderProductTable();
    products = originalProducts;
  }

  // Tema oscuro/claro
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

  // Navegación y logout
  window.logout = function() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = 'login.html';
  };

  // --- Inicialización ---
  initTheme();
  fetchData();

  // Añadir estilos CSS específicos para el admin
  const adminStyles = document.createElement('style');
  adminStyles.textContent = `
    .product-thumb {
      transition: transform 0.2s;
    }
    
    .product-thumb:hover {
      transform: scale(1.1);
    }
    
    .btn-group .btn {
      margin: 0 1px;
    }
    
    .toast-container {
      z-index: 9999;
    }
    
    .table-secondary {
      opacity: 0.7;
    }
    
    #image-preview {
      max-width: 200px;
      max-height: 150px;
      margin-top: 10px;
      border-radius: 8px;
      border: 2px solid #dee2e6;
    }
    
    .stat-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 15px;
      padding: 20px;
      margin-bottom: 20px;
    }
    
    .stat-number {
      font-size: 2rem;
      font-weight: bold;
    }
    
    .loading-spinner {
      display: none;
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      z-index: 9999;
    }
    
    .admin-header {
      background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
      color: white;
      padding: 20px 0;
      margin-bottom: 30px;
    }
    
    .form-section {
      background: #f8f9fa;
      padding: 25px;
      border-radius: 15px;
      margin-bottom: 30px;
      border-left: 4px solid #ff6b35;
    }
    
    .table-responsive {
      border-radius: 15px;
      overflow: hidden;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }
    
    .btn-outline-warning:hover,
    .btn-outline-success:hover,
    .btn-outline-danger:hover,
    .btn-outline-secondary:hover {
      transform: translateY(-1px);
    }
  `;
  document.head.appendChild(adminStyles);

  console.log('Admin panel inicializado correctamente');
});