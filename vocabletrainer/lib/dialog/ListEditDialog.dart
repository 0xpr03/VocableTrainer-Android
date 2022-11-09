import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class ListEditDialog extends StatefulWidget {
  /// Attriute, if set for editing
  final Attribute? attribute;

  const ListEditDialog({super.key, required this.attribute});

  @override
  State<ListEditDialog> createState() => _ListEditDialogState();
}

class _ListEditDialogState extends State<ListEditDialog> {
  final _formAddAttributeKey = GlobalKey<FormState>();
  String _attributeName = '';
  String _attributeUnit = '';

  @override
  Widget build(BuildContext context) {
    if (widget.attribute != null) {
      _attributeName = widget.attribute!.name;
      _attributeUnit = widget.attribute!.unit;
    }
    return AlertDialog(
      title: Text(widget.attribute == null
          ? 'Add Attribute'
          : 'Edit Attribute ${widget.attribute!.name}'),
      content: SingleChildScrollView(
        child: Form(
            autovalidateMode: AutovalidateMode.onUserInteraction,
            key: _formAddAttributeKey,
            child: ListBody(
              children: [
                TextFormField(
                  autofocus: true,
                  initialValue: _attributeName,
                  decoration: const InputDecoration(
                      helperText: 'Name', hintText: 'Attribute Name'),
                  onChanged: (value) {
                    _attributeName = value;
                  },
                ),
                TextFormField(
                  autofocus: false,
                  initialValue: _attributeUnit,
                  decoration:
                      const InputDecoration(hintText: 'kg', helperText: 'Unit'),
                  onChanged: (value) {
                    _attributeUnit = value;
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
          child: Text(widget.attribute == null ? 'Create' : 'Save'),
          onPressed: () {
            if (_formAddAttributeKey.currentState!.validate()) {
              Attribute a;
              if (widget.attribute == null) {
                var cache = Provider.of<WorkoutCache>(context, listen: false);
                a = cache.newAttribute(_attributeName, _attributeUnit, true);
              } else {
                a = widget.attribute!;
                a.name = _attributeName;
                a.unit = _attributeUnit;
              }
              Navigator.of(context).pop(a);
            }
          },
        ),
      ],
    );
  }
}
