import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vocabletrainer/dialog/EntryEditDialog.dart';
import 'package:vocabletrainer/storage/VEntry.dart';
import 'package:vocabletrainer/storage/VList.dart';

import '../common/scaffold.dart';
import '../dialog/ListEditDialog.dart';
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
  List<VEntry> _entries = [];
  final HashSet<int> _selectedFlag = HashSet();
  late StateStorage cache;

  @override
  void didChangeDependencies() {
    final args =
        ModalRoute.of(context)!.settings.arguments as ListViewArguments;
    _list = args.list;
    cache = Provider.of<StateStorage>(context);
    _entryFuture = cache.getEntries(_list);
    _selectedFlag.clear();

    super.didChangeDependencies();
  }

  @override
  Widget build(BuildContext context) {
    return BaseScaffold(
      hideDrawer: true,
      appbar: _selectedFlag.isNotEmpty
          ? AppBar(
              leading: IconButton(
                icon: const Icon(Icons.cancel),
                tooltip: "Cancel selection",
                onPressed: () {
                  setState(() {
                    _selectedFlag.clear();
                  });
                },
              ),
              title: Text(_selectedFlag.length.toString()),
              actions: [
                IconButton(
                  icon: const Icon(Icons.delete),
                  tooltip: "Delete selected entries",
                  onPressed: () async {
                    var ret = await showDialog<bool>(
                        context: context,
                        barrierDismissible: true,
                        builder: (BuildContext context) {
                          return AlertDialog(
                            title: const Text('Confirm entry deletion'),
                            content: SingleChildScrollView(
                                child: Text(
                                    'Do you want to delete ${_selectedFlag.length} entries?')),
                            actions: [
                              TextButton(
                                autofocus: true,
                                child: const Text('Cancel'),
                                onPressed: () {
                                  Navigator.of(context).pop();
                                },
                              ),
                              TextButton(
                                autofocus: false,
                                onPressed: () {
                                  Navigator.of(context).pop(true);
                                },
                                child: const Text('Delete'),
                              ),
                            ],
                          );
                        });
                    if (ret != null && ret == true) {
                      List<VEntry> delEntries = [];
                      for (var list in _entries) {
                        if (_selectedFlag.contains(list.id)) {
                          delEntries.add(list);
                        }
                      }
                      await cache.deleteEntries(delEntries);
                    }
                  },
                )
              ],
            )
          : null,
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
            }
          }),
      title: ListTile(
          title: Text(_list.name),
          subtitle: Text('Tap to change'),
          onTap: () async {
            var ret = await showDialog<VList?>(
              context: context,
              barrierDismissible: true,
              builder: (BuildContext context) {
                return ListEditDialog(list: _list);
              },
            );
            if (mounted && ret != null) {
              await cache.updateList(ret);
            }
          }),
      child: FutureBuilder(
          future: _entryFuture,
          builder: (context, snapshot) {
            if (snapshot.connectionState == ConnectionState.done) {
              var data = snapshot.data!;
              _entries = data;
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
                    leading: _selectedFlag.isNotEmpty
                        ? Icon(
                            _selectedFlag.contains(entry.id)
                                ? Icons.check_box
                                : Icons.check_box_outline_blank,
                          )
                        : null,
                    title: Text(entry.meaningsA.join("/"),
                        overflow: TextOverflow.fade, softWrap: false),
                    subtitle: Text(entry.meaningsB.join('/'),
                        overflow: TextOverflow.fade, softWrap: false),
                    onTap: () {
                      if (_selectedFlag.isEmpty) {
                        _showEdit(entry);
                      } else {
                        setState(() {
                          if (!_selectedFlag.remove(entry.id)) {
                            _selectedFlag.add(entry.id);
                          }
                        });
                      }
                    },
                    onLongPress: () {
                      if (_selectedFlag.isEmpty) {
                        setState(() {
                          _selectedFlag.add(entry.id);
                        });
                      }
                    },
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
