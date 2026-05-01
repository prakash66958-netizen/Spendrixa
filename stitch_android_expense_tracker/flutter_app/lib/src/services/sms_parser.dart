class ParsedTransaction {
  const ParsedTransaction({
    required this.amount,
    required this.isExpense,
    this.merchant = '',
    this.category = 'OTHER',
    this.accountTail = '',
    this.refNumber = '',
    this.source = 'SMS',
  });

  final double amount;
  final bool isExpense;
  final String merchant;
  final String category;
  final String accountTail;
  final String refNumber;
  final String source;
}

class SmsParser {
  static final RegExp _debitPattern = RegExp(
    r'(debited|debit|withdrawn|spent|paid|purchase|sent|used)',
    caseSensitive: false,
  );
  static final RegExp _creditPattern = RegExp(
    r'(credited\b|received|deposited|refund\b|cashback)',
    caseSensitive: false,
  );

  static final RegExp _amountPattern = RegExp(
    r'(?:Rs\.?|INR|₹)\s*([\d,]+(?:\.\d{1,2})?)',
    caseSensitive: false,
  );

  static final RegExp _accountPattern = RegExp(
    r'(?:A/?c|Ac|Acct|Account|a\/c)\s*(?:no\.?\s*)?(?:ending\s*)?([Xx*]+\d{3,6}|\d{3,6})',
    caseSensitive: false,
  );

  static final RegExp _merchantPattern = RegExp(
    r'(?:\bat\b|\bto\b|\bfor\b|\bvia\b|\bon\b)\s+([A-Za-z][A-Za-z0-9\s\-&.]+?)(?:\s+on\b|\s+ref\b|\s+via\b|\s+using\b|\s+UPI\b|[.]|\s*$)',
    caseSensitive: false,
  );

  static final RegExp _upiMerchantPattern = RegExp(
    r'(?:UPI|VPA|to)\s*[-:]?\s*([a-zA-Z0-9._]+@[a-zA-Z]+)',
    caseSensitive: false,
  );

  static final RegExp _refPattern = RegExp(
    r'(?:Ref\.?\s*(?:No\.?)?\s*|txn\s*(?:id)?\s*|IMPS\s*Ref\s*)[:.]?\s*(\w{6,})',
    caseSensitive: false,
  );

  static const Map<String, String> _merchantCategories = <String, String>{
    'swiggy': 'FOOD',
    'zomato': 'FOOD',
    'dominos': 'FOOD',
    'mcdonalds': 'FOOD',
    'starbucks': 'FOOD',
    'uber eats': 'FOOD',
    'blinkit': 'FOOD',
    'zepto': 'FOOD',
    'bigbasket': 'FOOD',
    'dunzo': 'FOOD',
    'uber': 'TRANSPORT',
    'ola': 'TRANSPORT',
    'rapido': 'TRANSPORT',
    'irctc': 'TRAVEL',
    'makemytrip': 'TRAVEL',
    'goibibo': 'TRAVEL',
    'cleartrip': 'TRAVEL',
    'yatra': 'TRAVEL',
    'amazon': 'SHOPPING',
    'flipkart': 'SHOPPING',
    'myntra': 'SHOPPING',
    'ajio': 'SHOPPING',
    'meesho': 'SHOPPING',
    'nykaa': 'SHOPPING',
    'netflix': 'SUBSCRIPTION',
    'spotify': 'SUBSCRIPTION',
    'hotstar': 'SUBSCRIPTION',
    'prime video': 'SUBSCRIPTION',
    'jiocinema': 'SUBSCRIPTION',
    'youtube': 'SUBSCRIPTION',
    'google play': 'SUBSCRIPTION',
    'apple': 'SUBSCRIPTION',
    'steam': 'GAMING',
    'playstation': 'GAMING',
    'pharmeasy': 'HEALTH',
    'practo': 'HEALTH',
    'apollo': 'HEALTH',
    'medplus': 'HEALTH',
    '1mg': 'HEALTH',
    'cult.fit': 'GYM',
    'cultfit': 'GYM',
    'bookmyshow': 'ENTERTAINMENT',
    'pvr': 'ENTERTAINMENT',
    'inox': 'ENTERTAINMENT',
    'rent': 'HOUSING',
    'electricity': 'HOUSING',
    'bescom': 'HOUSING',
    'water bill': 'HOUSING',
    'broadband': 'HOUSING',
    'airtel': 'HOUSING',
    'jio': 'HOUSING',
    'vi ': 'HOUSING',
  };

  static final Set<String> _bankSenders = <String>{
    'sbiinb', 'sbicrd', 'sbibnk', 'sbipsg',
    'hdfcbk', 'hdfcbn', 'cbssbi', 'icicib',
    'axisbk', 'kotakb', 'pnbsms', 'boiind',
    'baborb', 'iaborb', 'idfcfb', 'yesbk',
    'indbnk', 'fedbk', 'rblbnk', 'citibn',
    'unionb', 'canbnk', 'dfrbnk', 'centbk',
  };

  static bool isBankSms(String sender, String body) {
    final String s = sender.toLowerCase().replaceAll(RegExp(r'[^a-z]'), '');
    if (_bankSenders.any((String id) => s.contains(id))) return true;
    if (_debitPattern.hasMatch(body) || _creditPattern.hasMatch(body)) {
      if (_amountPattern.hasMatch(body)) return true;
    }
    return false;
  }

  static ParsedTransaction? parse(String body, {String sender = ''}) {
    final RegExpMatch? firstDebit = _debitPattern.firstMatch(body);
    final RegExpMatch? firstCredit = _creditPattern.firstMatch(body);
    final bool isDebit = firstDebit != null;
    final bool isCredit = firstCredit != null;
    if (!isDebit && !isCredit) return null;

    final RegExpMatch? amountMatch = _amountPattern.firstMatch(body);
    if (amountMatch == null) return null;

    final double? amount = double.tryParse(
      amountMatch.group(1)!.replaceAll(',', ''),
    );
    if (amount == null || amount <= 0) return null;

    String merchant = '';
    final RegExpMatch? merchantMatch = _merchantPattern.firstMatch(body);
    if (merchantMatch != null) {
      merchant = merchantMatch.group(1)!.trim();
    }
    if (merchant.isEmpty) {
      final RegExpMatch? upiMatch = _upiMerchantPattern.firstMatch(body);
      if (upiMatch != null) {
        merchant = upiMatch.group(1)!.split('@').first;
      }
    }

    String accountTail = '';
    final RegExpMatch? accountMatch = _accountPattern.firstMatch(body);
    if (accountMatch != null) {
      accountTail = accountMatch.group(1)!;
    }

    String refNumber = '';
    final RegExpMatch? refMatch = _refPattern.firstMatch(body);
    if (refMatch != null) {
      refNumber = refMatch.group(1)!;
    }

    final String category = _categorize(merchant, body);
    final bool isExpense = isDebit && (!isCredit || firstDebit.start < firstCredit.start);

    return ParsedTransaction(
      amount: amount,
      isExpense: isExpense,
      merchant: merchant,
      category: category,
      accountTail: accountTail,
      refNumber: refNumber,
    );
  }

  static String _categorize(String merchant, String body) {
    final String combined = '$merchant $body'.toLowerCase();
    for (final MapEntry<String, String> entry in _merchantCategories.entries) {
      if (combined.contains(entry.key)) return entry.value;
    }
    return 'OTHER';
  }
}
