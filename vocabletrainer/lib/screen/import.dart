import 'dart:collection';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'package:pick_or_save/pick_or_save.dart';
import 'package:provider/provider.dart';
import 'package:vocabletrainer/common/scaffold.dart';

import '../exporter/definitions.dart';
import '../storage/StateStorage.dart';
import '../storage/VList.dart';

class ImportWidget extends StatefulWidget {
  static const routeName = '/import';

  const ImportWidget({super.key});

  @override
  ListViewWidgetWidgetState createState() => ListViewWidgetWidgetState();
}

class ListViewWidgetWidgetState extends State<ImportWidget>
    with TickerProviderStateMixin {
  Future<void>? _exportFuture;
  List<VList> _lists = [];
  final HashSet<int> _selectedFlag = HashSet();
  late StateStorage _cache;
  late TabController _tabController;
  String? _selectedFile;

  final _fileTextController = TextEditingController();

  CSVKind _csvKindSelected = CSVKind.DEFAULT;

  bool _exportListMetadata = true;
  bool _exportMultipleLists = true;

  @override
  void dispose() {
    _fileTextController.dispose();
    super.dispose();
  }

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
        title: Text("Import from CSV"),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'Settings'),
            Tab(text: 'Preview'),
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
          'Import UTF8 encoded CSV file with (multiple) lists.\nSee wiki for details.'),
      Row(children: [
        Flexible(
            child: TextField(
                readOnly: true,
                controller: _fileTextController,
                decoration: InputDecoration(
                  border: OutlineInputBorder(),
                  hintText: 'Import File',
                ))),
        TextButton(
            onPressed: () async {
              // TODO: handle exceptions
              var ret = await PickOrSave().filePicker(
                  params: FilePickerParams(
                      enableMultipleSelection: false,
                      pickerType: PickerType.file));
              print(ret);
            },
            child: Text('Select File'))
      ]),
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
      ElevatedButton(onPressed: () async {}, child: Text("Import"))
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
