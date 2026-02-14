// /js/delivery.js

let user = null;
let currentOrders = [];
let selectedOrderId = null;
let locationWatchId = null;

document.addEventListener("DOMContentLoaded", () => {
  user = JSON.parse(localStorage.getItem("user") || "null");

  // Guard: must be DELIVERY
  if (!user || user.role !== "DELIVERY") {
    window.location.href = "/login.html";
    return;
  }

  initDeliveryDashboard();
});

async function initDeliveryDashboard() {
  updateUserGreeting();

  // Delivery person info (header)
  const infoEl = document.getElementById("deliveryPersonInfo");
  if (infoEl) infoEl.innerText = user.vehicleType || "Standard";

  updateAvailabilityButton();

  await loadMyOrders();
  await loadDeliveryStats();

  // Load earnings summary (default daily)
  await loadEarningsSummary('daily');

  // Start location sharing if user is online/available
  if (user.isAvailable) {
    startLocationSharing();
  }

  // Auto-refresh every 30 seconds
  setInterval(async () => {
    await loadMyOrders();
    if (selectedOrderId) {
      const exists = currentOrders.find((o) => o.id === selectedOrderId);
      if (exists) await showOrderDetails(selectedOrderId);
    }
  }, 30000);
}

function updateUserGreeting() {
  const whoEl = document.getElementById("who");
  const logoutLink = document.getElementById("logoutLink");

  if (whoEl) whoEl.textContent = user.name || user.email || "Partner";
  if (logoutLink) logoutLink.style.display = "inline";
}

async function loadMyOrders() {
  try {
    let res = await fetch(`/api/delivery/my-orders?deliveryPersonId=${user.id}`);

    if (!res.ok) {
      res = await fetch(`/api/admin/orders/assigned`);
      if (!res.ok) throw new Error("Failed to load orders");
      const all = await res.json();
      currentOrders = (all || []).filter(
        (o) => o.deliveryPerson && o.deliveryPerson.id === user.id
      );
    } else {
      currentOrders = await res.json();
    }

    renderOrderList();
    updateQuickStats();
  } catch (e) {
    console.error("Error loading orders:", e);
    const countEl = document.getElementById("ordersCount");
    if (countEl) {
      countEl.innerHTML =
        '<span style="color: #ef4444;">Failed to load orders</span>';
    }
  }
}

function renderOrderList() {
  const list = document.getElementById("orderList");
  const noOrdersMsg = document.getElementById("noOrdersMessage");
  const quickStats = document.getElementById("quickStats");

  if (!list || !noOrdersMsg || !quickStats) return;

  if (!currentOrders || currentOrders.length === 0) {
    list.innerHTML = "";
    noOrdersMsg.style.display = "block";
    quickStats.style.display = "none";
    const countEl = document.getElementById("ordersCount");
    if (countEl) countEl.innerText = "0 orders assigned";
    return;
  }

  noOrdersMsg.style.display = "none";
  quickStats.style.display = "grid";
  list.innerHTML = "";

  currentOrders.forEach((order) => {
    const orderDate = new Date(order.createdAt).toLocaleDateString();
    const itemCount = order.items ? order.items.length : 0;
    const customerName = order.user ? order.user.name || order.user.email : "Unknown";

    const el = document.createElement("div");
    el.className = "order-item";
    if (selectedOrderId === order.id) el.classList.add("selected");

    el.onclick = () => selectOrder(order.id);

    el.innerHTML = `
      <div class="order-header">
        <div>
          <div class="order-id">#${order.id}</div>
          <div class="order-meta">${orderDate} • ${itemCount} items • ${customerName}</div>
        </div>
        <div>
          <span class="status-badge status-${String(order.status || "").toLowerCase()}">${order.status || "—"}</span>
        </div>
      </div>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-top:8px;">
        <div style="font-weight:700;color:var(--amazon-orange);">Rs. ${formatNpr(order.total || 0)}</div>
        <div style="font-size:12px;color:var(--text-secondary);">${formatTime(order.createdAt)}</div>
      </div>
    `;

    list.appendChild(el);
  });

  const countEl = document.getElementById("ordersCount");
  if (countEl) countEl.innerText = `${currentOrders.length} orders assigned`;
}

function updateQuickStats() {
  const activeOrders = (currentOrders || []).filter(
    (o) => o.status !== "DELIVERED" && o.status !== "CANCELLED"
  ).length;

  const todayStr = new Date().toDateString();
  const todayOrders = (currentOrders || []).filter(
    (o) => new Date(o.createdAt).toDateString() === todayStr
  ).length;

  const completedOrders = (currentOrders || []).filter(
    (o) => o.status === "DELIVERED"
  ).length;

  const statActive = document.getElementById("statActive");
  const statToday = document.getElementById("statToday");
  const statCompleted = document.getElementById("statCompleted");
  const statRating = document.getElementById("statRating");

  if (statActive) statActive.innerText = activeOrders;
  if (statToday) statToday.innerText = todayOrders;
  if (statCompleted) statCompleted.innerText = completedOrders;
  if (statRating) statRating.innerText = user.rating || "5.0";
}

function selectOrder(orderId) {
  selectedOrderId = orderId;
  renderOrderList();
  showOrderDetails(orderId);
}

async function showOrderDetails(orderId) {
  const order = (currentOrders || []).find((o) => o.id === orderId);
  if (!order) return;

  const noSel = document.getElementById("noOrderSelected");
  const details = document.getElementById("orderDetails");
  if (noSel) noSel.style.display = "none";
  if (details) details.style.display = "block";

  setText("orderId", order.id);
  setText("orderTime", `Placed: ${formatDateTime(order.createdAt)}`);

  const badge = document.getElementById("orderStatusBadge");
  if (badge) {
    badge.innerText = order.status || "—";
    badge.className = `status-badge status-${String(order.status || "").toLowerCase()}`;
  }

  renderTimeline(order);

  setText("customerName", order.user?.name || "Unknown Customer");
  setText("customerEmail", order.user?.email || "No email");
  setText("customerPhone", order.user?.phone || "Not provided");

  const avatar = document.getElementById("customerAvatar");
  if (avatar) avatar.innerText = (order.user?.name?.charAt(0) || "C").toUpperCase();

  // ----- STRUCTURED ADDRESS DISPLAY -----
  const addressContainer = document.getElementById("deliveryAddressContainer");
  if (addressContainer) {
    // Build full address string for maps
    const fullAddress = buildFullAddress(order);
    // Also store it in a data attribute for getDirections
    addressContainer.dataset.fullAddress = fullAddress;

    // Render the detailed address
    addressContainer.innerHTML = renderStructuredAddress(order);
  }

  setText("paymentMethod", order.paymentMethod || "COD");

  // ETA (simple)
  if (order.createdAt) {
    const created = new Date(order.createdAt);
    const now = new Date();
    const diffHours = Math.floor((now - created) / (1000 * 60 * 60));
    setText("deliveryEta", diffHours < 1 ? "Within 1 hour" : `${diffHours} hours ago`);
  } else {
    setText("deliveryEta", "—");
  }

  renderItems(order);

  setText("orderSubtotal", `Rs. ${formatNpr(order.subtotal || 0)}`);
  setText("orderTax", `Rs. ${formatNpr(order.tax || 0)}`);
  setText("orderTotal", `Rs. ${formatNpr(order.total || 0)}`);
  setText("orderTotalQuick", formatNpr(order.total || 0));

  // Buttons state based on status
  const btnPickup = document.getElementById("btnPickup");
  const btnPaymentReceived = document.getElementById("btnPaymentReceived");
  const btnDeliver = document.getElementById("btnDeliver");
  const btnCancel = document.getElementById("btnCancel");
  const codCheckboxLabel = document.getElementById("codCheckboxLabel");

  if (btnPickup) {
    btnPickup.disabled = order.status !== "ASSIGNED";
    btnPickup.innerHTML =
      order.status === "PICKED_UP"
        ? '<i class="fas fa-check"></i> Picked Up'
        : '<i class="fas fa-box"></i> Pick Up';
  }

  // Show payment received button only when order is PICKED_UP
  if (btnPaymentReceived && codCheckboxLabel) {
    if (order.status === "PICKED_UP") {
      btnPaymentReceived.style.display = "inline-flex";
      // Show COD checkbox only if payment method is COD
      if (order.paymentMethod === "COD") {
        codCheckboxLabel.style.display = "inline-block";
      } else {
        codCheckboxLabel.style.display = "none";
      }
    } else {
      btnPaymentReceived.style.display = "none";
      codCheckboxLabel.style.display = "none";
    }
  }

  if (btnDeliver) btnDeliver.disabled = order.status !== "PAYMENT_RECEIVED";
  if (btnCancel) btnCancel.disabled = order.status === "CANCELLED" || order.status === "DELIVERED";

  // Show proof if exists
  const proofUploaded = document.getElementById('proofUploaded');
  const proofPreview = document.getElementById('proofImagePreview');
  if (order.deliveryProofImageUrl) {
    if (proofUploaded) proofUploaded.innerHTML = '✅ Uploaded';
    if (proofPreview) {
      proofPreview.style.display = 'block';
      proofPreview.innerHTML = `<img src="${order.deliveryProofImageUrl}" alt="Delivery Proof">`;
    }
  } else {
    if (proofUploaded) proofUploaded.innerHTML = '';
    if (proofPreview) {
      proofPreview.style.display = 'none';
      proofPreview.innerHTML = '';
    }
  }

  const notesEl = document.getElementById("deliveryNotes");
  if (notesEl) notesEl.value = order.deliveryNotes || "";

  setText(
    "selectedOrderInfo",
    `Order #${order.id} • ${formatDate(order.createdAt)} • ${order.status || "—"}`
  );
}

// Helper: Build full address string for maps
function buildFullAddress(order) {
  if (!order) return "";
  const parts = [
    order.streetAddress,
    order.city,
    order.district,
    order.province,
    order.postalCode,
    order.landmark ? `(${order.landmark})` : ""
  ].filter(Boolean);
  return parts.join(", ");
}

// Helper: Render structured address HTML
function renderStructuredAddress(order) {
  const rows = [];

  if (order.recipientName) {
    rows.push(`<div><strong>Recipient:</strong> ${order.recipientName}</div>`);
  }
  if (order.recipientPhone) {
    rows.push(`<div><strong>Phone:</strong> ${order.recipientPhone}</div>`);
  }
  if (order.streetAddress) {
    rows.push(`<div><strong>Street:</strong> ${order.streetAddress}</div>`);
  }
  if (order.city || order.district || order.province) {
    const line = [order.city, order.district, order.province].filter(Boolean).join(", ");
    rows.push(`<div><strong>City/District/Province:</strong> ${line}</div>`);
  }
  if (order.postalCode) {
    rows.push(`<div><strong>Postal Code:</strong> ${order.postalCode}</div>`);
  }
  if (order.landmark) {
    rows.push(`<div><strong>Landmark:</strong> ${order.landmark}</div>`);
  }

  if (rows.length === 0) {
    return '<div style="color: var(--text-secondary);">Address not specified</div>';
  }

  return rows.join('');
}

function renderTimeline(order) {
  const timeline = document.getElementById("orderTimeline");
  if (!timeline) return;

  const steps = [
    { key: "PLACED", label: "Placed", time: order.createdAt },
    { key: "ASSIGNED", label: "Assigned", time: order.assignedAt },
    { key: "PICKED_UP", label: "Picked Up", time: order.pickedUpAt },
    { key: "PAYMENT_RECEIVED", label: "Payment", time: order.paymentReceivedAt },
    { key: "DELIVERED", label: "Delivered", time: order.deliveredAt },
  ];

  const statusOrder = ["PLACED", "ASSIGNED", "PICKED_UP", "PAYMENT_RECEIVED", "DELIVERED"];
  const currentIndex = statusOrder.indexOf(order.status);

  timeline.innerHTML = steps
    .map((step, index) => {
      let className = "";
      if (step.time) className = "completed";
      else if (index === currentIndex) className = "active";
      else if (index < currentIndex) className = "completed";

      const timeStr = step.time ? formatTime(step.time) : "";

      return `
        <div class="timeline-step ${className}">
          <div class="dot"></div>
          <div class="timeline-label">${step.label}</div>
          <div class="timeline-label" style="font-size: 10px;">${timeStr}</div>
        </div>
      `;
    })
    .join("");
}

function renderItems(order) {
  const itemsList = document.getElementById("orderItems");
  const itemsCount = document.getElementById("itemsCount");
  if (!itemsList) return;

  itemsList.innerHTML = "";

  const items = order.items || [];
  if (itemsCount) itemsCount.innerText = `(${items.length} items)`;

  if (items.length === 0) {
    itemsList.innerHTML =
      '<div style="text-align:center;padding:20px;color:var(--text-secondary);">No items found</div>';
    if (itemsCount) itemsCount.innerText = "(0 items)";
    return;
  }

  items.forEach((item) => {
    const row = document.createElement("div");
    row.className = "item-row";
    const name = item.product?.name || "Unknown Product";
    const cat = item.product?.category?.name || "";
    const qty = item.quantity || 0;
    const price = item.priceAtPurchase || 0;

    row.innerHTML = `
      <div>
        <strong>${name}</strong>
        <div class="order-meta">${cat}</div>
      </div>
      <div style="text-align:right;">
        <div>${qty} × Rs. ${formatNpr(price)}</div>
        <div style="font-weight:bold;color:var(--amazon-orange);">
          Rs. ${formatNpr(qty * price)}
        </div>
      </div>
    `;
    itemsList.appendChild(row);
  });
}

async function updateStatus(newStatus) {
  if (!selectedOrderId) return alert("Select an order first.");

  const order = (currentOrders || []).find((o) => o.id === selectedOrderId);
  if (!order) return;

  // Validate status transitions
  if (newStatus === "PICKED_UP" && order.status !== "ASSIGNED") {
    alert("Order must be ASSIGNED before it can be picked up");
    return;
  }
  if (newStatus === "PAYMENT_RECEIVED" && order.status !== "PICKED_UP") {
    alert("Order must be PICKED_UP before confirming payment");
    return;
  }
  if (newStatus === "DELIVERED" && order.status !== "PAYMENT_RECEIVED") {
    alert("Payment must be received before delivery");
    return;
  }

  // Handle COD collection
  let codCollected = null;
  if (newStatus === "PAYMENT_RECEIVED") {
    if (order.paymentMethod === "COD") {
      const checkbox = document.getElementById("codCollectedCheckbox");
      codCollected = checkbox ? checkbox.checked : false;
      if (!codCollected) {
        alert("Please confirm COD collection by checking the checkbox");
        return;
      }
    }
  }

  if (!confirm(`Are you sure you want to mark this order as ${newStatus}?`)) return;

  try {
    const res = await fetch("/api/delivery/update-status", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        orderId: selectedOrderId,
        deliveryPersonId: user.id,
        status: newStatus,
        codCollected: codCollected
      }),
    });

    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(errorText || "Failed to update status");
    }

    const updatedOrder = await res.json();
    const idx = currentOrders.findIndex((o) => o.id === selectedOrderId);
    if (idx !== -1) currentOrders[idx] = updatedOrder;

    renderOrderList();
    await showOrderDetails(selectedOrderId);
    updateQuickStats();

    // Refresh earnings after delivery
    if (newStatus === "DELIVERED") {
      await loadEarningsSummary('daily');
    }

    alert(`Order status updated to ${newStatus}`);
  } catch (e) {
    console.error("Error updating status:", e);
    alert("Failed to update order status: " + e.message);
  }
}

// Load earnings summary
async function loadEarningsSummary(period = 'daily') {
  try {
    const res = await fetch(`/api/delivery/earnings/summary?deliveryPersonId=${user.id}&period=${period}`);
    if (!res.ok) throw new Error('Failed to load earnings');
    const data = await res.json();

    document.getElementById('earningsDeliveries').innerText = data.totalDeliveries;
    document.getElementById('earningsTotal').innerHTML = `Rs. ${formatNpr(data.totalEarnings)}`;
    document.getElementById('earningsIncentives').innerHTML = `Rs. ${formatNpr(data.incentives)}`;
    document.getElementById('earningsBonus').innerHTML = `Rs. ${formatNpr(data.bonus)}`;

    // Update performance badge
    const badgeEl = document.getElementById('performanceBadge');
    if (badgeEl && data.performanceBadge) {
      badgeEl.innerText = data.performanceBadge;
      badgeEl.className = `performance-badge badge-${data.performanceBadge.toLowerCase()}`;
    } else if (badgeEl) {
      badgeEl.innerText = '—';
      badgeEl.className = 'performance-badge';
    }
  } catch (e) {
    console.error('Earnings error:', e);
  }
}

function getBadgeColor(badge) {
  switch(badge) {
    case 'GOLD': return '#FFD700';
    case 'SILVER': return '#C0C0C0';
    case 'BRONZE': return '#CD7F32';
    default: return 'white';
  }
}

// Upload proof photo
async function handleProofUpload() {
  const fileInput = document.getElementById('proofFile');
  const file = fileInput.files[0];
  if (!file || !selectedOrderId) return;

  const formData = new FormData();
  formData.append('orderId', selectedOrderId);
  formData.append('deliveryPersonId', user.id);
  formData.append('file', file);

  try {
    const res = await fetch('/api/delivery/upload-proof', {
      method: 'POST',
      body: formData
    });
    if (!res.ok) throw new Error('Upload failed');
    const data = await res.json();

    document.getElementById('proofUploaded').innerHTML = '✅ Uploaded';

    // Show preview
    const preview = document.getElementById('proofImagePreview');
    preview.style.display = 'block';
    preview.innerHTML = `<img src="${data.imageUrl}" alt="Delivery Proof">`;

    // Update order in current list
    const idx = currentOrders.findIndex(o => o.id === selectedOrderId);
    if (idx !== -1) {
      currentOrders[idx].deliveryProofImageUrl = data.imageUrl;
    }

    alert('Proof uploaded successfully');
  } catch (e) {
    alert('Proof upload failed: ' + e.message);
  }
}

// Report issue
async function reportIssue() {
  if (!selectedOrderId) return alert('Select an order first');
  const issueType = document.getElementById('issueType').value;
  const description = document.getElementById('issueDescription').value.trim();
  if (!description) return alert('Please describe the issue');

  try {
    const res = await fetch('/api/delivery/report-issue', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: selectedOrderId,
        deliveryPersonId: user.id,
        issueType: issueType,
        description: description
      })
    });
    if (!res.ok) throw new Error('Failed to report issue');
    alert('Issue reported successfully');
    document.getElementById('issueDescription').value = '';
  } catch (e) {
    alert('Error: ' + e.message);
  }
}

// Live location sharing
function startLocationSharing() {
  if (!navigator.geolocation) return console.log('Geolocation not supported');
  if (locationWatchId) return;

  locationWatchId = navigator.geolocation.watchPosition(
    async (pos) => {
      const { latitude, longitude } = pos.coords;
      try {
        await fetch('/api/delivery/location', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ deliveryPersonId: user.id, latitude, longitude })
        });
      } catch (e) {
        console.error('Location update failed', e);
      }
    },
    (err) => console.error('Geolocation error:', err),
    { enableHighAccuracy: true, maximumAge: 30000, timeout: 27000 }
  );
}

function stopLocationSharing() {
  if (locationWatchId) {
    navigator.geolocation.clearWatch(locationWatchId);
    locationWatchId = null;
  }
}

// Online status toggle
async function toggleOnlineStatus() {
  const newStatus = !user.onlineStatus;

  try {
    const res = await fetch('/api/delivery/online-status', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        deliveryPersonId: user.id,
        online: newStatus
      })
    });
    if (!res.ok) throw new Error('Failed to update online status');

    user.onlineStatus = newStatus;
    localStorage.setItem('user', JSON.stringify(user));

    alert(`You are now ${newStatus ? 'Online' : 'Offline'}`);
  } catch (e) {
    alert('Error updating online status');
  }
}

async function saveNotes() {
  if (!selectedOrderId) return alert("Select an order first.");

  const notes = (document.getElementById("deliveryNotes")?.value || "").trim();

  try {
    const res = await fetch(`/api/orders/${selectedOrderId}/notes`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ notes }),
    });

    if (!res.ok) throw new Error("Failed to save notes");

    const idx = currentOrders.findIndex((o) => o.id === selectedOrderId);
    if (idx !== -1) currentOrders[idx].deliveryNotes = notes;

    alert("Notes saved successfully");
  } catch (e) {
    console.error("Error saving notes:", e);
    alert("Failed to save notes.");
  }
}

async function loadDeliveryStats() {
  try {
    const res = await fetch(`/api/admin/users/${user.id}`);
    if (!res.ok) return;

    const details = await res.json();
    const statCompleted = document.getElementById("statCompleted");
    const statRating = document.getElementById("statRating");
    if (statCompleted && details.completedDeliveries != null) {
      statCompleted.innerText = details.completedDeliveries;
    }
    if (statRating && details.rating != null) {
      statRating.innerText = details.rating;
    }
  } catch (e) {
    console.error("Error loading delivery stats:", e);
  }
}

async function toggleAvailability() {
  const newAvailability = !user.isAvailable;

  try {
    const res = await fetch(`/api/admin/delivery/${user.id}/availability`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ isAvailable: newAvailability }),
    });

    if (!res.ok) throw new Error("Failed to update availability");

    user.isAvailable = newAvailability;
    localStorage.setItem("user", JSON.stringify(user));
    updateAvailabilityButton();

    // Start/stop location sharing based on availability
    if (user.isAvailable) {
      startLocationSharing();
    } else {
      stopLocationSharing();
    }

    alert(`Availability updated: ${newAvailability ? "Available" : "Busy"}`);
  } catch (e) {
    console.error("Error toggling availability:", e);
    alert("Failed to update availability");
  }
}

function updateAvailabilityButton() {
  const btn = document.getElementById("availabilityBtn");
  if (!btn) return;

  if (user.isAvailable) {
    btn.innerHTML = '<i class="fas fa-toggle-on"></i> Available';
    btn.className = "btn btn-primary";
    btn.style.background = "";
    btn.style.color = "";
    btn.style.border = "";
  } else {
    btn.innerHTML = '<i class="fas fa-toggle-off"></i> Busy';
    btn.className = "btn btn-secondary";
    btn.style.background = "rgba(239, 68, 68, 0.2)";
    btn.style.color = "#ef4444";
    btn.style.border = "1px solid rgba(239, 68, 68, 0.3)";
  }
}

function filterOrders(filter) {
  const original = [...currentOrders];
  let filtered = [...currentOrders];

  if (filter === "active") {
    filtered = filtered.filter((o) => o.status !== "DELIVERED" && o.status !== "CANCELLED");
  } else if (filter === "completed") {
    filtered = filtered.filter((o) => o.status === "DELIVERED" || o.status === "CANCELLED");
  }

  currentOrders = filtered;
  renderOrderList();
  currentOrders = original;
}

function getDirections() {
  const order = (currentOrders || []).find((o) => o.id === selectedOrderId);
  if (!order) return alert("No order selected");

  const fullAddress = buildFullAddress(order);
  if (!fullAddress) return alert("No delivery address available");

  const encoded = encodeURIComponent(fullAddress);
  const url = `https://www.google.com/maps/search/?api=1&query=${encoded}`;
  window.open(url, "_blank");
}

function contactCustomer() {
  const order = (currentOrders || []).find((o) => o.id === selectedOrderId);
  const phone = order?.user?.phone;
  if (!phone) return alert("Customer phone number not available");
  window.open(`tel:${phone}`);
}

function refreshOrders() {
  loadMyOrders();
  if (selectedOrderId) showOrderDetails(selectedOrderId);
}

function logout() {
  stopLocationSharing();
  localStorage.removeItem("user");
  window.location.href = "/login.html";
}

// ---------------- Utilities ----------------
function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.innerText = value == null ? "" : String(value);
}

function formatNpr(n) {
  if (typeof n === "object" && n !== null) n = n.toString();
  return new Intl.NumberFormat("en-NP", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(n || 0);
}

function formatDate(dateString) {
  if (!dateString) return "N/A";
  const d = new Date(dateString);
  return d.toLocaleDateString("en-US", { year: "numeric", month: "short", day: "numeric" });
}

function formatTime(dateString) {
  if (!dateString) return "";
  const d = new Date(dateString);
  return d.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" });
}

function formatDateTime(dateString) {
  if (!dateString) return "N/A";
  const d = new Date(dateString);
  return d.toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

// Export for inline onclick usage
window.updateStatus = updateStatus;
window.saveNotes = saveNotes;
window.refreshOrders = refreshOrders;
window.logout = logout;
window.toggleAvailability = toggleAvailability;
window.filterOrders = filterOrders;
window.getDirections = getDirections;
window.contactCustomer = contactCustomer;
window.loadEarningsSummary = loadEarningsSummary;
window.handleProofUpload = handleProofUpload;
window.reportIssue = reportIssue;
window.toggleOnlineStatus = toggleOnlineStatus;