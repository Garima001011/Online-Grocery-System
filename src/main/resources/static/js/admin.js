const user = JSON.parse(localStorage.getItem("user") || "null");
if (!user || user.role !== "ADMIN") window.location.href = "/login.html";
document.getElementById("who").innerText = `${user.email} • ${user.role}`;

let stores = [];
let categories = [];

init();

async function init() {
  await reloadMeta();   // load stores + categories, fill dropdowns
  await refresh();      // load products
}

async function reloadMeta() {
  await Promise.all([loadStores(), loadCategories()]);
  hydrateDropdowns();
}

async function loadStores() {
  const r = await fetch("/api/stores");
  stores = r.ok ? await r.json() : [];
  document.getElementById("kStores").innerText = stores.length;

  const list = document.getElementById("storeList");
  list.innerHTML = "";
  stores.forEach(s => {
    const el = document.createElement("div");
    el.className = "item";
    el.innerHTML = `
      <div>
        <strong>${escapeHtml(s.name)}</strong>
        <div class="meta">${escapeHtml(s.location || "")}</div>
      </div>
      <div class="actions">
        <span class="badge">Store</span>
      </div>
    `;
    list.appendChild(el);
  });
}

async function loadCategories() {
  const r = await fetch("/api/categories");
  categories = r.ok ? await r.json() : [];
  document.getElementById("kCats").innerText = categories.length;
}

function hydrateDropdowns() {
  const storeSel = document.getElementById("pStore");
  storeSel.innerHTML = `<option value="">Select store</option>` +
    stores.map(s => `<option value="${s.id}">${escapeHtml(s.name)}</option>`).join("");

  const catSel = document.getElementById("pCategory");
  catSel.innerHTML = `<option value="">Select category</option>` +
    categories.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join("");
}

async function addStore() {
  const name = document.getElementById("storeName").value.trim();
  const location = document.getElementById("storeLocation").value.trim();
  if (!name) return alert("Store name is required.");

  const res = await fetch("/api/stores", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, location })
  });

  if (!res.ok) return alert("Store add failed (maybe duplicate).");

  document.getElementById("storeName").value = "";
  document.getElementById("storeLocation").value = "";

  await reloadMeta();
  await refresh();
}

async function addCategory() {
  const name = document.getElementById("catName").value.trim();
  if (!name) return alert("Category name is required.");

  const res = await fetch("/api/categories", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name })
  });

  if (!res.ok) return alert("Category add failed (maybe duplicate).");

  document.getElementById("catName").value = "";
  await reloadMeta();
  await refresh();
}

async function addProduct() {
  const name = document.getElementById("pName").value.trim();
  const price = Number(document.getElementById("pPrice").value);
  const stock = Number(document.getElementById("pStock").value);
  const storeId = Number(document.getElementById("pStore").value);
  const categoryId = Number(document.getElementById("pCategory").value);

  if (!name) return alert("Product name is required.");
  if (!storeId) return alert("Select a store.");
  if (!categoryId) return alert("Select a category.");
  if (Number.isNaN(price) || price < 0) return alert("Enter valid price.");
  if (!Number.isInteger(stock) || stock < 0) return alert("Enter valid stock.");

  const payload = { name, price, stock, storeId, categoryId };

  const res = await fetch("/api/products", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });

  if (!res.ok) return alert("Product add failed.");

  document.getElementById("pName").value = "";
  document.getElementById("pPrice").value = "";
  document.getElementById("pStock").value = "";

  await refresh();
}

async function refresh() {
  const q = document.getElementById("search").value.trim();
  const params = new URLSearchParams();
  if (q) params.set("q", q);

  const r = await fetch("/api/products" + (params.toString() ? `?${params}` : ""));
  const products = r.ok ? await r.json() : [];
  document.getElementById("kProducts").innerText = products.length;

  const list = document.getElementById("productList");
  list.innerHTML = "";
  products.forEach(p => {
    const el = document.createElement("div");
    el.className = "item";
    el.innerHTML = `
      <div>
        <strong>${escapeHtml(p.name)}</strong>
        <div class="meta">${escapeHtml(p.category?.name || "—")} • ${escapeHtml(p.store?.name || "—")} • Stock: ${p.stock}</div>
        <div class="meta">Rs. ${new Intl.NumberFormat("en-NP").format(p.price)}</div>
      </div>
      <div class="actions">
        <span class="badge">Product</span>
      </div>
    `;
    list.appendChild(el);
  });
}

function logout() {
  localStorage.removeItem("user");
  localStorage.removeItem("cart");
  window.location.href = "/login.html";
}

function escapeHtml(str) {
  return String(str || "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}
