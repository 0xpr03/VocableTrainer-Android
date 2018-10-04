package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.Constants;

import java.util.Objects;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.FragmentActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.CSVCustomFormat;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Widget.CustomEditTextPreference;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel.FormatViewModel;

/**
 * Fragment for custom cFormat preferences<br>
 *     Expects to be used inside a ViewModel & reacts getting visible/invisible
 */
public class FormatFragment extends PreferenceFragmentCompat implements FragmentActivity.BackButtonListener,
        SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String TAG = "FormatFragment";
    private static final String C_DIALOG_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";
    private static final int CHAR_POS = 0; // char pos for first char
    SwitchPreference swEscaping;
    SwitchPreference swComment;
    SwitchPreference swQuote;
    SwitchPreference swHeaderLine;
    SwitchPreference swIgnoreSpaces;
    SwitchPreference swIgnEmptyLines;
    SwitchPreference swMultimeaning;
    SwitchPreference swMMEscape;
    EditTextPreference tEscaping;
    EditTextPreference tComment;
    EditTextPreference tQuote;
    EditTextPreference tDelimiter;
    EditTextPreference tMultimeaning;
    EditTextPreference tMMEscape;
    private InputFilter[] lengthFilter = new InputFilter[] {new InputFilter.LengthFilter(1)};
    FormatViewModel model;
    CSVCustomFormat previousFormat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(FormatViewModel.class);
    }



    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_format);

        ActionBar ab = ((FragmentActivity) getActivity()).getSupportActionBar();

        if (ab != null) {
            ab.setSubtitle(null);
            ab.setHomeButtonEnabled(true);
        }

        setHasOptionsMenu(true);

        swEscaping = (SwitchPreference) findPreference(getString(R.string.k_pref_escape));
        swComment = (SwitchPreference) findPreference(getString(R.string.k_pref_comment));
        swQuote = (SwitchPreference) findPreference(getString(R.string.k_pref_quote));
        swHeaderLine = (SwitchPreference) findPreference(getString(R.string.k_pref_first_line_header));
        swIgnEmptyLines = (SwitchPreference) findPreference(getString(R.string.k_pref_ignore_empty_lines));
        swIgnoreSpaces = (SwitchPreference) findPreference(getString(R.string.k_pref_ignore_spaces));
        swMultimeaning = (SwitchPreference) findPreference(getString(R.string.k_pref_multi_transl));
        swMMEscape = (SwitchPreference) findPreference(getString(R.string.k_pref_multi_transl_escape));

        tEscaping = (EditTextPreference) findPreference(getString(R.string.k_pref_escape_char));
        tQuote = (EditTextPreference) findPreference(getString(R.string.k_pref_quote_char));
        tDelimiter = (EditTextPreference) findPreference(getString(R.string.k_pref_delimiter));
        tComment = (EditTextPreference) findPreference(getString(R.string.k_pref_comment_char));
        tMultimeaning = (EditTextPreference) findPreference(getString(R.string.k_pref_multi_transl_char));
        tMMEscape = (EditTextPreference) findPreference(getString(R.string.k_pref_multi_transl_escape_char));
    }

    @Override
    public void onStart() {
        super.onStart();
        loadPrefs();
    }

    @Override
    public void onStop() {
        super.onStop();
        updateCustomFormat();
    }

    /**
     * Save back the current format
     */
    private void updateCustomFormat() {
        if(verifyFormat()) {
            CSVCustomFormat newFormat = savePrefsToCSVFormat();
            model.setCustomFormat(newFormat);
        }
    }

    /**
     * Load CSVFormat to preferences
     */
    private void loadPrefs(){
        loadPrefsFromCSVFormat(model.getCustomFormatData());
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Load preferences from CSV cFormat
     *
     * @param cformat CSVCustomFormat to load
     */
    private void loadPrefsFromCSVFormat(@NonNull final CSVCustomFormat cformat) {
        CSVFormat format = cformat.getFormat();
        swEscaping.setChecked(format.isEscapeCharacterSet());
        swQuote.setChecked(format.isQuoteCharacterSet());
        swComment.setChecked(format.isCommentMarkerSet());
        swHeaderLine.setChecked(format.getSkipHeaderRecord());
        swIgnEmptyLines.setChecked(format.getIgnoreEmptyLines());
        swIgnoreSpaces.setChecked(format.getIgnoreSurroundingSpaces());
        swMultimeaning.setChecked(cformat.isMultiValueEnabled());
        swMMEscape.setChecked(cformat.isMVEscapeEnabled());

        tEscaping.setText(String.valueOf(format.getEscapeCharacter()));
        tComment.setText(String.valueOf(format.getCommentMarker()));
        tDelimiter.setText(String.valueOf(format.getDelimiter()));
        tQuote.setText(String.valueOf(format.getQuoteCharacter()));
        tMultimeaning.setText(String.valueOf(cformat.getMultiValueChar()));
        tMMEscape.setText(String.valueOf(cformat.getEscapeMVChar()));
    }

    /**
     * Verify the CSV Format input and return true on success<br>
     *     Shows a warning dialog on errors
     * @return
     */
    private boolean verifyFormat(){
        int partA = -1; // we have to initialize this..
        int partB = R.string.Pref_Delimiter;

        boolean passed = true;

        char delimiter = tDelimiter.getText().charAt(CHAR_POS);

        if (swQuote.isChecked() && delimiter == tQuote.getText().charAt(CHAR_POS)) {
            partA = R.string.Pref_Quote;
            passed = false;
        }

        if (swEscaping.isChecked() && delimiter == tEscaping.getText().charAt(CHAR_POS)) {
            partA = R.string.Pref_Escape;
            passed = false;
        }

        if (swComment.isChecked() && delimiter == tComment.getText().charAt(CHAR_POS)) {
            partA = R.string.Pref_Comment;
            passed = false;
        }

        if (swMultimeaning.isChecked() && delimiter == tMultimeaning.getText().charAt(CHAR_POS)) {
            partA = R.string.Pref_Multi_Transl;
            passed = false;
        }

        if (swQuote.isChecked() && tQuote.getText().charAt(CHAR_POS) == tComment.getText().charAt(CHAR_POS)) {
            partA = R.string.Pref_Quote;
            partB = R.string.Pref_Comment;
            passed = false;
        }

        if (swEscaping.isChecked() && tEscaping.getText().charAt(CHAR_POS) == tComment.getText().charAt(CHAR_POS)) {
            partA = R.string.Pref_Escape;
            partB = R.string.Pref_Comment;
            passed = false;
        }

        if(!passed){
            String sA = getString(partA);
            String sB = getString(partB);

            String msg = getString(R.string.Format_Error_Input_equals).replace("$A",sA).replace("$B",sB);

            final AlertDialog.Builder errorDiag = new AlertDialog.Builder(getActivity());

            errorDiag.setTitle(R.string.Format_Diag_error_Title);
            errorDiag.setMessage(msg);
            errorDiag.setPositiveButton(R.string.GEN_OK, (dialog, whichButton) -> {
            });

            errorDiag.show();
        }

        return passed;
    }

    /**
     * Creates a CSVFormat object out of the settings
     *
     * @return
     */
    private CSVCustomFormat savePrefsToCSVFormat() {
        CSVFormat format = CSVFormat.newFormat(tDelimiter.getText().charAt(CHAR_POS));
        format = format.withRecordSeparator(Constants.CRLF);
        if (swEscaping.isChecked())
            format = format.withEscape(tEscaping.getText().charAt(CHAR_POS));
        if (swComment.isChecked())
            format = format.withCommentMarker(tComment.getText().charAt(CHAR_POS));
        if (swQuote.isChecked())
            format = format.withQuote(tQuote.getText().charAt(CHAR_POS));
        if (swHeaderLine.isChecked())
            format = format.withFirstRecordAsHeader();
        if (swIgnEmptyLines.isChecked())
            format = format.withIgnoreEmptyLines();
        if (swIgnoreSpaces.isChecked())
            format = format.withIgnoreSurroundingSpaces();

        Character multiMeaningChar = null;
        Character multiMeaningEscapeChar = null;
        if (swMultimeaning.isChecked())
            multiMeaningChar = tMultimeaning.getText().charAt(CHAR_POS);
        if (swMMEscape.isChecked())
            multiMeaningEscapeChar = tMMEscape.getText().charAt(CHAR_POS);

        return new CSVCustomFormat(format.withAllowMissingColumnNames(),multiMeaningChar,multiMeaningEscapeChar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.format_pref, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fResetDefault:
                Log.d(TAG,"reset to default");
                loadPrefsFromCSVFormat(CSVCustomFormat.DEFAULT);
                return true;
            case R.id.fResetPrev:
                Log.d(TAG,"reset to previous" + (previousFormat != null));
                loadPrefsFromCSVFormat(previousFormat);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // hack for custom dialog to allow for edittext filters

        // dialog shown
        if (getFragmentManager().findFragmentByTag(C_DIALOG_TAG) != null) {
            return;
        }

        DialogFragment f = null;
        if (preference instanceof CustomEditTextPreference) {
            f = EditTextPreferenceDialog.newInstance(preference.getKey(),lengthFilter);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
        if (f != null) {
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), C_DIALOG_TAG);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(this.isResumed()){
            Log.d(TAG,"visible: "+isVisibleToUser);
            if(isVisibleToUser)
                previousFormat = model.getCustomFormatData();
            else
                updateCustomFormat();
            model.setInFormatFragment(isVisibleToUser);
        }
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        verifyFormat();
    }

    /**
     * Custom EditText preference dialog to allow for Filters
     * Because correct Android is hard.
     */
    public static class EditTextPreferenceDialog extends EditTextPreferenceDialogFragmentCompat {
        private InputFilter[] filters;

        public static EditTextPreferenceDialog newInstance(String key, InputFilter[] filters) {
            final EditTextPreferenceDialog
                    fragment = new EditTextPreferenceDialog();
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            fragment.filters = filters;
            return fragment;
        }

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            ((EditText)view.findViewById(android.R.id.edit)).setFilters(filters);
        }
    }
}
