// Copyright 2020 The Flutter team. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

//import 'package:calendar_timeline/calendar_timeline.dart';
import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vocabletrainer/common/scaffold.dart';

import '../dialog/ListEditDialog.dart';
import '../storage/StateStorage.dart';
import '../storage/VList.dart';
import 'ListView.dart';

class ListOverviewWidget extends StatefulWidget {
  static const routeName = '/lists';

  const ListOverviewWidget({
    super.key,
  });

  @override
  ListOverviewWidgetState createState() => ListOverviewWidgetState();
}

class ListOverviewWidgetState extends State<ListOverviewWidget> {
  List<VList>? data;
  final HashSet<int> _selectedFlag = HashSet();

  @override
  void dispose() {
    // _tabController?.dispose();
    super.dispose();
  }

  void _refreshLists(StateStorage cache, bool resetSelection) {
    cache.getLists().then((value) => setState(() {
          data = value;
          if (resetSelection) {
            _selectedFlag.clear();
          }
        }));
  }

  @override
  Widget build(BuildContext context) {
    var cache = Provider.of<StateStorage>(context);
    if (data == null) {
      data = [];
      _refreshLists(cache, false);
    }

    return BaseScaffold(
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
                  tooltip: "Delete selected lists",
                  onPressed: () async {
                    var ret = await showDialog<bool>(
                        context: context,
                        barrierDismissible: true,
                        builder: (BuildContext context) {
                          return AlertDialog(
                            title: const Text('Confirm list deletion'),
                            content: SingleChildScrollView(
                                child: Text(
                                    'Do you want to delete ${_selectedFlag.length} lists?')),
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
                      List<VList> delLists = [];
                      for (var list in data!) {
                        if (_selectedFlag.contains(list.id)) {
                          delLists.add(list);
                        }
                      }
                      await cache.deleteLists(delLists);
                      _refreshLists(cache, true);
                    }
                  },
                )
              ],
            )
          : null,
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.add),
        onPressed: () async {
          var ret = await showDialog<RawVList?>(
            context: context,
            barrierDismissible: true,
            builder: (BuildContext context) {
              return ListEditDialog(
                  list: RawVList(name: '', nameA: '', nameB: ''));
            },
          );
          if (mounted && ret != null) {
            VList list = await cache.createList(ret);
            if (!mounted) return;
            await Navigator.of(context).pushNamed(ListViewWidget.routeName,
                arguments: ListViewArguments(list));
            if (!mounted) return;
            _refreshLists(cache, false);
          }
        },
      ),
      title: const Text("Lists"),
      child: ListView.builder(
        itemCount: data!.length,
        itemBuilder: (context, index) {
          VList item = data![index];
          return Card(
              child: ListTile(
            onTap: () {
              if (_selectedFlag.isNotEmpty) {
                setState(() {
                  bool oldVal = _selectedFlag.contains(item.id);
                  if (oldVal) {
                    _selectedFlag.remove(item.id);
                  } else {
                    _selectedFlag.add(item.id);
                  }
                });
              } else {
                Navigator.of(context).pushNamed(ListViewWidget.routeName,
                    arguments: ListViewArguments(item));
              }
            },
            onLongPress: () {
              if (_selectedFlag.isEmpty) {
                setState(() {
                  _selectedFlag.add(item.id);
                });
              }
            },
            leading: _selectedFlag.isNotEmpty
                ? Icon(
                    _selectedFlag.contains(item.id)
                        ? Icons.check_box
                        : Icons.check_box_outline_blank,
                  )
                : null,
            title: Text(item.name),
            subtitle: Text("${item.nameA}/${item.nameB}"),
          ));
        },
      ),
    );
  }
}
