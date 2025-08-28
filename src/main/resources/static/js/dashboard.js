// Dashboard JavaScript functionality
document.addEventListener('DOMContentLoaded', function() {
    // Load user information
    updateDashboardUI();
    
    // Tab functionality
    const tabLinks = document.querySelectorAll('.sidebar-menu a');
    const tabs = document.querySelectorAll('.dashboard-tab');
    
    tabLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const tab = this.getAttribute('data-tab');
            
            // Update active state for links
            tabLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
            
            // Show the correct tab
            tabs.forEach(t => {
                t.classList.remove('active');
                if (t.id === `${tab}-tab`) {
                    t.classList.add('active');
                }
            });
        });
    });
    
    // Write poem button functionality
    const writePoemBtn = document.getElementById('write-poem-btn');
    const writePoemBtn2 = document.getElementById('write-poem-btn-2');
    
    if (writePoemBtn) {
        writePoemBtn.addEventListener('click', function(e) {
            e.preventDefault();
            openWritePoemTab();
        });
    }
    
    if (writePoemBtn2) {
        writePoemBtn2.addEventListener('click', function(e) {
            e.preventDefault();
            openWritePoemTab();
        });
    }
    
    // Write poem form submission
    const poemForm = document.querySelector('.write-poem-form');
    if (poemForm) {
        poemForm.addEventListener('submit', function(e) {
            e.preventDefault();
            alert('Poem published successfully!');
            
            // In a real app, we would submit this data to the server
            const title = document.getElementById('poem-title').value;
            const content = document.getElementById('poem-content').value;
            const category = document.getElementById('poem-category').value;
            
            // Reset form
            this.reset();
            
            // Return to My Poems tab
            openMyPoemsTab();
        });
    }
    
    // Profile form submission
    const profileForm = document.querySelector('.profile-form');
    if (profileForm) {
        profileForm.addEventListener('submit', function(e) {
            e.preventDefault();
            alert('Profile updated successfully!');
        });
    }
    
    // Password form submission
    const passwordForm = document.querySelector('.password-form');
    if (passwordForm) {
        passwordForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const newPassword = document.getElementById('new-password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            
            if (newPassword !== confirmPassword) {
                alert('Passwords do not match!');
                return;
            }
            
            alert('Password updated successfully!');
            this.reset();
        });
    }
    
    // Notification form submission
    const notificationForm = document.querySelector('.notification-form');
    if (notificationForm) {
        notificationForm.addEventListener('submit', function(e) {
            e.preventDefault();
            alert('Notification preferences saved!');
        });
    }
});

// Update dashboard UI with user info
function updateDashboardUI() {
    const userName = localStorage.getItem('inkedMind_userName') || 'User';
    const userRole = localStorage.getItem('inkedMind_userRole') || 'Poet';
    
    // Update user name and role in welcome message
    const userNameElements = document.querySelectorAll('.user-name');
    userNameElements.forEach(el => {
        el.textContent = userName;
    });
    
    // Update user role
    const userRoleElements = document.querySelectorAll('.user-role');
    userRoleElements.forEach(el => {
        el.textContent = userRole;
    });
    
    // Set first initial for avatar
    const firstInitial = userName.charAt(0).toUpperCase();
    const avatarElements = document.querySelectorAll('.user-avatar');
    avatarElements.forEach(el => {
        if (el.querySelector('i')) {
            el.innerHTML = firstInitial;
        }
    });
    
    // Update form fields if they exist
    const firstNameInput = document.getElementById('profile-first-name');
    const emailInput = document.getElementById('profile-email');
    
    if (firstNameInput && userName) {
        const nameParts = userName.split(' ');
        firstNameInput.value = nameParts[0] || '';
        
        const lastNameInput = document.getElementById('profile-last-name');
        if (lastNameInput && nameParts.length > 1) {
            lastNameInput.value = nameParts[1] || '';
        }
    }
    
    if (emailInput) {
        emailInput.value = localStorage.getItem('inkedMind_userEmail') || '';
    }
}

// Open Write Poem tab
function openWritePoemTab() {
    const tabLinks = document.querySelectorAll('.sidebar-menu a');
    const tabs = document.querySelectorAll('.dashboard-tab');
    
    tabLinks.forEach(l => l.classList.remove('active'));
    document.querySelector('.sidebar-menu a[data-tab="write-poem"]').classList.add('active');
    
    tabs.forEach(t => t.classList.remove('active'));
    document.getElementById('write-poem-tab').classList.add('active');
}

// Open My Poems tab
function openMyPoemsTab() {
    const tabLinks = document.querySelectorAll('.sidebar-menu a');
    const tabs = document.querySelectorAll('.dashboard-tab');
    
    tabLinks.forEach(l => l.classList.remove('active'));
    document.querySelector('.sidebar-menu a[data-tab="my-poems"]').classList.add('active');
    
    tabs.forEach(t => t.classList.remove('active'));
    document.getElementById('my-poems-tab').classList.add('active');
}