import 'dart:io';
import 'package:csv/csv.dart';
import 'package:intl/intl.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';

import '../models/app_transaction.dart';

class DataExportService {
  static Future<void> exportToCsv(List<AppTransaction> transactions) async {
    final List<List<dynamic>> rows = <List<dynamic>>[];

    // Add Header
    rows.add(<String>[
      'Date',
      'Merchant',
      'Amount',
      'Category',
      'Type',
      'Payment Method',
      'Note',
      'Status',
      'Verified',
      'Points'
    ]);

    // Add Data
    for (final AppTransaction txn in transactions) {
      rows.add(<dynamic>[
        DateFormat('yyyy-MM-dd HH:mm').format(txn.date),
        txn.merchant,
        txn.amount,
        txn.category,
        txn.type,
        txn.paymentMethod,
        txn.note,
        txn.status,
        txn.isVerified ? 'Yes' : 'No',
        txn.points,
      ]);
    }

    final String csvContent = const ListToCsvConverter().convert(rows);

    // Save to a temporary file
    final Directory directory = await getTemporaryDirectory();
    final String path = '${directory.path}/spendrixa_export_${DateTime.now().millisecondsSinceEpoch}.csv';
    final File file = File(path);
    await file.writeAsString(csvContent);

    // Share the file
    final XFile xFile = XFile(path);
    await Share.shareXFiles(<XFile>[xFile], text: 'My Spendrixa Transaction Export');
  }
}
