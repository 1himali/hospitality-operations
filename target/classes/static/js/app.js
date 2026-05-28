/* ============================================
   HOSPITALITY OPERATIONS — FRONTEND APP
   ============================================ */

const API = {
    rooms: '/api/v1/rooms',
    menu: '/api/v1/menu',
};

// ─── UTILITY ──────────────────────────────
function $(sel) { return document.querySelector(sel); }
function $$(sel) { return document.querySelectorAll(sel); }

function formatCurrency(val) {
    return '₹' + Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function formatStatus(s) {
    return s.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}

function formatType(t) {
    const map = { DELUXE_KING: 'Deluxe King', STD_DOUBLE: 'Std Double', SUITE: 'Suite' };
    return map[t] || t;
}

function formatCategory(c) {
    return c.charAt(0) + c.slice(1).toLowerCase();
}

// ─── TOAST ────────────────────────────────
function showToast(message, type = 'success') {
    const container = $('#toast-container');
    const icons = { success: '✓', error: '✕', info: 'ℹ' };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span class="toast-icon">${icons[type]}</span><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('toast-exit');
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// ─── MODAL HELPERS ────────────────────────
function openModal(id) {
    document.getElementById(id).classList.add('open');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

// ─── API HELPERS ──────────────────────────
async function apiFetch(url, options = {}) {
    try {
        const resp = await fetch(url, {
            headers: { 'Content-Type': 'application/json', ...options.headers },
            ...options,
        });
        if (resp.status === 204) return null;
        if (!resp.ok) {
            const err = await resp.json().catch(() => ({}));
            throw new Error(err.message || `HTTP ${resp.status}`);
        }
        return resp.json();
    } catch (e) {
        showToast(e.message, 'error');
        throw e;
    }
}

// ─── CLOCK ────────────────────────────────
function updateClock() {
    const el = $('#topbar-clock');
    const now = new Date();
    el.textContent = now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true })
        + '  •  ' + now.toLocaleDateString('en-IN', { weekday: 'short', day: '2-digit', month: 'short' });
}
setInterval(updateClock, 1000);
updateClock();

// ──────────────────────────────────────────
//  NAVIGATION
// ──────────────────────────────────────────
const pages = { rooms: 'Room Management', menu: 'Menu Management' };

$$('.nav-item').forEach(btn => {
    btn.addEventListener('click', () => {
        const page = btn.dataset.page;
        $$('.nav-item').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        $$('.page').forEach(p => p.classList.remove('active'));
        $(`#page-${page}`).classList.add('active');
        $('#page-title').textContent = pages[page];
        // Close mobile sidebar
        $('#sidebar').classList.remove('open');
    });
});

$('#sidebar-toggle').addEventListener('click', () => {
    $('#sidebar').classList.toggle('open');
});

// ──────────────────────────────────────────
//  ROOM MODULE
// ──────────────────────────────────────────
let roomsData = [];

async function loadRooms() {
    const filter = $('#room-status-filter').value;
    const url = filter ? `${API.rooms}?status=${filter}` : API.rooms;
    try {
        roomsData = await apiFetch(url);
        renderRoomStats();
        renderRoomGrid();
    } catch (e) { /* toast already shown */ }
}

function renderRoomStats() {
    const total = roomsData.length;
    const vacant = roomsData.filter(r => r.status === 'VACANT').length;
    const occupied = roomsData.filter(r => r.status === 'OCCUPIED').length;
    const reserved = roomsData.filter(r => r.status === 'RESERVED').length;
    const avgRate = total > 0 ? roomsData.reduce((s, r) => s + Number(r.ratePerNight), 0) / total : 0;

    $('#room-stats').innerHTML = `
        <div class="stat-card">
            <div class="stat-icon" style="background:${cssVar('--accent-info-bg')};color:${cssVar('--accent-info')}">🏨</div>
            <div class="stat-content"><div class="stat-value">${total}</div><div class="stat-label">Total Rooms</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon" style="background:${cssVar('--accent-success-bg')};color:${cssVar('--accent-success')}">✓</div>
            <div class="stat-content"><div class="stat-value">${vacant}</div><div class="stat-label">Vacant</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon" style="background:${cssVar('--accent-danger-bg')};color:${cssVar('--accent-danger')}">●</div>
            <div class="stat-content"><div class="stat-value">${occupied}</div><div class="stat-label">Occupied</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon" style="background:${cssVar('--accent-warning-bg')};color:${cssVar('--accent-warning')}">◉</div>
            <div class="stat-content"><div class="stat-value">${reserved}</div><div class="stat-label">Reserved</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon" style="background:${cssVar('--accent-purple-bg')};color:${cssVar('--accent-purple')}">₹</div>
            <div class="stat-content"><div class="stat-value">${formatCurrency(avgRate)}</div><div class="stat-label">Avg Rate</div></div>
        </div>
    `;
}

function cssVar(name) {
    return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

function renderRoomGrid() {
    const grid = $('#room-grid');
    if (roomsData.length === 0) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-state-icon">🏨</div><p>No rooms found. Click <strong>Add Room</strong> to get started.</p></div>`;
        return;
    }
    grid.innerHTML = roomsData.map(room => {
        const statusClass = room.status.toLowerCase();
        return `
        <div class="card" data-room-id="${room.id}">
            <div class="card-header">
                <div class="card-title-group">
                    <div class="card-title">Room ${room.roomNumber}</div>
                    <div class="card-subtitle">${formatType(room.type)} • Floor ${room.floor}</div>
                </div>
                <div class="card-actions">
                    <button class="btn btn-icon" title="Edit" onclick="editRoom(${room.id})">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    </button>
                    <button class="btn btn-icon danger" title="Delete" onclick="confirmDeleteRoom(${room.id}, '${room.roomNumber}')">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                    </button>
                </div>
            </div>
            <div class="card-body">
                <div class="card-detail">
                    <span class="card-detail-label">Status</span>
                    <span class="badge badge-${statusClass}"><span class="badge-dot"></span>${formatStatus(room.status)}</span>
                </div>
                <div class="card-detail">
                    <span class="card-detail-label">Rate / Night</span>
                    <span class="price-tag">${formatCurrency(room.ratePerNight)}</span>
                </div>
                ${room.issueNotes ? `<div class="card-detail"><span class="card-detail-label">Notes</span><span class="card-detail-value" style="max-width:180px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis" title="${room.issueNotes}">${room.issueNotes}</span></div>` : ''}
            </div>
            <div class="card-footer">
                <span style="font-size:var(--font-xs);color:var(--text-muted)">Updated ${formatDate(room.updatedAt)}</span>
                <div style="display:flex;gap:var(--space-2)">
                    ${buildStatusButtons(room)}
                </div>
            </div>
        </div>`;
    }).join('');
}

function buildStatusButtons(room) {
    const quickStatuses = ['VACANT', 'OCCUPIED', 'RESERVED'];
    return quickStatuses
        .filter(s => s !== room.status)
        .map(s => `<button class="btn btn-sm btn-ghost" onclick="quickStatus(${room.id},'${s}')">${formatStatus(s)}</button>`)
        .join('');
}

// Quick status update
window.quickStatus = async function(id, status) {
    try {
        await apiFetch(`${API.rooms}/${id}/status?status=${status}`, { method: 'PATCH' });
        showToast(`Room status → ${formatStatus(status)}`);
        loadRooms();
    } catch (e) { /* toast shown */ }
};

// Filter
$('#room-status-filter').addEventListener('change', loadRooms);

// Add room
$('#btn-add-room').addEventListener('click', () => {
    $('#room-modal-title').textContent = 'Add Room';
    $('#room-submit-btn').textContent = 'Save Room';
    $('#room-form').reset();
    $('#room-edit-id').value = '';
    openModal('room-modal');
});

// Edit room
window.editRoom = async function(id) {
    const room = roomsData.find(r => r.id === id);
    if (!room) return;
    $('#room-modal-title').textContent = 'Edit Room';
    $('#room-submit-btn').textContent = 'Update Room';
    $('#room-edit-id').value = room.id;
    $('#room-number').value = room.roomNumber;
    $('#room-type').value = room.type;
    $('#room-floor').value = room.floor;
    $('#room-rate').value = room.ratePerNight;
    $('#room-status-input').value = room.status;
    $('#room-image-url').value = room.imageUrl || '';
    $('#room-issue-notes').value = room.issueNotes || '';
    openModal('room-modal');
};

// Room form submit
$('#room-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const editId = $('#room-edit-id').value;
    const payload = {
        roomNumber: $('#room-number').value.trim(),
        type: $('#room-type').value,
        floor: parseInt($('#room-floor').value),
        status: $('#room-status-input').value,
        ratePerNight: parseFloat($('#room-rate').value),
        imageUrl: $('#room-image-url').value.trim() || null,
        issueNotes: $('#room-issue-notes').value.trim() || null,
    };

    try {
        if (editId) {
            await apiFetch(`${API.rooms}/${editId}`, { method: 'PUT', body: JSON.stringify(payload) });
            showToast('Room updated');
        } else {
            await apiFetch(API.rooms, { method: 'POST', body: JSON.stringify(payload) });
            showToast('Room created');
        }
        closeModal('room-modal');
        loadRooms();
    } catch (e) { /* toast shown */ }
});

// Delete room
let pendingDelete = { type: null, id: null };

window.confirmDeleteRoom = function(id, roomNumber) {
    pendingDelete = { type: 'room', id };
    $('#delete-modal-message').textContent = `Are you sure you want to delete Room ${roomNumber}?`;
    openModal('delete-modal');
};

// Room modal close
$('#room-modal-close').addEventListener('click', () => closeModal('room-modal'));
$('#room-modal-cancel').addEventListener('click', () => closeModal('room-modal'));

// ──────────────────────────────────────────
//  MENU MODULE
// ──────────────────────────────────────────
let menuData = [];

async function loadMenu() {
    const filter = $('#menu-category-filter').value;
    const url = filter ? `${API.menu}?category=${filter}` : API.menu;
    try {
        menuData = await apiFetch(url);
        renderMenuStats();
        renderMenuGrid();
    } catch (e) { /* toast shown */ }
}

function renderMenuStats() {
    const total = menuData.length;
    const available = menuData.filter(m => m.available).length;
    const unavailable = total - available;
    const avgPrice = total > 0 ? menuData.reduce((s, m) => s + Number(m.price), 0) / total : 0;
    const categories = new Set(menuData.map(m => m.category)).size;

    $('#menu-stats').innerHTML = `
        <div class="stat-card">
            <div class="stat-icon" style="background:${cssVar('--accent-info-bg')};color:${cssVar('--accent-info')}">🍽️</div>
            <div class="stat-content"><div class="stat-value">${total}</div><div class="stat-label">Total Items</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon" style="background:${cssVar('--accent-success-bg')};color:${cssVar('--accent-success')}">✓</div>
            <div class="stat-content"><div class="stat-value">${available}</div><div class="stat-label">Available</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon" style="background:${cssVar('--accent-danger-bg')};color:${cssVar('--accent-danger')}">✕</div>
            <div class="stat-content"><div class="stat-value">${unavailable}</div><div class="stat-label">Unavailable</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon" style="background:${cssVar('--accent-purple-bg')};color:${cssVar('--accent-purple')}">☰</div>
            <div class="stat-content"><div class="stat-value">${categories}</div><div class="stat-label">Categories</div></div>
        </div>
        <div class="stat-card">
            <div class="stat-icon" style="background:${cssVar('--accent-cyan-bg')};color:${cssVar('--accent-cyan')}">₹</div>
            <div class="stat-content"><div class="stat-value">${formatCurrency(avgPrice)}</div><div class="stat-label">Avg Price</div></div>
        </div>
    `;
}

function renderMenuGrid() {
    const grid = $('#menu-grid');
    if (menuData.length === 0) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-state-icon">🍽️</div><p>No menu items found. Click <strong>Add Item</strong> to get started.</p></div>`;
        return;
    }
    grid.innerHTML = menuData.map(item => {
        const availClass = item.available ? 'available' : 'unavailable';
        const availText = item.available ? 'Available' : 'Unavailable';
        const categoryEmojis = { STARTERS: '🥗', MAINS: '🍛', SIDES: '🥘', DESSERTS: '🍰', BEVERAGES: '🥤' };
        const emoji = categoryEmojis[item.category] || '🍽️';
        return `
        <div class="card" data-menu-id="${item.id}" style="${!item.available ? 'opacity:0.65' : ''}">
            <div class="card-header">
                <div class="card-title-group">
                    <div class="card-title">${emoji} ${item.name}</div>
                    <div class="card-subtitle"><span class="badge badge-category">${formatCategory(item.category)}</span></div>
                </div>
                <div class="card-actions">
                    <button class="btn btn-icon" title="Edit" onclick="editMenuItem(${item.id})">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    </button>
                    <button class="btn btn-icon danger" title="Delete" onclick="confirmDeleteMenu(${item.id}, '${item.name.replace(/'/g, "\\'")}')">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                    </button>
                </div>
            </div>
            <div class="card-body">
                <div class="card-detail">
                    <span class="card-detail-label">Price</span>
                    <span class="price-tag">${formatCurrency(item.price)}</span>
                </div>
                <div class="card-detail">
                    <span class="card-detail-label">Availability</span>
                    <span class="badge badge-${availClass}"><span class="badge-dot"></span>${availText}</span>
                </div>
            </div>
            <div class="card-footer">
                <span style="font-size:var(--font-xs);color:var(--text-muted)">Updated ${formatDate(item.updatedAt)}</span>
                <button class="btn btn-sm btn-ghost" onclick="toggleMenuAvailability(${item.id})">
                    ${item.available ? 'Mark Unavailable' : 'Mark Available'}
                </button>
            </div>
        </div>`;
    }).join('');
}

// Toggle availability
window.toggleMenuAvailability = async function(id) {
    try {
        const updated = await apiFetch(`${API.menu}/${id}/toggle-availability`, { method: 'PATCH' });
        showToast(`${updated.name} → ${updated.available ? 'Available' : 'Unavailable'}`);
        loadMenu();
    } catch (e) { /* toast shown */ }
};

// Filter
$('#menu-category-filter').addEventListener('change', loadMenu);

// Add menu item
$('#btn-add-menu-item').addEventListener('click', () => {
    $('#menu-modal-title').textContent = 'Add Menu Item';
    $('#menu-submit-btn').textContent = 'Save Item';
    $('#menu-form').reset();
    $('#menu-edit-id').value = '';
    $('#menu-available').checked = true;
    openModal('menu-modal');
});

// Edit menu item
window.editMenuItem = function(id) {
    const item = menuData.find(m => m.id === id);
    if (!item) return;
    $('#menu-modal-title').textContent = 'Edit Menu Item';
    $('#menu-submit-btn').textContent = 'Update Item';
    $('#menu-edit-id').value = item.id;
    $('#menu-name').value = item.name;
    $('#menu-category').value = item.category;
    $('#menu-price').value = item.price;
    $('#menu-available').checked = item.available;
    openModal('menu-modal');
};

// Menu form submit
$('#menu-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const editId = $('#menu-edit-id').value;
    const payload = {
        name: $('#menu-name').value.trim(),
        category: $('#menu-category').value,
        price: parseFloat($('#menu-price').value),
        available: $('#menu-available').checked,
    };

    try {
        if (editId) {
            await apiFetch(`${API.menu}/${editId}`, { method: 'PUT', body: JSON.stringify(payload) });
            showToast('Menu item updated');
        } else {
            await apiFetch(API.menu, { method: 'POST', body: JSON.stringify(payload) });
            showToast('Menu item created');
        }
        closeModal('menu-modal');
        loadMenu();
    } catch (e) { /* toast shown */ }
});

// Delete menu item
window.confirmDeleteMenu = function(id, name) {
    pendingDelete = { type: 'menu', id };
    $('#delete-modal-message').textContent = `Are you sure you want to delete "${name}"?`;
    openModal('delete-modal');
};

// Menu modal close
$('#menu-modal-close').addEventListener('click', () => closeModal('menu-modal'));
$('#menu-modal-cancel').addEventListener('click', () => closeModal('menu-modal'));

// ──────────────────────────────────────────
//  DELETE CONFIRMATION
// ──────────────────────────────────────────
$('#delete-modal-confirm').addEventListener('click', async () => {
    const { type, id } = pendingDelete;
    try {
        if (type === 'room') {
            await apiFetch(`${API.rooms}/${id}`, { method: 'DELETE' });
            showToast('Room deleted');
            loadRooms();
        } else if (type === 'menu') {
            await apiFetch(`${API.menu}/${id}`, { method: 'DELETE' });
            showToast('Menu item deleted');
            loadMenu();
        }
    } catch (e) { /* toast shown */ }
    closeModal('delete-modal');
});

$('#delete-modal-close').addEventListener('click', () => closeModal('delete-modal'));
$('#delete-modal-cancel').addEventListener('click', () => closeModal('delete-modal'));

// Close modals on overlay click
$$('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) overlay.classList.remove('open');
    });
});

// Close modals on Escape key
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        $$('.modal-overlay.open').forEach(m => m.classList.remove('open'));
    }
});

// ──────────────────────────────────────────
//  INIT
// ──────────────────────────────────────────
loadRooms();
loadMenu();
