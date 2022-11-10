import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vocabletrainer/dialog/ListEditDialog.dart';
import 'package:vocabletrainer/storage/StateStorage.dart';
import 'package:vocabletrainer/storage/VEntry.dart';
import 'package:vocabletrainer/storage/VList.dart';

import '../common/scaffold.dart';

class ListViewWidget extends StatefulWidget {
  static const routeName = '/list';

  const ListViewWidget({super.key});

  @override
  ListViewWidgetWidgetState createState() => ListViewWidgetWidgetState();
}

class ListViewWidgetWidgetState extends State<ListViewWidget> {
  VList? _list;
  List<VEntry> entries = [];
  RawVList? _rawList;

  @override
  void didChangeDependencies() {
    final args =
        ModalRoute.of(context)!.settings.arguments as ListViewArguments;
    _list = args.list;
    if (_list == null && _rawList == null) {
      _rawList = RawVList(name: 'New List', nameA: 'A Words', nameB: 'B Words');
    }

    if (_rawList != null) {
      WidgetsBinding.instance
          .addPostFrameCallback((_) => _showListEditorDialog(_rawList!));
    }
    super.didChangeDependencies();
  }

  @override
  Widget build(BuildContext context) {
    return BaseScaffold(
        hideDrawer: true,
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

  Future<void> _showListEditorDialog(RawVList entry) async {
    bool exitOnCancel = entry.isRaw();
    var ret = await showDialog<VList?>(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return ListEditDialog(list: entry);
      },
    );
    if (!mounted) {
      return;
    }
    if (ret != null) {
      setState(() {
        _rawList = null;
        _list = ret;
      });
    } else if (exitOnCancel) {
      Navigator.of(context).pop();
    }
  }
}

class ListViewArguments {
  final VList? list;
  ListViewArguments(this.list);
}
