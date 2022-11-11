import 'package:flutter/material.dart';
import 'package:vocabletrainer/storage/VEntry.dart';

class EntryEditDialog extends StatefulWidget {
  /// Attriute, if set for editing
  final RawVEntry entry;

  const EntryEditDialog({super.key, required this.entry});

  @override
  State<EntryEditDialog> createState() => _EntryEditDialogState();
}

class _EntryEditDialogState extends State<EntryEditDialog> {
  final _formAddAttributeKey = GlobalKey<FormState>();
  late String _tip;
  late String _addition;
  late List<String> _valuesA;
  late List<String> _valuesB;

  @override
  void initState() {
    _tip = widget.entry.tip;
    _addition = widget.entry.addition;
    _valuesA = widget.entry.meaningsA;
    _valuesB = widget.entry.meaningsB;
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    var formChilds = [
      TextFormField(
        autofocus: false,
        initialValue: _addition,
        decoration: const InputDecoration(
            helperText: 'Additions (tenses)', hintText: 'Addition'),
        onChanged: (value) {
          _addition = value;
        },
      ),
      TextFormField(
        autofocus: false,
        initialValue: _tip,
        decoration: const InputDecoration(hintText: 'Tip', helperText: 'Tip'),
        onChanged: (value) {
          _tip = value;
        },
      )
    ];
    for (var i = 0; i < _valuesA.length; i++) {
      formChilds.add(TextFormField(
        autofocus: false,
        initialValue: _valuesA[i],
        decoration: InputDecoration(
            hintText: widget.entry.list.nameA, helperText: 'Word'),
        onChanged: (value) {
          _valuesA[i] = value;
        },
      ));
    }
    for (var i = 0; i < _valuesB.length; i++) {
      formChilds.add(TextFormField(
        autofocus: false,
        initialValue: _valuesB[i],
        decoration: InputDecoration(
            hintText: widget.entry.list.nameA, helperText: 'Word'),
        onChanged: (value) {
          _valuesB[i] = value;
        },
      ));
    }
    return AlertDialog(
      title: Text(widget.entry.isRaw() ? 'Add' : 'Save'),
      content: SingleChildScrollView(
        child: Form(
            autovalidateMode: AutovalidateMode.onUserInteraction,
            key: _formAddAttributeKey,
            child: ListBody(
              children: formChilds,
            )),
      ),
      actions: [
        TextButton(
          child: const Text('Cancel'),
          onPressed: () {
            Navigator.of(context).pop();
          },
        ),
        TextButton(
          child: Text(widget.entry.isRaw() ? 'Create' : 'Save'),
          onPressed: () async {
            if (_formAddAttributeKey.currentState!.validate()) {
              widget.entry.tip = _tip;
              widget.entry.addition = _addition;
              widget.entry.meaningsA = _valuesA;
              widget.entry.meaningsB = _valuesB;
              Navigator.of(context).pop(widget.entry);
            }
          },
        ),
      ],
    );
  }
}
