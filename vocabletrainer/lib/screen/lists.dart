// Copyright 2020 The Flutter team. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:convert';

//import 'package:calendar_timeline/calendar_timeline.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:http/http.dart' as http;
import 'package:table_calendar/table_calendar.dart';
import 'package:tuple/tuple.dart';
import 'package:intl/intl.dart';

class CoursesOverviewWidget extends StatefulWidget {
  final http.Client? httpClient;

  static const routeName = '/lists';

  const CoursesOverviewWidget({
    this.httpClient,
    super.key,
  });

  @override
  CoursesOverviewWidgetState createState() => CoursesOverviewWidgetState();
}

class CoursesOverviewWidgetState extends State<CoursesOverviewWidget>
    with TickerProviderStateMixin {
  late Future<ClassesFutureValue> _fetchFuture;
  TabController? _tabController;
  late DateTime _selectedDate;
  final yourScrollController = ScrollController();
  final DateFormat keyDateFormat = DateFormat("yyyy'-'MM'-'dd");

  @override
  void initState() {
    super.initState();
    _resetSelectedDate();
    _fetchFuture = fetch();
  }

  @override
  void dispose() {
    _tabController?.dispose();
    super.dispose();
  }

  Future<ClassesFutureValue> fetch() async {
    final state = Provider.of<AccountState>(context, listen: false);
    var account = state.account!;
    return await fetchClasses(account.accessToken, account.userId,
        account.centerId, widget.httpClient!);
  }

  void refreshData() {
    // reload
    setState(() {
      _fetchFuture = fetch();
    });
  }

  void _resetSelectedDate() {
    _selectedDate = DateTime.now();
  }

  Widget coursesWidget(Map<String, List<Course>> courses) {
    final key = keyDateFormat.format(_selectedDate);
    final dayContent = courses[key];

    if (dayContent != null) {
      return courseDayView(dayContent);
    } else {
      return const Center(child: Text('No courses for this day.'));
    }
  }

  Widget courseDayView(List<Course> courses) {
    return ListView.builder(
        physics:
            const AlwaysScrollableScrollPhysics(), // allow refresh with 0 elements
        itemCount: courses.length,
        itemBuilder: (context, index) {
          return CourseWidget(
            course: courses[index],
          );
        });
  }

  @override
  Widget build(BuildContext context) {
    var state = context.watch<AccountState>();
    var courses = context.watch<Classes>();

    return Scaffold(
      appBar: AppBar(title: const Text('Home')),
      drawer: Drawer(
          child: ListView(padding: EdgeInsets.zero, children: [
        DrawerHeader(
          decoration: const BoxDecoration(
            color: Colors.deepPurple,
          ),
          child: Text('${state.account!.firstName} ${state.account!.lastName}'),
        ),
        ListTile(
          title: const Text('Logout'),
          onTap: () {
            Navigator.pushReplacementNamed(context, LogoutWidget.routeName);
          },
          trailing: const Icon(Icons.logout),
        ),
      ])),
      body: SafeArea(
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        TableCalendar(
          firstDay: DateTime.now().subtract(const Duration(days: 30)),
          lastDay: DateTime.now().add(const Duration(days: 30)),
          focusedDay: _selectedDate,
          selectedDayPredicate: (day) {
            return isSameDay(_selectedDate, day);
          },
          availableCalendarFormats: const {CalendarFormat.week: 'week'},
          calendarFormat: CalendarFormat.week,
          headerVisible: false,
          availableGestures: AvailableGestures.horizontalSwipe,
          rangeSelectionMode: RangeSelectionMode.disabled,
          onDaySelected: (selectedDay, focusedDay) =>
              setState(() => _selectedDate = selectedDay),
        ),
        Expanded(
            child: FutureBuilder<ClassesFutureValue>(
          future: _fetchFuture,
          builder: (context, snapshot) {
            if (snapshot.hasError) {
              if (snapshot.error! is ApiLoginInvalidException) {
                WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
                  Navigator.of(context).pushNamed(ReLoginWidget.routeName);
                });
              }
              return const Center(
                child: Text('An error has occurred loading courses!'),
              );
            } else if (snapshot.hasData) {
              final data = snapshot.data!;
              courses.update(data.item2['from'], data.item2['to'], data.item1);
              return RefreshIndicator(
                child: coursesWidget(data.item1!),
                onRefresh: () {
                  refreshData();
                  return _fetchFuture;
                },
              );
            } else {
              return const Center(
                child: CircularProgressIndicator(),
              );
            }
          },
        ))
      ])),
      //
    );
  }
}

class CourseWidget extends StatelessWidget {
  final Course course;
  const CourseWidget({required this.course, super.key});

  @override
  Widget build(BuildContext context) {
    Color color;
    Null Function()? onTap;
    switch (course.bookingState) {
      case 'BOOKABLE':
      case 'BOOKABLE_WAITINGLIST':
      case 'BOOKED':
        color = Colors.black; // TODO: don't hardcode colors
        onTap = () {
          Navigator.pushNamed(
            context,
            CourseDetailWidget.routeName,
            arguments: ScreenArguments(course),
          );
        };
        break;
      default:
        onTap = null;
        color = Colors.grey;
    }
    return Card(
        child: ListTile(
      title: Text(course.name),
      subtitle: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text(course.bookingState),
        Text('${course.bookedCount} / ${course.classCapacity}'),
      ]),
      // trailing: Text(course.bookingState),
      leading: Text(course.startTime),
      textColor: color,
      onTap: onTap,
    ));
  }
}
