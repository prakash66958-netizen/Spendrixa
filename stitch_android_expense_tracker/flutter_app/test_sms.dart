import 'package:lumina/src/services/sms_parser.dart';

void main() {
  final List<String> dummyMessages = [
    // Typical expense (debit) messages
    "Rs.500.00 debited from a/c **1234 on 29-04-26 to Zomato. Available balance Rs.10000.00",
    "Your ICICI Bank Credit Card **5678 is used for Rs 1,200.50 on 29-Apr at Amazon. Available credit limit is Rs 50,000.",
    "Spent Rs. 150 on Swiggy via UPI. Ref No 123456789.",
    "Paid ₹2500 to BESCOM using HDFC Bank. Txn ID: ABCD123",

    // Typical income (credit) messages
    "Your a/c **1234 is credited with Rs.15,000.00 on 29-Apr by Salary. Available balance Rs.25000.00",
    "Received Rs 500 from John Doe via UPI.",
    "Refund of Rs. 1200.00 processed for your Myntra order. It will be credited to your account.",
    "Cashback of ₹50 deposited in your wallet."
  ];

  print('--- TESTING SMS PARSER ---');
  for (var msg in dummyMessages) {
    print('\nMessage: "$msg"');

    // Simulating bank sender validation
    bool isBank = SmsParser.isBankSms('HDFCBK', msg);
    print('Is Bank SMS? $isBank');

    if (isBank) {
      final parsed = SmsParser.parse(msg, sender: 'HDFCBK');
      if (parsed != null) {
        print('✅ Parsed successfully:');
        print('  Type: ${parsed.isExpense ? "EXPENSE" : "INCOME"}');
        print('  Amount: ₹${parsed.amount}');
        print('  Merchant: ${parsed.merchant.isEmpty ? "Unknown" : parsed.merchant}');
        print('  Category: ${parsed.category}');
      } else {
        print('❌ Failed to parse transaction details.');
      }
    }
  }
}
