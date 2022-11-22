// Copyright 2020 The Flutter team. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:vocabletrainer/screen/export.dart';
import 'package:vocabletrainer/screen/import.dart';
import 'package:vocabletrainer/screen/lists.dart';

class BaseScaffold extends StatelessWidget {
  final Widget child;
  final Widget? floatingActionButton;

  /// If no appbar is set, use this for a title
  final Widget? title;

  /// Appbar to use, title will be ignored if this is set
  final AppBar? appbar;
  final bool hideDrawer;

  const BaseScaffold(
      {super.key,
      required this.child,
      this.floatingActionButton,
      this.title,
      this.hideDrawer = false,
      this.appbar});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: appbar ?? AppBar(title: title),
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
                title: const Text('Export'),
                onTap: () {
                  Navigator.pushReplacementNamed(
                      context, ExportWidget.routeName);
                },
                leading: const Icon(Icons.upload),
              ),
              ListTile(
                title: const Text('Import'),
                onTap: () {
                  Navigator.pushReplacementNamed(
                      context, ImportWidget.routeName);
                },
                leading: const Icon(Icons.download),
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
