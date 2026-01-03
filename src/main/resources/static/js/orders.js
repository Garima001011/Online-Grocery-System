// Orders JavaScript
let user = null;
let orders = [];
let currentFilter = 'all';

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    user = JSON.parse(localStorage.getItem('user'));
    if (!user) {
        window.location.href = '/login.html';
        return;
    }

    updateUserGreeting();
    updateCartCount();
    loadOrders();
    setupEventListeners();
});

// Update user greeting
function updateUserGreeting() {
    const greeting = document.getElementById('userGreeting');
    if (greeting && user) {
        greeting.textContent = user.name || user.email.split('@')[0];
    }
}

// Update cart count
function updateCartCount() {
    const cartCount = document.getElementById('cartCount');
    if (cartCount) {
        const cart = JSON.parse(localStorage.getItem('cart')) || [];
        const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
        cartCount.textContent = totalItems;
    }
}

// Load orders
async function loadOrders() {
    try {
        showLoading();
        const response = await fetch(`/api/orders/user/${user.id}`);
        if (response.ok) {
            orders = await response.json();
            console.log('Loaded orders:', orders); // Debug log
            displayOrders();
        } else {
            console.error('Failed to load orders');
            showEmptyState();
        }
    } catch (error) {
        console.error('Error loading orders:', error);
        showEmptyState();
    } finally {
        hideLoading();
    }
}

// Display orders
function displayOrders() {
    const ordersList = document.getElementById('ordersList');
    const emptyOrders = document.getElementById('emptyOrders');

    if (!orders || orders.length === 0) {
        ordersList.innerHTML = '';
        emptyOrders.style.display = 'block';
        return;
    }

    emptyOrders.style.display = 'none';
    ordersList.innerHTML = '';

    // Filter orders based on current selection
    let filteredOrders = orders;
    if (currentFilter === 'delivered') {
        filteredOrders = orders.filter(order => order.status === 'DELIVERED');
    } else if (currentFilter === 'pending') {
        filteredOrders = orders.filter(order => ['PLACED', 'ASSIGNED', 'PICKED_UP'].includes(order.status));
    } else if (currentFilter === 'returns') {
        filteredOrders = orders.filter(order =>
            order.items && order.items.some(item => item.returnStatus && item.returnStatus !== 'NONE')
        );
    }

    if (filteredOrders.length === 0) {
        ordersList.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #666;">
                <i class="fas fa-search" style="font-size: 40px; margin-bottom: 15px;"></i>
                <h3>No orders found</h3>
                <p>Try a different filter</p>
            </div>
        `;
        return;
    }

    filteredOrders.forEach(order => {
        ordersList.appendChild(createOrderCard(order));
    });
}

// Create order card HTML
function createOrderCard(order) {
    const orderDate = new Date(order.createdAt).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });

    // Format status for display
    const statusDisplay = {
        'PLACED': 'Order Placed',
        'ASSIGNED': 'Assigned to Delivery',
        'PICKED_UP': 'Picked Up',
        'DELIVERED': 'Delivered',
        'CANCELLED': 'Cancelled'
    }[order.status] || order.status;

    const totalAmount = order.total || (order.subtotal + order.tax);

    const card = document.createElement('div');
    card.className = 'order-card';
    card.innerHTML = `
        <div class="order-header">
            <div class="order-info">
                <div class="order-id">Order #${order.id}</div>
                <div class="order-date">Placed on ${orderDate}</div>
                <div style="font-size: 14px; color: #565959; margin-top: 5px;">
                    <i class="fas fa-map-marker-alt"></i> ${order.deliveryAddress}
                </div>
                ${order.deliveredAt ? `
                    <div style="font-size: 14px; color: #565959; margin-top: 5px;">
                        <i class="fas fa-check-circle"></i> Delivered on ${new Date(order.deliveredAt).toLocaleDateString()}
                    </div>
                ` : ''}
            </div>
            <div class="order-status">
                <span class="status-badge status-${order.status.toLowerCase()}">
                    ${statusDisplay}
                </span>
                <div style="font-size: 18px; font-weight: bold; color: #b12704; margin-top: 5px;">
                    Rs. ${parseFloat(totalAmount).toFixed(2)}
                </div>
            </div>
        </div>

        ${order.items && order.items.length > 0 ? `
            <div class="order-items">
                ${order.items.map(item => createOrderItemHTML(item, order.id, order.status)).join('')}
            </div>
        ` : '<p style="color: #666; padding: 20px; text-align: center;">No items in this order</p>'}

        <div class="order-actions">
            <button class="auth-button" style="padding: 8px 16px; font-size: 14px;"
                    onclick="trackOrder(${order.id})">
                <i class="fas fa-truck"></i> Track Order
            </button>
            <button class="auth-button" style="padding: 8px 16px; font-size: 14px; background: #f0f2f2; color: #0f1111; margin-left: 10px;"
                    onclick="reorder(${order.id})">
                <i class="fas fa-redo"></i> Buy Again
            </button>
        </div>
    `;

    return card;
}

// Create order item HTML
function createOrderItemHTML(item, orderId, orderStatus) {
    const productName = item.product?.name || item.productName || 'Product';
    const priceAtPurchase = item.priceAtPurchase || 0;
    const quantity = item.quantity || 1;
    const returnStatus = item.returnStatus || 'NONE';
    const refundAmount = item.refundAmount || 0;

    // Create return status badge if applicable
    const returnStatusBadge = returnStatus !== 'NONE' ? `
        <span class="return-status return-${returnStatus.toLowerCase()}">
            ${returnStatus}${refundAmount > 0 ? ` (Rs. ${refundAmount.toFixed(2)})` : ''}
        </span>
    ` : '';

    // Check if item can be returned
    const isDelivered = orderStatus === 'DELIVERED';
    const isWithinReturnPeriod = isDelivered &&
        (!item.returnRequestedAt || new Date(item.returnRequestedAt) > new Date(Date.now() - 7 * 24 * 60 * 60 * 1000));
    const canReturn = returnStatus === 'NONE' && isDelivered && isWithinReturnPeriod;

    // Get product image or icon
    const productIcon = getProductIcon(productName);

    return `
        <div class="order-item">
            <div class="order-item-image">
                ${productIcon}
            </div>
            <div class="order-item-details">
                <div class="order-item-name">${productName}</div>
                <div class="order-item-meta">
                    Quantity: ${quantity} × Rs. ${priceAtPurchase.toFixed(2)}
                    ${returnStatusBadge}
                </div>
                <div class="order-item-price">
                    Rs. ${(quantity * priceAtPurchase).toFixed(2)}
                </div>
                ${canReturn ? `
                    <div class="order-item-actions">
                        <button class="auth-button" style="padding: 6px 12px; font-size: 13px;"
                                onclick="requestReturn(${orderId}, ${item.id}, '${productName.replace(/'/g, "\\'")}', ${priceAtPurchase})">
                            <i class="fas fa-undo"></i> Return Item
                        </button>
                    </div>
                ` : ''}
            </div>
        </div>
    `;
}

// Get product icon based on name
function getProductIcon(productName) {
    const name = productName.toLowerCase();

    if (name.includes('rice') || name.includes('dal') || name.includes('daal') || name.includes('grain')) {
        return '<i class="fas fa-seedling" style="font-size: 40px; color: #999;"></i>';
    } else if (name.includes('oil') || name.includes('ghee')) {
        return '<i class="fas fa-oil-can" style="font-size: 40px; color: #999;"></i>';
    } else if (name.includes('spice') || name.includes('masala')) {
        return '<i class="fas fa-mortar-pestle" style="font-size: 40px; color: #999;"></i>';
    } else if (name.includes('noodle') || name.includes('snack')) {
        return '<i class="fas fa-cookie-bite" style="font-size: 40px; color: #999;"></i>';
    } else if (name.includes('dairy') || name.includes('milk') || name.includes('cheese')) {
        return '<i class="fas fa-cheese" style="font-size: 40px; color: #999;"></i>';
    } else if (name.includes('fruit') || name.includes('apple') || name.includes('banana')) {
        return '<i class="fas fa-apple-alt" style="font-size: 40px; color: #999;"></i>';
    } else if (name.includes('beverage') || name.includes('drink') || name.includes('cola')) {
        return '<i class="fas fa-wine-bottle" style="font-size: 40px; color: #999;"></i>';
    } else {
        return '<i class="fas fa-box" style="font-size: 40px; color: #999;"></i>';
    }
}

// Filter orders
function filterOrders(filter) {
    currentFilter = filter;

    // Update active tab
    document.querySelectorAll('.order-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    event.target.classList.add('active');

    displayOrders();
}

// Request return
function requestReturn(orderId, itemId, itemName, itemPrice) {
    document.getElementById('returnOrderId').value = orderId;
    document.getElementById('returnItemId').value = itemId;
    document.getElementById('returnItemName').textContent = itemName;
    document.getElementById('returnOrderInfo').textContent = `Order #${orderId}`;
    document.getElementById('returnItemPrice').textContent = `Price: Rs. ${itemPrice.toFixed(2)}`;

    document.getElementById('returnModal').style.display = 'flex';
}

// Close return modal
function closeReturnModal() {
    document.getElementById('returnModal').style.display = 'none';
    document.getElementById('returnForm').reset();
}

// Submit return request
async function submitReturnRequest(event) {
    event.preventDefault();

    const orderId = document.getElementById('returnOrderId').value;
    const itemId = document.getElementById('returnItemId').value;
    const reason = document.getElementById('returnReason').value;
    const description = document.getElementById('returnDescription').value;

    if (!reason) {
        alert('Please select a return reason');
        return;
    }

    try {
        const response = await fetch(`/api/orders/${orderId}/items/${itemId}/return`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                reason: reason,
                description: description,
                policy: 'NEPAL CAN MOVE FAST'
            })
        });

        if (response.ok) {
            const result = await response.json();
            alert('Return request submitted successfully! Nepal Can Move Fast will contact you for pickup.');
            closeReturnModal();
            loadOrders(); // Reload orders
        } else {
            const error = await response.text();
            alert(`Failed to submit return: ${error}`);
        }
    } catch (error) {
        console.error('Error submitting return:', error);
        alert('Failed to submit return request. Please try again.');
    }
}

// Show return policy
function showReturnPolicy() {
    document.getElementById('policyModal').style.display = 'flex';
}

// Close policy modal
function closePolicyModal() {
    document.getElementById('policyModal').style.display = 'none';
}

// Track order
async function trackOrder(orderId) {
    try {
        const response = await fetch(`/api/orders/${orderId}`);
        if (response.ok) {
            const order = await response.json();

            let trackingInfo = `Order #${orderId}\n`;
            trackingInfo += `Status: ${order.status}\n`;
            trackingInfo += `Placed: ${new Date(order.createdAt).toLocaleString()}\n`;

            if (order.deliveredAt) {
                trackingInfo += `Delivered: ${new Date(order.deliveredAt).toLocaleString()}\n`;
            } else if (order.pickedUpAt) {
                trackingInfo += `Picked up: ${new Date(order.pickedUpAt).toLocaleString()}\n`;
            } else if (order.assignedAt) {
                trackingInfo += `Assigned to delivery: ${new Date(order.assignedAt).toLocaleString()}\n`;
            }

            if (order.deliveryPerson) {
                trackingInfo += `Delivery person assigned\n`;
            }

            trackingInfo += `\nDelivery Address: ${order.deliveryAddress}`;

            alert(trackingInfo);
        } else {
            alert('Could not load tracking information.');
        }
    } catch (error) {
        console.error('Error tracking order:', error);
        alert('Error loading tracking information.');
    }
}

// Reorder
async function reorder(orderId) {
    try {
        const response = await fetch(`/api/orders/${orderId}`);
        if (response.ok) {
            const order = await response.json();

            if (order.items && order.items.length > 0) {
                let cart = JSON.parse(localStorage.getItem('cart')) || [];

                order.items.forEach(item => {
                    const existingItem = cart.find(ci => ci.productId === item.product?.id);

                    if (existingItem) {
                        existingItem.quantity += item.quantity;
                    } else {
                        cart.push({
                            productId: item.product?.id,
                            name: item.product?.name,
                            price: item.priceAtPurchase,
                            quantity: item.quantity
                        });
                    }
                });

                localStorage.setItem('cart', JSON.stringify(cart));
                updateCartCount();
                alert('Items added to cart successfully!');
            } else {
                alert('No items to reorder.');
            }
        } else {
            alert('Could not load order details.');
        }
    } catch (error) {
        console.error('Error reordering:', error);
        alert('Error adding items to cart.');
    }
}

// Search orders
function searchOrders() {
    const searchInput = document.getElementById('searchInput').value.toLowerCase();
    const searchTerm = searchInput.trim();

    if (!searchTerm) {
        displayOrders();
        return;
    }

    const filteredOrders = orders.filter(order => {
        // Search by order ID
        if (order.id.toString().includes(searchTerm)) return true;

        // Search in order items
        if (order.items) {
            const foundInItems = order.items.some(item => {
                const itemName = item.product?.name || '';
                return itemName.toLowerCase().includes(searchTerm);
            });
            if (foundInItems) return true;
        }

        // Search in delivery address
        if (order.deliveryAddress && order.deliveryAddress.toLowerCase().includes(searchTerm)) {
            return true;
        }

        return false;
    });

    // Create temporary display
    const ordersList = document.getElementById('ordersList');
    ordersList.innerHTML = '';

    if (filteredOrders.length === 0) {
        ordersList.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #666;">
                <i class="fas fa-search" style="font-size: 40px; margin-bottom: 15px;"></i>
                <h3>No orders matching "${searchTerm}"</h3>
                <p>Try searching with different terms</p>
            </div>
        `;
        return;
    }

    filteredOrders.forEach(order => {
        ordersList.appendChild(createOrderCard(order));
    });
}

// Setup event listeners
function setupEventListeners() {
    // Return form submission
    document.getElementById('returnForm').addEventListener('submit', submitReturnRequest);

    // Close modals on outside click
    window.addEventListener('click', (event) => {
        const returnModal = document.getElementById('returnModal');
        const policyModal = document.getElementById('policyModal');

        if (event.target === returnModal) {
            closeReturnModal();
        }
        if (event.target === policyModal) {
            closePolicyModal();
        }
    });

    // Search input enter key
    document.getElementById('searchInput')?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            searchOrders();
        }
    });
}

// Show loading state
function showLoading() {
    const ordersList = document.getElementById('ordersList');
    if (ordersList) {
        ordersList.innerHTML = `
            <div style="text-align: center; padding: 50px;">
                <i class="fas fa-spinner fa-spin" style="font-size: 40px; color: var(--amazon-orange);"></i>
                <p>Loading your orders...</p>
            </div>
        `;
    }
}

// Hide loading state
function hideLoading() {
    // Loading state is cleared by displayOrders
}

// Show empty state
function showEmptyState() {
    const ordersList = document.getElementById('ordersList');
    const emptyOrders = document.getElementById('emptyOrders');

    if (ordersList) ordersList.innerHTML = '';
    if (emptyOrders) emptyOrders.style.display = 'block';
}

// Export functions for global use
window.filterOrders = filterOrders;
window.requestReturn = requestReturn;
window.closeReturnModal = closeReturnModal;
window.submitReturnRequest = submitReturnRequest;
window.showReturnPolicy = showReturnPolicy;
window.closePolicyModal = closePolicyModal;
window.trackOrder = trackOrder;
window.reorder = reorder;
window.searchOrders = searchOrders;