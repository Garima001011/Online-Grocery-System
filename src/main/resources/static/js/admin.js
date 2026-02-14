// Admin Dashboard JavaScript
let currentUser = null;
let dashboardData = null;
let chartInstances = {};
let currentView = 'dashboard';

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    currentUser = JSON.parse(localStorage.getItem('user'));
    
    // Check if user is admin
    if (!currentUser || currentUser.role !== 'ADMIN') {
        window.location.href = '/login.html';
        return;
    }
    
    initAdminDashboard();
});

async function initAdminDashboard() {
    // Set user info
    document.getElementById('adminName').textContent = currentUser.name || currentUser.email;
    document.getElementById('adminRole').textContent = currentUser.role;
    document.getElementById('adminAvatar').textContent = currentUser.name?.charAt(0) || currentUser.email.charAt(0);
    
    // Initialize date pickers
    flatpickr("#notificationSchedule", {
        enableTime: true,
        dateFormat: "Y-m-d H:i",
    });
    
    flatpickr("#analyticsStartDate", {
        dateFormat: "Y-m-d",
        defaultDate: new Date().toISOString().split('T')[0]
    });
    
    flatpickr("#analyticsEndDate", {
        dateFormat: "Y-m-d",
        defaultDate: new Date().toISOString().split('T')[0]
    });
    
    // Setup navigation
    setupNavigation();
    
    // Load dashboard data
    await loadDashboardData();
    
    // Load initial tab content
    loadTabContent('dashboard');
    
    // Setup real-time updates
    setupRealtimeUpdates();
}

// Setup navigation
function setupNavigation() {
    // Sidebar navigation
    document.querySelectorAll('.nav-item-admin').forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove active class from all items
            document.querySelectorAll('.nav-item-admin').forEach(i => {
                i.classList.remove('active');
            });
            
            // Add active class to clicked item
            this.classList.add('active');
            
            // Get tab name
            const tab = this.getAttribute('data-tab');
            currentView = tab;
            
            // Update page title
            document.getElementById('pageTitle').textContent = 
                this.querySelector('.nav-text').textContent + ' - Admin Panel';
            
            // Load tab content
            loadTabContent(tab);
        });
    });
    
    // Tab navigation
    document.querySelectorAll('.admin-tab').forEach(tab => {
        tab.addEventListener('click', function() {
            const parent = this.closest('.admin-tabs');
            parent.querySelectorAll('.admin-tab').forEach(t => {
                t.classList.remove('active');
            });
            this.classList.add('active');
        });
    });
}

// Load tab content
async function loadTabContent(tab) {
    // Hide all tab contents
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    
    // Show selected tab
    document.getElementById(tab).classList.add('active');
    
    // Load specific content
    switch(tab) {
        case 'dashboard':
            await loadDashboard();
            break;
        case 'returns':
            await loadReturns();
            break;
        case 'orders':
            await loadOrders();
            break;
        case 'delivery':
            await loadDelivery();
            break;
        case 'catalog':
            await loadCatalog();
            break;
        case 'marketing':
            await loadMarketing();
            break;
        case 'analytics':
            await loadAnalytics();
            break;
        case 'users':
            await loadUsers();
            break;
    }
}

// Load dashboard data
async function loadDashboardData() {
    try {
        const response = await fetch('/api/admin/dashboard');
        if (response.ok) {
            dashboardData = await response.json();
            updateDashboardStats();
            updateSidebarStats();
        } else {
            console.error('Failed to load dashboard data');
        }
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

// Update dashboard stats
function updateDashboardStats() {
    if (!dashboardData) return;

    // Update stat cards
    document.getElementById('totalRevenue').textContent = `Rs. ${formatPrice(dashboardData.totalRevenue || 0)}`;
    document.getElementById('totalOrders').textContent = dashboardData.totalOrders || 0;
    document.getElementById('totalCustomers').textContent = dashboardData.totalCustomers || 0;
    document.getElementById('totalReturns').textContent = dashboardData.pendingReturns || 0;

    // Update trends
    document.getElementById('revenueTrend').textContent = `+${dashboardData.revenueGrowth || 0}%`;
    document.getElementById('ordersTrend').textContent = `+${dashboardData.orderGrowth || 0}%`;
    document.getElementById('customersTrend').textContent = `+${dashboardData.customerGrowth || 0}%`;
    document.getElementById('returnsTrend').textContent = `${dashboardData.returnRate || 0}%`;

    // Update returns badge
    const returnsBadge = document.getElementById('returnsBadge');
    if (dashboardData.pendingReturns > 0) {
        returnsBadge.textContent = dashboardData.pendingReturns;
        returnsBadge.style.display = 'block';
    }
}

// Update sidebar stats
function updateSidebarStats() {
    if (!dashboardData) return;

    const today = dashboardData.todayStats || {};
    document.getElementById('sidebarSales').textContent = `Rs. ${formatPrice(today.sales || 0)}`;
    document.getElementById('sidebarOrders').textContent = today.orders || 0;
    document.getElementById('sidebarCustomers').textContent = today.customers || 0;
}

// Load dashboard content
async function loadDashboard() {
    await loadRecentOrders();
    await loadCharts();
}

// Load recent orders
async function loadRecentOrders() {
    try {
        const response = await fetch('/api/admin/orders/recent?limit=10');
        if (response.ok) {
            const orders = await response.json();
            renderRecentOrders(orders);
        }
    } catch (error) {
        console.error('Error loading recent orders:', error);
    }
}

// Render recent orders
function renderRecentOrders(orders) {
    const tbody = document.querySelector('#recentOrdersTable tbody');
    tbody.innerHTML = '';

    orders.forEach(order => {
        const row = document.createElement('tr');

        const cancelledInfo = order.status === 'CANCELLED'
            ? `<div style="margin-top:6px;">
                    <small class="text-muted">
                        Reason: ${order.cancelReason || 'N/A'}<br>
                        Cancelled: ${formatDate(order.cancelledAt)}
                    </small>
               </div>`
            : '';

        row.innerHTML = `
            <td><strong>#${order.id}</strong></td>
            <td>
                <div>${order.user?.name || 'Unknown'}</div>
                <small class="text-muted">${order.user?.email || ''}</small>
                ${cancelledInfo}
            </td>
            <td>Rs. ${formatPrice(order.total)}</td>
            <td><span class="status-badge-admin status-${order.status.toLowerCase()}">${order.status}</span></td>
            <td>${formatDate(order.createdAt)}</td>
            <td>
                <button class="action-btn action-edit" onclick="viewOrderDetails(${order.id})">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        `;

        tbody.appendChild(row);
    });
}

// Load and render charts
async function loadCharts() {
    try {
        // Sales chart
        const salesResponse = await fetch('/api/admin/charts/sales?days=7');
        if (salesResponse.ok) {
            const salesData = await salesResponse.json();
            renderSalesChart(salesData);
        }

        // Orders chart
        const ordersResponse = await fetch('/api/admin/charts/orders-by-status');
        if (ordersResponse.ok) {
            const ordersData = await ordersResponse.json();
            renderOrdersChart(ordersData);
        }
    } catch (error) {
        console.error('Error loading charts:', error);
    }
}

// Render sales chart
function renderSalesChart(data) {
    const ctx = document.getElementById('salesChart').getContext('2d');

    if (chartInstances.salesChart) {
        chartInstances.salesChart.destroy();
    }

    chartInstances.salesChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.labels || [],
            datasets: [{
                label: 'Sales (Rs.)',
                data: data.values || [],
                borderColor: '#ff9900',
                backgroundColor: 'rgba(255, 153, 0, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return 'Rs. ' + formatPrice(value);
                        }
                    }
                }
            }
        }
    });
}

// Render orders chart
function renderOrdersChart(data) {
    const ctx = document.getElementById('ordersChart').getContext('2d');

    if (chartInstances.ordersChart) {
        chartInstances.ordersChart.destroy();
    }

    const colors = {
        'PLACED': '#ffc107',
        'ASSIGNED': '#17a2b8',
        'PICKED_UP': '#007bff',
        'DELIVERED': '#28a745',
        'CANCELLED': '#dc3545'
    };

    chartInstances.ordersChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.labels || [],
            datasets: [{
                data: data.values || [],
                backgroundColor: data.labels.map(label => colors[label] || '#6c757d'),
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

// Returns Management
async function loadReturns() {
    try {
        const response = await fetch('/api/admin/returns');
        if (response.ok) {
            const returns = await response.json();
            renderReturnsTable(returns);
        }
    } catch (error) {
        console.error('Error loading returns:', error);
    }
}

// Render returns table
function renderReturnsTable(returns) {
    const tbody = document.querySelector('#returnsTable tbody');
    tbody.innerHTML = '';

    returns.forEach(ret => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>#${ret.id}</td>
            <td>#${ret.orderId}</td>
            <td>${ret.customerName}</td>
            <td>${ret.productName}</td>
            <td>${truncateText(ret.returnReason, 30)}</td>
            <td>Rs. ${formatPrice(ret.refundAmount || 0)}</td>
            <td><span class="status-badge-admin status-${ret.returnStatus.toLowerCase()}">${ret.returnStatus}</span></td>
            <td>${formatDate(ret.returnRequestedAt)}</td>
            <td>
                ${ret.returnStatus === 'REQUESTED' ? `
                    <button class="action-btn action-approve" onclick="processReturn(${ret.id}, 'APPROVED')">
                        <i class="fas fa-check"></i>
                    </button>
                    <button class="action-btn action-reject" onclick="processReturn(${ret.id}, 'REJECTED')" style="margin-left: 5px;">
                        <i class="fas fa-times"></i>
                    </button>
                ` : ret.returnStatus === 'APPROVED' ? `
                    <button class="action-btn action-approve" onclick="processReturn(${ret.id}, 'REFUNDED')">
                        <i class="fas fa-money-check"></i> Refund
                    </button>
                ` : ''}
                <button class="action-btn action-edit" onclick="viewReturnDetails(${ret.id})" style="margin-left: 5px;">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Process return
async function processReturn(returnId, action) {
    if (!confirm(`Are you sure you want to ${action.toLowerCase()} this return?`)) {
        return;
    }

    try {
        const response = await fetch(`/api/admin/returns/${returnId}/process`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ action })
        });

        if (response.ok) {
            alert(`Return ${action.toLowerCase()} successfully!`);
            await loadReturns();
            await loadDashboardData(); // Refresh stats
        } else {
            const error = await response.text();
            alert(`Error: ${error}`);
        }
    } catch (error) {
        console.error('Error processing return:', error);
        alert('Failed to process return. Please try again.');
    }
}

// View return details
function viewReturnDetails(returnId) {
    alert('View return details for ID: ' + returnId);
    // You can implement modal or detailed view here
}

// View order details
function viewOrderDetails(orderId) {
    alert('View order details for ID: ' + orderId);
    // You can implement modal or detailed view here
}

/* ===========================
   ADDED: Orders Tab Functions
   =========================== */

// Load all orders for the Orders tab
async function loadOrders() {
    try {
        const response = await fetch('/api/orders');
        if (response.ok) {
            const orders = await response.json();
            renderOrdersTable(orders);
        } else {
            console.error('Failed to load orders');
        }
    } catch (error) {
        console.error('Error loading orders:', error);
    }
}

// Render orders in the admin orders table
function renderOrdersTable(orders) {
    const tbody = document.querySelector('#ordersTable tbody');
    tbody.innerHTML = '';

    orders.forEach(order => {
        const row = document.createElement('tr');
        const itemsCount = order.items ? order.items.length : 0;

        const cancelledInfo = order.status === 'CANCELLED'
            ? `<div style="margin-top:4px;">
                    <small class="text-muted">Reason: ${order.cancelReason || 'N/A'}<br>
                    Cancelled: ${formatDate(order.cancelledAt)}</small>
               </div>`
            : '';

        row.innerHTML = `
            <td><strong>#${order.id}</strong></td>
            <td>
                <div>${order.user?.name || 'Unknown'}</div>
                <small class="text-muted">${order.user?.email || ''}</small>
                ${cancelledInfo}
            </td>
            <td>${itemsCount} item${itemsCount !== 1 ? 's' : ''}</td>
            <td>Rs. ${formatPrice(order.total)}</td>
            <td><span class="status-badge-admin status-${order.status.toLowerCase()}">${order.status}</span></td>
            <td>${order.paymentStatus || 'PENDING'}</td>
            <td>${formatDate(order.createdAt)}</td>
            <td>
                <button class="action-btn action-edit" onclick="viewOrderDetails(${order.id})">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

/* ===========================
   ADDED: Marketing Tab Loader
   =========================== */

async function loadMarketing() {
    await loadNotifications();
    // Optionally load campaign chart data here
}

/* ===========================
   ADDED: Analytics Tab Loader
   =========================== */

async function loadAnalytics() {
    try {
        // Load top products
        const topProductsResponse = await fetch('/api/admin/products/top?limit=10');
        if (topProductsResponse.ok) {
            const topProducts = await topProductsResponse.json();
            renderTopProducts(topProducts);
        }

        // Load customer analytics (placeholder)
        const tbody = document.querySelector('#customerAnalyticsTable tbody');
        if (tbody) {
            tbody.innerHTML = `
                <tr><td colspan="5" style="text-align:center;">Customer analytics coming soon</td></tr>
            `;
        }
    } catch (error) {
        console.error('Error loading analytics:', error);
    }
}

function renderTopProducts(products) {
    const container = document.getElementById('topProductsList');
    if (!container) return;

    container.innerHTML = '';
    products.forEach(p => {
        container.innerHTML += `
            <div style="margin-bottom: 10px; padding: 8px; border-bottom: 1px solid #eee;">
                <strong>${p.product?.name || 'Unknown Product'}</strong> - Sold: ${p.salesCount || 0}
            </div>
        `;
    });
}

/* ===========================
   Catalog Management
   =========================== */

async function loadCatalog() {
    await loadCategories();
    await loadStores();
    await loadProducts();
}

// Load categories for dropdown
async function loadCategories() {
    try {
        const response = await fetch('/api/categories');
        if (response.ok) {
            const categories = await response.json();
            const select = document.getElementById('productCategory');
            select.innerHTML = '<option value="">Select Category</option>' +
                categories.map(cat => `<option value="${cat.id}">${cat.name}</option>`).join('');
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

// Load stores for dropdown
async function loadStores() {
    try {
        const response = await fetch('/api/stores');
        if (response.ok) {
            const stores = await response.json();
            const select = document.getElementById('productStore');
            select.innerHTML = '<option value="">Select Store</option>' +
                stores.map(store => `<option value="${store.id}">${store.name}</option>`).join('');
        }
    } catch (error) {
        console.error('Error loading stores:', error);
    }
}

// Load products
async function loadProducts() {
    try {
        const response = await fetch('/api/products');
        if (response.ok) {
            const products = await response.json();
            renderProductsList(products);
        }
    } catch (error) {
        console.error('Error loading products:', error);
    }
}

// Render products list
function renderProductsList(products) {
    const container = document.getElementById('productsList');
    container.innerHTML = '';

    products.forEach(product => {
        const item = document.createElement('div');
        item.className = 'notification-item';
        item.innerHTML = `
            <div class="notification-header">
                <strong>${product.name}</strong>
                <span class="status-badge-admin">${product.stock > 0 ? 'In Stock' : 'Out of Stock'}</span>
            </div>
            <div style="display: flex; gap: 15px; margin-top: 10px;">
                <div style="width: 60px; height: 60px; background: #f0f2f2; border-radius: 4px; display: flex; align-items: center; justify-content: center;">
                    ${product.imageUrl ?
                        `<img src="${product.imageUrl}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 4px;">` :
                        `<i class="fas fa-box" style="color: #999;"></i>`
                    }
                </div>
                <div style="flex: 1;">
                    <div style="color: #666; font-size: 14px;">${product.category?.name || 'No Category'}</div>
                    <div style="color: #b12704; font-weight: bold; margin: 5px 0;">Rs. ${formatPrice(product.price)}</div>
                    <div style="font-size: 12px; color: #666;">Stock: ${product.stock} | Store: ${product.store?.name || 'Unknown'}</div>
                </div>
            </div>
            <div style="margin-top: 10px; display: flex; gap: 5px;">
                <button class="action-btn action-edit" onclick="editProduct(${product.id})">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="action-btn action-delete" onclick="deleteProduct(${product.id})">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </div>
        `;
        container.appendChild(item);
    });
}

// Add product
async function addProduct() {
    const name = document.getElementById('productName').value.trim();
    const price = parseFloat(document.getElementById('productPrice').value);
    const stock = parseInt(document.getElementById('productStock').value);
    const categoryId = document.getElementById('productCategory').value;
    const storeId = document.getElementById('productStore').value;
    const description = document.getElementById('productDescription').value.trim();
    const imageFile = document.getElementById('productImage').files[0];

    // Validation
    if (!name || !price || !stock || !categoryId || !storeId) {
        alert('Please fill all required fields');
        return;
    }

    // Create FormData for file upload
    const formData = new FormData();
    formData.append('name', name);
    formData.append('price', price);
    formData.append('stock', stock);
    formData.append('categoryId', categoryId);
    formData.append('storeId', storeId);
    if (description) {
        formData.append('description', description);
    }
    if (imageFile) {
        formData.append('image', imageFile);
    }

    try {
        const response = await fetch('/api/admin/products', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            await response.json();
            alert('Product added successfully!');

            // Clear form
            document.getElementById('productName').value = '';
            document.getElementById('productPrice').value = '';
            document.getElementById('productStock').value = '';
            document.getElementById('productDescription').value = '';
            document.getElementById('productImage').value = '';
            document.getElementById('productImagePreview').innerHTML = '';

            // Refresh products list
            await loadProducts();
        } else {
            const error = await response.text();
            alert(`Error: ${error}`);
        }
    } catch (error) {
        console.error('Error adding product:', error);
        alert('Failed to add product. Please try again.');
    }
}

// Edit product
function editProduct(productId) {
    alert('Edit product ID: ' + productId);
    // Implement edit functionality
}

// Delete product
function deleteProduct(productId) {
    if (confirm('Are you sure you want to delete this product?')) {
        alert('Delete product ID: ' + productId);
        // Implement delete functionality
    }
}

// Search products
function searchProducts(query) {
    alert('Search products: ' + query);
    // Implement search functionality
}

// Marketing - Send notification
async function sendNotification() {
    const title = document.getElementById('notificationTitle').value.trim();
    const message = document.getElementById('notificationMessage').value.trim();
    const type = document.getElementById('notificationType').value;
    const audience = document.getElementById('notificationAudience').value;
    const schedule = document.getElementById('notificationSchedule').value;

    if (!title || !message) {
        alert('Please fill title and message');
        return;
    }

    const notificationData = {
        title,
        message,
        type,
        targetAudience: audience,
        scheduledFor: schedule ? schedule.replace(' ', 'T') + ':00' : null
    };

    try {
        const response = await fetch('/api/admin/notifications', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(notificationData)
        });

        if (response.ok) {
            await response.json();
            alert('Notification sent successfully!');

            // Clear form
            document.getElementById('notificationTitle').value = '';
            document.getElementById('notificationMessage').value = '';

            // Refresh notifications list
            await loadNotifications();
        } else {
            const error = await response.text();
            alert(`Error: ${error}`);
        }
    } catch (error) {
        console.error('Error sending notification:', error);
        alert('Failed to send notification. Please try again.');
    }
}

// Schedule notification
function scheduleNotification() {
    const schedule = document.getElementById('notificationSchedule').value;
    if (!schedule) {
        alert('Please select a schedule time');
        return;
    }
    sendNotification();
}

// Save draft
function saveDraft() {
    alert('Save as draft');
    // Implement save draft functionality
}

// Load notifications
async function loadNotifications() {
    try {
        const response = await fetch('/api/admin/notifications');
        if (response.ok) {
            const notifications = await response.json();
            renderNotifications(notifications);
        }
    } catch (error) {
        console.error('Error loading notifications:', error);
    }
}

// Render notifications
function renderNotifications(notifications) {
    const container = document.getElementById('notificationsList');
    container.innerHTML = '';

    notifications.forEach(notification => {
        const item = document.createElement('div');
        item.className = 'notification-item';
        item.innerHTML = `
            <div class="notification-header">
                <strong>${notification.title}</strong>
                <span class="notification-type type-${notification.type?.toLowerCase() || 'info'}">${notification.type || 'INFO'}</span>
            </div>
            <div style="color: #666; margin: 10px 0;">${notification.message}</div>
            <div style="display: flex; justify-content: space-between; font-size: 12px; color: #999;">
                <span>To: ${notification.targetAudience || 'ALL'}</span>
                <span>${formatDate(notification.createdAt)}</span>
            </div>
        `;
        container.appendChild(item);
    });
}

// Load delivery tab
async function loadDelivery() {
    await loadUnassignedOrders();
    await loadDeliveryPersons();
    await loadDeliveryStats();
}

// Load unassigned orders
async function loadUnassignedOrders() {
    try {
        const response = await fetch('/api/admin/orders/unassigned');
        if (response.ok) {
            const orders = await response.json();
            const select = document.getElementById('assignOrderSelect');
            select.innerHTML = '<option value="">Choose order...</option>' +
                orders.map(order => `<option value="${order.id}">Order #${order.id} - ${order.user?.name} - Rs. ${formatPrice(order.total)}</option>`).join('');
        }
    } catch (error) {
        console.error('Error loading unassigned orders:', error);
    }
}

// Load delivery persons
async function loadDeliveryPersons() {
    try {
        const response = await fetch('/api/admin/delivery-persons');
        if (response.ok) {
            const deliveryPersons = await response.json();
            const select = document.getElementById('assignDeliverySelect');
            select.innerHTML = '<option value="">Choose partner...</option>' +
                deliveryPersons.map(person => `<option value="${person.id}">${person.name} (${person.email})</option>`).join('');

            renderDeliveryPartnersList(deliveryPersons);
        }
    } catch (error) {
        console.error('Error loading delivery persons:', error);
    }
}

// Render delivery partners list
function renderDeliveryPartnersList(deliveryPersons) {
    const container = document.getElementById('deliveryPartnersList');
    container.innerHTML = '';

    deliveryPersons.forEach(person => {
        const item = document.createElement('div');
        item.className = 'notification-item';
        item.innerHTML = `
            <div class="notification-header">
                <strong>${person.name}</strong>
                <span class="status-badge-admin ${person.isAvailable ? 'status-delivered' : 'status-rejected'}">
                    ${person.isAvailable ? 'Available' : 'Busy'}
                </span>
            </div>
            <div style="color: #666; margin: 5px 0;">${person.email}</div>
            <div style="font-size: 12px; color: #666;">
                Vehicle: ${person.vehicleType || 'N/A'} (${person.vehicleNumber || 'N/A'})<br>
                Rating: ${person.rating || 5.0} | Deliveries: ${person.totalDeliveries || 0}
            </div>
            <div style="margin-top: 10px; display: flex; gap: 5px;">
                <button class="action-btn action-edit" onclick="toggleDeliveryAvailability(${person.id}, ${!person.isAvailable})">
                    <i class="fas ${person.isAvailable ? 'fa-pause' : 'fa-play'}"></i> ${person.isAvailable ? 'Set Busy' : 'Set Available'}
                </button>
                <button class="action-btn action-edit" onclick="viewDeliveryDetails(${person.id})">
                    <i class="fas fa-eye"></i> View
                </button>
            </div>
        `;
        container.appendChild(item);
    });
}

// Toggle delivery availability
async function toggleDeliveryAvailability(deliveryId, newAvailability) {
    try {
        const response = await fetch(`/api/admin/delivery/${deliveryId}/availability`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ isAvailable: newAvailability })
        });

        if (response.ok) {
            alert('Availability updated successfully!');
            await loadDeliveryPersons();
        } else {
            const error = await response.text();
            alert(`Error: ${error}`);
        }
    } catch (error) {
        console.error('Error toggling availability:', error);
        alert('Failed to update availability. Please try again.');
    }
}

// Assign order to delivery
async function assignOrderToDelivery() {
    const orderId = document.getElementById('assignOrderSelect').value;
    const deliveryPersonId = document.getElementById('assignDeliverySelect').value;

    if (!orderId || !deliveryPersonId) {
        alert('Please select both order and delivery partner');
        return;
    }

    try {
        const response = await fetch(`/api/admin/orders/${orderId}/assign`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ deliveryPersonId: parseInt(deliveryPersonId) })
        });

        if (response.ok) {
            alert('Order assigned successfully!');
            await loadUnassignedOrders();
            await loadDeliveryPersons();
        } else {
            const error = await response.text();
            alert(`Error: ${error}`);
        }
    } catch (error) {
        console.error('Error assigning order:', error);
        alert('Failed to assign order. Please try again.');
    }
}

// Add delivery partner
async function addDeliveryPartner() {
    const name = document.getElementById('deliveryName').value.trim();
    const email = document.getElementById('deliveryEmail').value.trim();
    const password = document.getElementById('deliveryPassword').value;

    if (!name || !email || !password) {
        alert('Please fill all required fields');
        return;
    }

    const userData = {
        name,
        email,
        password,
        role: 'DELIVERY'
    };

    try {
        const response = await fetch('/api/admin/delivery-partners', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            await response.json();
            alert('Delivery partner added successfully!');

            // Clear form
            document.getElementById('deliveryName').value = '';
            document.getElementById('deliveryEmail').value = '';
            document.getElementById('deliveryPassword').value = '';

            // Refresh delivery persons list
            await loadDeliveryPersons();
        } else {
            const error = await response.text();
            alert(`Error: ${error}`);
        }
    } catch (error) {
        console.error('Error adding delivery partner:', error);
        alert('Failed to add delivery partner. Please try again.');
    }
}

// Load delivery stats
async function loadDeliveryStats() {
    try {
        const response = await fetch('/api/admin/delivery/stats');
        if (response.ok) {
            const stats = await response.json();
            renderDeliveryChart(stats);
        }
    } catch (error) {
        console.error('Error loading delivery stats:', error);
    }
}

// Render delivery chart
function renderDeliveryChart(stats) {
    const ctx = document.getElementById('deliveryChart').getContext('2d');

    if (chartInstances.deliveryChart) {
        chartInstances.deliveryChart.destroy();
    }

    chartInstances.deliveryChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Total Delivery Persons', 'Available', 'Average Rating', 'Total Deliveries'],
            datasets: [{
                label: 'Delivery Performance',
                data: [
                    stats.totalDeliveryPersons || 0,
                    stats.availableDeliveryPersons || 0,
                    stats.averageRating || 0,
                    stats.totalDeliveries || 0
                ],
                backgroundColor: ['#007bff', '#28a745', '#ffc107', '#17a2b8'],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

// Load users
async function loadUsers() {
    await loadUserStats();
    await loadAllUsers();
}

// Load user stats
async function loadUserStats() {
    try {
        const response = await fetch('/api/admin/users/stats');
        if (response.ok) {
            const stats = await response.json();
            renderUserStats(stats);
        }
    } catch (error) {
        console.error('Error loading user stats:', error);
    }
}

// Render user stats
function renderUserStats(stats) {
    document.getElementById('totalUsers').textContent = stats.total || 0;
    document.getElementById('totalCustomers').textContent = stats.byRole?.CUSTOMER || 0;
    document.getElementById('totalDelivery').textContent = stats.byRole?.DELIVERY || 0;
    document.getElementById('totalAdmins').textContent = stats.byRole?.ADMIN || 0;
}

// Load all users
async function loadAllUsers() {
    try {
        const response = await fetch('/api/admin/delivery-persons');
        if (response.ok) {
            const users = await response.json();
            renderUsersList(users);
        }
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

// Render users list
function renderUsersList(users) {
    const container = document.getElementById('usersList');
    container.innerHTML = '';

    users.forEach(user => {
        const item = document.createElement('div');
        item.className = 'notification-item';
        item.innerHTML = `
            <div class="notification-header">
                <strong>${user.name}</strong>
                <span class="status-badge-admin">${user.role}</span>
            </div>
            <div style="color: #666; margin: 5px 0;">${user.email}</div>
            <div style="font-size: 12px; color: #666;">
                Phone: ${user.phone || 'N/A'}<br>
                Joined: ${formatDate(user.createdAt)}
            </div>
            ${user.role === 'DELIVERY' ? `
                <div style="font-size: 12px; color: #666; margin-top: 5px;">
                    Vehicle: ${user.vehicleType || 'N/A'} | Available: ${user.isAvailable ? 'Yes' : 'No'}
                </div>
            ` : ''}
            <div style="margin-top: 10px; display: flex; gap: 5px;">
                <button class="action-btn action-edit" onclick="editUser(${user.id})">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="action-btn action-delete" onclick="deleteUser(${user.id})">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </div>
        `;
        container.appendChild(item);
    });
}

// Filter users by role
async function filterUsers(role) {
    await loadAllUsers();
}

// Search users
function searchUsers(query) {
    const items = document.querySelectorAll('#usersList .notification-item');
    items.forEach(item => {
        const text = item.textContent.toLowerCase();
        item.style.display = text.includes(query.toLowerCase()) ? 'block' : 'none';
    });
}

// Toggle delivery fields
function toggleDeliveryFields() {
    const role = document.getElementById('newUserRole').value;
    const deliveryFields = document.getElementById('deliveryFields');
    deliveryFields.style.display = role === 'DELIVERY' ? 'block' : 'none';
}

// Add new user
async function addNewUser() {
    const name = document.getElementById('newUserName').value.trim();
    const email = document.getElementById('newUserEmail').value.trim();
    const password = document.getElementById('newUserPassword').value;
    const role = document.getElementById('newUserRole').value;
    const phone = document.getElementById('newUserPhone').value.trim();

    if (!name || !email || !password || !role) {
        alert('Please fill all required fields');
        return;
    }

    const userData = {
        name,
        email,
        password,
        role,
        phone
    };

    if (role === 'DELIVERY') {
        userData.vehicleType = document.getElementById('newUserVehicle').value.trim();
        userData.vehicleNumber = document.getElementById('newUserVehicleNumber').value.trim();
        userData.currentLocation = document.getElementById('newUserLocation').value.trim();
        userData.isAvailable = document.getElementById('newUserAvailable').checked;
    }

    try {
        const endpoint = role === 'DELIVERY' ? '/api/admin/delivery-partners' : '/api/auth/register';
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            await response.json();
            alert('User added successfully!');

            // Clear form
            document.getElementById('newUserName').value = '';
            document.getElementById('newUserEmail').value = '';
            document.getElementById('newUserPassword').value = '';
            document.getElementById('newUserPhone').value = '';
            document.getElementById('newUserVehicle').value = '';
            document.getElementById('newUserVehicleNumber').value = '';
            document.getElementById('newUserLocation').value = '';

            await loadAllUsers();
            await loadUserStats();
        } else {
            const error = await response.text();
            alert(`Error: ${error}`);
        }
    } catch (error) {
        console.error('Error adding user:', error);
        alert('Failed to add user. Please try again.');
    }
}

// Utility Functions
function formatPrice(price) {
    if (typeof price === 'object' && price !== null) {
        price = price.toString();
    }
    return new Intl.NumberFormat('en-NP', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(price || 0);
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function truncateText(text, length) {
    if (!text) return '';
    return text.length > length ? text.substring(0, length) + '...' : text;
}

function previewProductImage(input) {
    const preview = document.getElementById('productImagePreview');
    preview.innerHTML = '';

    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'image-preview-item';
            preview.appendChild(img);
        }
        reader.readAsDataURL(input.files[0]);
    }
}

function previewStoreImage(input) {
    const preview = document.getElementById('storeImagePreview');
    preview.innerHTML = '';

    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'image-preview-item';
            preview.appendChild(img);
        }
        reader.readAsDataURL(input.files[0]);
    }
}

function logout() {
    localStorage.removeItem('user');
    window.location.href = '/login-admin.html';
}

function setupRealtimeUpdates() {
    // Set up interval to refresh dashboard data every 30 seconds
    setInterval(async () => {
        if (currentView === 'dashboard') {
            await loadDashboardData();
        }
    }, 30000);
}

/* ===========================
   UPDATED: Orders Filter Stub
   =========================== */

function filterOrders(filter) {
    const rows = document.querySelectorAll('#ordersTable tbody tr');
    rows.forEach(row => {
        const statusCell = row.querySelector('td:nth-child(5) .status-badge-admin');
        if (!statusCell) return;

        const status = statusCell.textContent.trim().toLowerCase();

        if (filter === 'all') {
            row.style.display = '';
        } else if (filter === 'today') {
            const dateCell = row.querySelector('td:nth-child(7)');
            if (dateCell && new Date(dateCell.textContent).toDateString() === new Date().toDateString()) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        } else if (filter === 'pending') {
            row.style.display = ['placed', 'assigned', 'picked_up'].includes(status) ? '' : 'none';
        } else if (filter === 'delivered') {
            row.style.display = status === 'delivered' ? '' : 'none';
        } else if (filter === 'cancelled') {
            row.style.display = status === 'cancelled' ? '' : 'none';
        }
    });
}

// Export for global use
window.processReturn = processReturn;
window.addProduct = addProduct;
window.sendNotification = sendNotification;
window.scheduleNotification = scheduleNotification;
window.saveDraft = saveDraft;
window.logout = logout;
window.previewProductImage = previewProductImage;
window.previewStoreImage = previewStoreImage;
window.assignOrderToDelivery = assignOrderToDelivery;
window.addDeliveryPartner = addDeliveryPartner;
window.toggleDeliveryAvailability = toggleDeliveryAvailability;
window.addNewUser = addNewUser;
window.toggleDeliveryFields = toggleDeliveryFields;
window.filterUsers = filterUsers;
window.searchUsers = searchUsers;
window.filterReturns = filterReturns;
window.exportReturns = exportReturns;
window.filterOrders = filterOrders;
window.showCatalogTab = showCatalogTab;
window.addStore = addStore;
window.searchProducts = searchProducts;

// ADDED exports
window.loadOrders = loadOrders;
window.loadMarketing = loadMarketing;
window.loadAnalytics = loadAnalytics;

// Stub functions for unimplemented features
function filterReturns(filter) {
    alert('Filter returns by: ' + filter);
}

function exportReturns() {
    alert('Export returns as CSV');
}

function showCatalogTab(tab) {
    alert('Show catalog tab: ' + tab);
}

function addStore() {
    alert('Add store functionality');
}

function editUser(userId) {
    alert('Edit user ID: ' + userId);
}

function deleteUser(userId) {
    if (confirm('Are you sure you want to delete this user?')) {
        alert('Delete user ID: ' + userId);
    }
}

function viewDeliveryDetails(deliveryId) {
    alert('View delivery details for ID: ' + deliveryId);
}