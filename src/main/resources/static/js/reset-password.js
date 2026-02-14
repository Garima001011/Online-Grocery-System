document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    if (!token) {
        alert('Invalid reset link. No token provided.');
        window.location.href = '/login.html';
        return;
    }
    sessionStorage.setItem('resetToken', token);

    // Password toggle
    setupPasswordToggle('togglePassword', 'newPassword');
    setupPasswordToggle('toggleConfirmPassword', 'confirmPassword');

    document.getElementById('resetPasswordBtn').addEventListener('click', handleResetPassword);
});

function setupPasswordToggle(toggleId, inputId) {
    const toggle = document.getElementById(toggleId);
    const input = document.getElementById(inputId);
    if (toggle && input) {
        toggle.addEventListener('click', () => {
            const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
            input.setAttribute('type', type);
            toggle.querySelector('i').className = type === 'password' ? 'fas fa-eye' : 'fas fa-eye-slash';
        });
    }
}

async function handleResetPassword() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const token = sessionStorage.getItem('resetToken');

    // Clear previous errors
    document.getElementById('passwordError').style.display = 'none';
    document.getElementById('confirmError').style.display = 'none';

    if (!newPassword || newPassword.length < 6) {
        showError('passwordError', 'Password must be at least 6 characters');
        return;
    }

    if (newPassword !== confirmPassword) {
        showError('confirmError', 'Passwords do not match');
        return;
    }

    try {
        const response = await fetch('/api/auth/reset-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ token, newPassword })
        });

        if (response.ok) {
            alert('Password updated successfully! Please login with your new password.');
            sessionStorage.removeItem('resetToken');
            window.location.href = '/login.html';
        } else {
            const error = await response.text();
            showError('passwordError', error || 'Failed to reset password');
        }
    } catch (error) {
        console.error('Error:', error);
        showError('passwordError', 'Network error. Please try again.');
    }
}

function showError(elementId, message) {
    const el = document.getElementById(elementId);
    el.textContent = message;
    el.style.display = 'block';
}