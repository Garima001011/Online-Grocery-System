const user = JSON.parse(localStorage.getItem("user") || "null");
if (!user) window.location.href = "/login.html";

document.getElementById("who").innerText = `${user.email} • ${user.role}`;

let cart = JSON.parse(localStorage.getItem("cart") || "[]");

let stores = [];
let categories = [];
let currentStoreId = null;
let currentCategoryId = null;
let currentQ = "";

// Tax and promo variables
const TAX_RATE = 0.13;
let promoDiscountRate = 0; // e.g., 0.10 for 10%

init();

async function init() {
  await Promise.all([loadStores(), loadCategories()]);
  hydrateFilters();
  await loadProducts();
  renderCart();
}

async function loadStores() {
  const r = await fetch("/api/stores");
  stores = await r.json();
}

async function loadCategories() {
  const r = await fetch("/api/categories");
  categories = await r.json();
}

function hydrateFilters() {
  const storeSel = document.getElementById("storeSelect");
  storeSel.innerHTML = `<option value="">All Stores</option>` +
    stores.map(s => `<option value="${s.id}">${s.name}</option>`).join("");

  const catSel = document.getElementById("categorySelect");
  catSel.innerHTML = `<option value="">All Categories</option>` +
    categories.map(c => `<option value="${c.id}">${c.name}</option>`).join("");
}

function applyFilters() {
  currentQ = document.getElementById("q").value.trim();
  currentStoreId = document.getElementById("storeSelect").value || null;
  currentCategoryId = document.getElementById("categorySelect").value || null;
  loadProducts();
}

function resetFilters() {
  document.getElementById("q").value = "";
  document.getElementById("storeSelect").value = "";
  document.getElementById("categorySelect").value = "";
  currentQ = "";
  currentStoreId = null;
  currentCategoryId = null;
  loadProducts();
}

async function loadProducts() {
  const params = new URLSearchParams();
  if (currentQ) params.set("q", currentQ);
  if (currentStoreId) params.set("storeId", currentStoreId);
  if (currentCategoryId) params.set("categoryId", currentCategoryId);

  const r = await fetch("/api/products" + (params.toString() ? `?${params}` : ""));
  const products = await r.json();

  const list = document.getElementById("productList");
  list.innerHTML = "";

  products.forEach(p => {
    const li = document.createElement("div");
    li.className = "item";

    const storeName = p.store ? p.store.name : "—";
    const catName = p.category ? p.category.name : "—";

    li.innerHTML = `
      <div>
        <strong>${p.name}</strong>
        <div class="meta">${catName} • ${storeName} • Stock: ${p.stock}</div>
        <div class="meta">Price: Rs. ${formatNpr(p.price)}</div>
      </div>
      <div class="actions">
        <button ${p.stock <= 0 ? "disabled" : ""} onclick="addToCart(${p.id}, '${escapeQuotes(p.name)}', ${p.price}, ${p.stock}, ${p.store ? p.store.id : "null"})">
          Add
        </button>
      </div>
    `;
    list.appendChild(li);
  });
}

function addToCart(productId, name, price, stock, storeId) {
  // Standard rule: cart should be from one store at a time (realistic local delivery)
  const cartStoreId = getCartStoreId();
  if (cartStoreId && storeId && cartStoreId !== storeId) {
    alert("Cart contains items from another store. Clear cart to switch stores.");
    return;
  }

  const item = cart.find(i => i.productId === productId);
  if (item) {
    if (item.quantity + 1 > stock) {
      alert("Not enough stock.");
      return;
    }
    item.quantity++;
  } else {
    cart.push({ productId, name, price, quantity: 1, storeId });
  }
  saveCart();
}

function decQty(productId) {
  const item = cart.find(i => i.productId === productId);
  if (!item) return;
  item.quantity--;
  if (item.quantity <= 0) cart = cart.filter(i => i.productId !== productId);
  saveCart();
}

function incQty(productId) {
  const item = cart.find(i => i.productId === productId);
  if (!item) return;
  item.quantity++;
  saveCart();
}

function removeFromCart(productId) {
  cart = cart.filter(i => i.productId !== productId);
  saveCart();
}

function clearCart() {
  cart = [];
  saveCart();
}

function saveCart() {
  localStorage.setItem("cart", JSON.stringify(cart));
  renderCart();
}

function renderCart() {
  const list = document.getElementById("cartList");

  list.innerHTML = "";
  let count = 0;

  cart.forEach(i => {
    count += i.quantity;

    const li = document.createElement("div");
    li.className = "item";
    li.innerHTML = `
      <div>
        <strong>${i.name}</strong>
        <div class="meta">Rs. ${formatNpr(i.price)} × ${i.quantity} = Rs. ${formatNpr(i.price * i.quantity)}</div>
      </div>
      <div class="actions">
        <button class="secondary" onclick="decQty(${i.productId})">-</button>
        <button class="secondary" onclick="incQty(${i.productId})">+</button>
        <button class="danger" onclick="removeFromCart(${i.productId})">Remove</button>
      </div>
    `;
    list.appendChild(li);
  });

  if (cart.length > 0) {
    const clearBtn = document.createElement("button");
    clearBtn.className = "secondary";
    clearBtn.style.marginTop = "10px";
    clearBtn.textContent = "Clear Cart";
    clearBtn.onclick = clearCart;
    list.appendChild(clearBtn);
  }

  // Recalculate totals
  recalcTotals();
}

function money(n) {
  return new Intl.NumberFormat("en-NP", { maximumFractionDigits: 2 }).format(n);
}

function recalcTotals() {
  const cart = JSON.parse(localStorage.getItem("cart") || "[]");

  let subtotal = 0;
  cart.forEach(i => subtotal += (i.price * i.quantity));

  // apply promo discount on subtotal
  const discountedSubtotal = subtotal * (1 - promoDiscountRate);

  const tax = discountedSubtotal * TAX_RATE;
  const total = discountedSubtotal + tax;

  document.getElementById("subtotal").innerText = money(discountedSubtotal);
  document.getElementById("tax").innerText = money(tax);
  document.getElementById("total").innerText = money(total);
}

function applyPromo() {
  const code = (document.getElementById("promo").value || "").trim().toUpperCase();
  const msg = document.getElementById("promoMsg");

  // Demo promo codes (you can later move to DB)
  if (code === "") {
    promoDiscountRate = 0;
    msg.innerText = "Promo cleared.";
  } else if (code === "NEPAL10") {
    promoDiscountRate = 0.10;
    msg.innerText = "Applied NEPAL10 (10% off subtotal).";
  } else if (code === "FREESHIP") {
    promoDiscountRate = 0.05;
    msg.innerText = "Applied FREESHIP (5% off subtotal).";
  } else {
    promoDiscountRate = 0;
    msg.innerText = "Invalid promo code.";
  }

  recalcTotals();
}

function getPaymentMethod() {
  const r = document.querySelector('input[name="pay"]:checked');
  return r ? r.value : "COD";
}

function togglePayment() {
  const method = getPaymentMethod();
  document.getElementById("cardBox").style.display = (method === "CARD") ? "block" : "none";
  document.getElementById("cardMsg").innerText = "";
}

function luhnCheck(num) {
  const s = String(num).replace(/\s+/g, "");
  if (!/^\d{12,19}$/.test(s)) return false;
  let sum = 0, alt = false;
  for (let i = s.length - 1; i >= 0; i--) {
    let n = parseInt(s[i], 10);
    if (alt) {
      n *= 2;
      if (n > 9) n -= 9;
    }
    sum += n;
    alt = !alt;
  }
  return sum % 10 === 0;
}

function parseExp(mmYY) {
  const m = (mmYY || "").trim();
  const match = m.match(/^(\d{2})\/(\d{2})$/);
  if (!match) return null;
  const mm = parseInt(match[1], 10);
  const yy = parseInt(match[2], 10);
  if (mm < 1 || mm > 12) return null;

  // expiry end-of-month check (simple)
  const now = new Date();
  const year = 2000 + yy;
  const expDate = new Date(year, mm, 0, 23, 59, 59); // last day of month
  if (expDate < now) return null;

  return { mm, yy };
}

function cardBrand(num) {
  const s = String(num).replace(/\s+/g, "");
  if (s.startsWith("4")) return "VISA";
  if (/^5[1-5]/.test(s)) return "MASTERCARD";
  if (/^3[47]/.test(s)) return "AMEX";
  return "CARD";
}

function validateCard() {
  const number = document.getElementById("cardNumber").value;
  const exp = document.getElementById("cardExp").value;
  const cvv = document.getElementById("cardCvv").value;
  const msg = document.getElementById("cardMsg");

  if (!luhnCheck(number)) return msg.innerText = "Invalid card number.";
  if (!parseExp(exp)) return msg.innerText = "Invalid expiry (MM/YY) or card expired.";
  if (!/^\d{3,4}$/.test((cvv || "").trim())) return msg.innerText = "Invalid CVV.";

  msg.innerText = `Card looks valid (${cardBrand(number)} •••• ${String(number).replace(/\s+/g,"").slice(-4)}).`;
}

function getCartStoreId() {
  const first = cart.find(i => i.storeId != null);
  return first ? first.storeId : null;
}

async function placeOrder() {
  const address = document.getElementById("address").value.trim();
  if (!address) return alert("Delivery address required");
  if (cart.length === 0) return alert("Cart is empty");

  const promoCode = (document.getElementById("promo").value || "").trim().toUpperCase();
  const paymentMethod = getPaymentMethod();

  let card = null;
  if (paymentMethod === "CARD") {
    const number = document.getElementById("cardNumber").value;
    const exp = document.getElementById("cardExp").value;

    if (!luhnCheck(number) || !parseExp(exp)) {
      alert("Please enter valid card details.");
      return;
    }

    const clean = String(number).replace(/\s+/g, "");
    card = {
      brand: cardBrand(clean),
      last4: clean.slice(-4),
      expiry: exp.trim()
      // DO NOT send/store CVV in backend
    };
  }

  const payload = {
    userId: user.id,
    deliveryAddress: address,
    items: cart.map(i => ({ productId: i.productId, quantity: i.quantity })),
    promoCode: promoCode || null,
    paymentMethod: paymentMethod,
    card: card
  };

  const res = await fetch("/api/orders", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });

  if (!res.ok) {
    alert("Order failed (check stock / server).");
    return;
  }

  alert("Order placed successfully!");
  clearCart();
  document.getElementById("address").value = "";
  document.getElementById("promo").value = "";
  promoDiscountRate = 0;
  document.getElementById("promoMsg").innerText = "";

  // Reset payment method to COD
  document.querySelector('input[name="pay"][value="COD"]').checked = true;
  togglePayment();

  // Clear card fields
  document.getElementById("cardName").value = "";
  document.getElementById("cardNumber").value = "";
  document.getElementById("cardExp").value = "";
  document.getElementById("cardCvv").value = "";
  document.getElementById("cardMsg").innerText = "";

  await loadProducts();
}

function logout() {
  localStorage.removeItem("user");
  localStorage.removeItem("cart");
  window.location.href = "/login.html";
}

function formatNpr(n) {
  return new Intl.NumberFormat("en-NP", { maximumFractionDigits: 2 }).format(n);
}

function escapeQuotes(s) {
  return (s || "").replace(/'/g, "\\'");
}