// Firebase configuration for Spendrixa Web
const firebaseConfig = {
    apiKey: "AIzaSyBTgZYkxPMwRbASCAa-ThDSvL3CDUadKzQ",
    authDomain: "expense-tracker-1b22d.firebaseapp.com",
    projectId: "expense-tracker-1b22d",
    storageBucket: "expense-tracker-1b22d.firebasestorage.app",
    messagingSenderId: "45467822444",
    appId: "1:45467822444:web:a70d772424996e833e6b49",
    measurementId: "G-7RMFCJ198W"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();
const db = firebase.firestore();