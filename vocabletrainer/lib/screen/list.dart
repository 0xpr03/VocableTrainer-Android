import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vocabletrainer/storage/StateStorage.dart';
import 'package:vocabletrainer/storage/VList.dart';

import '../common/scaffold.dart';

class ListViewWidget extends StatefulWidget {
  static const routeName = '/list';

  @override
  ListViewWidgetWidgetState createState() => ListViewWidgetWidgetState();
}

class ListViewWidgetWidgetState extends State<ListViewWidget> {
  @override
  Widget build(BuildContext context) {
    return BaseScaffold(
      title: const Text("Lists"),
      child: Text("todo"),
    );
  }
}
