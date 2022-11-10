import 'package:vocabletrainer/storage/CDatabase.dart';

class VList extends RawVList {
  int id;
  int created;
  int changed;
  int shared;
  VList(
      {required super.name,
      required super.nameA,
      required super.nameB,
      required this.id,
      required this.created,
      required this.changed,
      required this.shared});

  VList.fromRaw(RawVList raw, this.id, int time)
      : changed = time,
        created = time,
        shared = 0,
        super(name: raw.name, nameA: raw.nameA, nameB: raw.nameB);

  VList.fromMap(Map<String, dynamic> result)
      : id = result[KEY_LIST],
        shared = result[KEY_SHARED],
        created = result[KEY_CREATED],
        changed = result[KEY_CHANGED],
        super(
            name: result[KEY_NAME_LIST],
            nameA: result[KEY_NAME_A],
            nameB: result[KEY_NAME_B]);

  @override
  Map<String, Object?> toMap() {
    return {
      KEY_NAME_LIST: name,
      KEY_NAME_A: nameA,
      KEY_NAME_B: nameB,
      KEY_LIST: id,
      KEY_SHARED: shared,
      KEY_CREATED: created,
      KEY_CHANGED: changed,
    };
  }

  @override
  bool isRaw() {
    return false;
  }
}

/// Raw list for new entries to insert
class RawVList {
  String name;
  String nameA;
  String nameB;
  RawVList({required this.name, required this.nameA, required this.nameB});

  Map<String, Object?> toMap() {
    return {
      KEY_NAME_LIST: name,
      KEY_NAME_A: nameA,
      KEY_NAME_B: nameB,
    };
  }

  bool isRaw() {
    return true;
  }
}
