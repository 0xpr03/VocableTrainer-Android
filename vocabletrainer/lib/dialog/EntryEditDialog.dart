import 'package:flutter/material.dart';
import 'package:vocabletrainer/storage/VEntry.dart';

class EntryEditDialog extends StatefulWidget {
  /// Attriute, if set for editing
  final RawVEntry entry;

  const EntryEditDialog({super.key, required this.entry});

  @override
  State<EntryEditDialog> createState() => _EntryEditDialogState();
}

class FocusElement {
  int focusId;
  bool focusA;
  FocusElement(this.focusId, this.focusA);

  bool isElement(bool focusA, int id) {
    return this.focusA == focusA && this.focusId == id;
  }
}

class _EntryEditDialogState extends State<EntryEditDialog> {
  final _formAddAttributeKey = GlobalKey<FormState>();
  late String _tip;
  late String _addition;
  late List<String> _valuesA;
  late List<String> _valuesB;
  FocusElement? _currentFocus;

  @override
  void initState() {
    _tip = widget.entry.tip;
    _addition = widget.entry.addition;
    _valuesA = widget.entry.meaningsA.toList(); // copy
    _valuesB = widget.entry.meaningsB.toList();
    super.initState();
  }

  Widget _renderMeaningButton(
      List<String> values, bool focusA, bool add, int i) {
    return InkWell(
      autofocus: false,
      onTap: () {
        setState(() {
          if (add) {
            // add new text-fields at the top of all friends textfields
            values.insert(i, '');
            _currentFocus = FocusElement(i, focusA);
          } else {
            values.removeAt(i);
          }
        });
      },
      child: Container(
        width: 30,
        height: 30,
        decoration: BoxDecoration(
          //color: (add) ? Colors.green : Colors.red,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Icon(
          (add) ? Icons.add : Icons.remove,
          color: Colors.white,
        ),
      ),
    );
  }

  Widget renderEntry(List<String> values, bool focusA, int i, String hint) {
    //TODO: autofocus doesn't work
    bool autofocus = _currentFocus?.isElement(focusA, i) ?? false;
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        children: [
          Expanded(
              child: TextFormField(
            autofocus: autofocus,
            initialValue: values[i],
            decoration: InputDecoration(hintText: 'Word', helperText: hint),
            onChanged: (value) {
              values[i] = value;
            },
          )),
          const SizedBox(
            width: 16,
          ),
          _renderMeaningButton(values, focusA, i == values.length - 1, i),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    List<Widget> formChilds = [];
    if (_valuesA.isEmpty) {
      _valuesA.add('');
    }
    if (_valuesB.isEmpty) {
      _valuesB.add('');
    }
    print(_valuesA);
    print(_valuesB);
    for (var i = 0; i < _valuesA.length; i++) {
      formChilds.add(renderEntry(_valuesA, true, i, widget.entry.list.nameA));
    }
    for (var i = 0; i < _valuesB.length; i++) {
      formChilds.add(renderEntry(_valuesB, false, i, widget.entry.list.nameB));
    }
    formChilds.addAll([
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
    ]);
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
          autofocus: false,
          child: const Text('Cancel'),
          onPressed: () {
            Navigator.of(context).pop();
          },
        ),
        TextButton(
          autofocus: false,
          onPressed: () async {
            if (_formAddAttributeKey.currentState!.validate()) {
              widget.entry.tip = _tip;
              widget.entry.addition = _addition;
              widget.entry.meaningsA = _valuesA;
              widget.entry.meaningsB = _valuesB;
              Navigator.of(context).pop(widget.entry);
            }
          },
          child: Text(widget.entry.isRaw() ? 'Create' : 'Save'),
        ),
      ],
    );
  }
}
