import 'package:csv/csv.dart';

enum CSVKind {
  DEFAULT,
  EXCEL,
  RFC4180,
  TABS,
  MYSQL,
  INFORMIX_UNLOAD,
  INFORMIX_UNLOAD_CSV,
  CUSTOM
}

void createConverter(CSVKind kind) {
  String textDelimiter = '"';
  String fieldDelimiter = ',';
  String eol = "\r\n";
  switch (kind) {
    case CSVKind.DEFAULT:
      break;
    case CSVKind.EXCEL:
      break;
    case CSVKind.RFC4180:
      // TODO: Handle this case.
      break;
    case CSVKind.TABS:
      // TODO: Handle this case.
      break;
    case CSVKind.MYSQL:
      // TODO: Handle this case.
      break;
    case CSVKind.INFORMIX_UNLOAD:
      // TODO: Handle this case.
      break;
    case CSVKind.INFORMIX_UNLOAD_CSV:
      // TODO: Handle this case.
      break;
    case CSVKind.CUSTOM:
      // TODO: Handle this case.
      break;
  }
}
