let user = null;
let products = [];
let stores = [];
let categories = [];
let cart = JSON.parse(localStorage.getItem("cart")) || [];
let filters = {
  category: null,
  store: null,
  maxPrice: 5000,
  inStockOnly: true,
  searchQuery: ""
};

document.addEventListener("DOMContentLoaded", async () => {
  const userData = localStorage.getItem("user");

  if (userData) {
    try {
      user = JSON.parse(userData);
      const userGreetingEl = document.getElementById("userGreeting");
      if (userGreetingEl) {
        const userName = user?.name || user?.email?.split("@")?.[0] || "User";
        userGreetingEl.textContent = userName;
        userGreetingEl.onclick = (e) => {
          e.preventDefault();
          e.stopPropagation();
          toggleUserDropdown();
        };
      }

      const dropdownEmailEl = document.getElementById("dropdownEmail");
      if (dropdownEmailEl && user?.email) dropdownEmailEl.textContent = user.email;

      const dropdownRoleEl = document.getElementById("dropdownRole");
      if (dropdownRoleEl && user?.role) {
        dropdownRoleEl.textContent =
          user.role.charAt(0).toUpperCase() + user.role.slice(1).toLowerCase();
      }

      const logoutLink = document.getElementById("logoutLink");
      if (logoutLink) logoutLink.style.display = "inline";
    } catch (error) {
      redirectToLogin();
      return;
    }
  } else {
    const userGreetingEl = document.getElementById("userGreeting");
    if (userGreetingEl) {
      userGreetingEl.textContent = "Sign in";
      userGreetingEl.onclick = function () {
        window.location.href = "/login.html";
      };
    }
    const logoutLink = document.getElementById("logoutLink");
    if (logoutLink) logoutLink.style.display = "none";
  }

  updateCartCount();

  await Promise.all([loadStores(), loadCategories(), loadProducts()]);
  setupFilters();

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      const dropdown = document.getElementById("userDropdown");
      if (dropdown) dropdown.style.display = "none";
    }
  });
});

function redirectToLogin() {
  localStorage.removeItem("user");
  window.location.href = "/login.html";
}

function toggleUserDropdown() {
  const dropdown = document.getElementById("userDropdown");
  if (!dropdown) return;

  if (!user) {
    window.location.href = "/login.html";
    return;
  }

  const isOpen = dropdown.style.display === "block";
  dropdown.style.display = isOpen ? "none" : "block";

  if (!isOpen) {
    setTimeout(() => {
      document.addEventListener("click", closeDropdownOnClickOutside);
    }, 100);
  } else {
    document.removeEventListener("click", closeDropdownOnClickOutside);
  }
}

function closeDropdownOnClickOutside(event) {
  const dropdown = document.getElementById("userDropdown");
  const greeting = document.getElementById("userGreeting");
  if (!dropdown || !greeting) return;

  if (!dropdown.contains(event.target) && !greeting.contains(event.target)) {
    dropdown.style.display = "none";
    document.removeEventListener("click", closeDropdownOnClickOutside);
  }
}

function logout() {
  const ok = confirm("Are you sure you want to sign out?");
  if (!ok) return false;

  localStorage.removeItem("user");
  user = null;

  alert("Successfully signed out!");
  window.location.href = "/login.html";
  return false;
}

function viewAccount() {
  showNotification("Account page coming soon!");
  const dropdown = document.getElementById("userDropdown");
  if (dropdown) dropdown.style.display = "none";
}

function viewOrders() {
  window.location.href = "/orders.html";
  const dropdown = document.getElementById("userDropdown");
  if (dropdown) dropdown.style.display = "none";
}

async function loadProducts() {
  try {
    const response = await fetch("/api/products");
    products = await response.json();
    displayProducts(products);
    loadSuggestions();
  } catch (error) {}
}

async function loadStores() {
  try {
    const response = await fetch("/api/stores");
    stores = await response.json();
  } catch (error) {}
}

async function loadCategories() {
  try {
    const response = await fetch("/api/categories");
    categories = await response.json();
  } catch (error) {}
}

function setupFilters() {
  const categoryFilters = document.getElementById("categoryFilters");
  const storeFilters = document.getElementById("storeFilters");

  if (categoryFilters) categoryFilters.innerHTML = "";
  if (storeFilters) storeFilters.innerHTML = "";

  if (categoryFilters && categories.length > 0) {
    categories.slice(0, 5).forEach((category) => {
      categoryFilters.innerHTML += `
        <label class="filter-option">
          <input type="checkbox" name="category" value="${category.id}">
          ${category.name}
        </label>
      `;
    });
  }

  if (storeFilters && stores.length > 0) {
    stores.slice(0, 5).forEach((store) => {
      storeFilters.innerHTML += `
        <label class="filter-option">
          <input type="checkbox" name="store" value="${store.id}">
          ${store.name}
        </label>
      `;
    });
  }

  const priceRange = document.getElementById("priceRange");
  const priceValue = document.getElementById("priceValue");
  if (priceRange && priceValue) {
    priceRange.addEventListener("input", (e) => {
      priceValue.textContent = e.target.value;
      filters.maxPrice = parseInt(e.target.value, 10);
    });
  }
}

function displayProducts(productsToDisplay) {
  const productsGrid = document.getElementById("productsGrid");
  if (!productsGrid) return;

  productsGrid.innerHTML = "";

  productsToDisplay.forEach((product) => {
    if (filters.maxPrice && product.price > filters.maxPrice) return;
    if (filters.inStockOnly && product.stock <= 0) return;

    const productCard = document.createElement("div");
    productCard.className = "product-card";

    const badge =
      product.stock < 10 ? `<div class="product-badge">Low Stock</div>` : "";
    const icon = getCategoryIcon(product.category?.name);

    productCard.innerHTML = `
      ${badge}
      <div style="text-align: center;">
        <i class="${icon}" style="font-size: 60px; color: #ddd;"></i>
      </div>
      <h3 class="product-title">${product.name}</h3>
      <div class="product-price">
        Rs. ${formatPrice(product.price)}
        ${
          product.originalPrice
            ? `<span>Rs. ${formatPrice(product.originalPrice)}</span>`
            : ""
        }
      </div>
      <div class="product-rating">
        ${generateStars(Math.random() * 2 + 3)}
        <span style="color: #565959; font-size: 14px;">(${Math.floor(
          Math.random() * 100
        )})</span>
      </div>
      <div class="product-store">
        <i class="fas fa-store"></i> ${product.store?.name || "Local Store"}
      </div>
      <div class="product-stock">
        ${
          product.stock > 0
            ? `<i class="fas fa-check-circle" style="color: green;"></i> In Stock (${product.stock} available)`
            : `<i class="fas fa-times-circle" style="color: red;"></i> Out of Stock`
        }
      </div>
      <button class="add-to-cart-btn" onclick="addToCart(${product.id})" ${
      product.stock <= 0 ? "disabled" : ""
    }>
        <i class="fas fa-cart-plus"></i> Add to Cart
      </button>
    `;

    productsGrid.appendChild(productCard);
  });
}

function loadSuggestions() {
  const suggestionsGrid = document.getElementById("suggestionsGrid");
  if (!suggestionsGrid) return;

  const suggestedProducts = [...products]
    .sort(() => Math.random() - 0.5)
    .slice(0, 6);

  suggestionsGrid.innerHTML = "";
  suggestedProducts.forEach((product) => {
    suggestionsGrid.innerHTML += `
      <div class="product-card">
        <div style="text-align: center;">
          <i class="${getCategoryIcon(
            product.category?.name
          )}" style="font-size: 40px; color: #ddd;"></i>
        </div>
        <h4 style="font-size: 14px; margin: 10px 0; height: 40px; overflow: hidden;">${
          product.name
        }</h4>
        <div style="color: var(--amazon-price); font-weight: bold; margin-bottom: 10px;">
          Rs. ${formatPrice(product.price)}
        </div>
        <button onclick="addToCart(${product.id})" style="width: 100%; padding: 5px; background: var(--amazon-light-gray); border: 1px solid var(--amazon-border); border-radius: 4px; cursor: pointer;">
          Add to Cart
        </button>
      </div>
    `;
  });
}

function addToCart(productId) {
  const product = products.find((p) => p.id === productId);
  if (!product) return;

  if (product.stock <= 0) {
    alert("This item is out of stock");
    return;
  }

  const existingItem = cart.find((item) => item.productId === productId);

  if (existingItem) {
    if (existingItem.quantity >= product.stock) {
      alert(`Only ${product.stock} items available in stock`);
      return;
    }
    existingItem.quantity++;
  } else {
    cart.push({
      productId: product.id,
      name: product.name,
      price: product.price,
      quantity: 1,
      storeId: product.store?.id
    });
  }

  saveCart();
  updateCartCount();
  showNotification(`${product.name} added to cart!`);
}

function saveCart() {
  localStorage.setItem("cart", JSON.stringify(cart));
}

function updateCartCount() {
  const cartCount = document.getElementById("cartCount");
  if (cartCount) {
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    cartCount.textContent = totalItems;
  }
}

function applyFilters() {
  const selectedCategories = Array.from(
    document.querySelectorAll('input[name="category"]:checked')
  ).map((cb) => parseInt(cb.value, 10));

  const selectedStores = Array.from(
    document.querySelectorAll('input[name="store"]:checked')
  ).map((cb) => parseInt(cb.value, 10));

  const inStockOnly = document.getElementById("inStockOnly")?.checked || false;
  filters.inStockOnly = inStockOnly;

  let filteredProducts = products.filter((product) => {
    if (product.price > filters.maxPrice) return false;
    if (inStockOnly && product.stock <= 0) return false;

    if (selectedCategories.length > 0 && product.category) {
      if (!selectedCategories.includes(product.category.id)) return false;
    }

    if (selectedStores.length > 0 && product.store) {
      if (!selectedStores.includes(product.store.id)) return false;
    }

    if (filters.searchQuery) {
      const query = filters.searchQuery.toLowerCase();
      const nameMatch = product.name?.toLowerCase().includes(query);
      const categoryMatch = product.category?.name
        ?.toLowerCase()
        .includes(query);
      if (!nameMatch && !categoryMatch) return false;
    }

    return true;
  });

  displayProducts(filteredProducts);
}

function searchProducts() {
  const headerInput = document.getElementById("searchInput");
  const heroInput = document.getElementById("heroSearchInput");
  const value = (headerInput?.value || heroInput?.value || "").trim();
  filters.searchQuery = value;
  applyFilters();
}

function sortProducts() {
  const sortBy = document.getElementById("sortBy")?.value || "default";
  let sortedProducts = [...products];

  switch (sortBy) {
    case "price_asc":
      sortedProducts.sort((a, b) => a.price - b.price);
      break;
    case "price_desc":
      sortedProducts.sort((a, b) => b.price - a.price);
      break;
    case "name":
      sortedProducts.sort((a, b) => (a.name || "").localeCompare(b.name || ""));
      break;
  }

  displayProducts(sortedProducts);
}

function formatPrice(price) {
  const n = Number(price || 0);
  return n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function generateStars(rating) {
  let stars = "";
  for (let i = 1; i <= 5; i++) {
    if (i <= Math.floor(rating)) stars += '<i class="fas fa-star"></i>';
    else if (i === Math.ceil(rating) && !Number.isInteger(rating))
      stars += '<i class="fas fa-star-half-alt"></i>';
    else stars += '<i class="far fa-star"></i>';
  }
  return stars;
}

function getCategoryIcon(categoryName) {
  const icons = {
    Fruits: "fas fa-apple-alt",
    Vegetables: "fas fa-carrot",
    Dairy: "fas fa-cheese",
    Bakery: "fas fa-bread-slice",
    Meat: "fas fa-drumstick-bite",
    Seafood: "fas fa-fish",
    Beverages: "fas fa-wine-bottle",
    Snacks: "fas fa-cookie",
    Rice: "fas fa-seedling",
    Oil: "fas fa-oil-can"
  };

  if (!categoryName) return "fas fa-shopping-basket";

  for (const [key, icon] of Object.entries(icons)) {
    if (categoryName.toLowerCase().includes(key.toLowerCase())) return icon;
  }
  return "fas fa-shopping-basket";
}

function showNotification(message) {
  const notification = document.createElement("div");
  notification.style.cssText = `
    position: fixed;
    top: 80px;
    right: 20px;
    background: var(--amazon-green);
    color: white;
    padding: 15px 20px;
    border-radius: 4px;
    z-index: 1000;
    animation: slideIn 0.3s ease;
  `;
  notification.textContent = message;
  document.body.appendChild(notification);

  setTimeout(() => {
    notification.style.animation = "slideOut 0.3s ease";
    setTimeout(() => notification.remove(), 300);
  }, 2000);
}

(function () {
  if (document.getElementById("amazonAnimStyles")) return;
  const style = document.createElement("style");
  style.id = "amazonAnimStyles";
  style.textContent = `
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
      from { transform: translateX(0); opacity: 1; }
      to { transform: translateX(100%); opacity: 0; }
    }
  `;
  document.head.appendChild(style);
})();
