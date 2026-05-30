/* ═══════════════════════════════════════════
   HOSPITALITY OPERATIONS — APP.JS
   Matches wireframe navigation structure:
   HOME → LODGING → ROOMS SEARCH / ACTION ITEMS
        → RESTAURANT → MENU MGMT / ORDER MGMT / TABLE MGMT / BILLING
   ═══════════════════════════════════════════ */

const API = { rooms: '/api/v1/rooms', menu: '/api/v1/menu', auth: '/api/v1/auth', actionItems: '/api/v1/action-items', bills: '/api/v1/bills', orders: '/api/v1/orders',     tables: '/api/v1/tables',
    inventory: '/api/v1/inventory', assistant: '/api/v1/assistant/query', metrics: '/api/v1/metrics', mockMode: '/api/v1/admin/mock-mode', analytics: '/api/v1/admin/analytics',
    userMgmt: '/api/v1/admin/users', payroll: '/api/v1/admin/payroll/employees', payrollRecords: '/api/v1/admin/payroll/records'
};

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

// ── GLOBAL SELECTION SYSTEM ────────────────
const _sel = { items: new Map() };
document.addEventListener('DOMContentLoaded', _restoreSel);

function selToggle(type, id, displayName, info, price, event) {
    if (event && event.target.closest('button')) return;
    const key = type + ':' + id;
    if (_sel.items.has(key)) {
        _sel.items.delete(key);
        if (event && event.currentTarget) event.currentTarget.classList.remove('selected');
    } else {
        _sel.items.set(key, { type, id, displayName, info, price, qty: 1 });
        if (event && event.currentTarget) event.currentTarget.classList.add('selected');
    }
    _persistSel();
    _refreshFab();
}

function selClear() {
    _sel.items.clear();
    _persistSel();
    _refreshFab();
    document.querySelectorAll('.card-selectable.selected').forEach(c => c.classList.remove('selected'));
    closeBillingCart();
}

function selCount() { return _sel.items.size; }

function selGetAll() { return Array.from(_sel.items.values()); }

function selHas(type, id) { return _sel.items.has(type + ':' + id); }

function selUpdateQty(type, id, qty) {
    const key = type + ':' + id;
    if (_sel.items.has(key)) {
        _sel.items.get(key).qty = Math.max(1, parseInt(qty) || 1);
        _persistSel();
    }
}

function selRemove(type, id) {
    const key = type + ':' + id;
    _sel.items.delete(key);
    _persistSel();
    _refreshFab();
    document.querySelector('.card-selectable[data-sel-type="' + type + '"][data-sel-id="' + id + '"]')?.classList.remove('selected');
    renderBillingCartItems();
}

function _persistSel() {
    try {
        const obj = {};
        _sel.items.forEach((v, k) => { obj[k] = v; });
        sessionStorage.setItem('opsSel', JSON.stringify(obj));
    } catch (e) {}
}

function _restoreSel() {
    try {
        const raw = sessionStorage.getItem('opsSel');
        if (raw) {
            const obj = JSON.parse(raw);
            _sel.items = new Map();
            Object.entries(obj).forEach(([k, v]) => _sel.items.set(k, v));
        }
    } catch (e) {}
}

function _refreshFab() {
    const fab = document.getElementById('fab-billing');
    const cnt = document.getElementById('fab-count');
    if (!fab || !cnt) return;
    const c = selCount();
    fab.style.display = c > 0 ? 'flex' : 'none';
    cnt.textContent = c;
}

function _refreshCardClasses() {
    document.querySelectorAll('.card-selectable').forEach(el => {
        const type = el.dataset.selType;
        const id = parseInt(el.dataset.selId);
        el.classList.toggle('selected', selHas(type, id));
    });
}

// ── AUTH STATE ─────────────────────────────
let authToken = localStorage.getItem('authToken') || null;
let userRole = localStorage.getItem('userRole') || null;
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
        userRole = data.role || null;
        isAuthenticated = true;
        localStorage.setItem('authToken', authToken);
        if (userRole) localStorage.setItem('userRole', userRole);

        toast('Login successful');
        const form = document.getElementById('login-form');
        if (form) form.reset();
        showAppPage();
        showPage('home');
        updateSidebarVisibility();
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
    userRole = null;
    isAuthenticated = false;
    localStorage.removeItem('authToken');
    localStorage.removeItem('userRole');
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
const sidebarPages = new Set(['rooms-search', 'restaurant', 'menu-mgmt', 'order-mgmt', 'table-mgmt', 'action-items', 'invoice-history', 'inventory', 'calculator', 'assistant', 'api-metrics', 'mock-mode', 'analytics', 'user-mgmt', 'payroll']);

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
    'invoice-history': 'INVOICE HISTORY',
    'inventory': 'INVENTORY',
    'calculator': 'CALCULATOR',
    'assistant': 'ASSISTANT',
    'api-metrics': 'API METRICS',
    'mock-mode': 'MOCK MODE',
    'analytics': 'ANALYTICS',
    'user-mgmt': 'USER MANAGEMENT',
    'payroll': 'PAYROLL MANAGER',
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
    'invoice-history': 'home',
    'inventory': 'home',
    'calculator': 'home',
    'assistant': 'home',
    'api-metrics': 'home',
    'mock-mode': 'home',
    'analytics': 'home',
    'user-mgmt': 'home',
    'payroll': 'home',
};

// Map pages → active sidebar item
const sidebarActive = {
    'rooms-search': 'rooms-search',
    'restaurant': 'restaurant',
    'menu-mgmt': 'restaurant',
    'order-mgmt': 'restaurant',
    'table-mgmt': 'restaurant',
    'action-items': 'action-items',
    'invoice-history': 'invoice-history',
    'inventory': 'inventory',
    'calculator': 'calculator',
    'assistant': 'assistant',
    'api-metrics': 'api-metrics',
    'mock-mode': 'mock-mode',
    'analytics': 'analytics',
    'user-mgmt': 'user-mgmt',
    'payroll': 'payroll',
};

function isOwner() {
    return userRole === 'ROLE_OWNER';
}

function updateSidebarVisibility() {
    const userMgmtBtn = document.getElementById('sidebar-user-mgmt');
    const payrollBtn = document.getElementById('sidebar-payroll');
    if (userMgmtBtn) {
        userMgmtBtn.style.display = isAdminOrOwner() ? '' : 'none';
    }
    if (payrollBtn) {
        payrollBtn.style.display = isOwner() ? '' : 'none';
    }
}

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
        updateSidebarVisibility();
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
    if (page === 'table-mgmt') loadTables();
    if (page === 'billing') loadBillingPage();
    if (page === 'order-mgmt') loadOrders();
    if (page === 'invoice-history') loadInvoiceHistory();
    if (page === 'inventory') loadInventory();
    if (page === 'calculator') initCalculator();
    if (page === 'assistant') initAssistant();
    if (page === 'api-metrics') loadApiMetrics();
    if (page === 'mock-mode') loadMockModePage();
    if (page === 'analytics') loadAnalytics();
    if (page === 'user-mgmt') loadUserMgmt();
    if (page === 'payroll') loadPayroll();

    // Restore selection card classes after page render
    setTimeout(_refreshCardClasses, 50);
    _refreshFab();
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
        if (checked.length > 0 && checked.length < 4) {
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
        const showIssue = r.status === 'UNDER_MAINTENANCE';
        const selClass = selHas('room', r.id) ? ' selected' : '';
        return `
        <div class="room-card card-selectable${selClass}" data-sel-type="room" data-sel-id="${r.id}" onclick="selToggle('room',${r.id},'RM ${r.roomNumber}','${fmtType(r.type)}',${r.ratePerNight},event)">
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
                <button class="btn btn-outline btn-sm" onclick="event.stopPropagation();editRoom(${r.id})">EDIT</button>
                <button class="btn btn-outline btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="event.stopPropagation();confirmDeleteRoom(${r.id},'${r.roomNumber}')">DELETE</button>
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
        html = data.map(item => {
            const selClass = selHas('menu', item.id) ? ' selected' : '';
            const escapedName = item.name.replace(/'/g,"\\'");
            return `
        <div class="menu-card card-selectable${selClass}${item.available ? '' : ' unavailable'}" data-sel-type="menu" data-sel-id="${item.id}" onclick="selToggle('menu',${item.id},'${escapedName}','${item.category}',${item.price},event)">
            <div class="menu-card-header">
                <span class="menu-card-name">${item.name}</span>
                <span class="menu-card-price">${fmt(item.price)}</span>
            </div>
            <div class="menu-card-body">
                <span class="badge badge-outlined">${item.category}</span>
            </div>
            <div class="menu-card-footer">
                <button class="btn btn-outline btn-sm" onclick="event.stopPropagation();editMenuItem(${item.id})">EDIT</button>
                <button class="btn btn-outline btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="event.stopPropagation();confirmDeleteMenu(${item.id},'${escapedName}')">DELETE</button>
            </div>
        </div>`;
        }).join('');
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
        } else if (type === 'inventory') {
            await api(`${API.inventory}/${id}`, { method: 'DELETE' });
            toast('Inventory item deleted');
            loadInventory();
        } else if (type === 'user') {
            await api(`${API.userMgmt}/${id}`, { method: 'DELETE' });
            toast('User deleted');
            loadUserMgmt();
        } else if (type === 'payroll') {
            await api(`${API.payroll}/${id}`, { method: 'DELETE' });
            toast('Employee deleted');
            closePayrollRecords();
            loadPayroll();
        } else if (type === 'payrollRecord') {
            await api(`${API.payrollRecords}/${id}`, { method: 'DELETE' });
            toast('Payroll record deleted');
            closePayrollRecords();
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
        loadTables();
    });
});

let tablesData = [];

async function loadTables() {
    const activeBtn = document.querySelector('#table-tab-bar .tab-btn.active');
    const statusFilter = activeBtn ? activeBtn.dataset.value : '';
    const searchTerm = document.getElementById('table-search')?.value?.trim() || '';
    try {
        const params = new URLSearchParams();
        if (statusFilter) params.set('status', statusFilter.toUpperCase());
        if (searchTerm) params.set('search', searchTerm);
        const qs = params.toString();
        const url = qs ? `${API.tables}?${qs}` : API.tables;
        tablesData = await api(url);
        renderTableGrid();
    } catch (e) { /* toast shown */ }
}

function renderTableGrid() {
    const grid = document.getElementById('table-grid');
    if (!grid) return;
    if (!tablesData.length) {
        grid.innerHTML = '<div class="empty-state"><div class="empty-state-icon">🪑</div><p>No tables found.</p></div>';
        return;
    }
    grid.innerHTML = tablesData.map(t => {
        const statusClass = t.status === 'AVAILABLE' ? 'table-card-available'
                          : t.status === 'OCCUPIED' ? 'table-card-occupied'
                          : 'table-card-reserved';
        const selClass = selHas('table', t.id) ? ' selected' : '';
        return `
        <div class="table-card ${statusClass} card-selectable${selClass}" data-sel-type="table" data-sel-id="${t.id}" onclick="selToggle('table',${t.id},'TABLE ${t.tableNumber}','Cap: ${t.capacity}',0,event)">
            <div class="table-card-header">
                <span class="table-card-id">${t.tableNumber}</span>
                <span class="badge ${t.status === 'AVAILABLE' ? 'badge-dashed' : t.status === 'OCCUPIED' ? 'badge-filled' : 'badge-outlined'}">${t.status}</span>
            </div>
            <div class="table-card-body">
                <div class="table-card-detail">Capacity: ${t.capacity}</div>
                ${t.location ? `<div class="table-card-detail">${t.location}</div>` : ''}
                ${t.reservationName ? `<div class="table-card-detail">Reserved: ${t.reservationName}</div>` : ''}
            </div>
        </div>`;
    }).join('');
}

// Table search
document.getElementById('table-search')?.addEventListener('input', function() {
    loadTables();
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
//  INVOICE HISTORY MODULE
// ═══════════════════════════════════════════
let invData = { content: [], totalPages: 0, totalElements: 0, number: 0 };
let invSearchTerm = '';
let invDateFilter = 'ALL';

function getInvDateRange(filter) {
    const now = new Date();
    const start = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    let from, to;
    switch (filter) {
        case 'TODAY':
            from = start;
            to = new Date(start.getTime() + 86400000);
            break;
        case 'WEEK': {
            const dayOfWeek = start.getDay();
            const mondayOffset = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
            from = new Date(start.getTime() + mondayOffset * 86400000);
            to = new Date(from.getTime() + 7 * 86400000);
            break;
        }
        case 'MONTH':
            from = new Date(now.getFullYear(), now.getMonth(), 1);
            to = new Date(now.getFullYear(), now.getMonth() + 1, 1);
            break;
        case 'CUSTOM': {
            const fromVal = document.getElementById('inv-date-from')?.value;
            const toVal = document.getElementById('inv-date-to')?.value;
            if (fromVal) from = new Date(fromVal + 'T00:00:00');
            if (toVal) to = new Date(toVal + 'T23:59:59');
            break;
        }
        default: return {};
    }
    return { dateFrom: from ? from.toISOString() : null, dateTo: to ? to.toISOString() : null };
}

async function loadInvoiceHistory(page = 0) {
    const params = new URLSearchParams();
    params.set('page', page);
    params.set('size', '15');
    const { dateFrom, dateTo } = getInvDateRange(invDateFilter);
    if (dateFrom) params.set('dateFrom', dateFrom);
    if (dateTo) params.set('dateTo', dateTo);
    try {
        invData = await api(`${API.bills}?${params.toString()}`);
        renderInvoiceHistory();
    } catch (e) { /* toast shown */ }
}

function renderInvoiceHistory() {
    const tbody = document.getElementById('inv-tbody');
    const pagination = document.getElementById('inv-pagination');
    if (!tbody || !pagination) return;

    let items = invData.content || [];
    if (invSearchTerm) {
        const term = invSearchTerm.toLowerCase();
        items = items.filter(inv =>
            (inv.orderReference && inv.orderReference.toLowerCase().includes(term)) ||
            (inv.serverName && inv.serverName.toLowerCase().includes(term)) ||
            (inv.aiDescription && inv.aiDescription.toLowerCase().includes(term)) ||
            (inv.id && inv.id.toString().includes(term))
        );
    }

    if (!items.length) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;padding:40px;color:var(--text-muted)">No invoices found.</td></tr>';
        pagination.innerHTML = '';
        return;
    }

    tbody.innerHTML = items.map(inv => {
        const ref = inv.invoiceNumber || inv.orderReference || 'INV-' + String(inv.id).padStart(4, '0');
        const itemsSummary = inv.aiDescription
            ? inv.aiDescription.substring(0, 50) + (inv.aiDescription.length > 50 ? '…' : '')
            : '—';
        return `<tr>
            <td class="inv-cell-ref">${ref}</td>
            <td class="inv-cell-date">${fmtDate(inv.createdAt)}</td>
            <td class="inv-cell-server">${inv.serverName || '—'}</td>
            <td class="inv-cell-items" title="${(inv.aiDescription || '').replace(/"/g,'&quot;')}">${itemsSummary}</td>
            <td class="inv-cell-num">${fmt(inv.subtotal)}</td>
            <td class="inv-cell-num">${fmt(inv.taxAmount)}</td>
            <td class="inv-cell-num">${inv.discount > 0 ? '-' + fmt(inv.discount) : '—'}</td>
            <td class="inv-cell-num inv-cell-total">${fmt(inv.totalDue)}</td>
            <td class="inv-cell-action"><button class="btn btn-sm btn-outline" onclick="viewInvoiceDetail(${inv.id})">VIEW</button></td>
        </tr>`;
    }).join('');

    renderInvPagination(pagination);
}

function renderInvPagination(container) {
    const total = invData.totalPages || 0;
    const current = invData.number || 0;
    if (total <= 1) { container.innerHTML = ''; return; }
    let html = `<button class="btn btn-sm btn-outline" onclick="loadInvoiceHistory(${current - 1})" ${current === 0 ? 'disabled' : ''}>← PREV</button>`;
    const start = Math.max(0, current - 2);
    const end = Math.min(total, current + 3);
    for (let i = start; i < end; i++) {
        html += `<button class="btn btn-sm ${i === current ? 'btn-primary' : 'btn-outline'}" onclick="loadInvoiceHistory(${i})">${i + 1}</button>`;
    }
    html += `<button class="btn btn-sm btn-outline" onclick="loadInvoiceHistory(${current + 1})" ${current >= total - 1 ? 'disabled' : ''}>NEXT →</button>`;
    container.innerHTML = html;
}

window.viewInvoiceDetail = async function(id) {
    try {
        const inv = await api(`${API.bills}/${id}`);
        const body = document.getElementById('inv-detail-body');
        if (!body) return;
        const items = (inv.lineItems && inv.lineItems.length)
            ? inv.lineItems.map(item =>
                `<tr><td>${item.quantity}</td><td>${item.description}</td><td>${fmtCurrency(item.unitPrice)}</td><td>${fmtCurrency(item.price)}</td></tr>`
              ).join('')
            : '<tr><td colspan="4" style="text-align:center;color:var(--text-muted)">No line items</td></tr>';
        const ref = inv.orderReference || 'INV-' + String(inv.id).padStart(4, '0');
        body.innerHTML = `
            <div class="billing-receipt" style="max-width:500px;margin:0 auto">
                <div class="receipt-header">
                    <div>
                        <div class="receipt-table-name">INVOICE</div>
                        <div>${inv.serverName ? 'Server: ' + inv.serverName : ''}</div>
                    </div>
                    <div>
                        <div>${ref}</div>
                        <div class="receipt-date">${fmtDate(inv.createdAt)}</div>
                    </div>
                </div>
                <table class="receipt-items">
                    <thead><tr><th>QTY</th><th>ITEM</th><th>PRICE</th><th>TOTAL</th></tr></thead>
                    <tbody>${items}</tbody>
                </table>
                <div class="receipt-totals">
                    <div class="receipt-total-row"><span>SUBTOTAL</span><span>${fmtCurrency(inv.subtotal)}</span></div>
                    <div class="receipt-total-row"><span>TAX (${(inv.taxRate * 100).toFixed(2)}%)</span><span>${fmtCurrency(inv.taxAmount)}</span></div>
                    <div class="receipt-total-row"><span>DISCOUNT</span><span>${fmtCurrency(inv.discount)}</span></div>
                    <div class="receipt-total-row total-due"><span>TOTAL DUE</span><span>${fmtCurrency(inv.totalDue)}</span></div>
                </div>
                ${inv.aiDescription ? '<div style="margin-top:16px;padding-top:16px;border-top:1px solid var(--border-light);font-size:var(--fs-xs);color:var(--text-muted);font-style:italic">' + inv.aiDescription + '</div>' : ''}
            </div>`;
        openModal('inv-detail-modal');
    } catch (e) { /* toast shown */ }
};

// Invoice date filter change
document.getElementById('inv-date-filter')?.addEventListener('change', function() {
    const customRange = document.getElementById('inv-custom-date-range');
    if (customRange) customRange.style.display = this.value === 'CUSTOM' ? 'flex' : 'none';
    invDateFilter = this.value;
    loadInvoiceHistory(0);
});

document.getElementById('inv-date-from')?.addEventListener('change', () => loadInvoiceHistory(0));
document.getElementById('inv-date-to')?.addEventListener('change', () => loadInvoiceHistory(0));

document.getElementById('inv-search')?.addEventListener('input', function() {
    invSearchTerm = this.value.toLowerCase().trim();
    renderInvoiceHistory();
});

// ═══════════════════════════════════════════
//  INVENTORY MODULE
// ═══════════════════════════════════════════
let inventoryData = [];
let inventoryTypeFilter = '';
let inventoryStatusFilter = '';
let inventoryCategoryFilter = '';
let inventorySearchTerm = '';

async function loadInventory() {
    const params = new URLSearchParams();
    if (inventoryTypeFilter) params.set('type', inventoryTypeFilter);
    if (inventoryStatusFilter) params.set('status', inventoryStatusFilter);
    if (inventoryCategoryFilter) params.set('category', inventoryCategoryFilter);
    const qs = params.toString();
    const url = qs ? `${API.inventory}?${qs}` : API.inventory;
    try {
        inventoryData = await api(url);
        renderInventoryGrid();
    } catch (e) { /* toast shown */ }
}

function renderInventoryGrid() {
    const grid = document.getElementById('inventory-grid');
    if (!grid) return;
    const data = inventorySearchTerm
        ? inventoryData.filter(item =>
            (item.name && item.name.toLowerCase().includes(inventorySearchTerm)) ||
            (item.category && item.category.toLowerCase().includes(inventorySearchTerm)) ||
            (item.unit && item.unit.toLowerCase().includes(inventorySearchTerm)) ||
            (item.notes && item.notes.toLowerCase().includes(inventorySearchTerm))
          )
        : inventoryData;

    if (!data.length) {
        grid.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📦</div><p>No inventory items found.</p></div>';
        return;
    }

    function invBadgeClass(s) {
        if (s === 'IN_STOCK') return 'badge-filled';
        if (s === 'LOW_STOCK') return 'badge-outlined';
        if (s === 'OUT_OF_STOCK') return 'badge-unavailable';
        return 'badge-outlined';
    }

    function fmtStatus(s) {
        return s.replace(/_/g, ' ');
    }

    grid.innerHTML = data.map(item => `
        <div class="room-card">
            <div class="room-card-header">
                <span class="room-card-id">${item.name}</span>
                <span class="badge ${invBadgeClass(item.status)}">${fmtStatus(item.status)}</span>
            </div>
            <div class="room-card-body">
                <div class="room-card-row"><span class="room-card-label">TYPE:</span><span class="room-card-value">${item.type}</span></div>
                <div class="room-card-row"><span class="room-card-label">CATEGORY:</span><span class="room-card-value">${item.category}</span></div>
                <div class="room-card-row"><span class="room-card-label">QTY:</span><span class="room-card-value">${item.quantity} ${item.unit}</span></div>
                <div class="room-card-row"><span class="room-card-label">REORDER:</span><span class="room-card-value">${item.reorderLevel} ${item.unit}</span></div>
                ${item.notes ? `<div class="room-card-row"><span class="room-card-label">NOTES:</span><span class="room-card-value">${item.notes}</span></div>` : ''}
            </div>
            <div class="room-card-actions" style="margin-top:12px;display:flex;gap:6px;justify-content:flex-end;flex-wrap:wrap">
                ${item.status !== 'DISCONTINUED' ? `
                    <button class="btn btn-outline btn-sm" onclick="adjustInvQty(${item.id}, 1)" style="color:var(--success)">+1</button>
                    <button class="btn btn-outline btn-sm" onclick="adjustInvQty(${item.id}, -1)" style="color:var(--danger)">-1</button>
                ` : ''}
                <button class="btn btn-outline btn-sm" onclick="editInventoryItem(${item.id})">EDIT</button>
                <button class="btn btn-outline btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="confirmDeleteInventory(${item.id},'${item.name.replace(/'/g,"\\'")}')">DELETE</button>
            </div>
        </div>
    `).join('');
}

window.adjustInvQty = async function(id, delta) {
    try {
        const updated = await api(`${API.inventory}/${id}/quantity?adjustment=${delta}`, { method: 'PATCH' });
        const idx = inventoryData.findIndex(i => i.id === id);
        if (idx >= 0) inventoryData[idx] = updated;
        renderInventoryGrid();
        toast(delta > 0 ? 'Stock increased' : 'Stock decreased');
    } catch (e) { /* toast shown */ }
};

window.editInventoryItem = function(id) {
    const item = inventoryData.find(i => i.id === id);
    if (!item) return;
    document.getElementById('inv-form-title').textContent = 'EDIT INVENTORY ITEM';
    document.getElementById('inv-submit-btn').textContent = 'UPDATE';
    document.getElementById('inv-edit-id').value = id;
    document.getElementById('inv-name').value = item.name;
    document.getElementById('inv-type').value = item.type;
    document.getElementById('inv-category').value = item.category;
    document.getElementById('inv-qty').value = item.quantity;
    document.getElementById('inv-reorder').value = item.reorderLevel;
    document.getElementById('inv-unit').value = item.unit;
    document.getElementById('inv-notes').value = item.notes || '';
    openModal('inv-form-modal');
};

window.confirmDeleteInventory = function(id, name) {
    document.getElementById('delete-msg').textContent = 'Delete "' + name + '" from inventory?';
    window.pendingDelete = { type: 'inventory', id };
    openModal('delete-modal');
};

// Add button
document.getElementById('btn-add-inventory')?.addEventListener('click', () => {
    document.getElementById('inv-form-title').textContent = 'ADD INVENTORY ITEM';
    document.getElementById('inv-submit-btn').textContent = 'SAVE';
    document.getElementById('inv-form').reset();
    document.getElementById('inv-edit-id').value = '';
    document.getElementById('inv-qty').value = '0';
    document.getElementById('inv-reorder').value = '5';
    document.getElementById('inv-unit').value = 'pcs';
    openModal('inv-form-modal');
});

// Form submit
document.getElementById('inv-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const editId = document.getElementById('inv-edit-id').value;
    const body = {
        name: document.getElementById('inv-name').value.trim(),
        type: document.getElementById('inv-type').value,
        category: document.getElementById('inv-category').value,
        quantity: parseInt(document.getElementById('inv-qty').value) || 0,
        reorderLevel: parseInt(document.getElementById('inv-reorder').value) || 5,
        unit: document.getElementById('inv-unit').value.trim() || 'pcs',
        notes: document.getElementById('inv-notes').value.trim() || null
    };
    if (!body.name || !body.type || !body.category) { toast('Fill required fields', 'error'); return; }
    try {
        if (editId) {
            await api(`${API.inventory}/${editId}`, { method: 'PUT', body: JSON.stringify(body) });
            toast('Item updated');
        } else {
            await api(API.inventory, { method: 'POST', body: JSON.stringify(body) });
            toast('Item created');
        }
        closeModal('inv-form-modal');
        loadInventory();
    } catch (e) { /* toast shown */ }
});

// Type filter
$$('#inv-type-filters .filter-list-item').forEach(btn => {
    btn.addEventListener('click', () => {
        $$('#inv-type-filters .filter-list-item').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        inventoryTypeFilter = btn.dataset.value || '';
        loadInventory();
    });
});

// Status filter
$$('#inv-status-filters .filter-list-item').forEach(btn => {
    btn.addEventListener('click', () => {
        $$('#inv-status-filters .filter-list-item').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        inventoryStatusFilter = btn.dataset.value || '';
        loadInventory();
    });
});

// Category filter
$$('#inv-category-filters .filter-list-item').forEach(btn => {
    btn.addEventListener('click', () => {
        $$('#inv-category-filters .filter-list-item').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        inventoryCategoryFilter = btn.dataset.value || '';
        loadInventory();
    });
});

// Search
document.getElementById('inventory-search')?.addEventListener('input', function() {
    inventorySearchTerm = this.value.toLowerCase().trim();
    renderInventoryGrid();
});

// ═══════════════════════════════════════════
//  ASSISTANT CHAT MODULE
// ═══════════════════════════════════════════
let assistantInitialized = false;

function initAssistant() {
    if (assistantInitialized) return;
    assistantInitialized = true;

    const input = document.getElementById('chat-input');
    const sendBtn = document.getElementById('btn-chat-send');
    const messages = document.getElementById('chat-messages');

    function addMessage(text, role) {
        const div = document.createElement('div');
        div.className = 'chat-msg chat-msg-' + role;
        div.innerHTML = role === 'assistant'
            ? '<div class="chat-msg-avatar">A</div><div class="chat-msg-bubble"><p style="margin:0;white-space:pre-wrap">' + escapeHtml(text) + '</p></div>'
            : '<div class="chat-msg-bubble user"><p style="margin:0;white-space:pre-wrap">' + escapeHtml(text) + '</p></div><div class="chat-msg-avatar user">U</div>';
        messages.appendChild(div);
        messages.scrollTop = messages.scrollHeight;
    }

    function escapeHtml(s) {
        return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
    }

    async function sendQuery() {
        const q = input.value.trim();
        if (!q || q.length > 500) return;
        addMessage(q, 'user');
        input.value = '';
        sendBtn.disabled = true;
        sendBtn.textContent = '...';
        try {
            const res = await api(API.assistant, { method: 'POST', body: JSON.stringify({ query: q }) });
            addMessage(res.responseText, 'assistant');
        } catch (e) {
            addMessage('Sorry, something went wrong. Please try again.', 'assistant');
        } finally {
            sendBtn.disabled = false;
            sendBtn.textContent = 'SEND';
        }
    }

    sendBtn?.addEventListener('click', sendQuery);
    input?.addEventListener('keydown', function(e) { if (e.key === 'Enter') sendQuery(); });
}

// ═══════════════════════════════════════════
//  CALCULATOR MODULE
// ═══════════════════════════════════════════
let calcState = { display: '0', previous: 0, operator: null, waiting: false, done: false };

function initCalculator() {
    calcState = { display: '0', previous: 0, operator: null, waiting: false, done: false };
    const screen = document.getElementById('calc-screen');
    if (screen) screen.textContent = '0';
    const taxInput = document.getElementById('calc-tax-rate');
    const discInput = document.getElementById('calc-discount-rate');
    if (taxInput) taxInput.value = '8.875';
    if (discInput) discInput.value = '0';
    const resultDisplay = document.getElementById('calc-result-display');
    if (resultDisplay) resultDisplay.textContent = '\u20B90.00';
}

function calcRefresh() {
    const screen = document.getElementById('calc-screen');
    if (screen) screen.textContent = calcState.display;
}

function calcDigit(d) {
    if (calcState.done) {
        calcState.display = d === '.' ? '0.' : d;
        calcState.done = false;
        calcState.operator = null;
        calcState.previous = 0;
        calcRefresh();
        return;
    }
    if (calcState.waiting) {
        calcState.display = d === '.' ? '0.' : d;
        calcState.waiting = false;
        calcRefresh();
        return;
    }
    if (d === '.') {
        if (calcState.display.includes('.')) return;
        calcState.display += '.';
    } else {
        if (calcState.display === '0') {
            calcState.display = d;
        } else {
            calcState.display += d;
        }
    }
    calcRefresh();
}

function calcOperator(op) {
    const current = parseFloat(calcState.display);
    if (calcState.operator && !calcState.waiting) {
        calcState.display = calcCompute(calcState.previous, current, calcState.operator);
        calcState.previous = parseFloat(calcState.display);
    } else {
        calcState.previous = current;
    }
    calcState.operator = op;
    calcState.waiting = true;
    calcState.done = false;
    calcRefresh();
}

function calcEquals() {
    if (!calcState.operator) return;
    const current = parseFloat(calcState.display);
    const result = calcCompute(calcState.previous, current, calcState.operator);
    calcState.display = formatCalcResult(result);
    calcState.operator = null;
    calcState.waiting = false;
    calcState.done = true;
    calcRefresh();
}

function calcCompute(a, b, op) {
    switch (op) {
        case '+': return a + b;
        case '-': return a - b;
        case '*': return a * b;
        case '/': return b !== 0 ? a / b : 0;
        default: return b;
    }
}

function formatCalcResult(n) {
    if (!isFinite(n) || isNaN(n)) return 'Error';
    return String(parseFloat(n.toFixed(10)));
}

function calcClear() {
    calcState = { display: '0', previous: 0, operator: null, waiting: false, done: false };
    calcRefresh();
}

function calcNegate() {
    if (calcState.display === '0') return;
    calcState.display = calcState.display.startsWith('-')
        ? calcState.display.slice(1)
        : '-' + calcState.display;
    calcRefresh();
}

function calcPercent() {
    const val = parseFloat(calcState.display) / 100;
    calcState.display = formatCalcResult(val);
    calcRefresh();
}

function calcBackspace() {
    if (calcState.done || calcState.waiting) return;
    if (calcState.display.length <= 1 || (calcState.display.length === 2 && calcState.display.startsWith('-'))) {
        calcState.display = '0';
    } else {
        calcState.display = calcState.display.slice(0, -1);
    }
    calcRefresh();
}

function calcApplyTax() {
    const rate = parseFloat(document.getElementById('calc-tax-rate')?.value) || 0;
    const base = parseFloat(calcState.display);
    if (isNaN(base)) return;
    const result = base + (base * rate / 100);
    calcState.display = formatCalcResult(result);
    calcState.done = true;
    calcRefresh();
    const resultDisplay = document.getElementById('calc-result-display');
    if (resultDisplay) resultDisplay.textContent = '\u20B9' + Number(result).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function calcApplyDiscount() {
    const rate = parseFloat(document.getElementById('calc-discount-rate')?.value) || 0;
    const base = parseFloat(calcState.display);
    if (isNaN(base)) return;
    const result = base - (base * rate / 100);
    calcState.display = formatCalcResult(result);
    calcState.done = true;
    calcRefresh();
    const resultDisplay = document.getElementById('calc-result-display');
    if (resultDisplay) resultDisplay.textContent = '\u20B9' + Number(result).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// Keyboard support
document.addEventListener('keydown', function(e) {
    const page = document.getElementById('page-calculator');
    if (!page || !page.classList.contains('active')) return;
    if (e.key >= '0' && e.key <= '9') { calcDigit(e.key); return; }
    if (e.key === '.') { calcDigit('.'); return; }
    if (e.key === 'Enter' || e.key === '=') { e.preventDefault(); calcEquals(); return; }
    if (e.key === 'Escape') { calcClear(); return; }
    if (e.key === 'Backspace') { e.preventDefault(); calcBackspace(); return; }
    if (e.key === '+' || e.key === '-') { calcOperator(e.key); return; }
    if (e.key === '*') { calcOperator('*'); return; }
    if (e.key === '/') { e.preventDefault(); calcOperator('/'); return; }
    if (e.key === '%') { calcPercent(); return; }
});

// ═══════════════════════════════════════════
//  BILLING CART MODAL (FAB → Invoice)
// ═══════════════════════════════════════════

function openBillingCart() {
    renderBillingCartItems();
    const body = document.getElementById('billing-cart-body');
    if (body) body.scrollTop = 0;
    openModal('billing-cart-modal');
}

function closeBillingCart() {
    closeModal('billing-cart-modal');
}

function renderBillingCartItems() {
    const container = document.getElementById('cart-items-container');
    const totals = document.getElementById('cart-totals');
    const actions = document.getElementById('cart-actions');
    if (!container) return;

    const items = selGetAll();
    if (!items.length) {
        container.innerHTML = '<div class="empty-state" style="padding:20px 0"><p>No items selected.</p></div>';
        if (totals) totals.style.display = 'none';
        if (actions) actions.style.display = 'none';
        return;
    }

    if (totals) totals.style.display = 'block';
    if (actions) actions.style.display = 'flex';

    container.innerHTML = items.map((item, i) => {
        const price = parseFloat(item.price) || 0;
        const lineTotal = price * item.qty;
        const typeLabel = item.type === 'room' ? 'ROOM' : item.type === 'table' ? 'TABLE' : item.type === 'menu' ? 'MENU' : 'ITEM';
        return `
        <div class="cart-item-row">
            <span class="cart-item-type">${typeLabel}</span>
            <span class="cart-item-desc">${item.displayName || ''}${item.info ? ' — ' + item.info : ''}</span>
            <input type="number" class="cart-item-qty input" style="width:50px;margin:0;padding:4px 6px;text-align:center" value="${item.qty}" min="1"
                onchange="selUpdateQty('${item.type}',${item.id},this.value);renderBillingCartItems()">
            <span class="cart-item-price">${fmt(price * item.qty)}</span>
            <button class="cart-item-remove" onclick="selRemove('${item.type}',${item.id})" title="Remove">&times;</button>
        </div>`;
    }).join('');

    updateCartTotals();
}

function updateCartTotals() {
    const subtotalEl = document.getElementById('cart-subtotal');
    const taxEl = document.getElementById('cart-tax');
    const totalEl = document.getElementById('cart-total');
    if (!subtotalEl) return;

    let subtotal = 0;
    selGetAll().forEach(item => {
        subtotal += (parseFloat(item.price) || 0) * (item.qty || 1);
    });

    const tax = subtotal * 0.08875;
    const discount = parseFloat(document.getElementById('cart-discount-input')?.value) || 0;
    const total = subtotal + tax - discount;

    subtotalEl.textContent = fmt(subtotal);
    taxEl.textContent = fmt(tax);
    totalEl.textContent = fmt(total);
}

async function generateInvoiceFromCart() {
    const name = document.getElementById('cart-customer-name')?.value?.trim();
    const phone = document.getElementById('cart-phone')?.value?.trim();
    if (!name || !phone) {
        toast('Customer Name and Phone Number are required', 'error');
        return;
    }

    const items = selGetAll();
    if (!items.length) {
        toast('No items selected', 'error');
        return;
    }

    const roomIds = items.filter(i => i.type === 'room').map(i => i.id);
    const tableIds = items.filter(i => i.type === 'table').map(i => i.id);
    const menuItemIds = items.filter(i => i.type === 'menu').map(i => i.id);
    const email = document.getElementById('cart-email')?.value?.trim() || null;
    const notes = document.getElementById('cart-notes')?.value?.trim() || null;
    const discount = parseFloat(document.getElementById('cart-discount-input')?.value) || 0;

    const body = {
        customerName: name,
        phoneNumber: phone,
        email: email,
        notes: notes,
        discount: discount > 0 ? discount : 0,
        selectedRoomIds: roomIds.length ? roomIds : null,
        selectedTableIds: tableIds.length ? tableIds : null,
        selectedMenuItemIds: menuItemIds.length ? menuItemIds : null,
    };

    try {
        const result = await api(API.bills, { method: 'POST', body: JSON.stringify(body) });
        const invRef = result.invoiceNumber || result.orderReference || '#' + result.id;
        toast('Invoice ' + invRef + ' generated');
        selClear();
        closeBillingCart();
    } catch (e) { /* toast shown */ }
}

// ═══════════════════════════════════════════
//  API METRICS MODULE
// ═══════════════════════════════════════════

async function loadApiMetrics() {
    try {
        const [summary, endpoints] = await Promise.all([
            api(API.metrics + '/summary'),
            api(API.metrics + '/endpoints')
        ]);

        if (summary) {
            document.getElementById('metrics-total').textContent = (summary.totalRequests || 0).toLocaleString();
            document.getElementById('metrics-success').textContent = (summary.successRequests || 0).toLocaleString();
            document.getElementById('metrics-failures').textContent = (summary.failedRequests || 0).toLocaleString();
            const rate = summary.errorRate != null ? (summary.errorRate * 100).toFixed(2) + '%' : '0.00%';
            document.getElementById('metrics-error-rate').textContent = rate;
        }

        const tbody = document.getElementById('metrics-endpoints-tbody');
        if (tbody && endpoints) {
            if (!endpoints.length) {
                tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:40px;color:var(--text-muted)">No data yet. Start using the API to see metrics.</td></tr>';
            } else {
                tbody.innerHTML = endpoints.map(e => `
                    <tr>
                        <td style="font-weight:700">${e.endpoint}</td>
                        <td style="text-align:right">${e.count}</td>
                        <td style="text-align:right">${e.avgDurationMs}ms</td>
                        <td style="text-align:right">${e.failures}</td>
                    </tr>
                `).join('');
            }
        }
    } catch (e) { /* toast shown */ }
}

async function downloadMetricsExport(format) {
    try {
        const url = `${API.metrics}/export/${format}`;
        const response = await fetch(url, {
            headers: { 'Authorization': 'Bearer ' + authToken }
        });
        if (!response.ok) { toast('Export failed', 'error'); return; }
        const blob = await response.blob();
        const dlUrl = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = dlUrl;
        a.download = `api_metrics.${format}`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(dlUrl);
        toast('Metrics exported as ' + format.toUpperCase());
    } catch (e) { toast('Export failed', 'error'); }
}

document.getElementById('btn-export-metrics-csv')?.addEventListener('click', () => downloadMetricsExport('csv'));
document.getElementById('btn-export-metrics-json')?.addEventListener('click', () => downloadMetricsExport('json'));

// ═══════════════════════════════════════════
//  ANALYTICS MODULE
// ═══════════════════════════════════════════

async function loadAnalytics() {
    try {
        const data = await api(API.analytics);
        if (!data) return;

        // Revenue
        if (data.revenue) {
            document.getElementById('rev-total').textContent = fmt(data.revenue.totalRevenue);
            document.getElementById('rev-paid').textContent = fmt(data.revenue.paidRevenue);
            document.getElementById('rev-unpaid').textContent = fmt(data.revenue.unpaidRevenue);
            document.getElementById('rev-avg').textContent = fmt(data.revenue.averageOrderValue);
        }

        // Invoices
        if (data.invoices) {
            document.getElementById('inv-total').textContent = data.invoices.totalInvoices;
            document.getElementById('inv-paid').textContent = data.invoices.paidInvoices;
            document.getElementById('inv-unpaid').textContent = data.invoices.unpaidInvoices;
            document.getElementById('inv-cancelled').textContent = data.invoices.cancelledInvoices;
        }

        // Rooms
        if (data.rooms) {
            document.getElementById('room-total').textContent = data.rooms.totalRooms;
            document.getElementById('room-occupied').textContent = data.rooms.occupiedRooms;
            document.getElementById('room-vacant').textContent = data.rooms.vacantRooms;
            document.getElementById('room-rate').textContent = (data.rooms.occupancyRate * 100).toFixed(1) + '%';
        }

        // Tables
        if (data.tables) {
            document.getElementById('table-total').textContent = data.tables.totalTables;
            document.getElementById('table-available').textContent = data.tables.availableTables;
            document.getElementById('table-occupied').textContent = data.tables.occupiedTables;
            document.getElementById('table-util').textContent = (data.tables.utilizationRate * 100).toFixed(1) + '%';
        }

        // Billing
        if (data.billing) {
            document.getElementById('bill-total').textContent = data.billing.totalBills;
            document.getElementById('bill-tax').textContent = fmt(data.billing.totalTaxCollected);
            document.getElementById('bill-discount').textContent = fmt(data.billing.totalDiscountsGiven);
            document.getElementById('bill-avg').textContent = fmt(data.billing.averageBillAmount);
        }

        // Inventory
        if (data.inventory) {
            document.getElementById('inv-total-items').textContent = data.inventory.totalItems;
            document.getElementById('inv-instock').textContent = data.inventory.inStockItems;
            document.getElementById('inv-lowstock').textContent = data.inventory.lowStockItems;
            document.getElementById('inv-reorder').textContent = data.inventory.itemsBelowReorderLevel;
        }
    } catch (e) { /* toast shown */ }
}

// ═══════════════════════════════════════════
//  USER MANAGEMENT MODULE (Admin/Owner)
// ═══════════════════════════════════════════

let usersData = [];

async function loadUserMgmt() {
    if (!isAdminOrOwner()) {
        document.getElementById('user-mgmt-grid').innerHTML = '<div class="empty-state"><p>Access denied.</p></div>';
        return;
    }
    try {
        usersData = await api(API.userMgmt);
        renderUserMgmt();
    } catch (e) { /* toast shown */ }
}

function renderUserMgmt() {
    const grid = document.getElementById('user-mgmt-grid');
    if (!grid) return;
    if (!usersData.length) {
        grid.innerHTML = '<div class="empty-state"><p>No users found.</p></div>';
        return;
    }
    grid.innerHTML = usersData.map(u => `
        <div class="room-card">
            <div class="room-card-header">
                <span class="room-card-id">${u.username}</span>
                <span class="badge badge-outlined">${u.role}</span>
            </div>
            <div class="room-card-body">
                <div class="room-card-row"><span class="room-card-label">ID:</span><span class="room-card-value">${u.id}</span></div>
                <div class="room-card-row"><span class="room-card-label">SCHEMA:</span><span class="room-card-value">${u.tenantSchema}</span></div>
                <div class="room-card-row"><span class="room-card-label">CREATED:</span><span class="room-card-value">${fmtDate(u.createdAt)}</span></div>
            </div>
            <div class="room-card-actions" style="margin-top:12px;display:flex;gap:6px;justify-content:flex-end;flex-wrap:wrap">
                <button class="btn btn-outline btn-sm" onclick="openResetPwModal(${u.id},'${u.username.replace(/'/g,"\\'")}')">RESET PASSWORD</button>
                <button class="btn btn-outline btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="confirmDeleteUser(${u.id},'${u.username.replace(/'/g,"\\'")}')">DELETE</button>
            </div>
        </div>
    `).join('');
}

window.openResetPwModal = function(userId, username) {
    document.getElementById('reset-pw-user-id').value = userId;
    document.getElementById('reset-pw-value').value = '';
    document.getElementById('reset-pw-modal').querySelector('.modal-title').textContent = 'RESET PASSWORD — ' + username;
    openModal('reset-pw-modal');
};

window.confirmDeleteUser = function(userId, username) {
    pendingDelete = { type: 'user', id: userId };
    document.getElementById('delete-msg').textContent = 'Delete user "' + username + '"? This cannot be undone.';
    openModal('delete-modal');
};

// Add user button
document.getElementById('btn-add-user')?.addEventListener('click', function() {
    document.getElementById('user-modal-title').textContent = 'ADD USER';
    document.getElementById('user-submit-btn').textContent = 'CREATE';
    document.getElementById('user-form').reset();
    document.getElementById('user-edit-id').value = '';
    openModal('user-modal');
});

// User form submit
document.getElementById('user-form')?.addEventListener('submit', async function(e) {
    e.preventDefault();
    const body = {
        username: document.getElementById('user-username').value.trim(),
        password: document.getElementById('user-password').value,
        role: document.getElementById('user-role').value,
    };
    try {
        await api(API.userMgmt, { method: 'POST', body: JSON.stringify(body) });
        toast('User created');
        closeModal('user-modal');
        loadUserMgmt();
    } catch (e) { /* toast shown */ }
});

// Reset password form submit
document.getElementById('reset-pw-form')?.addEventListener('submit', async function(e) {
    e.preventDefault();
    const userId = document.getElementById('reset-pw-user-id').value;
    const password = document.getElementById('reset-pw-value').value;
    try {
        await api(`${API.userMgmt}/${userId}/reset-password`, {
            method: 'PUT',
            body: JSON.stringify({ password })
        });
        toast('Password reset successful');
        closeModal('reset-pw-modal');
    } catch (e) { /* toast shown */ }
});

// ═══════════════════════════════════════════
//  PAYROLL MODULE (Owner only)
// ═══════════════════════════════════════════

let employeesData = [];
let payrollStatusFilter = '';
let payrollDeptFilter = '';
let payrollSearchTerm = '';
let selectedEmployeeId = null;

async function loadPayroll() {
    if (!isOwner()) {
        document.getElementById('payroll-grid').innerHTML = '<div class="empty-state"><p>Access denied. Owner role required.</p></div>';
        return;
    }
    try {
        const params = new URLSearchParams();
        if (payrollStatusFilter) params.set('status', payrollStatusFilter);
        if (payrollDeptFilter) params.set('department', payrollDeptFilter);
        if (payrollSearchTerm) params.set('name', payrollSearchTerm);
        const qs = params.toString();
        const url = qs ? `${API.payroll}?${qs}` : API.payroll;
        employeesData = await api(url);
        renderPayroll();
    } catch (e) { /* toast shown */ }
}

function renderPayroll() {
    const grid = document.getElementById('payroll-grid');
    if (!grid) return;
    if (!employeesData.length) {
        grid.innerHTML = '<div class="empty-state"><p>No employees found. Add your first employee.</p></div>';
        return;
    }
    grid.innerHTML = employeesData.map(emp => `
        <div class="room-card ${selectedEmployeeId === emp.id ? 'selected' : ''}" style="cursor:pointer" onclick="selectPayrollEmployee(${emp.id})">
            <div class="room-card-header">
                <span class="room-card-id">${emp.name}</span>
                <span class="badge ${emp.status === 'ACTIVE' ? 'badge-filled' : 'badge-outlined'}">${emp.status}</span>
            </div>
            <div class="room-card-body">
                <div class="room-card-row"><span class="room-card-label">POSITION:</span><span class="room-card-value">${emp.position}</span></div>
                <div class="room-card-row"><span class="room-card-label">DEPT:</span><span class="room-card-value">${emp.department}</span></div>
                <div class="room-card-row"><span class="room-card-label">SALARY:</span><span class="room-card-value">${fmt(emp.salary)}</span></div>
                <div class="room-card-row"><span class="room-card-label">JOINED:</span><span class="room-card-value">${emp.hireDate ? fmtDate(emp.hireDate) : '—'}</span></div>
                ${emp.phone ? `<div class="room-card-row"><span class="room-card-label">PHONE:</span><span class="room-card-value">${emp.phone}</span></div>` : ''}
            </div>
            <div class="room-card-actions" style="margin-top:12px;display:flex;gap:6px;justify-content:flex-end;flex-wrap:wrap">
                <button class="btn btn-outline btn-sm" onclick="event.stopPropagation();viewPayrollRecords(${emp.id},'${emp.name.replace(/'/g,"\\'")}')">RECORDS</button>
                <button class="btn btn-outline btn-sm" onclick="event.stopPropagation();editEmployee(${emp.id})">EDIT</button>
                <button class="btn btn-outline btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="event.stopPropagation();confirmDeleteEmployee(${emp.id},'${emp.name.replace(/'/g,"\\'")}')">DELETE</button>
            </div>
        </div>
    `).join('');
}

function selectPayrollEmployee(id) {
    selectedEmployeeId = id;
    renderPayroll();
}

// ── Employee CRUD ──

window.editEmployee = async function(id) {
    const emp = employeesData.find(e => e.id === id);
    if (!emp) return;
    document.getElementById('employee-modal-title').textContent = 'EDIT EMPLOYEE';
    document.getElementById('employee-submit-btn').textContent = 'UPDATE';
    document.getElementById('employee-edit-id').value = id;
    document.getElementById('emp-name').value = emp.name;
    document.getElementById('emp-position').value = emp.position;
    document.getElementById('emp-department').value = emp.department;
    document.getElementById('emp-salary').value = emp.salary;
    document.getElementById('emp-phone').value = emp.phone || '';
    document.getElementById('emp-email').value = emp.email || '';
    document.getElementById('emp-hire-date').value = emp.hireDate ? emp.hireDate.substring(0, 10) : '';
    document.getElementById('emp-status').value = emp.status || 'ACTIVE';
    openModal('employee-modal');
};

window.confirmDeleteEmployee = function(id, name) {
    pendingDelete = { type: 'payroll', id };
    document.getElementById('delete-msg').textContent = 'Delete employee "' + name + '"? This cannot be undone.';
    openModal('delete-modal');
};

// Add employee button
document.getElementById('btn-add-employee')?.addEventListener('click', function() {
    document.getElementById('employee-modal-title').textContent = 'ADD EMPLOYEE';
    document.getElementById('employee-submit-btn').textContent = 'SAVE';
    document.getElementById('employee-form').reset();
    document.getElementById('employee-edit-id').value = '';
    document.getElementById('emp-salary').value = '30000';
    document.getElementById('emp-status').value = 'ACTIVE';
    document.getElementById('emp-hire-date').value = new Date().toISOString().substring(0, 10);
    openModal('employee-modal');
});

// Employee form submit
document.getElementById('employee-form')?.addEventListener('submit', async function(e) {
    e.preventDefault();
    const editId = document.getElementById('employee-edit-id').value;
    const hireDateVal = document.getElementById('emp-hire-date').value;
    const body = {
        name: document.getElementById('emp-name').value.trim(),
        position: document.getElementById('emp-position').value.trim(),
        department: document.getElementById('emp-department').value,
        salary: parseFloat(document.getElementById('emp-salary').value),
        status: document.getElementById('emp-status').value,
        phone: document.getElementById('emp-phone').value.trim() || null,
        email: document.getElementById('emp-email').value.trim() || null,
    };
    if (hireDateVal) {
        body.hireDate = new Date(hireDateVal + 'T00:00:00Z').toISOString();
    }
    try {
        if (editId) {
            await api(`${API.payroll}/${editId}`, { method: 'PUT', body: JSON.stringify(body) });
            toast('Employee updated');
        } else {
            await api(API.payroll, { method: 'POST', body: JSON.stringify(body) });
            toast('Employee created');
        }
        closeModal('employee-modal');
        loadPayroll();
    } catch (e) { /* toast shown */ }
});

// Export payroll CSV
document.getElementById('btn-export-payroll')?.addEventListener('click', async function() {
    try {
        const response = await fetch(API.payroll + '/export/csv', {
            headers: { 'Authorization': 'Bearer ' + authToken }
        });
        if (!response.ok) { toast('Export failed', 'error'); return; }
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'payroll_employees.csv';
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
        toast('Payroll exported');
    } catch (e) { toast('Export failed', 'error'); }
});

// ── Payroll Records ──

async function viewPayrollRecords(employeeId, employeeName) {
    selectedEmployeeId = employeeId;
    renderPayroll();
    const panel = document.getElementById('payroll-records-panel');
    const title = document.getElementById('payroll-records-title');
    const list = document.getElementById('payroll-records-list');
    if (!panel || !title || !list) return;
    title.textContent = 'PAYROLL RECORDS — ' + employeeName;
    list.innerHTML = '<div class="loading-state">Loading records...</div>';
    document.getElementById('record-employee-id').value = employeeId;
    panel.style.display = 'block';
    try {
        const records = await api(`${API.payrollRecords}/employee/${employeeId}`);
        if (!records || !records.length) {
            list.innerHTML = '<div class="empty-state" style="padding:20px"><p>No payroll records yet.</p></div>';
        } else {
            list.innerHTML = records.map(r => `
                <div class="payroll-record-row">
                    <div class="payroll-record-main">
                        <span class="payroll-record-date">${fmtDate(r.paymentDate)}</span>
                        <span class="badge ${r.status === 'PAID' ? 'badge-filled' : 'badge-outlined'}">${r.status}</span>
                    </div>
                    <div class="payroll-record-details">
                        <span>Base: ${fmt(r.baseSalary)}</span>
                        <span>Bonus: ${fmt(r.bonus)}</span>
                        <span>Deduct: ${fmt(r.deductions)}</span>
                        <span style="font-weight:800">Net: ${fmt(r.netPay)}</span>
                    </div>
                    ${r.notes ? `<div class="payroll-record-notes">${r.notes}</div>` : ''}
                    <div class="payroll-record-actions">
                        <button class="btn btn-outline btn-sm" onclick="editPayrollRecord(${r.id})">EDIT</button>
                        <button class="btn btn-outline btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="confirmDeletePayrollRecord(${r.id})">DELETE</button>
                    </div>
                </div>
            `).join('');
        }
    } catch (e) {
        list.innerHTML = '<div class="empty-state"><p>Failed to load records.</p></div>';
    }
}

function closePayrollRecords() {
    const panel = document.getElementById('payroll-records-panel');
    if (panel) panel.style.display = 'none';
    if (selectedEmployeeId) {
        loadPayroll();
    }
}

document.getElementById('btn-close-records')?.addEventListener('click', closePayrollRecords);

// Add record button
document.getElementById('btn-add-record')?.addEventListener('click', function() {
    const empId = document.getElementById('record-employee-id').value;
    if (!empId) { toast('Select an employee first', 'error'); return; }
    document.getElementById('record-modal-title').textContent = 'ADD PAYROLL RECORD';
    document.getElementById('record-submit-btn').textContent = 'SAVE';
    document.getElementById('record-form').reset();
    document.getElementById('record-edit-id').value = '';
    document.getElementById('record-employee-id').value = empId;
    document.getElementById('rec-bonus').value = '0';
    document.getElementById('rec-deductions').value = '0';
    document.getElementById('rec-payment-date').value = new Date().toISOString().substring(0, 10);
    document.getElementById('rec-status').value = 'PAID';
    openModal('record-modal');
});

window.editPayrollRecord = async function(id) {
    try {
        const rec = await api(`${API.payrollRecords}/${id}`);
        document.getElementById('record-modal-title').textContent = 'EDIT PAYROLL RECORD';
        document.getElementById('record-submit-btn').textContent = 'UPDATE';
        document.getElementById('record-edit-id').value = id;
        document.getElementById('record-employee-id').value = rec.employeeId;
        document.getElementById('rec-bonus').value = rec.bonus;
        document.getElementById('rec-deductions').value = rec.deductions;
        document.getElementById('rec-payment-date').value = rec.paymentDate ? rec.paymentDate.substring(0, 10) : '';
        document.getElementById('rec-status').value = rec.status;
        document.getElementById('rec-notes').value = rec.notes || '';
        openModal('record-modal');
    } catch (e) { /* toast shown */ }
};

window.confirmDeletePayrollRecord = function(id) {
    pendingDelete = { type: 'payrollRecord', id };
    document.getElementById('delete-msg').textContent = 'Delete this payroll record?';
    openModal('delete-modal');
};

// Record form submit
document.getElementById('record-form')?.addEventListener('submit', async function(e) {
    e.preventDefault();
    const editId = document.getElementById('record-edit-id').value;
    const empId = document.getElementById('record-employee-id').value;
    const paymentDate = document.getElementById('rec-payment-date').value;
    if (!paymentDate) { toast('Payment date is required', 'error'); return; }
    const body = {
        employeeId: parseInt(empId),
        bonus: parseFloat(document.getElementById('rec-bonus').value) || 0,
        deductions: parseFloat(document.getElementById('rec-deductions').value) || 0,
        paymentDate: new Date(paymentDate + 'T00:00:00Z').toISOString(),
        notes: document.getElementById('rec-notes').value.trim() || null,
        status: document.getElementById('rec-status').value,
    };
    try {
        if (editId) {
            await api(`${API.payrollRecords}/${editId}`, { method: 'PUT', body: JSON.stringify(body) });
            toast('Record updated');
        } else {
            await api(API.payrollRecords, { method: 'POST', body: JSON.stringify(body) });
            toast('Record created');
        }
        closeModal('record-modal');
        const emp = employeesData.find(e => e.id === parseInt(empId));
        if (emp) viewPayrollRecords(parseInt(empId), emp.name);
    } catch (e) { /* toast shown */ }
});

// ── Payroll Filters ──

document.getElementById('payroll-search')?.addEventListener('input', function() {
    payrollSearchTerm = this.value.toLowerCase().trim();
    loadPayroll();
});

document.querySelectorAll('#payroll-status-filters .filter-list-item').forEach(btn => {
    btn.addEventListener('click', function() {
        document.querySelectorAll('#payroll-status-filters .filter-list-item').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
        payrollStatusFilter = this.dataset.value || '';
        loadPayroll();
    });
});

document.querySelectorAll('#payroll-dept-filters .filter-list-item').forEach(btn => {
    btn.addEventListener('click', function() {
        document.querySelectorAll('#payroll-dept-filters .filter-list-item').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
        payrollDeptFilter = this.dataset.value || '';
        loadPayroll();
    });
});

// ═══════════════════════════════════════════
//  MOCK MODE MODULE
// ═══════════════════════════════════════════

function isAdminOrOwner() {
    return userRole === 'ROLE_ADMIN' || userRole === 'ROLE_OWNER';
}

async function loadMockModePage() {
    const controls = document.getElementById('mock-mode-controls');
    if (controls) {
        controls.style.display = isAdminOrOwner() ? '' : 'none';
    }
    await refreshMockModeStatus();
}

async function refreshMockModeStatus() {
    try {
        const status = await api(API.mockMode);
        updateMockModeUI(status && status.enabled);
    } catch (e) {
        // Show as disabled if endpoint is not reachable
        updateMockModeUI(false);
    }
}

function updateMockModeUI(enabled) {
    const indicator = document.getElementById('mock-mode-indicator');
    const badge = document.getElementById('mock-mode-badge');
    const enableBtn = document.getElementById('btn-mock-enable');
    const disableBtn = document.getElementById('btn-mock-disable');

    if (indicator) {
        indicator.textContent = enabled ? 'ON' : 'OFF';
        indicator.className = 'mock-mode-status-indicator ' + (enabled ? 'on' : 'off');
    }

    if (badge) {
        badge.style.display = enabled ? 'inline-block' : 'none';
    }

    if (enableBtn) enableBtn.style.display = enabled ? 'none' : '';
    if (disableBtn) disableBtn.style.display = enabled ? '' : 'none';
}

async function enableMockMode() {
    try {
        const r = await api(API.mockMode, {
            method: 'POST',
            body: JSON.stringify({ enabled: true })
        });
        updateMockModeUI(r && r.enabled);
        toast('Mock mode enabled');
    } catch (e) { /* toast shown */ }
}

async function disableMockMode() {
    try {
        const r = await api(API.mockMode, {
            method: 'POST',
            body: JSON.stringify({ enabled: false })
        });
        updateMockModeUI(r && r.enabled);
        toast('Mock mode disabled');
    } catch (e) { /* toast shown */ }
}

document.addEventListener('click', function (e) {
    if (e.target.id === 'btn-mock-enable') enableMockMode();
    if (e.target.id === 'btn-mock-disable') disableMockMode();
});

// Initialize mock mode badge on app startup
(function () {
    document.addEventListener('DOMContentLoaded', async function () {
        if (authToken) {
            try {
                const status = await api(API.mockMode);
                const badge = document.getElementById('mock-mode-badge');
                if (badge && status && status.enabled) {
                    badge.style.display = 'inline-block';
                }
            } catch (e) { /* badge stays hidden */ }
        }
    });
})();
