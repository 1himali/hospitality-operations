/* ═══════════════════════════════════════════
   HOSPITALITY OPERATIONS — APP.JS
   Matches wireframe navigation structure:
   HOME → LODGING → ROOMS SEARCH / ACTION ITEMS
        → RESTAURANT → MENU MGMT / ORDER MGMT / TABLE MGMT / BILLING
   ═══════════════════════════════════════════ */

const API = { rooms: '/api/v1/rooms', menu: '/api/v1/menu', auth: '/api/v1/auth' };

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
    $('#topbar').style.display = 'none';
    $('.app-body').style.display = 'none';
    $('#login-page').style.display = 'flex';
}

function showAppPage() {
    $('#topbar').style.display = 'flex';
    $('.app-body').style.display = 'flex';
    $('#login-page').style.display = 'none';
}

// Check authentication on page load
(function() {
    if (authToken) {
        showAppPage();
    } else {
        showLoginPage();
    }
})();

// Login form
$('#login-form').addEventListener('submit', async e => {
    e.preventDefault();
    const username = $('#login-username').value.trim();
    const password = $('#login-password').value;
    
    $('#login-error').style.display = 'none';
    
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
        $('#login-form').reset();
        showAppPage();
        showPage('home');
    } catch (err) {
        $('#login-error').textContent = err.message;
        $('#login-error').style.display = 'block';
    }
});

// Logout button
$('#btn-logout').addEventListener('click', () => {
    authToken = null;
    isAuthenticated = false;
    localStorage.removeItem('authToken');
    $('#login-form').reset();
    showLoginPage();
    toast('Logged out');
});

// ═══════════════════════════════════════════
//  NAVIGATION
// ═══════════════════════════════════════════
const navHistory = [];
let currentPage = 'home';

// Pages that show the sidebar
const sidebarPages = new Set(['rooms-search', 'restaurant', 'menu-mgmt', 'order-mgmt']);

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
//  THEME TOGGLE
// ═══════════════════════════════════════════
$('#btn-theme').addEventListener('click', () => {
    const html = document.documentElement;
    const next = html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
    html.setAttribute('data-theme', next);
    localStorage.setItem('theme', next);
});
// Restore saved theme
(function() {
    const saved = localStorage.getItem('theme');
    if (saved) document.documentElement.setAttribute('data-theme', saved);
})();

// ═══════════════════════════════════════════
//  ROOM SEARCH MODULE
// ═══════════════════════════════════════════
let roomsData = [];

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
    if (!roomsData.length) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-state-icon">🏨</div><p>No rooms found matching filters.</p></div>`;
        return;
    }
    grid.innerHTML = roomsData.map(r => {
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
            <div class="room-card-actions" style="margin-top:12px;display:flex;gap:6px;justify-content:flex-end">
                <button class="btn btn-outline btn-sm" onclick="editRoom(${r.id})">EDIT</button>
                <button class="btn btn-outline btn-sm" style="color:var(--danger);border-color:var(--danger)" onclick="confirmDeleteRoom(${r.id},'${r.roomNumber}')">DELETE</button>
            </div>
        </div>`;
    }).join('');
}

// Filter checkboxes
$$('#room-filter-checks input').forEach(cb => cb.addEventListener('change', loadRooms));

// Clear filters
$('#btn-clear-room-filters').addEventListener('click', () => {
    $$('#room-filter-checks input').forEach(cb => cb.checked = false);
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

    if (!menuData.length && menuCategoryFilter) {
        html = `<div class="empty-state"><div class="empty-state-icon">🍽️</div><p>No items in this category.</p></div>`;
    } else if (!menuData.length) {
        html = `<div class="empty-state"><div class="empty-state-icon">🍽️</div><p>No menu items yet. Add your first item!</p></div>`;
    } else {
        html = menuData.map(item => `
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

// Category filter
$$('#menu-category-filters .filter-list-item').forEach(btn => {
    btn.addEventListener('click', () => {
        $$('#menu-category-filters .filter-list-item').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        menuCategoryFilter = btn.dataset.value || '';
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
        });
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
//  INIT — show home page (if authenticated)
// ═══════════════════════════════════════════
if (isAuthenticated) {
    showPage('home');
}
