document.addEventListener('DOMContentLoaded', () => {
    // Theme switcher
    const themeToggle = document.getElementById('theme-toggle');
    const body = document.body;

    // Apply saved theme on page load
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme && savedTheme === 'dark-mode') {
        body.classList.add('dark-mode');
    }

    // Toggle theme on button click
    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            body.classList.toggle('dark-mode');
            if (body.classList.contains('dark-mode')) {
                localStorage.setItem('theme', 'dark-mode');
            } else {
                localStorage.setItem('theme', 'light-mode');
            }
        });
    }

    // Mobile navigation toggle
    const navToggle = document.querySelector('.nav-toggle');
    const navLinks = document.querySelector('.nav-links');

    if (navToggle) {
        navToggle.addEventListener('click', () => {
            navLinks.classList.toggle('active');
        });
    }

    // Scrolled header effect
    const header = document.getElementById('site-header');
    if (header) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 50) {
                header.classList.add('scrolled');
            } else {
                header.classList.remove('scrolled');
            }
        });
    }

    console.log('Document ready - Inked Mind Poetry Platform');

    // Close mobile nav when clicking outside
    document.addEventListener('click', function(event) {
        if (navLinks && navLinks.classList.contains('active')) {
            if (!event.target.closest('#main-nav')) {
                navLinks.classList.remove('active');
            }
        }
    });

    // Newsletter Form Submission
    const newsletterForm = document.querySelector('.newsletter-form');
    if (newsletterForm) {
        newsletterForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const emailInput = this.querySelector('input[type="email"]');
            if (emailInput.value) {
                alert('Thank you for subscribing to our newsletter!');
                emailInput.value = '';
            }
        });
    }

    
    // Check for user login status
    updateUserInterface();
});

// Update UI based on user login status
function updateUserInterface() {
    const isLoggedIn = localStorage.getItem('inkedMind_userLoggedIn');
    const userRole = localStorage.getItem('inkedMind_userRole');
    const authLinks = document.querySelector('.auth-links');
    
    if (isLoggedIn === 'true' && authLinks) {
        // Replace login/signup links with user menu
        authLinks.innerHTML = `
            <a href="./pages/dashboard.html" class="btn-login">Dashboard</a>
            <a href="#" id="logout-btn" class="btn-primary">Log Out</a>
        `;
        
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', function(e) {
                e.preventDefault();
                logoutUser();
            });
        }
    }
}

// Handle user logout
function logoutUser() {
    localStorage.removeItem('inkedMind_userLoggedIn');
    localStorage.removeItem('inkedMind_userRole');
    localStorage.removeItem('inkedMind_userName');
    localStorage.removeItem('inkedMind_userEmail');
    alert('You have been logged out successfully!');
    window.location.href = '/';
}