let cart = JSON.parse(localStorage.getItem('cart')) || [];
let promoDiscount = 0;

document.addEventListener('DOMContentLoaded', () => {
    const user = JSON.parse(localStorage.getItem('user'));
    if (user) {
        document.getElementById('userGreeting').textContent = user.email.split('@')[0];
    }

    // Ensure all cart items have the selected property and weightKg
    cart = cart.map(item => ({
        ...item,
        selected: item.selected !== undefined ? item.selected : true,
        weightKg: item.weightKg || 0  // Ensure weightKg exists
    }));
    saveCart();

    loadCart();
    loadRecommendedItems();
    setupPaymentToggle();
});

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

    let cartHTML = `
        <div style="display: flex; align-items: center; padding: 10px 0; border-bottom: 1px solid #ddd; margin-bottom: 10px;">
            <div style="width: 30px; margin-right: 15px;">
                <input type="checkbox" id="selectAll" ${cart.every(item => item.selected) ? 'checked' : ''} onchange="toggleSelectAll(this.checked)">
            </div>
            <div style="flex:1; font-weight: bold;">Product</div>
            <div style="width: 80px; text-align: center; font-weight: bold;">Weight</div>
            <div style="width: 120px; text-align: center; font-weight: bold;">Quantity</div>
            <div style="width: 100px; text-align: right; font-weight: bold;">Price</div>
            <div style="width: 80px; text-align: center; font-weight: bold;">Remove</div>
        </div>
    `;
    let subtotal = 0;

    cart.forEach((item, index) => {
        const itemTotal = item.price * item.quantity;
        const itemWeight = (item.weightKg || 0) * item.quantity;
        if (item.selected) subtotal += itemTotal;

        cartHTML += `
            <div class="cart-item" id="cartItem-${index}" style="display: flex; align-items: center; padding: 15px 0; border-bottom: 1px solid #eee;">
                <div style="width: 30px; margin-right: 15px;">
                    <input type="checkbox" ${item.selected ? 'checked' : ''} onchange="toggleItemSelection(${index}, this.checked)">
                </div>
                <div style="flex:1; display: flex; align-items: center;">
                    ${item.imageUrl
                      ? `<img src="${item.imageUrl}" alt="${item.name}" style="width: 80px; height: 80px; object-fit: cover; border-radius: 4px; margin-right: 20px;">`
                      : `<i class="fas fa-shopping-basket" style="font-size: 60px; color: #ddd; margin-right: 20px;"></i>`
                    }
                    <div class="cart-item-details">
                        <h3 class="cart-item-title">${item.name}</h3>
                        <div class="cart-item-price">Rs. ${item.price.toFixed(2)}</div>
                        ${item.weightKg ? `<div class="cart-item-weight" style="font-size: 12px; color: #666;">${item.weightKg} kg per item</div>` : ''}
                    </div>
                </div>

                <div style="width: 80px; text-align: center; font-size: 14px; color: #666;">
                    ${itemWeight.toFixed(2)} kg
                </div>

                <div class="cart-item-quantity" style="width: 120px; text-align: center;">
                    <button class="quantity-btn" onclick="updateQuantity(${index}, -1)">-</button>
                    <input type="text" class="quantity-input" value="${item.quantity}" readonly style="width: 40px; text-align: center;">
                    <button class="quantity-btn" onclick="updateQuantity(${index}, 1)">+</button>
                </div>

                <div style="width: 100px; text-align: right; font-weight: bold; color: var(--amazon-price);">
                    Rs. ${itemTotal.toFixed(2)}
                </div>

                <div style="width: 80px; text-align: center;">
                    <span class="remove-item" onclick="removeItem(${index})" style="cursor: pointer; color: #d93025;">
                        <i class="fas fa-trash"></i>
                    </span>
                </div>
            </div>
        `;
    });

    if (cartItemsContainer) {
        cartItemsContainer.innerHTML = cartHTML;
    }

    updateOrderSummary(subtotal);
}

function toggleItemSelection(index, checked) {
    cart[index].selected = checked;
    saveCart();
    loadCart();
}

function toggleSelectAll(checked) {
    cart = cart.map(item => ({ ...item, selected: checked }));
    saveCart();
    loadCart();
}

function updateQuantity(index, change) {
    const item = cart[index];
    const newQuantity = item.quantity + change;

    if (newQuantity < 1) {
        removeItem(index);
        return;
    }

    item.quantity = newQuantity;
    saveCart();
    loadCart();
}

function removeItem(index) {
    if (confirm('Are you sure you want to remove this item from your cart?')) {
        cart.splice(index, 1);
        saveCart();
        loadCart();
    }
}

function saveCart() {
    localStorage.setItem('cart', JSON.stringify(cart));
}

// Calculate shipping fee based on weight (matches backend logic)
function calculateShippingFee(totalWeight) {
    if (totalWeight <= 0) return 0;
    if (totalWeight <= 1) return 100;
    return 100 + (Math.ceil(totalWeight - 1) * 50);
}

function updateOrderSummary(subtotal) {
    // Calculate total weight from selected items
    const totalWeight = cart.reduce((sum, item) => {
        if (item.selected && item.weightKg) {
            return sum + (item.weightKg * item.quantity);
        }
        return sum;
    }, 0);

    // Calculate shipping based on weight (matching backend logic)
    const shipping = calculateShippingFee(totalWeight);
    const tax = subtotal * 0.13;
    const discount = subtotal * promoDiscount;
    const total = subtotal + shipping + tax - discount;

    // Update all summary elements
    const itemsSubtotalEl = document.getElementById('itemsSubtotal');
    const shippingFeeEl = document.getElementById('shippingFee');
    const weightInfoEl = document.getElementById('weightInfo');
    const taxAmountEl = document.getElementById('taxAmount');
    const promoDiscountEl = document.getElementById('promoDiscount');
    const orderTotalEl = document.getElementById('orderTotal');

    if (itemsSubtotalEl) itemsSubtotalEl.textContent = `Rs. ${subtotal.toFixed(2)}`;
    if (shippingFeeEl) shippingFeeEl.textContent = `Rs. ${shipping.toFixed(2)}`;
    if (taxAmountEl) taxAmountEl.textContent = `Rs. ${tax.toFixed(2)}`;
    if (promoDiscountEl) promoDiscountEl.textContent = `- Rs. ${discount.toFixed(2)}`;
    if (orderTotalEl) orderTotalEl.textContent = `Rs. ${total.toFixed(2)}`;

    // Display weight info
    if (weightInfoEl) {
        weightInfoEl.innerHTML = `
            <span>Total weight:</span>
            <span>${totalWeight.toFixed(2)} kg</span>
            <span style="font-size: 11px; color: #666; margin-left: 10px;">(Shipping: Rs. ${shipping.toFixed(2)})</span>
        `;
    }

    const selectedCount = cart.filter(item => item.selected).length;
    const itemCountEl = document.getElementById('itemCount');
    if (itemCountEl) {
        itemCountEl.textContent = selectedCount;
    }

    const checkoutBtn = document.getElementById('checkoutBtn');
    if (checkoutBtn) {
        checkoutBtn.textContent = `Proceed to Checkout (${selectedCount} item${selectedCount !== 1 ? 's' : ''})`;
    }

    // Update the shipping breakdown in checkout summary if it exists
    updateShippingBreakdown(totalWeight, shipping);
}

// Optional: Add a detailed shipping breakdown
function updateShippingBreakdown(totalWeight, shipping) {
    const shippingBreakdown = document.getElementById('shippingBreakdown');
    if (shippingBreakdown && totalWeight > 0) {
        let breakdownText = '';
        if (totalWeight <= 1) {
            breakdownText = 'Base fee (up to 1kg): Rs. 100';
        } else {
            const extraKg = Math.ceil(totalWeight - 1);
            const extraFee = extraKg * 50;
            breakdownText = `Base fee (1kg): Rs. 100 + Extra ${extraKg}kg: Rs. ${extraFee} = Rs. ${shipping}`;
        }
        shippingBreakdown.textContent = breakdownText;
        shippingBreakdown.style.display = 'block';
    } else if (shippingBreakdown) {
        shippingBreakdown.style.display = 'none';
    }
}

function applyPromoCode() {
    const promoCode = document.getElementById('promoCode').value.toUpperCase();
    const promoMessage = document.getElementById('promoMessage');

    const validCodes = {
      // Festivals
      'DASHAIN10': 0.10,
      'TIHAR20': 0.20,
      'HOLI15': 0.15,
      'CHHATH20': 0.20,
      'MAGHESANKRANTI10': 0.10,
      'TEEJ15': 0.15,

      // Nepal's Day
      'REPUBLIC20': 0.20,
      'CONSTITUTION25': 0.25,
      'DEMOCRACY15': 0.15,
      'NATIONALDAY30': 0.30,

      // Tourism & Pride
      'EVEREST30': 0.30,
      'VISITNEPAL20': 0.20,
      'ANNAPURNA15': 0.15,
      'LUMBINI25': 0.25,

      // Shopping & Seasonal Sales
      'NEWYEAR20': 0.20,
      'MONSOON15': 0.15,
      'WINTER25': 0.25,
      'SUMMER10': 0.10,

      // Special Days
      'STUDENT20': 0.20,
      'FAMILY40': 0.40
    };

    if (validCodes[promoCode]) {
        promoDiscount = validCodes[promoCode];
        promoMessage.innerHTML = `<span style="color: green;"><i class="fas fa-check-circle"></i> Promo code applied! ${promoCode} discount: ${promoDiscount * 100}%</span>`;
    } else {
        promoDiscount = 0;
        promoMessage.innerHTML = `<span style="color: red;"><i class="fas fa-times-circle"></i> Invalid promo code</span>`;
    }

    const subtotal = cart.reduce((sum, item) => sum + (item.selected ? item.price * item.quantity : 0), 0);
    updateOrderSummary(subtotal);
}

function setupPaymentToggle() {
    const paymentRadios = document.querySelectorAll('input[name="payment"]');
    const cardDetails = document.getElementById('cardDetails');

    if (paymentRadios && cardDetails) {
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
}

async function loadRecommendedItems() {
    const recommendedItems = document.getElementById('recommendedItems');
    if (!recommendedItems) return;

    try {
        const response = await fetch('/api/products');
        const products = await response.json();

        const randomProducts = [...products].sort(() => Math.random() - 0.5).slice(0, 4);

        recommendedItems.innerHTML = '';
        randomProducts.forEach(product => {
            const imageHtml = product.imageUrl
              ? `<img src="${product.imageUrl}" alt="${product.name}" style="width: 100%; height: 120px; object-fit: cover; border-radius: 4px;">`
              : `<i class="fas fa-shopping-basket" style="font-size: 40px; color: #ddd;"></i>`;

            recommendedItems.innerHTML += `
                <div class="product-card">
                    <div style="text-align: center; height: 120px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 4px; overflow: hidden;">
                        ${imageHtml}
                    </div>
                    <h4 style="font-size: 14px; margin: 10px 0; height: 40px; overflow: hidden;">${product.name}</h4>
                    <div style="color: var(--amazon-price); font-weight: bold; margin-bottom: 10px;">
                        Rs. ${product.price.toFixed(2)}
                    </div>
                    <div style="font-size: 11px; color: #666; margin-bottom: 10px;">
                        ${product.weightKg ? `${product.weightKg} kg` : 'Weight not specified'}
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

async function addToCartFromRecommendation(productId) {
    try {
        // Fetch the full product details including weight
        const response = await fetch(`/api/products/${productId}`);
        const product = await response.json();

        addToCart(product);
    } catch (error) {
        console.error('Error adding product to cart:', error);
        alert('Failed to add item to cart');
    }
}

// Function to add product to cart (call this from shop page)
function addToCart(product) {
    const existingItem = cart.find(item => item.productId === product.id);

    if (existingItem) {
        existingItem.quantity++;
    } else {
        cart.push({
            productId: product.id,
            name: product.name,
            price: product.price,
            weightKg: product.weightKg || 0,
            imageUrl: product.imageUrl,
            quantity: 1,
            selected: true
        });
    }

    saveCart();
    loadCart();

    // Show success message
    const toast = document.createElement('div');
    toast.innerHTML = `✓ Added ${product.name} to cart`;
    toast.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        background: #4CAF50;
        color: white;
        padding: 12px 20px;
        border-radius: 4px;
        z-index: 1000;
        animation: slideIn 0.3s ease;
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 2000);
}

async function placeOrder() {
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user) {
        alert('Please login to place an order');
        window.location.href = '/login.html';
        return;
    }

    const deliveryAddress = document.getElementById('deliveryAddress');
    if (!deliveryAddress || !deliveryAddress.value.trim()) {
        alert('Please enter your delivery address');
        return;
    }

    const paymentMethod = document.querySelector('input[name="payment"]:checked');
    if (!paymentMethod) {
        alert('Please select a payment method');
        return;
    }

    const selectedItems = cart.filter(item => item.selected);
    if (selectedItems.length === 0) {
        alert('Please select at least one item to checkout');
        return;
    }

    // Prepare order items
    const items = selectedItems.map(item => ({
        productId: item.productId,
        quantity: item.quantity
    }));

    // Calculate totals (matching backend calculation)
    const subtotal = selectedItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const totalWeight = selectedItems.reduce((sum, item) => sum + ((item.weightKg || 0) * item.quantity), 0);
    const shipping = calculateShippingFee(totalWeight);
    const tax = subtotal * 0.13;
    const discount = subtotal * promoDiscount;
    const total = subtotal + shipping + tax - discount;

    const promoCode = document.getElementById('promoCode');

    const orderData = {
        userId: user.id,
        deliveryAddress: deliveryAddress.value.trim(),
        items: items,
        subtotal: subtotal,
        tax: tax,
        shippingFee: shipping,
        total: total,
        promoCode: promoCode ? promoCode.value || null : null,
        paymentMethod: paymentMethod.value
    };

    try {
        const response = await fetch('/api/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(orderData)
        });

        if (response.ok) {
            const order = await response.json();

            // Remove only the selected items from cart
            cart = cart.filter(item => !item.selected);
            saveCart();

            alert(`Order placed successfully! Order ID: ${order.id}\nTotal: Rs. ${total.toFixed(2)}`);

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

// Make functions globally available
window.toggleItemSelection = toggleItemSelection;
window.toggleSelectAll = toggleSelectAll;
window.updateQuantity = updateQuantity;
window.removeItem = removeItem;
window.applyPromoCode = applyPromoCode;
window.placeOrder = placeOrder;
window.addToCart = addToCart;