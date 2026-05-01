// Firebase Auth for Web
let isSignUp = false;
const form = document.getElementById('authForm');
const toggleAuthBtn = document.getElementById('toggleAuthBtn');
const toggleText = document.getElementById('toggleText');
const formSubtitle = document.getElementById('formSubtitle');
const submitBtn = document.getElementById('submitBtn');
const btnText = document.getElementById('btnText');
const errorMessage = document.getElementById('errorMessage');

// Check if already logged in
auth.onAuthStateChanged(user => {
    if (user) {
        window.location.href = 'dashboard.html';
    }
});

toggleAuthBtn.addEventListener('click', (e) => {
    e.preventDefault();
    isSignUp = !isSignUp;
    updateUI();
});

function updateUI() {
    if (isSignUp) {
        toggleText.textContent = 'Already have an account?';
        toggleAuthBtn.textContent = 'Sign In';
        formSubtitle.textContent = 'Create your account';
        btnText.textContent = 'Create Account';
    } else {
        toggleText.textContent = "Don't have an account?";
        toggleAuthBtn.textContent = 'Sign Up';
        formSubtitle.textContent = 'Welcome back';
        btnText.textContent = 'Sign In';
    }
    errorMessage.textContent = '';
}

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    submitBtn.disabled = true;
    btnText.classList.add('hidden');
    document.getElementById('btnLoader').classList.remove('hidden');
    errorMessage.textContent = '';

    try {
        if (isSignUp) {
            await auth.createUserWithEmailAndPassword(email, password);
        } else {
            await auth.signInWithEmailAndPassword(email, password);
        }
        window.location.href = 'dashboard.html';
    } catch (error) {
        errorMessage.textContent = error.message;
    } finally {
        submitBtn.disabled = false;
        btnText.classList.remove('hidden');
        document.getElementById('btnLoader').classList.add('hidden');
    }
});