import 'package:csv/csv.dart';
import 'package:flutter/material.dart';

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

const List<DropdownMenuItem> dropdownItems = const [
  DropdownMenuItem(value: CSVKind.DEFAULT, child: Text("Default")),
  DropdownMenuItem(value: CSVKind.EXCEL, child: Text("Excel")),
  DropdownMenuItem(value: CSVKind.RFC4180, child: Text("RFC 4180")),
  DropdownMenuItem(value: CSVKind.TABS, child: Text("Tabs")),
  DropdownMenuItem(value: CSVKind.MYSQL, child: Text("Mysql")),
  DropdownMenuItem(
      value: CSVKind.INFORMIX_UNLOAD, child: Text("Informix UNLOAD")),
  DropdownMenuItem(
      value: CSVKind.INFORMIX_UNLOAD_CSV, child: Text("Informix UNLOAD CSV")),
  DropdownMenuItem(value: CSVKind.CUSTOM, child: Text("Custom")),
];
