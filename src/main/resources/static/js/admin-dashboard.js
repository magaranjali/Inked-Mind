// Admin Dashboard JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Initialize admin dashboard
    initializeAdminDashboard();
    
    // Initialize tab switching
    initializeTabSwitching();
    
    // Initialize search and filter functionality
    initializeSearchAndFilter();
    
    // Initialize notification system
    initializeNotifications();
});

function initializeAdminDashboard() {
    // Load initial data
    loadDashboardStats();
    loadRecentActivity();
    
    // Set up periodic updates
    setInterval(loadDashboardStats, 30000); // Update every 30 seconds
}

function initializeTabSwitching() {
    const menuLinks = document.querySelectorAll('.admin-menu a');
    const tabs = document.querySelectorAll('.admin-tab');
    
    menuLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove active class from all menu items and tabs
            menuLinks.forEach(l => l.classList.remove('active'));
            tabs.forEach(tab => tab.classList.remove('active'));
            
            // Add active class to clicked menu item
            this.classList.add('active');
            
            // Show corresponding tab
            const tabId = this.getAttribute('data-tab') + '-tab';
            const targetTab = document.getElementById(tabId);
            if (targetTab) {
                targetTab.classList.add('active');
            }
        });
    });
}

function initializeSearchAndFilter() {
    // Poet search functionality
    const poetSearch = document.querySelector('#poets-tab .search-input');
    if (poetSearch) {
        poetSearch.addEventListener('input', function() {
            filterPoets(this.value);
        });
    }
    
    // Poet filter functionality
    const poetFilter = document.querySelector('#poets-tab .filter-select');
    if (poetFilter) {
        poetFilter.addEventListener('change', function() {
            filterPoetsByStatus(this.value);
        });
    }
    
    // Poem search functionality
    const poemSearch = document.querySelector('#poems-tab .search-input');
    if (poemSearch) {
        poemSearch.addEventListener('input', function() {
            filterPoems(this.value);
        });
    }
}

function initializeNotifications() {
    // Create notification container if it doesn't exist
    if (!document.querySelector('.notification-container')) {
        const container = document.createElement('div');
        container.className = 'notification-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 1000;
            display: flex;
            flex-direction: column;
            gap: 10px;
        `;
        document.body.appendChild(container);
    }
}

// Dashboard Stats Functions
function loadDashboardStats() {
    // Simulate loading dashboard statistics
    // In a real application, this would fetch data from an API
    const stats = {
        pendingApprovals: Math.floor(Math.random() * 20) + 5,
        totalPoets: 247 + Math.floor(Math.random() * 10),
        totalPoems: 1834 + Math.floor(Math.random() * 50)
    };
    
    updateStatsDisplay(stats);
}

function updateStatsDisplay(stats) {
    const statElements = document.querySelectorAll('.admin-stats .stat-number');
    if (statElements.length >= 3) {
        statElements[0].textContent = stats.pendingApprovals;
        statElements[1].textContent = stats.totalPoets;
        statElements[2].textContent = stats.totalPoems.toLocaleString();
    }
}

function loadRecentActivity() {
    // Simulate recent activity updates
    // In a real application, this would fetch from an API
    console.log('Loading recent activity...');
}

// Poet Management Functions
function viewPoet(poetId) {
    showNotification(`Viewing details for poet: ${poetId}`, 'info');
    // In a real application, this would open a modal or navigate to poet details
    console.log(`Viewing poet: ${poetId}`);
}

function approvePoet(poetId) {
    if (confirm(`Are you sure you want to approve poet: ${poetId}?`)) {
        // Simulate API call
        setTimeout(() => {
            showNotification(`Poet ${poetId} has been approved successfully!`, 'success');
            // Remove from pending list
            removePendingItem(poetId);
            // Update stats
            loadDashboardStats();
        }, 500);
    }
}

function rejectPoet(poetId) {
    const reason = prompt(`Please provide a reason for rejecting poet: ${poetId}`);
    if (reason) {
        // Simulate API call
        setTimeout(() => {
            showNotification(`Poet ${poetId} has been rejected.`, 'warning');
            // Remove from pending list
            removePendingItem(poetId);
            // Update stats
            loadDashboardStats();
        }, 500);
    }
}

function suspendPoet(poetId) {
    const reason = prompt(`Please provide a reason for suspending poet: ${poetId}`);
    if (reason) {
        // Simulate API call
        setTimeout(() => {
            showNotification(`Poet ${poetId} has been suspended.`, 'warning');
            // Update poet status in table
            updatePoetStatus(poetId, 'suspended');
        }, 500);
    }
}

function activatePoet(poetId) {
    if (confirm(`Are you sure you want to activate poet: ${poetId}?`)) {
        // Simulate API call
        setTimeout(() => {
            showNotification(`Poet ${poetId} has been activated.`, 'success');
            // Update poet status in table
            updatePoetStatus(poetId, 'active');
        }, 500);
    }
}

function deletePoet(poetId) {
    if (confirm(`Are you sure you want to permanently delete poet: ${poetId}? This action cannot be undone.`)) {
        // Simulate API call
        setTimeout(() => {
            showNotification(`Poet ${poetId} has been deleted.`, 'error');
            // Remove poet from table
            removePoetFromTable(poetId);
            // Update stats
            loadDashboardStats();
        }, 500);
    }
}

function viewApplication(poetId) {
    showNotification(`Viewing application details for: ${poetId}`, 'info');
    // In a real application, this would open a modal with full application details
    console.log(`Viewing application for: ${poetId}`);
}

// Poem Management Functions
function viewPoem(poemId) {
    showNotification(`Viewing poem: ${poemId}`, 'info');
    // In a real application, this would open the poem in a modal or new page
    console.log(`Viewing poem: ${poemId}`);
}

function editPoem(poemId) {
    showNotification(`Opening editor for poem: ${poemId}`, 'info');
    // In a real application, this would open an editing interface
    console.log(`Editing poem: ${poemId}`);
}

function deletePoem(poemId) {
    if (confirm(`Are you sure you want to delete this poem? This action cannot be undone.`)) {
        // Simulate API call
        setTimeout(() => {
            showNotification(`Poem has been deleted.`, 'error');
            // Remove poem from grid
            removePoemFromGrid(poemId);
            // Update stats
            loadDashboardStats();
        }, 500);
    }
}

function publishPoem(poemId) {
    if (confirm(`Are you sure you want to publish this poem?`)) {
        // Simulate API call
        setTimeout(() => {
            showNotification(`Poem has been published.`, 'success');
            // Update poem status
            updatePoemStatus(poemId, 'published');
        }, 500);
    }
}

function resolveReports(poemId) {
    if (confirm(`Mark all reports for this poem as resolved?`)) {
        // Simulate API call
        setTimeout(() => {
            showNotification(`Reports have been resolved.`, 'success');
            // Update poem status
            updatePoemStatus(poemId, 'published');
        }, 500);
    }
}

// Filter Functions
function filterPoets(searchTerm) {
    const rows = document.querySelectorAll('#poets-tab tbody tr');
    rows.forEach(row => {
        const poetName = row.querySelector('.poet-info strong').textContent.toLowerCase();
        const email = row.cells[1].textContent.toLowerCase();
        
        if (poetName.includes(searchTerm.toLowerCase()) || email.includes(searchTerm.toLowerCase())) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

function filterPoetsByStatus(status) {
    const rows = document.querySelectorAll('#poets-tab tbody tr');
    rows.forEach(row => {
        const statusBadge = row.querySelector('.status-badge');
        const poetStatus = statusBadge.textContent.toLowerCase();
        
        if (status === 'all' || poetStatus === status) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

function filterPoems(searchTerm) {
    const cards = document.querySelectorAll('.poem-card-admin');
    cards.forEach(card => {
        const title = card.querySelector('h4').textContent.toLowerCase();
        const author = card.querySelector('.poem-author').textContent.toLowerCase();
        
        if (title.includes(searchTerm.toLowerCase()) || author.includes(searchTerm.toLowerCase())) {
            card.style.display = '';
        } else {
            card.style.display = 'none';
        }
    });
}

// Utility Functions
function showNotification(message, type = 'info') {
    const container = document.querySelector('.notification-container');
    const notification = document.createElement('div');
    
    const colors = {
        success: '#28a745',
        error: '#dc3545',
        warning: '#ffc107',
        info: '#17a2b8'
    };
    
    notification.style.cssText = `
        background: ${colors[type]};
        color: white;
        padding: 12px 20px;
        border-radius: 6px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        margin-bottom: 10px;
        animation: slideIn 0.3s ease;
        max-width: 300px;
        word-wrap: break-word;
    `;
    
    notification.textContent = message;
    container.appendChild(notification);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 5000);
}

function removePendingItem(poetId) {
    const pendingItems = document.querySelectorAll('.pending-item');
    pendingItems.forEach(item => {
        const name = item.querySelector('h4').textContent.toLowerCase();
        if (name.includes(poetId.toLowerCase())) {
            item.style.animation = 'fadeOut 0.3s ease';
            setTimeout(() => {
                if (item.parentNode) {
                    item.parentNode.removeChild(item);
                }
            }, 300);
        }
    });
}

function updatePoetStatus(poetId, newStatus) {
    const rows = document.querySelectorAll('#poets-tab tbody tr');
    rows.forEach(row => {
        const poetName = row.querySelector('.poet-info strong').textContent.toLowerCase();
        if (poetName.includes(poetId.toLowerCase())) {
            const statusBadge = row.querySelector('.status-badge');
            statusBadge.textContent = newStatus.charAt(0).toUpperCase() + newStatus.slice(1);
            statusBadge.className = `status-badge ${newStatus}`;
            
            // Update action buttons based on new status
            const actionButtons = row.querySelector('.action-buttons');
            updateActionButtons(actionButtons, newStatus);
        }
    });
}

function updateActionButtons(container, status) {
    // Clear existing buttons
    container.innerHTML = '';
    
    // View button (always present)
    const viewBtn = document.createElement('button');
    viewBtn.className = 'btn-sm btn-info';
    viewBtn.innerHTML = '<i class="fas fa-eye"></i>';
    viewBtn.onclick = () => viewPoet('user');
    container.appendChild(viewBtn);
    
    // Status-specific buttons
    if (status === 'active') {
        const suspendBtn = document.createElement('button');
        suspendBtn.className = 'btn-sm btn-warning';
        suspendBtn.innerHTML = '<i class="fas fa-pause"></i>';
        suspendBtn.onclick = () => suspendPoet('user');
        container.appendChild(suspendBtn);
    } else if (status === 'suspended') {
        const activateBtn = document.createElement('button');
        activateBtn.className = 'btn-sm btn-success';
        activateBtn.innerHTML = '<i class="fas fa-play"></i>';
        activateBtn.onclick = () => activatePoet('user');
        container.appendChild(activateBtn);
    }
    
    // Delete button (always present)
    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'btn-sm btn-danger';
    deleteBtn.innerHTML = '<i class="fas fa-trash"></i>';
    deleteBtn.onclick = () => deletePoet('user');
    container.appendChild(deleteBtn);
}

function removePoetFromTable(poetId) {
    const rows = document.querySelectorAll('#poets-tab tbody tr');
    rows.forEach(row => {
        const poetName = row.querySelector('.poet-info strong').textContent.toLowerCase();
        if (poetName.includes(poetId.toLowerCase())) {
            row.style.animation = 'fadeOut 0.3s ease';
            setTimeout(() => {
                if (row.parentNode) {
                    row.parentNode.removeChild(row);
                }
            }, 300);
        }
    });
}

function removePoemFromGrid(poemId) {
    const cards = document.querySelectorAll('.poem-card-admin');
    cards.forEach(card => {
        // In a real application, you'd match by actual poem ID
        card.style.animation = 'fadeOut 0.3s ease';
        setTimeout(() => {
            if (card.parentNode) {
                card.parentNode.removeChild(card);
            }
        }, 300);
        return; // Remove only the first match for demo
    });
}

function updatePoemStatus(poemId, newStatus) {
    const cards = document.querySelectorAll('.poem-card-admin');
    cards.forEach(card => {
        // In a real application, you'd match by actual poem ID
        const statusBadge = card.querySelector('.poem-status');
        statusBadge.textContent = newStatus.charAt(0).toUpperCase() + newStatus.slice(1);
        statusBadge.className = `poem-status ${newStatus}`;
        return; // Update only the first match for demo
    });
}

// Add CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    @keyframes fadeOut {
        from {
            opacity: 1;
            transform: scale(1);
        }
        to {
            opacity: 0;
            transform: scale(0.9);
        }
    }
`;
document.head.appendChild(style);
