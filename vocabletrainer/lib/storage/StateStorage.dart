import 'package:flutter/material.dart';
import 'package:sqflite/sqflite.dart';
import 'package:uuid/uuid.dart';
import 'package:vocabletrainer/storage/VEntry.dart';

import 'CDatabase.dart';
import 'VList.dart';

class StateStorage with ChangeNotifier {
  late Database _db;

  StateStorage();

  Future<void> initDb() {
    return initDatabase().then(
      (value) {
        _db = value;
      },
    );
  }

  Future<VList> createList(RawVList raw) async {
    int time = DateTime.now().millisecondsSinceEpoch;
    var data = raw.toMap();
    data[KEY_CREATED] = time;
    data[KEY_CHANGED] = time;
    data[KEY_SHARED] = 0;
    int id = await _db.insert(TBL_LISTS, data,
        conflictAlgorithm: ConflictAlgorithm.rollback);
    return VList.fromRaw(raw, id, time);
  }

  Future<VEntry> createEntry(RawVEntry raw) async {
    int time = DateTime.now().millisecondsSinceEpoch;
    UuidValue uuid = Uuid().v7obj();
    int? id;
    await _db.transaction(
      (txn) async {
        var values = {
          KEY_LIST: raw.list.id,
          KEY_TIP: raw.tip,
          KEY_ADDITION: raw.addition,
          KEY_CREATED: time,
          KEY_CHANGED: time,
          KEY_ENTRY_UUID: uuid.toBytes(),
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
    return VEntry.fromRaw(raw, id!, uuid, time);
  }

  Future<List<VList>> getLists() async {
    var res = await _db.query(TBL_LISTS);
    return res.map((e) => VList.fromMap(e)).toList();
  }

  Future<List<VEntry>> getEntries(VList list) async {
    var res = await _db
        .query(TBL_ENTRIES, where: "$KEY_LIST = ?", whereArgs: [list.id]);
    var entries =
        res.map((e) => VEntry.withoutMeanings(result: e, list: list)).toList();

    for (var entry in entries) {
      var mA = await _db.query(TBL_WORDS_A,
          columns: [KEY_MEANING],
          where: "$KEY_ENTRY = ?",
          whereArgs: [entry.id]);
      var mB = await _db.query(TBL_WORDS_B,
          columns: [KEY_MEANING],
          where: "$KEY_ENTRY = ?",
          whereArgs: [entry.id]);
      entry.meaningsA = mA.map((e) => e[KEY_MEANING] as String).toList();
      entry.meaningsB = mB.map((e) => e[KEY_MEANING] as String).toList();
    }
    return entries;
  }

  /// Update list, also updates timestamps
  Future<void> updateList(VList list) async {
    int time = DateTime.now().millisecondsSinceEpoch;
    list.changed = time;
    await _db.update(TBL_LISTS, list.toMap(),
        where: '$KEY_LIST = ?', whereArgs: [list.id]);
  }

  /// Update entry, also updates timestamps
  Future<void> updateEntry(VEntry entry) async {
    int time = DateTime.now().millisecondsSinceEpoch;
    entry.changed = time;

    await _db.transaction(
      (txn) async {
        var values = {
          KEY_TIP: entry.tip,
          KEY_ADDITION: entry.addition,
          KEY_CHANGED: time,
        };
        Batch b = txn.batch();
        b.update(TBL_ENTRIES, values,
            where: '$KEY_ENTRY = ?', whereArgs: [entry.id]);
        b.delete(TBL_WORDS_A, where: '$KEY_ENTRY = ?', whereArgs: [entry.id]);
        b.delete(TBL_WORDS_B, where: '$KEY_ENTRY = ?', whereArgs: [entry.id]);
        for (var meaning in entry.meaningsA) {
          b.insert(TBL_WORDS_A, {KEY_MEANING: meaning, KEY_ENTRY: entry.id},
              conflictAlgorithm: ConflictAlgorithm.rollback);
        }
        for (var meaning in entry.meaningsB) {
          b.insert(TBL_WORDS_B, {KEY_MEANING: meaning, KEY_ENTRY: entry.id},
              conflictAlgorithm: ConflictAlgorithm.rollback);
        }
        await b.commit(noResult: true);
      },
    );
  }
}
