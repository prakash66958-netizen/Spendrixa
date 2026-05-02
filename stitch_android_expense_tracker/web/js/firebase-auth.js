// Firebase Auth for Web
let isSignUp = false;
const form = document.getElementById('authForm');
const toggleAuthBtn = document.getElementById('toggleAuthBtn');
const toggleText = document.getElementById('toggleText');
const formSubtitle = document.getElementById('formSubtitle');
const submitBtn = document.getElementById('submitBtn');
const btnText = document.getElementById('btnText');
const errorMessage = document.getElementById('errorMessage');
const forgotPasswordBtn = document.getElementById('forgotPassword');
const usernameContainer = document.getElementById('usernameContainer');
const usernameInput = document.getElementById('username');

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
        usernameContainer.classList.remove('hidden');
        usernameInput.required = true;
    } else {
        toggleText.textContent = "Don't have an account?";
        toggleAuthBtn.textContent = 'Sign Up';
        formSubtitle.textContent = 'Welcome back';
        btnText.textContent = 'Sign In';
        usernameContainer.classList.add('hidden');
        usernameInput.required = false;
    }
    errorMessage.textContent = '';
}

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const username = usernameInput.value.trim().toLowerCase();

    submitBtn.disabled = true;
    btnText.classList.add('hidden');
    document.getElementById('btnLoader').classList.remove('hidden');
    errorMessage.textContent = '';

    try {
        if (isSignUp) {
            // Check username uniqueness
            const usernameDoc = await db.collection('usernames').doc(username).get();
            if (usernameDoc.exists) {
                throw new Error('Username is already taken. Please choose another.');
            }

            const userCredential = await auth.createUserWithEmailAndPassword(email, password);
            const user = userCredential.user;

            // Save user profile with username
            await db.collection('users').doc(user.uid).set({
                email: email,
                name: username, // Default name to username
                username: username,
                role: 'user',
                createdAt: Date.now()
            }, { merge: true });

            // Claim username
            await db.collection('usernames').doc(username).set({
                uid: user.uid
            });

            await user.sendEmailVerification();
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

forgotPasswordBtn.addEventListener('click', async (e) => {
    e.preventDefault();
    const email = document.getElementById('email').value;
    if (!email) {
        errorMessage.textContent = 'Please enter your email address first.';
        errorMessage.style.color = '#ba1a1a';
        return;
    }

    try {
        await auth.sendPasswordResetEmail(email);
        errorMessage.textContent = 'Password reset email sent! Check your inbox.';
        errorMessage.style.color = '#006c49'; // secondary/success color
    } catch (error) {
        errorMessage.textContent = error.message;
        errorMessage.style.color = '#ba1a1a';
    }
});