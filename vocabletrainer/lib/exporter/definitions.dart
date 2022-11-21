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

CsvCodec createCodec(CSVKind kind) {
  String textDelimiter = '"'; // quotes
  String fieldDelimiter = ','; // delimiter
  String eol = "\r\n";
  switch (kind) {
    case CSVKind.RFC4180: // can't decide on ignore-empty-lines
    case CSVKind.DEFAULT:
    case CSVKind.EXCEL:
      // TODO: ignore blank lines
      break;
    case CSVKind.TABS:
      textDelimiter = '"';
      fieldDelimiter = '\t';
      break;
    case CSVKind.MYSQL:
      // TODO: can't set escape, null, empty-lines
      eol = '\n';
      fieldDelimiter = '\t';
      textDelimiter = '';
      break;
    case CSVKind.INFORMIX_UNLOAD_CSV:
    // TODO: unset escape
    case CSVKind.INFORMIX_UNLOAD:
      // TODO: can't set escape
      textDelimiter = '"';
      fieldDelimiter = ',';
      eol = '\n';
      break;
    case CSVKind.CUSTOM:
      break;
  }
  return CsvCodec(
      eol: eol, fieldDelimiter: fieldDelimiter, textDelimiter: textDelimiter);
}
