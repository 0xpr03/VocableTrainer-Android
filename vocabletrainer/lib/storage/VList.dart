import 'package:vocabletrainer/storage/CDatabase.dart';

class VList {
  String name;
  String nameA;
  String nameB;
  int id;
  int created;
  int changed;
  int shared;
  VList(
      {required this.name,
      required this.nameA,
      required this.nameB,
      required this.id,
      required this.created,
      required this.changed,
      required this.shared});

  VList.fromRaw(RawVList raw, this.id, int time)
      : name = raw.name,
        nameA = raw.nameA,
        nameB = raw.nameB,
        changed = time,
        created = time,
        shared = 0;

  VList.fromMap(Map<String, dynamic> result)
      : name = result[KEY_NAME_LIST],
        id = result[KEY_LIST],
        nameA = result[KEY_NAME_A],
        nameB = result[KEY_NAME_B],
        shared = result[KEY_SHARED],
        created = result[KEY_CREATED],
        changed = result[KEY_CHANGED];
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
}
