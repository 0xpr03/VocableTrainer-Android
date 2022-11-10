// Copyright 2020 The Flutter team. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:vocabletrainer/screen/lists.dart';

class BaseScaffold extends StatelessWidget {
  final Widget child;
  final Widget? floatingActionButton;
  final Widget title;
  final bool hideDrawer;

  const BaseScaffold(
      {super.key,
      required this.child,
      this.floatingActionButton,
      required this.title,
      this.hideDrawer = false});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: title),
      drawer: hideDrawer
          ? null
          : Drawer(
              child: ListView(padding: EdgeInsets.zero, children: [
              DrawerHeader(
                decoration:
                    BoxDecoration(color: Theme.of(context).primaryColor),
                child: Text('Placeholder'),
              ),
              ListTile(
                title: const Text('Lists'),
                onTap: () {
                  Navigator.pushReplacementNamed(
                      context, ListOverviewWidget.routeName);
                },
                leading: const Icon(Icons.list),
              ),
              ListTile(
                title: const Text('Logout'),
                onTap: () {
                  //Navigator.pushReplacementNamed(context, LogoutWidget.routeName);
                },
                leading: const Icon(Icons.logout),
              ),
            ])),
      body: SafeArea(child: child),
      floatingActionButton: floatingActionButton,
    );
  }
}
