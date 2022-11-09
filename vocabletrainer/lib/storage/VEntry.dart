import 'package:vocabletrainer/storage/CDatabase.dart';
import 'package:vocabletrainer/storage/VList.dart';

class VEntry {
  List<String> meaningsA;
  List<String> meaningsB;
  VList list;
  int id;
  String tip;
  String addition;
  int changed;
  int created;
  VEntry(
      {required this.meaningsA,
      required this.meaningsB,
      required this.list,
      required this.id,
      required this.tip,
      required this.changed,
      required this.created,
      required this.addition});

  VEntry.fromRaw(RawVEntry raw, this.id, int time)
      : meaningsA = raw.meaningsA,
        meaningsB = raw.meaningsB,
        list = raw.list,
        tip = raw.tip,
        addition = raw.addition,
        created = time,
        changed = time;
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
}
