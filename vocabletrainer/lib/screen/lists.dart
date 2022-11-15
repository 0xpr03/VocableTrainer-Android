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

  @override
  Widget build(BuildContext context) {
    var cache = Provider.of<StateStorage>(context);
    if (data == null) {
      data = [];
      cache.getLists().then((value) => setState(() {
            data = value;
          }));
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
                    print("Todo: deletion");
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
            Navigator.of(context).pushNamed(ListViewWidget.routeName,
                arguments: ListViewArguments(list));
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
