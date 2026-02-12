// Amazon-style Registration JavaScript
let selectedRole = 'CUSTOMER';

document.addEventListener('DOMContentLoaded', () => {
    // Setup password toggles
    setupPasswordToggles();

    // Setup form submission
    setupRegistrationForm();
});

function setupPasswordToggles() {
    // Setup for password field
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');

    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', () => {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);

            const icon = togglePassword.querySelector('i');
            icon.className = type === 'password' ? 'fas fa-eye' : 'fas fa-eye-slash';
        });
    }

    // Setup for confirm password field
    const toggleConfirmPassword = document.getElementById('toggleConfirmPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    if (toggleConfirmPassword && confirmPasswordInput) {
        toggleConfirmPassword.addEventListener('click', () => {
            const type = confirmPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            confirmPasswordInput.setAttribute('type', type);

            const icon = toggleConfirmPassword.querySelector('i');
            icon.className = type === 'password' ? 'fas fa-eye' : 'fas fa-eye-slash';
        });
    }
}

function setupRegistrationForm() {
    const registerButton = document.getElementById('registerButton');

    if (registerButton) {
        registerButton.addEventListener('click', handleRegistration);
    }

    // Allow Enter key in any field to submit
    const inputs = document.querySelectorAll('.form-input');
    inputs.forEach(input => {
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') handleRegistration();
        });
    });
}

function selectRole(role) {
    selectedRole = role;
    document.getElementById('role').value = role;

    // Update UI
    document.querySelectorAll('.role-option').forEach(option => {
        if (option.dataset.role === role) {
            option.classList.add('selected');
        } else {
            option.classList.remove('selected');
        }
    });
}

async function handleRegistration() {
    const name = document.getElementById('name').value.trim();
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();   // NEW
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const registerButton = document.getElementById('registerButton');

    // Clear previous errors
    clearErrors();

    // Validate inputs
    let isValid = true;

    if (!name) {
        showError('nameError', 'Enter your name');
        isValid = false;
    }

    if (!email) {
        showError('emailError', 'Enter your email');
        isValid = false;
    } else if (!isValidEmail(email)) {
        showError('emailError', 'Enter a valid email address');
        isValid = false;
    }

    // NEW: Phone validation
    if (!phone) {
        showError('phoneError', 'Enter your mobile number');
        isValid = false;
    } else if (!/^\d{10,15}$/.test(phone)) {
        showError('phoneError', 'Enter a valid phone number (10-15 digits)');
        isValid = false;
    }

    if (!password) {
        showError('passwordError', 'Enter a password');
        isValid = false;
    } else if (password.length < 6) {
        showError('passwordError', 'Password must be at least 6 characters');
        isValid = false;
    }

    if (password !== confirmPassword) {
        showError('confirmPasswordError', 'Passwords do not match');
        isValid = false;
    }

    if (!isValid) return;

    // Show loading state
    registerButton.classList.add('loading');
    registerButton.disabled = true;

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name: name,
                email: email,
                phone: phone,           // NEW: send phone number
                password: password,
                role: selectedRole
            })
        });

        if (response.ok) {
            const user = await response.json();

            // Show success message
            showSuccess('Account created successfully! Redirecting to login...');

            // Redirect to login page after 2 seconds
            setTimeout(() => {
                window.location.href = '/login.html';
            }, 2000);

        } else if (response.status === 409) {
            showError('emailError', 'An account with this email already exists');
        } else {
            const error = await response.text();
            showError('emailError', 'Registration failed. Please try again.');
        }

    } catch (error) {
        console.error('Registration error:', error);
        showError('emailError', 'Network error. Please try again.');
    } finally {
        // Reset loading state
        registerButton.classList.remove('loading');
        registerButton.disabled = false;
    }
}

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
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