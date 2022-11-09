import 'package:flutter/material.dart';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';
import 'package:vocabletrainer/storage/VEntry.dart';

import 'CDatabase.dart';
import 'VList.dart';

class StateStorage with ChangeNotifier {
  late Database _db;

  StateStorage() {
    initDatabase().then((value) {
      print("DB initialized");
      _db = value;
    });
  }

  Future<void> initDb() {
    return initDatabase().then(
      (value) {
        print("DB initialized");
        _db = value;
      },
    );
  }

  Future<VList> createList(RawVList raw) async {
    int time = DateTime.now().millisecondsSinceEpoch;
    int id = await _db.insert(TBL_LISTS, raw.toMap(),
        conflictAlgorithm: ConflictAlgorithm.rollback);
    return VList.fromRaw(raw, id, time);
  }

  Future<VEntry> createEntry(RawVEntry raw) async {
    int time = DateTime.now().millisecondsSinceEpoch;
    int? id;
    await _db.transaction(
      (txn) async {
        var values = {
          KEY_LIST: raw.list.id,
          KEY_TIP: raw.tip,
          KEY_ADDITION: raw.addition,
          KEY_CREATED: time,
          KEY_CHANGED: time,
        };
        id = await txn.insert(TBL_ENTRIES, values,
            conflictAlgorithm: ConflictAlgorithm.rollback);
        Batch b = txn.batch();
        for (var meaning in raw.meaningsA) {
          b.insert(TBL_WORDS_A, {KEY_MEANING: meaning, KEY_ENTRY: id},
              conflictAlgorithm: ConflictAlgorithm.rollback);
        }
        for (var meaning in raw.meaningsB) {
          b.insert(TBL_WORDS_B, {KEY_MEANING: meaning, KEY_ENTRY: id},
              conflictAlgorithm: ConflictAlgorithm.rollback);
        }
        await b.commit(noResult: true);
      },
    );
    return VEntry.fromRaw(raw, id!, time);
  }

  Future<List<VList>> getLists() async {
    var res = await _db.query(TBL_LISTS);
    return res.map((e) => VList.fromMap(e)).toList();
  }
}
