import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:vocabletrainer/common/scaffold.dart';

import '../exporter/definitions.dart';
import '../storage/StateStorage.dart';
import '../storage/VList.dart';

class ExportWidget extends StatefulWidget {
  static const routeName = '/export';

  const ExportWidget({super.key});

  @override
  ListViewWidgetWidgetState createState() => ListViewWidgetWidgetState();
}

class ListViewWidgetWidgetState extends State<ExportWidget>
    with TickerProviderStateMixin {
  Future<void>? _exportFuture;
  List<VList> _lists = [];
  final HashSet<int> _selectedFlag = HashSet();
  late StateStorage _cache;
  late TabController _tabController;
  static List<DropdownMenuItem> dropdownItems = const [
    DropdownMenuItem(value: CSVKind.DEFAULT, child: Text("Default")),
    DropdownMenuItem(value: CSVKind.EXCEL, child: Text("Excel")),
    DropdownMenuItem(value: CSVKind.RFC4180, child: Text("RFC 4180")),
    DropdownMenuItem(value: CSVKind.TABS, child: Text("Tabs")),
    DropdownMenuItem(value: CSVKind.MYSQL, child: Text("Mysql")),
    DropdownMenuItem(
        value: CSVKind.INFORMIX_UNLOAD, child: Text("Informix UNLOAD")),
    DropdownMenuItem(
        value: CSVKind.INFORMIX_UNLOAD_CSV, child: Text("Informix UNLOAD CSV")),
    DropdownMenuItem(value: CSVKind.CUSTOM, child: Text("Custom")),
  ];
  CSVKind _csvKindSelected = CSVKind.DEFAULT;

  bool _exportListMetadata = true;
  bool _exportMultipleLists = true;

  @override
  void initState() {
    _tabController = TabController(
      initialIndex: 0,
      length: 3,
      vsync: this,
    );
    super.initState();
  }

  @override
  void didChangeDependencies() {
    _cache = Provider.of<StateStorage>(context);
    _cache.getLists().then((value) {
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
          controller: _tabController,
          tabs: const [
            Tab(text: 'Settings'),
            Tab(text: 'Lists'),
            Tab(text: 'Format'),
          ],
        ),
      ),
      child: TabBarView(
        controller: _tabController,
        children: [
          _settingTab(),
          _listTab(),
          Icon(Icons.construction),
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
        value: _csvKindSelected,
        borderRadius: BorderRadius.circular(2.0),
        icon: const Icon(Icons.arrow_drop_down),
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
          onPressed: () async {
            if (_selectedFlag.isEmpty) {
              showSnackbar('No lists selected for export!');
              return;
            }
            List<VList> lists = [];
            for (var list in _lists) {
              if (_selectedFlag.contains(list.id)) {
                lists.add(list);
              }
            }
            var exporter = createCodec(_csvKindSelected);
            StringBuffer sb = StringBuffer();
            for (var list in lists) {
              var entries = await _cache.getEntries(list);
              if (_exportListMetadata) {
                exporter.encoder.convertSingleRow(
                    sb, ['TABLE\\', '//INFO', '//START//'],
                    returnString: false);
                sb.write(exporter.encoder.eol);
                exporter.encoder.convertSingleRow(
                    sb, [list.name, list.nameA, list.nameB],
                    returnString: false);
                sb.write(exporter.encoder.eol);
              }
              for (var entry in entries) {
                exporter.encoder.convertSingleRow(sb, entry.asCSVRow(),
                    returnString: false);
                sb.write(exporter.encoder.eol);
              }
            }
            print(sb);
          },
          child: Text("Export"))
    ]));
  }

  void showSnackbar(String text) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        action: SnackBarAction(
          label: 'Dismiss',
          onPressed: () {
            // do nothing
          },
        ),
        content: Text(text),
        behavior: SnackBarBehavior.fixed,
      ),
    );
  }

  Widget _listTab() {
    return ListView.builder(
        itemCount: _lists.length,
        itemBuilder: (context, index) {
          var list = _lists[index];
          return Card(
              child: ListTile(
                  leading: Icon(
                    _selectedFlag.contains(list.id)
                        ? Icons.check_box
                        : Icons.check_box_outline_blank,
                  ),
                  title: Text(list.name),
                  onTap: () {
                    setState(() {
                      if (!_selectedFlag.remove(list.id)) {
                        _selectedFlag.add(list.id);
                      }
                    });
                  }));
        });
  }
}
