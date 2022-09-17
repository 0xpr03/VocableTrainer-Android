// Copyright 2020 The Flutter team. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:convert';

//import 'package:calendar_timeline/calendar_timeline.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:tuple/tuple.dart';
import 'package:vocabletrainer/common/scaffold.dart';

import 'list.dart';

class CoursesOverviewWidget extends StatefulWidget {
  static const routeName = '/lists';

  const CoursesOverviewWidget({
    super.key,
  });

  @override
  CoursesOverviewWidgetState createState() => CoursesOverviewWidgetState();
}

class CoursesOverviewWidgetState extends State<CoursesOverviewWidget> {
  // late Future<ClassesFutureValue> _fetchFuture;

  @override
  void initState() {
    super.initState();
    // _resetSelectedDate();
    // _fetchFuture = fetch();
  }

  @override
  void dispose() {
    // _tabController?.dispose();
    super.dispose();
  }

  // Future<ClassesFutureValue> fetch() async {
  //   final state = Provider.of<AccountState>(context, listen: false);
  //   var account = state.account!;
  //   return await fetchClasses(account.accessToken, account.userId,
  //       account.centerId, widget.httpClient!);
  // }

  void refreshData() {
    // reload
    // setState(() {
    //   _fetchFuture = fetch();
    // });
  }

  // void _resetSelectedDate() {
  //   _selectedDate = DateTime.now();
  // }

  // Widget coursesWidget(Map<String, List<Course>> courses) {
  //   final key = keyDateFormat.format(_selectedDate);
  //   final dayContent = courses[key];

  //   if (dayContent != null) {
  //     return courseDayView(dayContent);
  //   } else {
  //     return const Center(child: Text('No courses for this day.'));
  //   }
  // }

  // Widget courseDayView(List<Course> courses) {
  //   return ListView.builder(
  //       physics:
  //           const AlwaysScrollableScrollPhysics(), // allow refresh with 0 elements
  //       itemCount: courses.length,
  //       itemBuilder: (context, index) {
  //         return CourseWidget(
  //           course: courses[index],
  //         );
  //       });
  // }

  @override
  Widget build(BuildContext context) {
    //var state = context.watch<AccountState>();

    return BaseScaffold(
      child: ListView.builder(
        itemBuilder: (context, index) {
          return Text("TODO");
        },
      ),
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.add),
        onPressed: () {
          Navigator.of(context).pushNamed(ListViewWidget.routeName);
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
