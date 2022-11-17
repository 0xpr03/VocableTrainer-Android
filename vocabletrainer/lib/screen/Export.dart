import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vocabletrainer/common/scaffold.dart';

import '../storage/StateStorage.dart';
import '../storage/VList.dart';

class ExportWidget extends StatefulWidget {
  static const routeName = '/export';

  const ExportWidget({super.key});

  @override
  ListViewWidgetWidgetState createState() => ListViewWidgetWidgetState();
}

enum CSVKind {
  DEFAULT,
  EXCEL,
  RFC4180,
  TABS,
  MYSQL,
  INFORMIX_UNLOAD,
  INFORMIX_UNLOAD_CSV,
  CUSTOM
}

class ListViewWidgetWidgetState extends State<ExportWidget>
    with TickerProviderStateMixin {
  Future<void>? _exportFuture;
  List<VList> _lists = [];
  final HashSet<int> _selectedFlag = HashSet();
  late StateStorage cache;
  late TabController tabController;
  static List<DropdownMenuItem> dropdownItems = [
    DropdownMenuItem(child: Text("Default"), value: CSVKind.DEFAULT),
    DropdownMenuItem(child: Text("Excel"), value: CSVKind.EXCEL),
    DropdownMenuItem(child: Text("RFC 4180"), value: CSVKind.RFC4180),
    DropdownMenuItem(child: Text("Tabs"), value: CSVKind.TABS),
    DropdownMenuItem(child: Text("Mysql"), value: CSVKind.MYSQL),
    DropdownMenuItem(
        child: Text("Informix UNLOAD"), value: CSVKind.INFORMIX_UNLOAD),
    DropdownMenuItem(
        child: Text("Informix UNLOAD CSV"), value: CSVKind.INFORMIX_UNLOAD_CSV),
    DropdownMenuItem(child: Text("Custom"), value: CSVKind.CUSTOM),
  ];
  CSVKind _csvKindSelected = CSVKind.DEFAULT;

  bool _exportListMetadata = true;
  bool _exportMultipleLists = true;

  @override
  void initState() {
    tabController = TabController(
      initialIndex: 0,
      length: 3,
      vsync: this,
    );
    super.initState();
  }

  @override
  void didChangeDependencies() {
    cache = Provider.of<StateStorage>(context);
    cache.getLists().then((value) {
      setState(() {
        _lists = value;
      });
    });
    super.didChangeDependencies();
  }

  @override
  Widget build(BuildContext context) {
    return BaseScaffold(
      appbar: AppBar(
        title: Text("Export to CSV"),
        bottom: TabBar(
          controller: tabController,
          tabs: const [
            Tab(text: 'Settings'),
            Tab(text: 'Lists'),
            Tab(text: 'Format'),
          ],
        ),
      ),
      child: TabBarView(
        controller: tabController,
        children: [
          _settingTab(),
          _listTab(),
          Icon(Icons.directions_bike),
        ],
      ),
    );
  }

  Widget _settingTab() {
    return SingleChildScrollView(
        child: Column(children: [
      Text(
          'Export UTF8 encoded CSV file with (multiple) lists.\nSee wiki for more information.'),
      DropdownButton(
        items: dropdownItems,
        hint: Text("Hint"),
        value: _csvKindSelected,
        borderRadius: BorderRadius.circular(2.0),
        icon: const Icon(Icons.arrow_downward),
        onChanged: (value) {
          setState(() {
            _csvKindSelected = value;
          });
        },
      ),
      CheckboxListTile(
          value: _exportListMetadata,
          title: Text("Export Metadata (names)"),
          onChanged: (value) {
            setState(() {
              _exportListMetadata = !_exportListMetadata;
            });
          }),
      ElevatedButton(
          onPressed: () {
            print("todo");
          },
          child: Text("Export"))
    ]));
  }

  Widget _listTab() {
    return ListView.builder(
        itemCount: _lists.length,
        itemBuilder: (context, index) {
          var list = _lists[index];
          return Card(child: ListTile(title: Text(list.name)));
        });
  }
}
