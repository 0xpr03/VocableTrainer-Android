// Copyright 2020 The Flutter team. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';

class BaseScaffold extends StatelessWidget {
  final Widget child;
  final Widget? floatingActionButton;

  const BaseScaffold(
      {super.key, required this.child, this.floatingActionButton});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Lists')),
      drawer: Drawer(
          child: ListView(padding: EdgeInsets.zero, children: [
        const DrawerHeader(
          decoration: BoxDecoration(
            color: Colors.deepPurple,
          ),
          child: Text('Placeholder'),
        ),
        ListTile(
          title: const Text('Logout'),
          onTap: () {
            //Navigator.pushReplacementNamed(context, LogoutWidget.routeName);
          },
          trailing: const Icon(Icons.logout),
        ),
      ])),
      body: SafeArea(child: child),
      floatingActionButton: floatingActionButton,
    );
  }
}
