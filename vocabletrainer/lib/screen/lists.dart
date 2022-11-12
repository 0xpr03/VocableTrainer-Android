// Copyright 2020 The Flutter team. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

//import 'package:calendar_timeline/calendar_timeline.dart';
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
  final Map<int, bool> _selectedFlag = {};
  bool _selectionMode = false;

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
              if (_selectionMode) {
                setState(() {
                  bool oldVal = _selectedFlag[index] ?? false;
                  _selectedFlag[index] = !oldVal;
                  if (oldVal) {
                    // recalc mode
                    _selectionMode = _selectedFlag.containsValue(true);
                  }
                });
              } else {
                Navigator.of(context).pushNamed(ListViewWidget.routeName,
                    arguments: ListViewArguments(item));
              }
            },
            onLongPress: () {
              if (!_selectionMode) {
                setState(() {
                  _selectionMode = true;
                  _selectedFlag[index] = true;
                });
              }
            },
            leading: _selectionMode
                ? Icon(
                    _selectedFlag[index] ?? false
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
