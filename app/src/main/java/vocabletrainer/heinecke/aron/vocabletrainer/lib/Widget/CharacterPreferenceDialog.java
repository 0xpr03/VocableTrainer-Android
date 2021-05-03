package vocabletrainer.heinecke.aron.vocabletrainer.lib.Widget;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.GenericSpinnerEntry;

/**
 * Character Preference Dialog
 */
public class CharacterPreferenceDialog extends PreferenceDialogFragmentCompat {
    private static final int MAX_LENGTH = 1;
    private static final String ARG_TITLE = "title";
    private static String PLACEHOLDER;
    TextInputEditText charInput;
    TextInputLayout charInputLayout;
    Spinner spPreset;
    ArrayAdapter<GenericSpinnerEntry<Character>> adapter;
    private CustomItemSelectedListener listener;
    private Button okButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    public static CharacterPreferenceDialog newInstance(Preference preference) {
        final CharacterPreferenceDialog
                fragment = new CharacterPreferenceDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, preference.getKey());
        b.putString(ARG_TITLE, preference.getTitle().toString());
        fragment.setArguments(b);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        okButton = dialog.findViewById(AlertDialog.BUTTON_POSITIVE);
        return dialog;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        view.getContext().getTheme().applyStyle(R.style.CustomDialog, true);
        PLACEHOLDER = getString(R.string.Placeholder);
        DialogPreference preference = getPreference();

        charInput = view.findViewById(R.id.tCharInput);
        charInputLayout = view.findViewById(R.id.tCharInputLayout);
        charInputLayout.setCounterEnabled(true);
        charInputLayout.setCounterMaxLength(MAX_LENGTH);
        String hint = getArguments().getString(ARG_TITLE,PLACEHOLDER);
        charInputLayout.setHint(hint);
        charInput.setSingleLine();
        charInput.setHint(hint);
        charInput.setText(((EditTextPreference) preference).getText());
        spPreset = view.findViewById(R.id.spPreset);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(new GenericSpinnerEntry<>(null,getString(R.string.Character_Custom)));
        adapter.add(new GenericSpinnerEntry<>('\t',getString(R.string.Character_Tab)));
        adapter.add(new GenericSpinnerEntry<>('\r',getString(R.string.Character_Line_Feed)));
        adapter.add(new GenericSpinnerEntry<>(',',getString(R.string.Character_Comma)));
        adapter.add(new GenericSpinnerEntry<>(';',getString(R.string.Character_Semicolon)));
        adapter.add(new GenericSpinnerEntry<>('"',getString(R.string.Character_Quotations_Mark)));
        adapter.add(new GenericSpinnerEntry<>('\\',getString(R.string.Character_Backslash)));
        spPreset.setAdapter(adapter);

        listener = new CustomItemSelectedListener() {
            @Override
            public void itemSelected(AdapterView<?> parent, View view, int position, long id) {
                Character selected = adapter.getItem(position).getObject();
                if(selected != null){
                    charInput.setText(String.valueOf(selected));
                }
            }
        };

        spPreset.setOnItemSelectedListener(listener);

        charInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //listener.disableNextEvent();
                updateSpinner();
                int length = s.toString().toCharArray().length;
                if(length == MAX_LENGTH){
                    charInputLayout.setError(null);
                }else{
                    charInputLayout.setError(getString(R.string.Character_Incorrect_Amount));
                }
            }
        });
    }

    private void updateSpinner(){
        String input = charInput.getText().toString();
        switch(input){
            case "\t":
                spPreset.setSelection(1);
                break;
            case "\r":
                spPreset.setSelection(2);
                break;
            case ",":
                spPreset.setSelection(3);
                break;
            case ";":
                spPreset.setSelection(4);
                break;
            case "\"":
                spPreset.setSelection(5);
                break;
            case "\\":
                spPreset.setSelection(6);
                break;
            default:
                spPreset.setSelection(0);
                if(input.length() == MAX_LENGTH) {
                    adapter.getItem(0).updateObject(input.charAt(0));
                }
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if(positiveResult && charInputLayout.getError() == null){
            DialogPreference preference = getPreference();
            if (preference instanceof EditTextPreference) {
                EditTextPreference textPreference =
                        ((EditTextPreference) preference);
                // This allows the client to ignore the user value.
                if(textPreference.callChangeListener(charInput.getText().toString())){
                    textPreference.setText(charInput.getText().toString());
                }
            }
        }
    }
}
