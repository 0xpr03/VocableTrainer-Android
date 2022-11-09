// Copyright 2020 The Flutter team. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:convert';

//import 'package:calendar_timeline/calendar_timeline.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:tuple/tuple.dart';
import 'package:vocabletrainer/common/scaffold.dart';

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
    var cache = Provider.of<StateStorage>(context); //Zugriff auf Uebungsliste
    cache.getLists().then((value) => setState(() {
          data = value;
        }));

    return BaseScaffold(
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.add),
        onPressed: () {
          Navigator.of(context).pushNamed(ListViewWidget.routeName,
              arguments: ListViewArguments(null));
        },
      ),
      title: const Text("Lists"),
      child: ListView.builder(
        itemCount: data.length,
        itemBuilder: (context, index) {
          VList item = data[index];
          return Card(
              child: ListTile(
            title: Text(item.name),
            subtitle: Text("${item.nameA}/${item.nameB}"),
          ));
        },
      ),
    );
  }
}

// class CourseWidget extends StatelessWidget {
//   final Course course;
//   const CourseWidget({required this.course, super.key});

//   @override
//   Widget build(BuildContext context) {
//     Color color;
//     Null Function()? onTap;
//     switch (course.bookingState) {
//       case 'BOOKABLE':
//       case 'BOOKABLE_WAITINGLIST':
//       case 'BOOKED':
//         color = Colors.black; // TODO: don't hardcode colors
//         onTap = () {
//           Navigator.pushNamed(
//             context,
//             CourseDetailWidget.routeName,
//             arguments: ScreenArguments(course),
//           );
//         };
//         break;
//       default:
//         onTap = null;
//         color = Colors.grey;
//     }
//     return Card(
//         child: ListTile(
//       title: Text(course.name),
//       subtitle: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
//         Text(course.bookingState),
//         Text('${course.bookedCount} / ${course.classCapacity}'),
//       ]),
//       // trailing: Text(course.bookingState),
//       leading: Text(course.startTime),
//       textColor: color,
//       onTap: onTap,
//     ));
//   }
// }
