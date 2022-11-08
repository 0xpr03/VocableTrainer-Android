import 'package:flutter/material.dart';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

import 'CDatabase.dart';

class StateStorage with ChangeNotifier {
  late Database _db;

  StateStorage() {
    initDatabase().then((value) => _db = value);
  }
}
