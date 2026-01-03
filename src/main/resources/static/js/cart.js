// Amazon-like Cart JavaScript
let cart = JSON.parse(localStorage.getItem('cart')) || [];
let promoDiscount = 0;

document.addEventListener('DOMContentLoaded', () => {
    const user = JSON.parse(localStorage.getItem('user'));
    if (user) {
        document.getElementById('userGreeting').textContent = user.email.split('@')[0];
    }

    loadCart();
    loadRecommendedItems();
    setupPaymentToggle();
});

// Load cart items
function loadCart() {
    const cartItemsContainer = document.getElementById('cartItemsContainer');
    const cartSummary = document.querySelector('.checkout-summary');

    if (cart.length === 0) {
        if (cartItemsContainer) {
            cartItemsContainer.innerHTML = `
                <div style="text-align: center; padding: 40px;">
                    <i class="fas fa-shopping-cart" style="font-size: 60px; color: #ccc; margin-bottom: 20px;"></i>
                    <h3>Your cart is empty</h3>
                    <p>Add items to your cart to see them here</p>
                    <a href="/shop.html" style="display: inline-block; margin-top: 20px; padding: 10px 20px; background: var(--amazon-orange); color: black; text-decoration: none; border-radius: 4px;">
                        Start Shopping
                    </a>
                </div>
            `;
        }
        if (cartSummary) cartSummary.style.display = 'none';
        return;
    }

    if (cartSummary) cartSummary.style.display = 'block';

    let cartHTML = '';
    let subtotal = 0;

    cart.forEach((item, index) => {
        const itemTotal = item.price * item.quantity;
        subtotal += itemTotal;

        cartHTML += `
            <div class="cart-item" id="cartItem-${index}">
                <div style="display: flex; align-items: center;">
                    <i class="fas fa-shopping-basket" style="font-size: 60px; color: #ddd; margin-right: 20px;"></i>
                    <div class="cart-item-details">
                        <h3 class="cart-item-title">${item.name}</h3>
                        <div class="cart-item-price">Rs. ${item.price.toFixed(2)}</div>

                        <div class="cart-item-quantity">
                            <button class="quantity-btn" onclick="updateQuantity(${index}, -1)">-</button>
                            <input type="text" class="quantity-input" value="${item.quantity}" readonly>
                            <button class="quantity-btn" onclick="updateQuantity(${index}, 1)">+</button>
                            <span style="margin-left: 20px;" class="remove-item" onclick="removeItem(${index})">
                                <i class="fas fa-trash"></i> Delete
                            </span>
                        </div>

                        <div style="font-weight: bold; color: var(--amazon-price); margin-top: 10px;">
                            Rs. ${itemTotal.toFixed(2)}
                        </div>
                    </div>
                </div>
            </div>
        `;
    });

    if (cartItemsContainer) {
        cartItemsContainer.innerHTML = cartHTML;
    }

    updateOrderSummary(subtotal);
}

// Update item quantity
function updateQuantity(index, change) {
    const item = cart[index];
    const newQuantity = item.quantity + change;

    if (newQuantity < 1) {
        removeItem(index);
        return;
    }

    // Here you would typically check stock with the server
    // For now, we'll assume stock is available

    item.quantity = newQuantity;
    saveCart();
    loadCart();
}

// Remove item from cart
function removeItem(index) {
    if (confirm('Are you sure you want to remove this item from your cart?')) {
        cart.splice(index, 1);
        saveCart();
        loadCart();
    }
}

// Save cart to localStorage
function saveCart() {
    localStorage.setItem('cart', JSON.stringify(cart));
}

// Update order summary
function updateOrderSummary(subtotal) {
    const shipping = 50;
    const tax = subtotal * 0.13;
    const discount = subtotal * promoDiscount;
    const total = subtotal + shipping + tax - discount;

    document.getElementById('itemsSubtotal').textContent = `Rs. ${subtotal.toFixed(2)}`;
    document.getElementById('taxAmount').textContent = `Rs. ${tax.toFixed(2)}`;
    document.getElementById('promoDiscount').textContent = `- Rs. ${discount.toFixed(2)}`;
    document.getElementById('orderTotal').textContent = `Rs. ${total.toFixed(2)}`;
}

// Apply promo code
function applyPromoCode() {
    const promoCode = document.getElementById('promoCode').value.toUpperCase();
    const promoMessage = document.getElementById('promoMessage');

    const validCodes = {
        'WELCOME10': 0.10,
        'SAVE20': 0.20,
        'FREESHIP': 0.05
    };

    if (validCodes[promoCode]) {
        promoDiscount = validCodes[promoCode];
        promoMessage.innerHTML = `<span style="color: green;"><i class="fas fa-check-circle"></i> Promo code applied! ${promoCode} discount: ${promoDiscount * 100}%</span>`;
    } else {
        promoDiscount = 0;
        promoMessage.innerHTML = `<span style="color: red;"><i class="fas fa-times-circle"></i> Invalid promo code</span>`;
    }

    // Recalculate totals
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    updateOrderSummary(subtotal);
}

// Setup payment method toggle
function setupPaymentToggle() {
    const paymentRadios = document.querySelectorAll('input[name="payment"]');
    const cardDetails = document.getElementById('cardDetails');

    paymentRadios.forEach(radio => {
        radio.addEventListener('change', () => {
            if (radio.value === 'CARD') {
                cardDetails.style.display = 'block';
            } else {
                cardDetails.style.display = 'none';
            }
        });
    });
}

// Load recommended items
async function loadRecommendedItems() {
    const recommendedItems = document.getElementById('recommendedItems');
    if (!recommendedItems) return;

    try {
        const response = await fetch('/api/products');
        const products = await response.json();

        // Get 4 random products
        const randomProducts = [...products]
            .sort(() => Math.random() - 0.5)
            .slice(0, 4);

        recommendedItems.innerHTML = '';
        randomProducts.forEach(product => {
            recommendedItems.innerHTML += `
                <div class="product-card">
                    <div style="text-align: center;">
                        <i class="fas fa-shopping-basket" style="font-size: 40px; color: #ddd;"></i>
                    </div>
                    <h4 style="font-size: 14px; margin: 10px 0; height: 40px; overflow: hidden;">${product.name}</h4>
                    <div style="color: var(--amazon-price); font-weight: bold; margin-bottom: 10px;">
                        Rs. ${product.price.toFixed(2)}
                    </div>
                    <button onclick="addToCartFromRecommendation(${product.id})" style="width: 100%; padding: 5px; background: var(--amazon-light-gray); border: 1px solid var(--amazon-border); border-radius: 4px; cursor: pointer;">
                        Add to Cart
                    </button>
                </div>
            `;
        });
    } catch (error) {
        console.error('Error loading recommended items:', error);
    }
}

// Add to cart from recommendations
function addToCartFromRecommendation(productId) {
    // Add item to cart
    const item = {
        productId: productId,
        quantity: 1
    };

    // For simplicity, we'll just reload the page
    // In a real app, you'd make an API call to get product details
    alert('Item added to cart!');
    // Reload cart
    loadCart();
}

// Place order
async function placeOrder() {
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user) {
        alert('Please login to place an order');
        window.location.href = '/login.html';
        return;
    }

    const deliveryAddress = document.getElementById('deliveryAddress').value;
    if (!deliveryAddress.trim()) {
        alert('Please enter your delivery address');
        return;
    }

    const paymentMethod = document.querySelector('input[name="payment"]:checked').value;

    // Prepare order items
    const items = cart.map(item => ({
        productId: item.productId,
        quantity: item.quantity
    }));

    // Calculate totals
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const shipping = 50;
    const tax = subtotal * 0.13;
    const discount = subtotal * promoDiscount;
    const total = subtotal + shipping + tax - discount;

    try {
        const response = await fetch('/api/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: user.id,
                deliveryAddress: deliveryAddress,
                items: items,
                subtotal: subtotal,
                tax: tax,
                total: total,
                promoCode: document.getElementById('promoCode').value || null,
                paymentMethod: paymentMethod
            })
        });

        if (response.ok) {
            const order = await response.json();

            // Clear cart
            cart = [];
            saveCart();

            // Show success message
            alert(`Order placed successfully! Order ID: ${order.id}`);

            // Redirect to order confirmation page
            window.location.href = `/order-confirmation.html?orderId=${order.id}`;
        } else {
            const error = await response.text();
            alert(`Order failed: ${error}`);
        }
    } catch (error) {
        console.error('Error placing order:', error);
        alert('Failed to place order. Please try again.');
    }
}