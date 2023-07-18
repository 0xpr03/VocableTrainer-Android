import 'package:flutter/material.dart';
import 'package:vocabletrainer/storage/VList.dart';

class ListEditDialog extends StatefulWidget {
  /// Attriute, if set for editing
  final RawVList list;

  const ListEditDialog({super.key, required this.list});

  @override
  State<ListEditDialog> createState() => _ListEditDialogState();
}

class _ListEditDialogState extends State<ListEditDialog> {
  final _formAddAttributeKey = GlobalKey<FormState>();
  String _listName = '';
  String _nameA = '';
  String _nameB = '';

  @override
  Widget build(BuildContext context) {
    _listName = widget.list.name;
    _nameA = widget.list.nameA;
    _nameB = widget.list.nameB;
    return AlertDialog(
      title: Text(widget.list.isRaw()
          ? 'Create List'
          : 'Edit List ${widget.list.name}'),
      content: SingleChildScrollView(
        child: Form(
            autovalidateMode: AutovalidateMode.onUserInteraction,
            key: _formAddAttributeKey,
            child: ListBody(
              children: [
                TextFormField(
                  autofocus: true,
                  initialValue: _listName,
                  decoration: const InputDecoration(
                      helperText: 'List Name', hintText: 'List Name'),
                  onChanged: (value) {
                    _listName = value;
                  },
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Please enter a name.';
                    }
                    return null;
                  },
                ),
                TextFormField(
                  autofocus: false,
                  initialValue: _nameA,
                  decoration: const InputDecoration(
                      hintText: 'A Words', helperText: 'Column Name'),
                  onChanged: (value) {
                    _nameA = value;
                  },
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Please enter a column name.';
                    }
                    return null;
                  },
                ),
                TextFormField(
                  autofocus: false,
                  initialValue: _nameB,
                  decoration: const InputDecoration(
                      hintText: 'B Words', helperText: 'Column Name'),
                  onChanged: (value) {
                    _nameB = value;
                  },
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Please enter a column name.';
                    }
                    return null;
                  },
                ),
              ],
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
          child: Text(widget.list.isRaw() ? 'Create' : 'Save'),
          onPressed: () async {
            if (_formAddAttributeKey.currentState!.validate()) {
              widget.list.name = _listName;
              widget.list.nameA = _nameA;
              widget.list.nameB = _nameB;
              Navigator.of(context).pop(widget.list);
            }
          },
        ),
      ],
    );
  }
}
