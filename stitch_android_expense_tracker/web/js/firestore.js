// Firestore operations for dashboard
const currencyFormat = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
});

const dateFormat = new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
});

let detachTransactionsListener = null;
let detachProfileListener = null;
let latestBudget = 0;
let allTransactions = [];

auth.onAuthStateChanged(user => {
    if (!user) {
        cleanupListeners();
        window.location.href = 'index.html';
        return;
    }
    loadDashboard(user);
});

document.getElementById('logoutBtn').addEventListener('click', () => {
    cleanupListeners();
    auth.signOut().then(() => {
        window.location.href = 'index.html';
    });
});

function cleanupListeners() {
    if (detachTransactionsListener) {
        detachTransactionsListener();
        detachTransactionsListener = null;
    }
    if (detachProfileListener) {
        detachProfileListener();
        detachProfileListener = null;
    }
}

function loadDashboard(user) {
    const transactionsList = document.getElementById('transactionsList');
    transactionsList.innerHTML = '<p class="loading">Loading transactions...</p>';
    cleanupListeners();

    detachTransactionsListener = db.collection('users')
        .doc(user.uid)
        .collection('transactions')
        .orderBy('date', 'desc')
        .onSnapshot(snapshot => {
            allTransactions = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
            renderTransactions(allTransactions.slice(0, 5));
            updateTotals(allTransactions, latestBudget);
        }, error => {
            console.error('Error loading transactions:', error);
            transactionsList.innerHTML = '<p class="error-message">Failed to load transactions</p>';
        });

    detachProfileListener = db.collection('users')
        .doc(user.uid)
        .onSnapshot(snapshot => {
            latestBudget = snapshot.exists && typeof snapshot.data().monthlyBudget === 'number'
                ? snapshot.data().monthlyBudget
                : 0;
            updateTotals(allTransactions, latestBudget);
        }, error => {
            console.error('Error loading profile:', error);
        });
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function parseFirestoreDate(value) {
    if (value && typeof value.toDate === 'function') {
        return value.toDate();
    }
    if (typeof value === 'number') {
        return new Date(value);
    }
    if (value instanceof Date) {
        return value;
    }
    return new Date();
}

function renderTransactions(transactions) {
    const transactionsList = document.getElementById('transactionsList');

    if (!transactions.length) {
        transactionsList.innerHTML = '<p class="empty-state">No transactions yet</p>';
        return;
    }

    transactionsList.innerHTML = '';

    transactions.forEach(txn => {
        const isIncome = txn.type === 'INCOME';
        const item = document.createElement('div');
        item.className = 'transaction-item';

        const safeCategory = escapeHtml(txn.category || 'OTHER');
        const safeMerchant = escapeHtml(txn.merchant || prettifyCategory(txn.category));
        const formattedDate = dateFormat.format(parseFirestoreDate(txn.date));

        item.innerHTML = `
            <div class="transaction-left">
                <div class="transaction-icon">
                    <span class="material-symbols-outlined">${getCategoryIcon(txn.category)}</span>
                </div>
                <div class="transaction-info">
                    <h4>${safeMerchant}</h4>
                    <p>${escapeHtml(prettifyCategory(txn.category))} &bull; ${escapeHtml(formattedDate)}</p>
                </div>
            </div>
            <div class="transaction-right">
                <p class="amount ${isIncome ? 'income' : ''}">${isIncome ? '+' : '-'}${currencyFormat.format(txn.amount || 0)}</p>
                ${txn.isVerified ? '<p class="verified">Verified</p>' : ''}
            </div>
        `;
        transactionsList.appendChild(item);
    });
}

function updateTotals(transactions, monthlyBudget) {
    const totalIncome = transactions
        .filter(t => t.type === 'INCOME')
        .reduce((sum, t) => sum + (t.amount || 0), 0);

    const totalExpense = transactions
        .filter(t => t.type === 'EXPENSE')
        .reduce((sum, t) => sum + (t.amount || 0), 0);

    const spentAmount = totalExpense;
    const remaining = monthlyBudget - spentAmount;
    const usagePercent = monthlyBudget > 0 ? Math.min(100, Math.round((spentAmount / monthlyBudget) * 100)) : 0;

    document.getElementById('monthlyBudget').textContent = currencyFormat.format(monthlyBudget);
    document.getElementById('spentAmount').textContent = currencyFormat.format(spentAmount);
    document.getElementById('remainingAmount').textContent = currencyFormat.format(remaining);
    document.getElementById('usagePercent').textContent = `${usagePercent}% Spent`;
    document.getElementById('totalIncome').textContent = currencyFormat.format(totalIncome);
    document.getElementById('totalExpense').textContent = currencyFormat.format(totalExpense);
    document.getElementById('progressFill').style.width = `${usagePercent}%`;
}

function getCategoryIcon(category) {
    const icons = {
        FOOD: 'restaurant',
        TRANSPORT: 'directions_car',
        SHOPPING: 'shopping_bag',
        GAMING: 'sports_esports',
        GYM: 'fitness_center',
        ENTERTAINMENT: 'movie',
        TECHNOLOGY: 'cloud',
        HEALTH: 'local_hospital',
        TRAVEL: 'flight',
        SUBSCRIPTION: 'subscriptions',
        HOUSING: 'home',
        INCOME: 'account_balance',
        OTHER: 'receipt'
    };
    return icons[category] || 'receipt';
}

function prettifyCategory(category = 'OTHER') {
    return category.charAt(0) + category.slice(1).toLowerCase();
}
