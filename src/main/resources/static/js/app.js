/* ═══════════════════════════════════════════
   HOSPITALITY OPERATIONS — APP.JS
   Matches wireframe navigation structure:
   HOME → LODGING → ROOMS SEARCH / ACTION ITEMS
        → RESTAURANT → MENU MGMT / ORDER MGMT / TABLE MGMT / BILLING
   ═══════════════════════════════════════════ */

const API = { rooms: '/api/v1/rooms', menu: '/api/v1/menu', auth: '/api/v1/auth', actionItems: '/api/v1/action-items', bills: '/api/v1/bills', orders: '/api/v1/orders', tables: '/api/v1/tables' };

// ── Helpers ───────────────────────────────
const $ = sel => document.querySelector(sel);
const $$ = sel => document.querySelectorAll(sel);

function fmt(val) { return '₹' + Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
function fmtDate(iso) { if (!iso) return '—'; return new Date(iso).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' }); }
function fmtType(t) { return { DELUXE_KING:'DELUXE KING', STD_DOUBLE:'STD DOUBLE', SUITE:'SUITE' }[t] || t; }
function fmtStatus(s) { return s.replace(/_/g,' '); }

// ── Toast ─────────────────────────────────
function toast(msg, type = 'success') {
    const el = document.createElement('div');
    el.className = `toast toast-${type}`;
    el.innerHTML = `<span>${type === 'success' ? '✓' : '✕'}</span> ${msg}`;
    $('#toast-container').appendChild(el);
    setTimeout(() => { el.classList.add('exit'); setTimeout(() => el.remove(), 300); }, 3000);
}

// ── Modal ─────────────────────────────────
function openModal(id) { document.getElementById(id).classList.add('open'); }
window.closeModal = function(id) { document.getElementById(id).classList.remove('open'); };

$$('.modal-overlay').forEach(ov => {
    ov.addEventListener('click', e => { if (e.target === ov) ov.classList.remove('open'); });
});
document.addEventListener('keydown', e => { if (e.key === 'Escape') $$('.modal-overlay.open').forEach(m => m.classList.remove('open')); });

// ── AUTH STATE ─────────────────────────────
let authToken = localStorage.getItem('authToken') || null;
let isAuthenticated = !!authToken;

// ── API ───────────────────────────────────
async function api(url, opts = {}) {
    try {
        const headers = { 'Content-Type': 'application/json', ...opts.headers };
        if (authToken) {
            headers['Authorization'] = `Bearer ${authToken}`;
        }
        const r = await fetch(url, { headers, ...opts });
        if (r.status === 204) return null;
        if (!r.ok) { 
            if (r.status === 401) {
                authToken = null;
                isAuthenticated = false;
                localStorage.removeItem('authToken');
                showLoginPage();
                throw new Error('Session expired. Please login again.');
            }
            const e = await r.json().catch(() => ({})); 
            throw new Error(e.message || `HTTP ${r.status}`); 
        }
        return r.json();
    } catch (e) { toast(e.message, 'error'); throw e; }
}

// ═══════════════════════════════════════════
//  AUTHENTICATION
// ═══════════════════════════════════════════

function showLoginPage() {
    const topbar = document.getElementById('topbar');
    const appBody = document.querySelector('.app-body');
    const loginPage = document.getElementById('login-page');
    if (topbar) topbar.style.display = 'none';
    if (appBody) appBody.style.display = 'none';
    if (loginPage) loginPage.style.display = 'flex';
}

function showAppPage() {
    const topbar = document.getElementById('topbar');
    const appBody = document.querySelector('.app-body');
    const loginPage = document.getElementById('login-page');
    if (topbar) topbar.style.display = 'flex';
    if (appBody) appBody.style.display = 'flex';
    if (loginPage) loginPage.style.display = 'none';
}

// Check authentication on page load
(function() {
    document.addEventListener('DOMContentLoaded', () => {
        if (authToken) {
            showAppPage();
            showPage('home');
        } else {
            showLoginPage();
        }
    });
})();

// Login form
document.getElementById('login-form')?.addEventListener('submit', async e => {
    e.preventDefault();
    const usernameInput = document.getElementById('login-username');
    const passwordInput = document.getElementById('login-password');
    const errorEl = document.getElementById('login-error');
    if (!usernameInput || !passwordInput) return;
    
    const username = usernameInput.value.trim();
    const password = passwordInput.value;
    
    if (errorEl) errorEl.style.display = 'none';
    
    try {
        const response = await fetch(`${API.auth}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || 'Login failed');
        }
        
        const data = await response.json();
        authToken = data.token;
        isAuthenticated = true;
        localStorage.setItem('authToken', authToken);
        
        toast('Login successful');
        const form = document.getElementById('login-form');
        if (form) form.reset();
        showAppPage();
        showPage('home');
    } catch (err) {
        if (errorEl) {
            errorEl.textContent = err.message;
            errorEl.style.display = 'block';
        }
    }
});

// Logout button
document.getElementById('btn-logout')?.addEventListener('click', () => {
    authToken = null;
    isAuthenticated = false;
    localStorage.removeItem('authToken');
    const form = document.getElementById('login-form');
    if (form) form.reset();
    showLoginPage();
    toast('Logged out');
});

// ═══════════════════════════════════════════
//  NAVIGATION
// ═══════════════════════════════════════════
const navHistory = [];
let currentPage = 'home';

// Pages that show the sidebar
const sidebarPages = new Set(['rooms-search', 'restaurant', 'menu-mgmt', 'order-mgmt', 'action-items']);

// Map pages → topbar titles
const pageTitles = {
    'home': 'MANAGEMENT SYSTEM',
    'lodging': 'MANAGEMENT SYSTEM',
    'rooms-search': 'MANAGEMENT SYSTEM',
    'action-items': 'ACTION ITEMS',
    'restaurant': 'MANAGEMENT SYSTEM',
    'menu-mgmt': 'MANAGEMENT SYSTEM',
    'order-mgmt': 'MANAGEMENT SYSTEM',
    'table-mgmt': 'MANAGEMENT SYSTEM',
    'billing': 'BILLING',
};

// Map pages → parent pages (for back button)
const pageParent = {
    'lodging': 'home',
    'rooms-search': 'lodging',
    'action-items': 'lodging',
    'restaurant': 'home',
    'menu-mgmt': 'restaurant',
    'order-mgmt': 'restaurant',
    'table-mgmt': 'restaurant',
    'billing': 'restaurant',
};

// Map pages → active sidebar item
const sidebarActive = {
    'rooms-search': 'rooms-search',
    'restaurant': 'restaurant',
    'menu-mgmt': 'restaurant',
    'order-mgmt': 'restaurant',
    'table-mgmt': 'restaurant',
    'action-items': 'action-items',
};

function navigate(page) {
    if (page === currentPage) return;
    navHistory.push(currentPage);
    showPage(page);
}

function goBack() {
    const prev = navHistory.pop() || pageParent[currentPage] || 'home';
    showPage(prev);
}

function showPage(page) {
    currentPage = page;

    // Update pages
    $$('.page').forEach(p => p.classList.remove('active'));
    const el = $(`#page-${page}`);
    if (el) el.classList.add('active');

    // Topbar title
    $('#topbar-title').textContent = pageTitles[page] || 'MANAGEMENT SYSTEM';

    // Sidebar visibility
    const sidebar = $('#sidebar');
    if (sidebarPages.has(page)) {
        sidebar.classList.remove('hidden');
        // Update active item
        $$('.sidebar-item').forEach(si => {
            si.classList.toggle('active', si.dataset.target === sidebarActive[page]);
        });
    } else {
        sidebar.classList.add('hidden');
    }

    // Load data for the page
    if (page === 'rooms-search') loadRooms();
    if (page === 'menu-mgmt') loadMenu();
    if (page === 'action-items') loadActionItems();
    if (page === 'billing') loadBillingPage();
    if (page === 'order-mgmt') loadOrders();
}

// Back button
$('#btn-back').addEventListener('click', goBack);

// Hub card navigation
$$('[data-navigate]').forEach(btn => {
    btn.addEventListener('click', () => navigate(btn.dataset.navigate));
});

// Sidebar navigation
$$('.sidebar-item').forEach(item => {
    item.addEventListener('click', () => {
        const target = item.dataset.target;
        if (target && target !== currentPage) {
            navHistory.length = 0; // Reset history for sidebar nav
            showPage(target);
        }
    });
});

// ═══════════════════════════════════════════
//  ROOM SEARCH MODULE
// ═══════════════════════════════════════════
let roomsData = [];
let roomsSearchTerm = '';

async function loadRooms() {
    // Gather checked filters
    const checked = [...$$('#room-filter-checks input:checked')].map(c => c.value);
    try {
        const allRooms = await api(API.rooms);
        if (checked.length > 0 && checked.length < 6) {
            roomsData = allRooms.filter(r => checked.includes(r.status));
        } else {
            roomsData = allRooms;
        }
        renderRoomGrid();
    } catch (e) { /* toast shown */ }
}

function roomBadgeClass(status) {
    switch (status) {
        case 'VACANT': return 'badge-filled';
        case 'OCCUPIED': return 'badge-outlined';
        case 'RESERVED': return 'badge-dashed';
        case 'MAINT': return 'badge-filled';
        case 'MAINTAINED': return 'badge-outlined';
        case 'UNDER_MAINTENANCE': return 'badge-outlined';
        default: return 'badge-outlined';
    }
}

function renderRoomGrid() {
    const grid = $('#room-grid');
    const data = roomsSearchTerm
        ? roomsData.filter(r => (r.roomNumber && r.roomNumber.toLowerCase().includes(roomsSearchTerm))
            || (r.type && r.type.toLowerCase().includes(roomsSearchTerm))
            || (r.floor && r.floor.toString().includes(roomsSearchTerm))
            || (r.status && r.status.toLowerCase().includes(roomsSearchTerm))
            || (r.issueNotes && r.issueNotes.toLowerCase().includes(roomsSearchTerm)))
        : roomsData;
    if (!data.length) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-state-icon">🏨</div><p>No rooms found matching filters.</p></div>`;
        return;
    }
    grid.innerHTML = data.map(r => {
        const showIssue = r.status === 'MAINT' || r.status === 'UNDER_MAINTENANCE' || r.status === 'MAINTAINED';
        return `
        <div class="room-card">
            <div class="room-card-header">
                <span class="room-card-id">RM ${r.roomNumber}</span>
                <span class="badge ${roomBadgeClass(r.status)}">${fmtStatus(r.status)}</span>
            </div>
            <div class="room-card-body">
                <div class="room-card-row"><span class="room-card-label">TYPE:</span><span class="room-card-value">${fmtType(r.type)}</span></div>
                <div class="room-card-row"><span class="room-card-label">FLOOR:</span><span class="room-card-value">${String(r.floor).padStart(2,'0')}</span></div>
                ${showIssue && r.issueNotes
                    ? `<div class="room-card-row"><span class="room-card-label">ISSUE:</span><span class="room-card-value">${r.issueNotes}</span></div>`
                    : `<div class="room-card-row"><span class="room-card-label">RATE:</span><span class="room-card-value">${fmt(r.ratePerNight)}/NT</span></div>`
                }
            </div>
            <div class="room-card-image">${r.imageUrl ? `<img src="${r.imageUrl}" alt="Room ${r.roomNumber}">` : 'NO IMAGE'}</div>
            <div class="room-card-actions" style="margin-top:12px;display:flex;gap:6px;justify-content:flex-end;flex-wrap:wrap">
                <button class="btn btn-outline btn-sm" onclick="billRoom(${r.id})" style="color:var(--primary)">BILL</button>
                <button class="btn btn-outline btn-sm" onclick="editRoom(${r.id})">EDIT</button>
                <button class="btn btn-outline btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="confirmDeleteRoom(${r.id},'${r.roomNumber}')">DELETE</button>
            </div>
        </div>`;
    }).join('');
}

// Filter checkboxes
$$('#room-filter-checks input').forEach(cb => cb.addEventListener('change', loadRooms));

// Room search
document.getElementById('room-search')?.addEventListener('input', function() {
    roomsSearchTerm = this.value.toLowerCase().trim();
    renderRoomGrid();
});

// Clear filters
$('#btn-clear-room-filters').addEventListener('click', () => {
    $$('#room-filter-checks input').forEach(cb => cb.checked = false);
    const searchInput = document.getElementById('room-search');
    if (searchInput) { searchInput.value = ''; roomsSearchTerm = ''; }
    loadRooms();
});

// Add room
$('#btn-add-room').addEventListener('click', () => {
    $('#room-modal-title').textContent = 'ADD ROOM';
    $('#room-submit-btn').textContent = 'SAVE';
    $('#room-form').reset();
    $('#room-edit-id').value = '';
    openModal('room-modal');
});

// Edit room
window.editRoom = async function(id) {
    const r = roomsData.find(x => x.id === id);
    if (!r) return;
    $('#room-modal-title').textContent = 'EDIT ROOM';
    $('#room-submit-btn').textContent = 'UPDATE';
    $('#room-edit-id').value = r.id;
    $('#room-number').value = r.roomNumber;
    $('#room-type').value = r.type;
    $('#room-floor').value = r.floor;
    $('#room-rate').value = r.ratePerNight;
    $('#room-status-input').value = r.status;
    $('#room-image-url').value = r.imageUrl || '';
    $('#room-issue-notes').value = r.issueNotes || '';
    openModal('room-modal');
};

// Room form submit
$('#room-form').addEventListener('submit', async e => {
    e.preventDefault();
    const editId = $('#room-edit-id').value;
    const body = JSON.stringify({
        roomNumber: $('#room-number').value.trim(),
        type: $('#room-type').value,
        floor: parseInt($('#room-floor').value),
        status: $('#room-status-input').value,
        ratePerNight: parseFloat($('#room-rate').value),
        imageUrl: $('#room-image-url').value.trim() || null,
        issueNotes: $('#room-issue-notes').value.trim() || null,
    });
    try {
        if (editId) {
            await api(`${API.rooms}/${editId}`, { method: 'PUT', body });
            toast('Room updated');
        } else {
            await api(API.rooms, { method: 'POST', body });
            toast('Room created');
        }
        closeModal('room-modal');
        loadRooms();
    } catch (e) { /* toast shown */ }
});

// Delete room
let pendingDelete = {};

window.confirmDeleteRoom = function(id, num) {
    pendingDelete = { type: 'room', id };
    $('#delete-msg').textContent = `Delete Room ${num}? This cannot be undone.`;
    openModal('delete-modal');
};

// ═══════════════════════════════════════════
//  MENU MANAGEMENT MODULE
// ═══════════════════════════════════════════
let menuData = [];
let menuCategoryFilter = '';
let menuSearchTerm = '';

async function loadMenu() {
    const url = menuCategoryFilter ? `${API.menu}?category=${menuCategoryFilter}` : API.menu;
    try {
        menuData = await api(url);
        renderMenuGrid();
    } catch (e) { /* toast shown */ }
}

function renderMenuGrid() {
    const grid = $('#menu-grid');
    let html = '';
    const data = menuSearchTerm
        ? menuData.filter(item => (item.name && item.name.toLowerCase().includes(menuSearchTerm))
            || (item.category && item.category.toLowerCase().includes(menuSearchTerm))
            || (item.price && item.price.toString().includes(menuSearchTerm)))
        : menuData;

    if (!data.length && (menuCategoryFilter || menuSearchTerm)) {
        html = `<div class="empty-state"><div class="empty-state-icon">🍽️</div><p>No items matching filters.</p></div>`;
    } else if (!data.length) {
        html = `<div class="empty-state"><div class="empty-state-icon">🍽️</div><p>No menu items yet. Add your first item!</p></div>`;
    } else {
        html = data.map(item => `
        <div class="menu-card${item.available ? '' : ' unavailable'}">
            <div class="menu-card-header">
                <span class="menu-card-name">${item.name}</span>
                <span class="menu-card-price">${fmt(item.price)}</span>
            </div>
            <div class="menu-card-body">
                <span class="badge badge-outlined">${item.category}</span>
            </div>
            <div class="menu-card-footer">
                <button class="btn btn-outline btn-sm" onclick="editMenuItem(${item.id})">EDIT</button>
                <button class="btn btn-outline btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="confirmDeleteMenu(${item.id},'${item.name.replace(/'/g,"\\'")}')">DELETE</button>
            </div>
        </div>`).join('');
    }

    // Always append the "Add New Item" card
    html += `
    <div class="add-new-card" id="add-new-menu-card">
        <span class="add-new-card-icon">+</span>
        <span class="add-new-card-label">ADD NEW ITEM</span>
    </div>`;

    grid.innerHTML = html;

    // Wire up the add-new card click
    $('#add-new-menu-card').addEventListener('click', () => {
        $('#menu-modal-title').textContent = 'ADD ITEM';
        $('#menu-submit-btn').textContent = 'SAVE';
        $('#menu-form').reset();
        $('#menu-edit-id').value = '';
        $('#menu-available').checked = true;
        openModal('menu-modal');
    });
}

// Menu search
document.getElementById('menu-search')?.addEventListener('input', function() {
    menuSearchTerm = this.value.toLowerCase().trim();
    renderMenuGrid();
});

// Category filter
$$('#menu-category-filters .filter-list-item').forEach(btn => {
    btn.addEventListener('click', () => {
        $$('#menu-category-filters .filter-list-item').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        menuCategoryFilter = btn.dataset.value || '';
        document.getElementById('menu-search')?.value ? (document.getElementById('menu-search').value = '', menuSearchTerm = '') : null;
        loadMenu();
    });
});

// Add menu item button (toolbar)
$('#btn-add-menu-item').addEventListener('click', () => {
    $('#menu-modal-title').textContent = 'ADD ITEM';
    $('#menu-submit-btn').textContent = 'SAVE';
    $('#menu-form').reset();
    $('#menu-edit-id').value = '';
    $('#menu-available').checked = true;
    openModal('menu-modal');
});

// Edit menu item
window.editMenuItem = function(id) {
    const item = menuData.find(m => m.id === id);
    if (!item) return;
    $('#menu-modal-title').textContent = 'EDIT ITEM';
    $('#menu-submit-btn').textContent = 'UPDATE';
    $('#menu-edit-id').value = item.id;
    $('#menu-name').value = item.name;
    $('#menu-category').value = item.category;
    $('#menu-price').value = item.price;
    $('#menu-available').checked = item.available;
    openModal('menu-modal');
};

// Menu form submit
$('#menu-form').addEventListener('submit', async e => {
    e.preventDefault();
    const editId = $('#menu-edit-id').value;
    const body = JSON.stringify({
        name: $('#menu-name').value.trim(),
        category: $('#menu-category').value,
        price: parseFloat($('#menu-price').value),
        available: $('#menu-available').checked,
    });
    try {
        if (editId) {
            await api(`${API.menu}/${editId}`, { method: 'PUT', body });
            toast('Menu item updated');
        } else {
            await api(API.menu, { method: 'POST', body });
            toast('Menu item created');
        }
        closeModal('menu-modal');
        loadMenu();
    } catch (e) { /* toast shown */ }
});

// Delete menu item
window.confirmDeleteMenu = function(id, name) {
    pendingDelete = { type: 'menu', id };
    $('#delete-msg').textContent = `Delete "${name}"? This cannot be undone.`;
    openModal('delete-modal');
};

// ═══════════════════════════════════════════
//  ACTION ITEMS MODULE
// ═══════════════════════════════════════════
let actionItemsData = [];
let actionStatusFilter = '';
let actionCategoryFilter = '';
let actionSearchTerm = '';

async function loadActionItems() {
    const params = new URLSearchParams();
    if (actionStatusFilter) params.set('status', actionStatusFilter);
    if (actionCategoryFilter) params.set('category', actionCategoryFilter);
    const qs = params.toString();
    const url = qs ? `${API.actionItems}?${qs}` : API.actionItems;
    try {
        actionItemsData = await api(url);
        renderActionItemGrid();
    } catch (e) { /* toast shown */ }
}

function renderActionItemGrid() {
    const grid = $('#action-grid');
    let html = '';
    const data = actionSearchTerm
        ? actionItemsData.filter(item => (item.title && item.title.toLowerCase().includes(actionSearchTerm))
            || (item.description && item.description.toLowerCase().includes(actionSearchTerm))
            || (item.category && item.category.toLowerCase().includes(actionSearchTerm)))
        : actionItemsData;

    if (!data.length) {
        html = `<div class="empty-state"><div class="empty-state-icon">📋</div><p>No action items found matching filters.</p></div>`;
    } else {
        html = data.map(item => {
            const isCompleted = item.status === 'COMPLETED';
            return `
            <div class="room-card" style="${isCompleted ? 'opacity:0.6' : ''}">
                <div class="room-card-header">
                    <span class="room-card-id">${item.title}</span>
                    <span class="badge ${isCompleted ? 'badge-filled' : 'badge-outlined'}">${item.status}</span>
                </div>
                <div class="room-card-body">
                    <div class="room-card-row"><span class="room-card-label">CATEGORY:</span><span class="room-card-value">${item.category}</span></div>
                    ${item.description ? `<div class="room-card-row"><span class="room-card-label">DESC:</span><span class="room-card-value">${item.description}</span></div>` : ''}
                    <div class="room-card-row"><span class="room-card-label">CREATED:</span><span class="room-card-value">${fmtDate(item.createdAt)}</span></div>
                    ${item.completedAt ? `<div class="room-card-row"><span class="room-card-label">DONE:</span><span class="room-card-value">${fmtDate(item.completedAt)}</span></div>` : ''}
                </div>
                <div class="room-card-actions" style="margin-top:12px;display:flex;gap:6px;justify-content:flex-end;flex-wrap:wrap">
                    <button class="btn btn-outline btn-sm" onclick="toggleActionItemStatus(${item.id},'${isCompleted ? 'TODO' : 'COMPLETED'}')">${isCompleted ? 'REOPEN' : 'COMPLETE'}</button>
                    <button class="btn btn-outline btn-sm" onclick="editActionItem(${item.id})">EDIT</button>
                    <button class="btn btn-outline btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="confirmDeleteActionItem(${item.id},'${item.title.replace(/'/g,"\\'")}')">DELETE</button>
                </div>
            </div>`;
        }).join('');
    }

    html += `
    <div class="add-new-card" id="add-new-action-card">
        <span class="add-new-card-icon">+</span>
        <span class="add-new-card-label">ADD NEW TASK</span>
    </div>`;

    grid.innerHTML = html;

    document.getElementById('add-new-action-card')?.addEventListener('click', () => {
        document.getElementById('action-modal-title').textContent = 'ADD TASK';
        document.getElementById('action-submit-btn').textContent = 'SAVE';
        document.getElementById('action-form').reset();
        document.getElementById('action-edit-id').value = '';
        openModal('action-modal');
    });
}

window.editActionItem = function(id) {
    const item = actionItemsData.find(x => x.id === id);
    if (!item) return;
    document.getElementById('action-modal-title').textContent = 'EDIT TASK';
    document.getElementById('action-submit-btn').textContent = 'UPDATE';
    document.getElementById('action-edit-id').value = item.id;
    document.getElementById('action-title').value = item.title;
    document.getElementById('action-description').value = item.description || '';
    document.getElementById('action-category').value = item.category;
    openModal('action-modal');
};

window.toggleActionItemStatus = async function(id, newStatus) {
    try {
        const updated = await api(`${API.actionItems}/${id}/status?status=${newStatus}`, { method: 'PATCH' });
        toast(updated.status === 'COMPLETED' ? 'Task completed' : 'Task reopened');
        loadActionItems();
    } catch (e) { /* toast shown */ }
};

window.confirmDeleteActionItem = function(id, title) {
    pendingDelete = { type: 'action', id };
    document.getElementById('delete-msg').textContent = `Delete "${title}"? This cannot be undone.`;
    openModal('delete-modal');
};

document.getElementById('action-form')?.addEventListener('submit', async e => {
    e.preventDefault();
    const editId = document.getElementById('action-edit-id').value;
    const body = JSON.stringify({
        title: document.getElementById('action-title').value.trim(),
        description: document.getElementById('action-description').value.trim() || null,
        category: document.getElementById('action-category').value,
    });
    try {
        if (editId) {
            await api(`${API.actionItems}/${editId}`, { method: 'PUT', body });
            toast('Task updated');
        } else {
            await api(API.actionItems, { method: 'POST', body });
            toast('Task created');
        }
        closeModal('action-modal');
        loadActionItems();
    } catch (e) { /* toast shown */ }
});

// ═══════════════════════════════════════════
//  DELETE CONFIRM HANDLER
// ═══════════════════════════════════════════
$('#btn-confirm-delete').addEventListener('click', async () => {
    const { type, id } = pendingDelete;
    try {
        if (type === 'room') {
            await api(`${API.rooms}/${id}`, { method: 'DELETE' });
            toast('Room deleted');
            loadRooms();
        } else if (type === 'menu') {
            await api(`${API.menu}/${id}`, { method: 'DELETE' });
            toast('Menu item deleted');
            loadMenu();
        } else if (type === 'action') {
            await api(`${API.actionItems}/${id}`, { method: 'DELETE' });
            toast('Task deleted');
            loadActionItems();
        } else if (type === 'order') {
            await api(`${API.orders}/${id}`, { method: 'DELETE' });
            toast('Order deleted');
            loadOrders();
        }
    } catch (e) { /* toast shown */ }
    closeModal('delete-modal');
});

// ═══════════════════════════════════════════
//  TOGGLE GROUPS (Action Items: TO DO / COMPLETED)
// ═══════════════════════════════════════════
$$('.toggle-group').forEach(group => {
    group.querySelectorAll('.toggle-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            group.querySelectorAll('.toggle-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            if (group.id === 'action-todo-toggle') {
                actionStatusFilter = btn.dataset.value || '';
                loadActionItems();
            }
        });
    });
});

// Action search
document.getElementById('action-search')?.addEventListener('input', function() {
    actionSearchTerm = this.value.toLowerCase().trim();
    renderActionItemGrid();
});

// ═══════════════════════════════════════════
//  ACTION ITEMS CATEGORY FILTERS
// ═══════════════════════════════════════════
$$('#action-category-filters .filter-list-item').forEach(btn => {
    btn.addEventListener('click', () => {
        $$('#action-category-filters .filter-list-item').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        actionCategoryFilter = btn.dataset.value || '';
        loadActionItems();
    });
});

// ═══════════════════════════════════════════
//  ORDER STATUS FILTERS (shell)
// ═══════════════════════════════════════════
$$('#order-status-filters .filter-list-item').forEach(btn => {
    btn.addEventListener('click', () => {
        $$('#order-status-filters .filter-list-item').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
    });
});

// ═══════════════════════════════════════════
//  TABLE MANAGEMENT TABS (shell)
// ═══════════════════════════════════════════
$$('#table-tab-bar .tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        $$('#table-tab-bar .tab-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
    });
});

// ═══════════════════════════════════════════
//  ORDER MANAGEMENT MODULE
// ═══════════════════════════════════════════
let ordersData = [];
let orderStatusFilter = 'ALL';
let ordersSearchTerm = '';
let menuItemsForOrders = [];
let tablesForOrders = [];
let orderFormItems = [];

function getDateRange(filter) {
    const now = new Date();
    const start = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    let from, to;

    switch (filter) {
        case 'TODAY':
            from = start;
            to = new Date(start.getTime() + 86400000);
            break;
        case 'WEEK':
            const dayOfWeek = start.getDay();
            const mondayOffset = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
            from = new Date(start.getTime() + mondayOffset * 86400000);
            to = new Date(from.getTime() + 7 * 86400000);
            break;
        case 'MONTH':
            from = new Date(now.getFullYear(), now.getMonth(), 1);
            to = new Date(now.getFullYear(), now.getMonth() + 1, 1);
            break;
        case 'CUSTOM':
            const fromVal = document.getElementById('order-date-from')?.value;
            const toVal = document.getElementById('order-date-to')?.value;
            if (fromVal) from = new Date(fromVal + 'T00:00:00');
            if (toVal) to = new Date(toVal + 'T23:59:59');
            break;
        default:
            return {};
    }
    return { dateFrom: from ? from.toISOString() : null, dateTo: to ? to.toISOString() : null };
}

async function loadOrders() {
    const params = new URLSearchParams();

    // Status filter
    const activeStatusBtn = document.querySelector('#order-status-filters .filter-list-item.active');
    const status = activeStatusBtn ? activeStatusBtn.dataset.value : 'ALL';
    if (status && status !== 'ALL' && status !== 'ACTIVE') {
        params.set('status', status);
    } else if (status === 'ACTIVE') {
        params.set('status', 'ACTIVE');
    }

    // Date filter
    const dateFilter = document.getElementById('order-date-filter')?.value || 'ALL';
    const { dateFrom, dateTo } = getDateRange(dateFilter);
    if (dateFrom) params.set('dateFrom', dateFrom);
    if (dateTo) params.set('dateTo', dateTo);

    const qs = params.toString();
    const url = qs ? `${API.orders}?${qs}` : API.orders;

    try {
        ordersData = await api(url);
        renderOrderGrid();
    } catch (e) { /* toast shown */ }
}

function renderOrderGrid() {
    const grid = document.getElementById('order-grid');
    if (!grid) return;
    const data = ordersSearchTerm
        ? ordersData.filter(order => (order.orderReference && order.orderReference.toLowerCase().includes(ordersSearchTerm))
            || (order.status && order.status.toLowerCase().includes(ordersSearchTerm))
            || (order.tableNumber && order.tableNumber.toLowerCase().includes(ordersSearchTerm))
            || (order.id && order.id.toString().includes(ordersSearchTerm))
            || (order.items && order.items.some(i => i.menuItemName && i.menuItemName.toLowerCase().includes(ordersSearchTerm))))
        : ordersData;

    if (!data.length) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-state-icon">📝</div><p>No orders found matching filters.</p></div>`;
        return;
    }

    grid.innerHTML = data.map(order => {
        const itemSummary = order.items && order.items.length
            ? order.items.map(i => `${i.quantity}x ${i.menuItemName || 'Item #' + i.menuItemId}`).join(', ')
            : 'No items';
        return `
        <div class="room-card">
            <div class="room-card-header">
                <span class="room-card-id">${order.orderReference || 'ORD #' + order.id}</span>
                <span class="badge ${order.status === 'COMPLETED' ? 'badge-filled' : order.status === 'CANCELLED' ? 'badge-unavailable' : 'badge-outlined'}">${order.status}</span>
            </div>
            <div class="room-card-body">
                <div class="room-card-row"><span class="room-card-label">TABLE:</span><span class="room-card-value">${order.tableNumber || '—'}</span></div>
                <div class="room-card-row"><span class="room-card-label">TOTAL:</span><span class="room-card-value">${fmt(order.totalAmount)}</span></div>
                <div class="room-card-row"><span class="room-card-label">ITEMS:</span><span class="room-card-value" style="font-size:10px;text-transform:none;font-weight:600">${itemSummary}</span></div>
                <div class="room-card-row"><span class="room-card-label">DATE:</span><span class="room-card-value">${fmtDate(order.createdAt)}</span></div>
            </div>
            <div class="room-card-actions" style="margin-top:12px;display:flex;gap:6px;justify-content:flex-end;flex-wrap:wrap">
                ${order.status === 'NEW' ? `<button class="btn btn-outline btn-sm" onclick="updateOrderStatus(${order.id},'PREPARING')" style="color:var(--success)">PREPARE</button>` : ''}
                ${order.status === 'PREPARING' ? `<button class="btn btn-outline btn-sm" onclick="updateOrderStatus(${order.id},'READY')" style="color:var(--warning)">MARK READY</button>` : ''}
                ${order.status === 'READY' ? `<button class="btn btn-outline btn-sm" onclick="updateOrderStatus(${order.id},'COMPLETED')" style="color:var(--success)">COMPLETE</button>` : ''}
                ${order.status === 'NEW' || order.status === 'PREPARING' || order.status === 'READY' ? `<button class="btn btn-outline btn-sm" onclick="updateOrderStatus(${order.id},'CANCELLED')" style="color:var(--danger)">CANCEL</button>` : ''}
                <button class="btn btn-outline btn-sm" onclick="editOrder(${order.id})">EDIT</button>
                <button class="btn btn-outline btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="confirmDeleteOrder(${order.id},'${order.orderReference}')">DELETE</button>
            </div>
        </div>`;
    }).join('');
}

window.updateOrderStatus = async function(id, newStatus) {
    try {
        const updated = await api(`${API.orders}/${id}/status?status=${newStatus}`, { method: 'PATCH' });
        toast('Order ' + newStatus.toLowerCase());
        loadOrders();
    } catch (e) { /* toast shown */ }
};

window.confirmDeleteOrder = function(id, ref) {
    pendingDelete = { type: 'order', id };
    document.getElementById('delete-msg').textContent = `Delete order ${ref || '#' + id}? This cannot be undone.`;
    openModal('delete-modal');
};

// Order search
document.getElementById('order-search')?.addEventListener('input', function() {
    ordersSearchTerm = this.value.toLowerCase().trim();
    renderOrderGrid();
});

// Order status filter buttons
document.querySelectorAll('#order-status-filters .filter-list-item').forEach(btn => {
    btn.addEventListener('click', function() {
        document.querySelectorAll('#order-status-filters .filter-list-item').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
        loadOrders();
    });
});

// Date filter change
document.getElementById('order-date-filter')?.addEventListener('change', function() {
    const customRange = document.getElementById('order-custom-date-range');
    if (customRange) {
        customRange.style.display = this.value === 'CUSTOM' ? 'flex' : 'none';
    }
    loadOrders();
});

// Custom date range inputs
document.getElementById('order-date-from')?.addEventListener('change', loadOrders);
document.getElementById('order-date-to')?.addEventListener('change', loadOrders);

// CREATE ORDER button
document.getElementById('btn-create-order')?.addEventListener('click', openCreateOrderModal);

async function openCreateOrderModal() {
    // Load menu items and tables for the form
    try {
        menuItemsForOrders = await api(API.menu);
        tablesForOrders = await api(API.tables);
    } catch (e) { return; }

    document.getElementById('order-modal-title').textContent = 'CREATE ORDER';
    document.getElementById('order-submit-btn').textContent = 'SAVE ORDER';
    document.getElementById('order-form').reset();
    document.getElementById('order-edit-id').value = '';
    document.getElementById('order-reference').value = '';

    // Populate table dropdown
    const tableSelect = document.getElementById('order-table');
    tableSelect.innerHTML = '<option value="">— NO TABLE —</option>' +
        tablesForOrders.filter(t => t.status === 'AVAILABLE' || t.status === 'OCCUPIED')
            .map(t => `<option value="${t.id}">${t.tableNumber} (${t.status})</option>`).join('');

    orderFormItems = [];
    renderOrderFormItems();
    openModal('order-modal');
}

window.editOrder = async function(id) {
    try {
        const order = await api(`${API.orders}/${id}`);
        menuItemsForOrders = await api(API.menu);
        tablesForOrders = await api(API.tables);

        document.getElementById('order-modal-title').textContent = 'EDIT ORDER';
        document.getElementById('order-submit-btn').textContent = 'UPDATE';
        document.getElementById('order-edit-id').value = order.id;
        document.getElementById('order-reference').value = order.orderReference || '';

        const tableSelect = document.getElementById('order-table');
        tableSelect.innerHTML = '<option value="">— NO TABLE —</option>' +
            tablesForOrders.map(t => `<option value="${t.id}" ${t.id === order.tableId ? 'selected' : ''}>${t.tableNumber}</option>`).join('');
        tableSelect.value = order.tableId || '';

        orderFormItems = (order.items || []).map(item => ({
            menuItemId: item.menuItemId,
            menuItemName: item.menuItemName,
            quantity: item.quantity,
            unitPrice: item.unitPrice
        }));
        renderOrderFormItems();
        openModal('order-modal');
    } catch (e) { /* toast shown */ }
};

function renderOrderFormItems() {
    const container = document.getElementById('order-items-container');
    if (!container) return;

    if (!orderFormItems.length) {
        container.innerHTML = '<div style="padding:16px;text-align:center;color:var(--text-muted);font-size:var(--fs-sm)">No items. Click "+ ADD ITEM" to add menu items.</div>';
        updateOrderFormTotal();
        return;
    }

    container.innerHTML = orderFormItems.map((item, i) => {
        const menuOpts = menuItemsForOrders.filter(m => m.available)
            .map(m => `<option value="${m.id}" data-price="${m.price}" ${m.id === item.menuItemId ? 'selected' : ''}>${m.name} — ${fmt(m.price)}</option>`).join('');
        return `
        <div style="display:flex;gap:8px;align-items:center;padding:8px;background:var(--bg-surface-alt);border-radius:var(--radius)">
            <select style="flex:1;padding:6px 8px;background:var(--bg-input);border:1px solid var(--border);border-radius:var(--radius);color:var(--text-primary);font-size:var(--fs-sm);font-weight:600"
                onchange="updateOrderFormItem(${i},'menuItemId',this.value)">
                <option value="">— SELECT —</option>
                ${menuOpts}
            </select>
            <input type="number" value="${item.quantity}" min="1" style="width:50px;padding:6px 8px;background:var(--bg-input);border:1px solid var(--border);border-radius:var(--radius);color:var(--text-primary);font-size:var(--fs-sm);font-weight:600;text-align:center"
                onchange="updateOrderFormItem(${i},'quantity',this.value)">
            <button type="button" class="btn btn-sm" style="background:none;border:none;color:var(--danger);font-size:1.1rem;cursor:pointer;padding:2px 6px" onclick="removeOrderFormItem(${i})">&times;</button>
        </div>`;
    }).join('');

    updateOrderFormTotal();
}

function updateOrderFormItem(index, field, value) {
    if (index >= 0 && index < orderFormItems.length) {
        if (field === 'menuItemId') {
            const menuItem = menuItemsForOrders.find(m => m.id === parseInt(value));
            orderFormItems[index].menuItemId = parseInt(value);
            orderFormItems[index].menuItemName = menuItem ? menuItem.name : '';
            orderFormItems[index].unitPrice = menuItem ? menuItem.price : 0;
        } else if (field === 'quantity') {
            orderFormItems[index].quantity = parseInt(value) || 1;
        }
        updateOrderFormTotal();
    }
}

function removeOrderFormItem(index) {
    orderFormItems.splice(index, 1);
    renderOrderFormItems();
}

function updateOrderFormTotal() {
    const totalEl = document.getElementById('order-form-total');
    if (!totalEl) return;
    let total = 0;
    orderFormItems.forEach(item => {
        const menuItem = menuItemsForOrders.find(m => m.id === item.menuItemId);
        const price = menuItem ? menuItem.price : (item.unitPrice || 0);
        total += price * (item.quantity || 1);
    });
    totalEl.textContent = fmt(total);
}

// Add order item button
document.getElementById('btn-add-order-item')?.addEventListener('click', () => {
    const firstAvailable = menuItemsForOrders.find(m => m.available);
    orderFormItems.push({
        menuItemId: firstAvailable ? firstAvailable.id : null,
        menuItemName: firstAvailable ? firstAvailable.name : '',
        quantity: 1,
        unitPrice: firstAvailable ? firstAvailable.price : 0
    });
    renderOrderFormItems();
});

// Order form submit
document.getElementById('order-form')?.addEventListener('submit', async e => {
    e.preventDefault();

    const editId = document.getElementById('order-edit-id').value;
    const tableId = document.getElementById('order-table').value;
    const reference = document.getElementById('order-reference').value.trim();

    const validItems = orderFormItems.filter(item => item.menuItemId);
    if (!validItems.length) {
        toast('Add at least one menu item', 'error');
        return;
    }

    const body = {
        tableId: tableId ? parseInt(tableId) : null,
        orderReference: reference || null,
        items: validItems.map(item => ({
            menuItemId: item.menuItemId,
            quantity: item.quantity || 1,
        }))
    };

    try {
        if (editId) {
            await api(`${API.orders}/${editId}`, { method: 'PUT', body: JSON.stringify(body) });
            toast('Order updated');
        } else {
            await api(API.orders, { method: 'POST', body: JSON.stringify(body) });
            toast('Order created');
        }
        closeModal('order-modal');
        loadOrders();
    } catch (e) { /* toast shown */ }
});

// ═══════════════════════════════════════════
//  BILLING MODULE — Cart-style invoice
// ═══════════════════════════════════════════
const billingCart = { rooms: [], items: [], discount: 0, orderId: null, orderData: null };
const TAX_RATE = 0.08875;
let billingMenuItems = [];
let billingOrders = [];
let billingRoomsData = [];
let billingRoomSearchTerm = '';
let billingMenuSearchTerm = '';
let billingOrderSearchTerm = '';

function fmtCurrency(val) { return '₹' + Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }

window.billRoom = function(roomId) {
    const room = roomsData.find(r => r.id === roomId);
    if (!room) return;
    if (billingCart.rooms.find(r => r.id === roomId)) {
        toast('Room already in cart', 'error');
        return;
    }
    billingCart.rooms.push({ id: room.id, roomNumber: room.roomNumber, type: room.type, ratePerNight: room.ratePerNight, nights: 1 });
    toast('Room added to invoice');
    navigate('billing');
};

async function loadBillingPage() {
    const cartView = document.getElementById('billing-cart-view');
    const receiptView = document.getElementById('billing-receipt-view');
    if (cartView) cartView.style.display = 'block';
    if (receiptView) receiptView.style.display = 'none';

    try {
        const [menu, orders, rooms] = await Promise.all([
            api(API.menu),
            api(API.orders),
            api(API.rooms)
        ]);
        billingMenuItems = menu;
        billingOrders = orders;
        billingRoomsData = rooms;
    } catch (e) { /* toast shown */ }

    billingRoomSearchTerm = billingMenuSearchTerm = billingOrderSearchTerm = '';
    ['billing-room-search','billing-menu-search','billing-order-search'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = '';
    });
    populateBillingDropdowns();
    renderBillingCart();
}

function populateBillingDropdowns() {
    // Menu dropdown
    const menuSelect = document.getElementById('billing-menu-select');
    if (menuSelect) {
        const term = billingMenuSearchTerm.toLowerCase();
        const filtered = term
            ? billingMenuItems.filter(m => m.available && (m.name.toLowerCase().includes(term) || m.category.toLowerCase().includes(term)))
            : billingMenuItems.filter(m => m.available);
        menuSelect.innerHTML = '<option value="">— SELECT MENU ITEM —</option>' +
            filtered.map(m =>
                `<option value="${m.id}" data-price="${m.price}">${m.name} — ${fmt(m.price)}</option>`
            ).join('');
    }

    // Room dropdown
    const roomSelect = document.getElementById('billing-room-select');
    if (roomSelect) {
        const term = billingRoomSearchTerm.toLowerCase();
        let occupiedRooms = billingRoomsData.filter(r => r.status === 'OCCUPIED');
        if (term) {
            occupiedRooms = occupiedRooms.filter(r =>
                (r.roomNumber && r.roomNumber.toLowerCase().includes(term))
                || (r.type && r.type.toLowerCase().includes(term))
            );
        }
        roomSelect.innerHTML = '<option value="">— SELECT ROOM —</option>' +
            occupiedRooms.map(r =>
                `<option value="${r.id}" data-rate="${r.ratePerNight}">RM ${r.roomNumber} — ${fmtType(r.type)} — ${fmt(r.ratePerNight)}/NT</option>`
            ).join('');
    }

    // Order dropdown
    const orderSelect = document.getElementById('billing-order-select');
    if (orderSelect) {
        const term = billingOrderSearchTerm.toLowerCase();
        let activeOrders = billingOrders.filter(o => o.status !== 'COMPLETED' && o.status !== 'CANCELLED');
        if (term) {
            activeOrders = activeOrders.filter(o =>
                (o.orderReference && o.orderReference.toLowerCase().includes(term))
                || (o.tableNumber && o.tableNumber.toLowerCase().includes(term))
                || (o.id && o.id.toString().includes(term))
            );
        }
        orderSelect.innerHTML = '<option value="">— SELECT ORDER —</option>' +
            activeOrders.map(o =>
                `<option value="${o.id}" data-ref="${o.orderReference || ''}">${o.orderReference || 'ORD #' + o.id} — ${o.tableNumber || 'No table'} — ${fmt(o.totalAmount)}</option>`
            ).join('');
    }
}

function renderBillingCart() {
    renderBillingOrder();
    renderBillingRooms();
    renderBillingItems();
    updateBillingTotals();
}

function renderBillingOrder() {
    const section = document.getElementById('billing-order-section');
    if (!section) return;
    if (!billingCart.orderId || !billingCart.orderData) {
        section.style.display = 'none';
        return;
    }
    section.style.display = 'block';
    document.getElementById('billing-order-ref').textContent = billingCart.orderData.orderReference || 'ORD #' + billingCart.orderData.id;
    document.getElementById('billing-order-table').textContent = billingCart.orderData.tableNumber ? 'Table: ' + billingCart.orderData.tableNumber : '';
    document.getElementById('billing-order-total').textContent = fmt(billingCart.orderData.totalAmount);

    const itemsContainer = document.getElementById('billing-order-items');
    if (itemsContainer) {
        const items = billingCart.orderData.items || [];
        itemsContainer.innerHTML = items.length ? items.map(item => `
            <div class="billing-item-row" style="background:transparent;padding:4px 0">
                <span class="billing-item-desc" style="font-size:var(--fs-xs)">${item.menuItemName || 'Item #' + item.menuItemId}</span>
                <span class="billing-item-qty" style="font-size:var(--fs-xs)">${item.quantity}x</span>
                <span class="billing-item-price" style="font-size:var(--fs-xs)">${fmt(item.subtotal)}</span>
            </div>
        `).join('') : '<div style="font-size:var(--fs-xs);color:var(--text-muted)">No items in order</div>';
    }
}

function renderBillingRooms() {
    const container = document.getElementById('billing-rooms-list');
    const emptyMsg = document.getElementById('billing-no-rooms');
    if (!container) return;

    if (!billingCart.rooms.length) {
        container.innerHTML = '';
        if (emptyMsg) emptyMsg.style.display = 'block';
        return;
    }
    if (emptyMsg) emptyMsg.style.display = 'none';

    container.innerHTML = billingCart.rooms.map((r, i) => `
        <div class="billing-item-row">
            <span class="billing-item-desc">RM ${r.roomNumber} — ${fmtType(r.type)}</span>
            <span class="billing-item-qty">
                <input type="number" class="input" style="width:60px;margin:0;padding:4px 8px" value="${r.nights}" min="1"
                    onchange="updateRoomNights(${i}, this.value)">
                <span style="color:var(--text-muted);font-size:10px">NT</span>
            </span>
            <span class="billing-item-price">${fmtCurrency(r.ratePerNight)}/NT</span>
            <button class="billing-item-remove" onclick="removeRoomFromCart(${i})" title="Remove">&times;</button>
        </div>
    `).join('');
}

function renderBillingItems() {
    const container = document.getElementById('billing-items-list');
    const emptyMsg = document.getElementById('billing-no-items');
    if (!container) return;

    if (!billingCart.items.length) {
        container.innerHTML = '';
        if (emptyMsg) emptyMsg.style.display = 'block';
        return;
    }
    if (emptyMsg) emptyMsg.style.display = 'none';

    container.innerHTML = billingCart.items.map((item, i) => {
        const total = item.quantity * item.unitPrice;
        const sourceTag = item.source === 'menu' ? '<span style="font-size:9px;color:var(--text-muted);background:var(--bg-surface);padding:2px 6px;border-radius:2px;margin-right:6px">MENU</span>' : '';
        const sourceTagManual = item.source === 'manual' ? '<span style="font-size:9px;color:var(--text-muted);background:var(--bg-surface);padding:2px 6px;border-radius:2px;margin-right:6px">MANUAL</span>' : '';
        return `
        <div class="billing-item-row">
            <span class="billing-item-desc">${sourceTag}${sourceTagManual}${item.description}</span>
            <span class="billing-item-qty">${item.quantity}x</span>
            <span class="billing-item-price">${fmtCurrency(total)}</span>
            <button class="billing-item-remove" onclick="removeBillingItem(${i})" title="Remove">&times;</button>
        </div>
    `}).join('');
}

function updateBillingTotals() {
    const subtotalEl = document.getElementById('billing-subtotal');
    const taxEl = document.getElementById('billing-tax');
    const totalEl = document.getElementById('billing-total');
    if (!subtotalEl) return;

    let subtotal = 0;
    billingCart.rooms.forEach(r => subtotal += r.ratePerNight * r.nights);
    billingCart.items.forEach(item => subtotal += item.quantity * item.unitPrice);

    const tax = subtotal * TAX_RATE;
    const discount = parseFloat(document.getElementById('billing-discount')?.value) || 0;
    const total = subtotal + tax - discount;

    subtotalEl.textContent = fmtCurrency(subtotal);
    taxEl.textContent = fmtCurrency(tax);
    totalEl.textContent = fmtCurrency(total);
}

window.updateRoomNights = function(index, value) {
    if (index >= 0 && index < billingCart.rooms.length) {
        billingCart.rooms[index].nights = parseInt(value) || 1;
        updateBillingTotals();
    }
};

window.removeRoomFromCart = function(index) {
    billingCart.rooms.splice(index, 1);
    renderBillingCart();
};

window.removeBillingItem = function(index) {
    billingCart.items.splice(index, 1);
    renderBillingCart();
};

// ── Billing search filters ──
document.getElementById('billing-room-search')?.addEventListener('input', function() {
    billingRoomSearchTerm = this.value.toLowerCase().trim();
    populateBillingDropdowns();
});
document.getElementById('billing-menu-search')?.addEventListener('input', function() {
    billingMenuSearchTerm = this.value.toLowerCase().trim();
    populateBillingDropdowns();
});
document.getElementById('billing-order-search')?.addEventListener('input', function() {
    billingOrderSearchTerm = this.value.toLowerCase().trim();
    populateBillingDropdowns();
});

// ── Add room from dropdown ──
document.getElementById('btn-add-billing-room')?.addEventListener('click', () => {
    const select = document.getElementById('billing-room-select');
    const nightsInput = document.getElementById('billing-room-nights');
    if (!select || !nightsInput) return;
    const roomId = parseInt(select.value);
    if (!roomId) { toast('Select a room', 'error'); return; }
    const room = billingRoomsData.find(r => r.id === roomId);
    if (!room) return;
    if (billingCart.rooms.find(r => r.id === roomId)) {
        toast('Room already in cart', 'error');
        return;
    }
    const nights = parseInt(nightsInput.value) || 1;
    billingCart.rooms.push({
        id: room.id,
        roomNumber: room.roomNumber,
        type: room.type,
        ratePerNight: room.ratePerNight,
        nights
    });
    select.value = '';
    nightsInput.value = '1';
    renderBillingCart();
    toast('Room added');
});

// ── Add menu item from dropdown ──
document.getElementById('btn-add-billing-menu')?.addEventListener('click', () => {
    const select = document.getElementById('billing-menu-select');
    const qtyInput = document.getElementById('billing-menu-qty');
    if (!select || !qtyInput) return;
    const menuId = parseInt(select.value);
    if (!menuId) { toast('Select a menu item', 'error'); return; }
    const menuItem = billingMenuItems.find(m => m.id === menuId);
    if (!menuItem) return;
    const quantity = parseInt(qtyInput.value) || 1;
    billingCart.items.push({
        description: menuItem.name.toUpperCase(),
        quantity,
        unitPrice: menuItem.price,
        source: 'menu'
    });
    select.value = '';
    qtyInput.value = '1';
    renderBillingCart();
    toast('Menu item added');
});

// ── Add custom item (manual entry) ──
document.getElementById('btn-add-billing-item')?.addEventListener('click', () => {
    const desc = document.getElementById('billing-item-desc');
    const qty = document.getElementById('billing-item-qty');
    const price = document.getElementById('billing-item-price');
    if (!desc || !qty || !price) return;
    if (!desc.value.trim()) { toast('Enter a description', 'error'); return; }
    const quantity = parseInt(qty.value) || 1;
    const unitPrice = parseFloat(price.value) || 0;
    if (unitPrice <= 0) { toast('Enter a valid price', 'error'); return; }
    billingCart.items.push({ description: desc.value.trim().toUpperCase(), quantity, unitPrice, source: 'manual' });
    desc.value = '';
    qty.value = '1';
    price.value = '';
    renderBillingCart();
    toast('Item added');
});

// ── Load existing order items ──
document.getElementById('btn-load-billing-order')?.addEventListener('click', async () => {
    const select = document.getElementById('billing-order-select');
    if (!select) return;
    const orderId = parseInt(select.value);
    if (!orderId) { toast('Select an order', 'error'); return; }

    try {
        const order = await api(`${API.orders}/${orderId}`);
        billingCart.orderId = orderId;
        billingCart.orderData = order;
        select.value = '';
        toast('Order loaded: ' + (order.orderReference || 'ORD #' + order.id));
        renderBillingCart();
    } catch (e) { /* toast shown */ }
});

// ── Remove linked order ──
document.getElementById('btn-remove-billing-order')?.addEventListener('click', () => {
    billingCart.orderId = null;
    billingCart.orderData = null;
    renderBillingCart();
    toast('Order removed from bill');
});

// Discount input
document.getElementById('billing-discount')?.addEventListener('input', updateBillingTotals);

// Generate invoice
document.getElementById('btn-generate-bill')?.addEventListener('click', generateInvoice);

async function generateInvoice() {
    const hasRooms = billingCart.rooms.length > 0;
    const hasItems = billingCart.items.length > 0;
    const hasOrder = !!billingCart.orderId;
    if (!hasRooms && !hasItems && !hasOrder) {
        toast('Add at least one room, item, or order to the invoice', 'error');
        return;
    }

    const body = {};
    if (billingCart.orderId) body.orderId = billingCart.orderId;
    if (billingCart.rooms.length) {
        body.roomItems = billingCart.rooms.map(r => ({ roomId: r.id, nights: r.nights }));
    }
    if (billingCart.items.length) {
        body.additionalItems = billingCart.items.map(item => ({
            description: item.description,
            quantity: item.quantity,
            unitPrice: item.unitPrice
        }));
    }
    body.discount = parseFloat(document.getElementById('billing-discount')?.value) || 0;
    body.serverName = document.getElementById('billing-server-name')?.value.trim() || null;

    try {
        const result = await api(API.bills, { method: 'POST', body: JSON.stringify(body) });
        toast('Invoice generated');
        billingCart.rooms = [];
        billingCart.items = [];
        billingCart.orderId = null;
        billingCart.orderData = null;
        billingCart.discount = 0;
        document.getElementById('billing-discount').value = '0';
        if (document.getElementById('billing-server-name')) document.getElementById('billing-server-name').value = '';
        renderBillingReceipt(result);
    } catch (e) { /* toast shown */ }
}

function renderBillingReceipt(bill) {
    const cartView = document.getElementById('billing-cart-view');
    const receiptView = document.getElementById('billing-receipt-view');
    const receiptEl = document.getElementById('billing-receipt');
    if (!cartView || !receiptView || !receiptEl) return;

    cartView.style.display = 'none';
    receiptView.style.display = 'block';

    const items = bill.lineItems || [];
    const itemsHtml = items.length ? items.map(item => `
        <tr><td>${item.quantity}</td><td>${item.description}</td><td>${fmtCurrency(item.unitPrice)}</td><td>${fmtCurrency(item.price)}</td></tr>
    `).join('') : `
        <tr><td colspan="4" style="text-align:center;color:var(--text-muted)">No line items</td></tr>
    `;

    receiptEl.innerHTML = `
        <div class="receipt-header">
            <div>
                <div class="receipt-table-name">INVOICE</div>
                <div class="receipt-server">${bill.serverName ? 'Server: ' + bill.serverName : ''}</div>
            </div>
            <div>
                ${bill.orderReference ? `<div class="receipt-order-ref">${bill.orderReference}</div>` : ''}
                <div class="receipt-date">${fmtDate(bill.createdAt)}</div>
            </div>
        </div>
        <table class="receipt-items">
            <thead><tr><th>QTY</th><th>ITEM</th><th>PRICE</th><th>TOTAL</th></tr></thead>
            <tbody>${itemsHtml}</tbody>
        </table>
        <div class="receipt-totals">
            <div class="receipt-total-row"><span>SUBTOTAL</span><span>${fmtCurrency(bill.subtotal)}</span></div>
            <div class="receipt-total-row"><span>TAX (${(bill.taxRate * 100).toFixed(2)}%)</span><span>${fmtCurrency(bill.taxAmount)}</span></div>
            <div class="receipt-total-row"><span>DISCOUNT</span><span>${fmtCurrency(bill.discount)}</span></div>
            <div class="receipt-total-row total-due"><span>TOTAL DUE</span><span>${fmtCurrency(bill.totalDue)}</span></div>
        </div>
        ${bill.aiDescription ? `<div style="margin-top:16px;padding-top:16px;border-top:1px solid var(--border-light);font-size:var(--fs-xs);color:var(--text-muted);font-style:italic">${bill.aiDescription}</div>` : ''}
    `;
}

// New invoice button
document.getElementById('btn-new-bill')?.addEventListener('click', () => {
    billingCart.orderId = null;
    billingCart.orderData = null;
    const cartView = document.getElementById('billing-cart-view');
    const receiptView = document.getElementById('billing-receipt-view');
    if (cartView) cartView.style.display = 'block';
    if (receiptView) receiptView.style.display = 'none';
});

// GENERATE INVOICE button on rooms page
document.getElementById('btn-generate-invoice')?.addEventListener('click', () => {
    if (billingCart.rooms.length === 0 && billingCart.items.length === 0) {
        toast('Use the BILL button on a room card to add rooms to the invoice', 'info');
    }
    navigate('billing');
});

// ═══════════════════════════════════════════
//  INIT — navigation init is handled in the
//  DOMContentLoaded callback above
// ═══════════════════════════════════════════
