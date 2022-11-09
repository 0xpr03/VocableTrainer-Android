import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vocabletrainer/storage/StateStorage.dart';
import 'package:vocabletrainer/storage/VEntry.dart';
import 'package:vocabletrainer/storage/VList.dart';

import '../common/scaffold.dart';

class ListViewWidget extends StatefulWidget {
  static const routeName = '/list';

  @override
  ListViewWidgetWidgetState createState() => ListViewWidgetWidgetState();
}

class ListViewWidgetWidgetState extends State<ListViewWidget> {
  VList? _list;
  List<VEntry> entries = [];
  RawVList? _rawList;
  @override
  Widget build(BuildContext context) {
    final args =
        ModalRoute.of(context)!.settings.arguments as ListViewArguments;
    _list = args.list;

    if (_list == null && _rawList == null) {
      _rawList = RawVList(name: 'New List', nameA: 'A Words', nameB: 'B Words');
    }

    return BaseScaffold(
        title: Text(_list?.name ?? _rawList!.name),
        child: ListView.builder(
          itemCount: entries.length,
          itemBuilder: (context, index) {
            var entry = entries[index];
            return Card(
                child: ListTile(
              subtitle: Text(entry.meaningsA.toString()),
            ));
          },
        ));
  }
}

class ListViewArguments {
  final VList? list;
  ListViewArguments(this.list);
}
