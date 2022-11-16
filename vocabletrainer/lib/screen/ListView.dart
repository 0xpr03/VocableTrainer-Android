import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vocabletrainer/dialog/EntryEditDialog.dart';
import 'package:vocabletrainer/storage/VEntry.dart';
import 'package:vocabletrainer/storage/VList.dart';

import '../common/scaffold.dart';
import '../storage/StateStorage.dart';

class ListViewWidget extends StatefulWidget {
  static const routeName = '/list';

  const ListViewWidget({super.key});

  @override
  ListViewWidgetWidgetState createState() => ListViewWidgetWidgetState();
}

class ListViewWidgetWidgetState extends State<ListViewWidget> {
  late VList _list;
  Future<List<VEntry>>? _entryFuture;

  @override
  void didChangeDependencies() {
    final args =
        ModalRoute.of(context)!.settings.arguments as ListViewArguments;
    _list = args.list;
    super.didChangeDependencies();
  }

  @override
  Widget build(BuildContext context) {
    var cache = Provider.of<StateStorage>(context);
    // _entryFuture ??= cache.getEntries(_list);
    _entryFuture = cache.getEntries(_list);
    return BaseScaffold(
      hideDrawer: true,
      floatingActionButton: FloatingActionButton(
          child: const Icon(Icons.add),
          onPressed: () async {
            var ret = await showDialog<RawVEntry?>(
              context: context,
              barrierDismissible: true,
              builder: (BuildContext context) {
                return EntryEditDialog(
                    entry: RawVEntry(
                        meaningsA: [],
                        meaningsB: [],
                        addition: '',
                        tip: '',
                        list: _list));
              },
            );
            if (mounted && ret != null) {
              await cache.createEntry(ret);
              if (!mounted) return;
              setState(() {
                // reload list
              });
            }
          }),
      title: Text(_list.name),
      child: FutureBuilder(
          future: _entryFuture,
          builder: (context, snapshot) {
            if (snapshot.connectionState == ConnectionState.done) {
              var data = snapshot.data!;
              if (data.isEmpty) {
                return const Center(
                    child: Text('No entries, you can create one.'));
              }
              return ListView.builder(
                itemCount: data.length,
                itemBuilder: (context, index) {
                  var entry = data[index];
                  return Card(
                      child: ListTile(
                    title: Text(entry.meaningsA.join("/"),
                        overflow: TextOverflow.fade, softWrap: false),
                    subtitle: Text(entry.meaningsB.join('/'),
                        overflow: TextOverflow.fade, softWrap: false),
                    onTap: () => _showEdit(entry),
                  ));
                },
              );
            } else {
              return Center(
                  child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: const [
                    CircularProgressIndicator(),
                    Text("Loading Entries")
                  ]));
            }
          }),
    );
  }

  Future<void> _showEdit(VEntry entry) async {
    var ret = await showDialog<VEntry?>(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return EntryEditDialog(entry: entry);
      },
    );
    if (mounted && ret != null) {
      var cache = Provider.of<StateStorage>(context, listen: false);
      await cache.updateEntry(ret);
      if (!mounted) return;
      setState(() {
        // reload list
      });
    }
  }
}

class ListViewArguments {
  final VList list;
  ListViewArguments(this.list);
}
