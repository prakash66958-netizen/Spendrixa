// Final Optimized Admin functionality for Spendrixa Console
const CURRENCIES = {
    USD: { symbol: '$', locale: 'en-US' },
    EUR: { symbol: '€', locale: 'de-DE' },
    GBP: { symbol: '£', locale: 'en-GB' },
    INR: { symbol: '₹', locale: 'en-IN' },
    JPY: { symbol: '¥', locale: 'ja-JP' },
    AUD: { symbol: 'A$', locale: 'en-AU' },
    CAD: { symbol: 'C$', locale: 'en-CA' }
};

function getCurrencyFormatter(currencyCode) {
    const config = CURRENCIES[currencyCode] || CURRENCIES.USD;
    return new Intl.NumberFormat(config.locale, {
        style: 'currency',
        currency: currencyCode || 'USD',
        minimumFractionDigits: currencyCode === 'JPY' ? 0 : 2
    });
}

const defaultFormatter = getCurrencyFormatter('USD');

const dateFormat = new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
});

// Admin check logic
auth.onAuthStateChanged(async (user) => {
    if (!user) {
        window.location.href = '/index.html';
        return;
    }

    try {
        const userDoc = await db.collection('users').doc(user.uid).get();
        if (!userDoc.exists || userDoc.data().role !== 'admin') {
            alert('Access denied. Administrator privileges required.');
            window.location.href = '/dashboard.html';
            return;
        }

        const adminNameEl = document.getElementById('adminName');
        if (adminNameEl) adminNameEl.textContent = userDoc.data().name || user.email;

        initializePage();
    } catch (error) {
        console.error('Error verifying admin status:', error);
        window.location.href = '/dashboard.html';
    }
});

function initializePage() {
    const path = window.location.pathname;
    if (path.includes('users.html') || path.endsWith('/users')) {
        loadUsers();
    } else if (path.includes('transactions.html') || path.endsWith('/transactions')) {
        loadTransactions();
    } else if (path.includes('user_detail.html') || path.endsWith('/user_detail')) {
        const urlParams = new URLSearchParams(window.location.search);
        const uid = urlParams.get('uid');
        if (uid) loadUserDetail(uid);
    } else {
        loadOverview();
    }
}

/**
 * Optimized helper to fetch all transactions without using collectionGroup.
 * Fetches users first, then fetches their transaction subcollections in parallel.
 */
async function fetchAllTransactions() {
    const usersSnapshot = await db.collection('users').get();
    const userMap = {};
    usersSnapshot.docs.forEach(doc => {
        userMap[doc.id] = doc.data();
    });

    const txnPromises = usersSnapshot.docs.map(userDoc =>
        userDoc.ref.collection('transactions').get().then(s =>
            s.docs.map(doc => {
                const userData = userMap[userDoc.id];
                return {
                    id: doc.id,
                    userId: userDoc.id,
                    userName: userData.name || userData.displayName || 'User',
                    userCurrency: userData.currency || 'USD',
                    ...doc.data()
                };
            })
        )
    );
    const results = await Promise.all(txnPromises);
    return {
        usersCount: usersSnapshot.size,
        transactions: results.flat().sort((a, b) => (b.date || 0) - (a.date || 0))
    };
}

async function loadOverview() {
    try {
        const statUsers = document.getElementById('stat-users');
        const statTransactions = document.getElementById('stat-transactions');
        const statRevenue = document.getElementById('stat-revenue');
        const recentTxnsBody = document.getElementById('recent-transactions-body');
        const activityStream = document.getElementById('activity-stream');

        const data = await fetchAllTransactions();

        if (statUsers) statUsers.textContent = data.usersCount.toLocaleString();
        if (statTransactions) statTransactions.textContent = data.transactions.length.toLocaleString();

        let totalRevenue = 0;
        data.transactions.forEach(txn => {
            if (txn.type === 'INCOME') totalRevenue += (txn.amount || 0);
        });

        if (statRevenue) statRevenue.textContent = defaultFormatter.format(totalRevenue);

        if (recentTxnsBody) {
            recentTxnsBody.innerHTML = '';
            const overviewTxns = data.transactions.slice(0, 5);
            if (overviewTxns.length === 0) {
                recentTxnsBody.innerHTML = '<tr><td colspan="5" class="px-6 py-10 text-center text-slate-400">No transactions found</td></tr>';
            } else {
                overviewTxns.forEach(txn => {
                    const formatter = getCurrencyFormatter(txn.userCurrency);
                    const row = document.createElement('tr');
                    row.className = 'hover:bg-slate-50 transition-colors cursor-pointer';
                    row.onclick = () => window.location.href = `/admin/user_detail.html?uid=${txn.userId}`;
                    row.innerHTML = `
                        <td class="px-6 py-4 font-mono text-xs text-blue-600 font-bold">${txn.id.substring(0, 8)}</td>
                        <td class="px-6 py-4 font-semibold">${txn.merchant || 'Internal'}</td>
                        <td class="px-6 py-4">
                            <span class="px-2 py-0.5 rounded-full ${txn.type === 'INCOME' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'} text-[10px] font-bold uppercase">
                                ${txn.type}
                            </span>
                        </td>
                        <td class="px-6 py-4 font-bold text-slate-900">${formatter.format(txn.amount)}</td>
                        <td class="px-6 py-4 text-right text-slate-500 text-xs">${new Date(txn.date).toLocaleTimeString()}</td>
                    `;
                    recentTxnsBody.appendChild(row);
                });
            }
        }

        if (activityStream) {
            activityStream.innerHTML = '';
            const latestActivities = data.transactions.slice(0, 10);
            if (latestActivities.length === 0) {
                activityStream.innerHTML = '<p class="text-center text-slate-400 py-10 italic">No activity yet</p>';
            } else {
                latestActivities.forEach(txn => {
                    const formatter = getCurrencyFormatter(txn.userCurrency);
                    const timeAgo = Math.floor((Date.now() - (txn.date || Date.now())) / 60000);
                    const timeText = timeAgo < 1 ? 'Just now' : timeAgo < 60 ? `${timeAgo}m ago` : `${Math.floor(timeAgo/60)}h ago`;
                    const item = document.createElement('div');
                    item.className = 'flex gap-3 p-3 rounded-lg bg-slate-50 border border-slate-100 hover:border-blue-200 transition-colors cursor-pointer';
                    item.onclick = () => window.location.href = `/admin/user_detail.html?uid=${txn.userId}`;
                    item.innerHTML = `
                        <div class="shrink-0 w-8 h-8 rounded-full ${txn.type === 'INCOME' ? 'bg-emerald-500' : 'bg-blue-500'} flex items-center justify-center text-white shadow-sm">
                            <span class="material-symbols-outlined text-sm">${txn.type === 'INCOME' ? 'add_card' : 'payments'}</span>
                        </div>
                        <div class="flex-1">
                            <p class="text-xs text-slate-900 font-bold">${txn.type === 'INCOME' ? 'Income' : 'Expense'} recorded</p>
                            <p class="text-[11px] text-slate-500 leading-tight">${txn.merchant || txn.category} • ${formatter.format(txn.amount)}</p>
                            <p class="text-[10px] text-blue-600 font-medium mt-1 uppercase tracking-tighter">${timeText}</p>
                        </div>
                    `;
                    activityStream.appendChild(item);
                });
            }
        }
    } catch (error) {
        console.error('Error loading overview:', error);
    }
}

async function loadUsers() {
    const tableBody = document.getElementById('users-table-body');
    const footerCount = document.getElementById('user-count-footer');
    if (!tableBody) return;

    try {
        const snapshot = await db.collection('users').get();
        tableBody.innerHTML = '';
        if (snapshot.empty) {
            tableBody.innerHTML = '<tr><td colspan="5" class="px-6 py-12 text-center text-slate-400 italic">No users found</td></tr>';
            if (footerCount) footerCount.textContent = '0 users found';
            return;
        }
        if (footerCount) footerCount.textContent = `Displaying all ${snapshot.size} platform users`;

        snapshot.docs.forEach(doc => {
            const userData = doc.data();
            const uid = doc.id;
            const email = userData.email || 'No email';
            const name = userData.name || userData.displayName || 'Unnamed User';
            const role = userData.role || 'user';
            const username = userData.username || 'no_username';

            const row = document.createElement('tr');
            row.className = 'hover:bg-slate-50 transition-colors cursor-pointer';
            row.onclick = () => window.location.href = `/admin/user_detail.html?uid=${uid}`;
            row.innerHTML = `
                <td class="px-6 py-4">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-full bg-slate-900 text-white flex items-center justify-center font-bold text-sm">
                            ${name.substring(0, 2).toUpperCase()}
                        </div>
                        <div>
                            <div class="text-sm font-black text-slate-900">${name}</div>
                            <div class="text-[10px] text-slate-400 font-bold uppercase tracking-tighter">@${username}</div>
                            <div class="text-[11px] text-slate-500">${email}</div>
                        </div>
                    </div>
                </td>
                <td class="px-6 py-4">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider ${role === 'admin' ? 'bg-purple-100 text-purple-800' : 'bg-slate-100 text-slate-600'}">
                        ${role}
                    </span>
                </td>
                <td class="px-6 py-4 font-mono text-[10px] text-slate-400">${uid}</td>
                <td class="px-6 py-4">
                    <span class="inline-flex items-center gap-1.5 text-emerald-600 text-xs font-bold">
                        <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span> ACTIVE
                    </span>
                </td>
                <td class="px-6 py-4 text-right space-x-2" onclick="event.stopPropagation()">
                    <button class="p-2 hover:bg-blue-50 rounded-lg text-blue-600 transition-colors" onclick="editUser('${uid}', '${name}', '${email}', '${role}')">
                        <span class="material-symbols-outlined text-lg">edit</span>
                    </button>
                    <button class="p-2 hover:bg-rose-50 rounded-lg text-rose-600 transition-colors" onclick="deleteUser('${uid}')">
                        <span class="material-symbols-outlined text-lg">delete</span>
                    </button>
                </td>
            `;
            tableBody.appendChild(row);
        });
    } catch (error) {
        console.error('Error loading users:', error);
        tableBody.innerHTML = '<tr><td colspan="5" class="px-6 py-12 text-center text-rose-500 italic">Database connection error</td></tr>';
    }
}

async function loadTransactions() {
    const tableBody = document.getElementById('transactions-table-body');
    const footerCount = document.getElementById('transaction-count-footer');
    if (!tableBody) return;

    try {
        const data = await fetchAllTransactions();
        tableBody.innerHTML = '';
        if (data.transactions.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="8" class="px-6 py-12 text-center text-slate-400 italic">No transactions found</td></tr>';
            if (footerCount) footerCount.textContent = '0 records found';
            return;
        }
        if (footerCount) footerCount.textContent = `Audited ${data.transactions.length} total platform transactions`;

        data.transactions.forEach(txn => {
            const isIncome = txn.type === 'INCOME';
            const date = new Date(txn.date || Date.now());
            const formatter = getCurrencyFormatter(txn.userCurrency);
            const row = document.createElement('tr');
            row.className = 'hover:bg-slate-50 transition-colors cursor-pointer';
            row.onclick = () => window.location.href = `/admin/user_detail.html?uid=${txn.userId}`;
            row.innerHTML = `
                <td class="px-6 py-4 font-mono text-[10px] text-blue-600 font-bold">${txn.id.substring(0, 8)}</td>
                <td class="px-6 py-4 text-xs font-bold text-slate-900">${txn.userName}</td>
                <td class="px-6 py-4">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded text-[10px] font-black uppercase ${isIncome ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'}">
                        ${txn.type}
                    </span>
                </td>
                <td class="px-6 py-4 text-xs font-bold text-slate-600">${txn.category}</td>
                <td class="px-6 py-4 font-black text-right ${isIncome ? 'text-emerald-600' : 'text-slate-900'}">
                    ${isIncome ? '+' : '-'}${formatter.format(txn.amount)}
                </td>
                <td class="px-6 py-4">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold bg-blue-50 text-blue-700">
                        VERIFIED
                    </span>
                </td>
                <td class="px-6 py-4 text-[10px] text-slate-500 font-medium">${dateFormat.format(date)}</td>
                <td class="px-6 py-4 text-center space-x-1" onclick="event.stopPropagation()">
                    <button class="p-1.5 hover:bg-blue-50 rounded-lg text-blue-600 transition-colors" onclick="editTransaction('${txn.userId}', '${txn.id}', ${txn.amount}, '${txn.merchant}', '${txn.category}')">
                        <span class="material-symbols-outlined text-lg">edit</span>
                    </button>
                    <button class="p-1.5 hover:bg-rose-50 rounded-lg text-rose-600 transition-colors" onclick="deleteTransaction('${txn.userId}', '${txn.id}')">
                        <span class="material-symbols-outlined text-lg">delete</span>
                    </button>
                </td>
            `;
            tableBody.appendChild(row);
        });
    } catch (error) {
        console.error('Error loading transactions:', error);
        tableBody.innerHTML = '<tr><td colspan="8" class="px-6 py-12 text-center text-rose-500 italic">Synchronization failed</td></tr>';
    }
}

async function loadUserDetail(uid) {
    const header = document.getElementById('user-detail-header');
    const stats = document.getElementById('user-detail-stats');
    const tableBody = document.getElementById('user-transactions-body');

    try {
        const userDoc = await db.collection('users').doc(uid).get();
        if (!userDoc.exists) {
            alert('User not found');
            window.location.href = '/admin/users.html';
            return;
        }

        const userData = userDoc.data();
        const formatter = getCurrencyFormatter(userData.currency);

        if (header) {
            header.innerHTML = `
                <div class="flex items-center gap-6">
                    <div class="w-20 h-20 rounded-full bg-slate-900 text-white flex items-center justify-center text-3xl font-bold shadow-xl">
                        ${(userData.name || 'U').substring(0, 1).toUpperCase()}
                    </div>
                    <div>
                        <h2 class="text-3xl font-black text-slate-900">${userData.name || 'Unnamed User'}</h2>
                        <p class="text-slate-500 font-bold uppercase tracking-widest text-xs">@${userData.username || 'no_username'} • ${userData.email}</p>
                    </div>
                </div>
            `;
        }

        const txnsSnapshot = await userDoc.ref.collection('transactions').orderBy('date', 'desc').get();
        const txns = txnsSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

        const totalIncome = txns.filter(t => t.type === 'INCOME').reduce((s, t) => s + (t.amount || 0), 0);
        const totalExpense = txns.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + (t.amount || 0), 0);

        if (stats) {
            stats.innerHTML = `
                <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                    <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Monthly Budget</p>
                    <p class="text-2xl font-black text-slate-900">${formatter.format(userData.monthlyBudget || 0)}</p>
                </div>
                <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                    <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Total Income</p>
                    <p class="text-2xl font-black text-emerald-600">${formatter.format(totalIncome)}</p>
                </div>
                <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                    <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Total Expenses</p>
                    <p class="text-2xl font-black text-rose-600">${formatter.format(totalExpense)}</p>
                </div>
                <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                    <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Balance</p>
                    <p class="text-2xl font-black text-blue-600">${formatter.format(totalIncome - totalExpense)}</p>
                </div>
            `;
        }

        if (tableBody) {
            tableBody.innerHTML = '';
            if (txns.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="5" class="px-6 py-10 text-center text-slate-400 italic">No transactions for this user</td></tr>';
            } else {
                txns.forEach(txn => {
                    const isIncome = txn.type === 'INCOME';
                    const row = document.createElement('tr');
                    row.className = 'hover:bg-slate-50 transition-colors';
                    row.innerHTML = `
                        <td class="px-6 py-4 text-xs font-medium text-slate-500">${dateFormat.format(new Date(txn.date))}</td>
                        <td class="px-6 py-4 font-bold text-slate-900">${txn.merchant || txn.category}</td>
                        <td class="px-6 py-4"><span class="px-2 py-0.5 rounded-full text-[10px] font-black uppercase ${isIncome ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'}">${txn.type}</span></td>
                        <td class="px-6 py-4 font-black text-right ${isIncome ? 'text-emerald-600' : 'text-slate-900'}">${isIncome ? '+' : '-'}${formatter.format(txn.amount)}</td>
                        <td class="px-6 py-4 text-right">
                            <button class="p-1.5 hover:bg-rose-50 rounded-lg text-rose-600 transition-colors" onclick="deleteTransaction('${uid}', '${txn.id}'); loadUserDetail('${uid}');">
                                <span class="material-symbols-outlined text-lg">delete</span>
                            </button>
                        </td>
                    `;
                    tableBody.appendChild(row);
                });
            }
        }
    } catch (error) {
        console.error('Error loading user detail:', error);
    }
}

async function deleteTransaction(userId, txnId) {
    if (!confirm('Permanently delete this transaction?')) return;
    try {
        await db.collection('users').doc(userId).collection('transactions').doc(txnId).delete();
        // Determine if we need to reload a list or a detail page
        if (window.location.pathname.includes('user_detail.html')) {
            const urlParams = new URLSearchParams(window.location.search);
            loadUserDetail(urlParams.get('uid'));
        } else {
            loadTransactions();
            if (window.location.pathname.includes('index.html')) loadOverview();
        }
    } catch (error) { alert('Delete failed: ' + error.message); }
}

async function deleteUser(uid) {
    if (!confirm('Permanently delete this user and all data?')) return;
    try {
        await db.collection('users').doc(uid).delete();
        loadUsers();
    } catch (error) { alert('Delete failed: ' + error.message); }
}

async function editUser(uid, currentName, currentEmail, currentRole) {
    const newName = prompt("Update User Name:", currentName);
    if (newName === null) return;
    const newRole = prompt("Assign Role (user/admin):", currentRole);
    if (newRole === null) return;
    try {
        await db.collection('users').doc(uid).update({ name: newName, role: newRole });
        loadUsers();
    } catch (error) { alert('Update failed: ' + error.message); }
}

async function editTransaction(userId, txnId, currentAmount, currentMerchant, currentCategory) {
    const newAmount = prompt("Update Amount:", currentAmount);
    if (newAmount === null) return;
    const newMerchant = prompt("Update Merchant:", currentMerchant);
    if (newMerchant === null) return;
    try {
        await db.collection('users').doc(userId).collection('transactions').doc(txnId).update({
            amount: parseFloat(newAmount),
            merchant: newMerchant
        });
        loadTransactions();
    } catch (error) { alert('Update failed: ' + error.message); }
}
