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
  List<VList> data = [];

  @override
  void dispose() {
    // _tabController?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    var cache = Provider.of<StateStorage>(context);
    cache.getLists().then((value) => setState(() {
          data = value;
        }));

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
        itemCount: data.length,
        itemBuilder: (context, index) {
          VList item = data[index];
          return Card(
              child: ListTile(
            onTap: () => Navigator.of(context).pushNamed(
                ListViewWidget.routeName,
                arguments: ListViewArguments(item)),
            title: Text(item.name),
            subtitle: Text("${item.nameA}/${item.nameB}"),
          ));
        },
      ),
    );
  }
}
