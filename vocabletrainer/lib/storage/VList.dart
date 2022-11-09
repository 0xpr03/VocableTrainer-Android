import 'package:vocabletrainer/storage/CDatabase.dart';

class VList {
  String name;
  String nameA;
  String nameB;
  int id;
  VList(
      {required this.name,
      required this.nameA,
      required this.nameB,
      required this.id});

  VList.fromRaw(RawVList raw, this.id)
      : name = raw.name,
        nameA = raw.nameA,
        nameB = raw.nameB;
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
