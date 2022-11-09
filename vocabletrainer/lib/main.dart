import 'dart:io';

import 'package:flutter/material.dart';
import 'package:vocabletrainer/screen/list.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:sqflite/sqflite.dart';

import 'common/theme.dart';
import 'screen/lists.dart';

void main() {
  if (Platform.isWindows || Platform.isLinux) {
    // Initialize FFI and use it on Windows and linux
    sqfliteFfiInit();
    databaseFactory = databaseFactoryFfi;
  }
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: appTheme,
      darkTheme: ThemeData.dark(),
      themeMode: ThemeMode.system,
      routes: {
        ListViewWidget.routeName: (context) => ListViewWidget(),
        ListOverviewWidget.routeName: (context) => ListOverviewWidget(),
      },
      initialRoute: ListOverviewWidget.routeName,
    );
  }
}
