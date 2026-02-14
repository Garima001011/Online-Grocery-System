document.addEventListener('DOMContentLoaded', () => {
    const sendButton = document.getElementById('sendResetLink');
    const emailInput = document.getElementById('email');

    sendButton.addEventListener('click', handleForgotPassword);

    emailInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleForgotPassword();
    });
});

async function handleForgotPassword() {
    const email = document.getElementById('email').value.trim();
    const errorDiv = document.getElementById('emailError');

    // Clear previous error
    errorDiv.style.display = 'none';

    if (!email) {
        errorDiv.textContent = 'Please enter your email';
        errorDiv.style.display = 'block';
        return;
    }

    try {
        const response = await fetch('/api/auth/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });

        if (response.ok) {
            alert('If the email exists, a reset link has been sent. Please check your inbox (and spam folder).');
            window.location.href = '/login.html';
        } else {
            const error = await response.text();
            errorDiv.textContent = error || 'Request failed';
            errorDiv.style.display = 'block';
        }
    } catch (error) {
        console.error('Error:', error);
        errorDiv.textContent = 'Network error. Please try again.';
        errorDiv.style.display = 'block';
    }
}