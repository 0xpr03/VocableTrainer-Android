import 'package:flutter/material.dart';

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
      child: ListView.builder(
        itemBuilder: (context, index) {
          return Text("list");
        },
      ),
      title: Text("list"),
    );
  }
}
