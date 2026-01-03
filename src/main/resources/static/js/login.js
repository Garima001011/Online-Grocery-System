// Amazon-style Login JavaScript
document.addEventListener('DOMContentLoaded', () => {
    // Check if user is already logged in
    const user = localStorage.getItem('user');
    if (user) {
        redirectToDashboard(JSON.parse(user));
    }

    // Setup password toggle
    setupPasswordToggle();

    // Setup form submission
    setupLoginForm();
});

function setupPasswordToggle() {
    const toggleButton = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');

    if (toggleButton && passwordInput) {
        toggleButton.addEventListener('click', () => {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);

            // Toggle eye icon
            const icon = toggleButton.querySelector('i');
            if (type === 'password') {
                icon.className = 'fas fa-eye';
            } else {
                icon.className = 'fas fa-eye-slash';
            }
        });
    }
}

function setupLoginForm() {
    const loginButton = document.getElementById('loginButton');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');

    if (loginButton) {
        loginButton.addEventListener('click', handleLogin);
    }

    // Allow Enter key to submit
    if (emailInput && passwordInput) {
        emailInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') handleLogin();
        });

        passwordInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') handleLogin();
        });
    }
}

async function handleLogin() {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const loginButton = document.getElementById('loginButton');

    // Clear previous errors
    clearErrors();

    // Validate inputs
    if (!email) {
        showError('emailError', 'Enter your email or mobile phone number');
        return;
    }

    if (!password) {
        showError('passwordError', 'Enter your password');
        return;
    }

    // Show loading state
    loginButton.classList.add('loading');
    loginButton.disabled = true;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            const user = await response.json();

            // Save user to localStorage
            localStorage.setItem('user', JSON.stringify(user));

            // Show success message
            showSuccess('Login successful! Redirecting...');

            // Redirect based on role
            setTimeout(() => {
                redirectToDashboard(user);
            }, 1000);

        } else {
            const error = await response.text();
            showError('passwordError', 'Incorrect email or password');
        }

    } catch (error) {
        console.error('Login error:', error);
        showError('passwordError', 'Network error. Please try again.');
    } finally {
        // Reset loading state
        loginButton.classList.remove('loading');
        loginButton.disabled = false;
    }
}

function redirectToDashboard(user) {
    switch (user.role) {
        case 'ADMIN':
            window.location.href = '/admin.html';
            break;
        case 'DELIVERY':
            window.location.href = '/delivery.html';
            break;
        default:
            window.location.href = '/shop.html';
    }
}

function showError(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.style.display = 'block';
    }
}

function showSuccess(message) {
    // Create success toast
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: var(--amazon-green);
        color: white;
        padding: 15px 20px;
        border-radius: 4px;
        z-index: 1000;
        animation: slideIn 0.3s ease;
    `;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3000);
}

function clearErrors() {
    const errors = document.querySelectorAll('.error-message');
    errors.forEach(error => {
        error.style.display = 'none';
    });
}

// Add CSS animation for toast
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
`;
document.head.appendChild(style);