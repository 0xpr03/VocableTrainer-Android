import 'dart:io';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:sqflite/sqflite.dart';
import 'package:vocabletrainer/screen/export.dart';
import 'package:vocabletrainer/screen/import.dart';

import 'common/theme.dart';
import 'screen/ListView.dart';
import 'screen/lists.dart';
import 'storage/StateStorage.dart';

Future<void> main() async {
  runApp(const LoadingScreen());
  if (Platform.isWindows || Platform.isLinux) {
    // Initialize FFI and use it on Windows and linux
    sqfliteFfiInit();
    databaseFactory = databaseFactoryFfi;
  }
  var storage = StateStorage();
  await storage.initDb();
  runApp(MyApp(storage));
}

class LoadingScreen extends StatelessWidget {
  const LoadingScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        theme: appTheme,
        darkTheme: ThemeData.dark(),
        themeMode: ThemeMode.system,
        home: Scaffold(
            body: Center(
                child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: const [
              CircularProgressIndicator(),
              Text("Loading DB")
            ]))));
  }
}

class MyApp extends StatelessWidget {
  final StateStorage storage;
  const MyApp(this.storage, {super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
        providers: [
          ChangeNotifierProvider<StateStorage>(
            create: (context) => storage,
          )
        ],
        child: MaterialApp(
          title: 'VTA',
          theme: appTheme,
          darkTheme: ThemeData.dark(),
          themeMode: ThemeMode.system,
          routes: {
            ListViewWidget.routeName: (context) => ListViewWidget(),
            ListOverviewWidget.routeName: (context) => ListOverviewWidget(),
            ExportWidget.routeName: (context) => ExportWidget(),
            ImportWidget.routeName: (context) => ImportWidget(),
          },
          initialRoute: ListOverviewWidget.routeName,
        ));
  }
}

// class MyApp extends StatelessWidget {
//   const MyApp({super.key});

//   // This widget is the root of your application.
//   @override
//   Widget build(BuildContext context) {
   
//   }
// }
