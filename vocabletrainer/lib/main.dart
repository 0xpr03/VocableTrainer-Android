import 'package:flutter/material.dart';
import 'package:vocabletrainer/screen/list.dart';

import 'common/theme.dart';
import 'screen/lists.dart';

void main() {
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
