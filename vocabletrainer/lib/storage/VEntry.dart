import 'package:uuid/uuid.dart';
import 'package:vocabletrainer/storage/CDatabase.dart';
import 'package:vocabletrainer/storage/VList.dart';

class VEntry extends RawVEntry {
  final int id;
  int changed;
  final int created;
  final UuidValue uuid;
  VEntry(
      {required super.meaningsA,
      required super.meaningsB,
      required super.list,
      required this.id,
      required super.tip,
      required this.changed,
      required this.uuid,
      required this.created,
      required super.addition});

  VEntry.fromRaw(RawVEntry raw, this.id, this.uuid, int time)
      : created = time,
        changed = time,
        super(
          meaningsA: raw.meaningsA,
          meaningsB: raw.meaningsB,
          list: raw.list,
          tip: raw.tip,
          addition: raw.addition,
        );
  VEntry.withoutMeanings(
      {required Map<String, dynamic> result, required super.list})
      : id = result[KEY_ENTRY],
        created = result[KEY_CREATED],
        changed = result[KEY_CHANGED],
        uuid = UuidValue.fromByteList(result[KEY_ENTRY_UUID]),
        super(
            meaningsA: [],
            meaningsB: [],
            tip: result[KEY_TIP],
            addition: result[KEY_ADDITION]);
  @override
  bool isRaw() {
    return false;
  }
}

/// Raw list entry for insertion
class RawVEntry {
  List<String> meaningsA;
  List<String> meaningsB;
  VList list;
  String tip;
  String addition;
  RawVEntry(
      {required this.meaningsA,
      required this.meaningsB,
      required this.list,
      required this.tip,
      required this.addition});

  bool isRaw() {
    return true;
  }
}
