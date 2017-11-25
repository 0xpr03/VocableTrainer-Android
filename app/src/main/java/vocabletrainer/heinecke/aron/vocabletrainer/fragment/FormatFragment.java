package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

import vocabletrainer.heinecke.aron.vocabletrainer.activity.ExImportActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.FragmentActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.R;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;

/**
 * Fragment for custom format preferences
 */
public class FormatFragment extends PreferenceFragment implements FragmentActivity.BackButtonListner{
    private static final String TAG = "FormatFragment";
    private static final int CHAR_POS = 0;
    SwitchPreference swEscaping;
    SwitchPreference swComment;
    SwitchPreference swQuote;
    SwitchPreference swHeaderLine;
    SwitchPreference swIgnoreSpaces;
    SwitchPreference swIgnEmptyLines;
    EditTextPreference tEscaping;
    EditTextPreference tComment;
    EditTextPreference tQuote;
    EditTextPreference tDelimtier;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar ab = ((FragmentActivity) getActivity()).getSupportActionBar();

        if (ab != null) {
            ab.setSubtitle(null);
            ab.setHomeButtonEnabled(true);
        }

        setHasOptionsMenu(true);

        addPreferencesFromResource(R.xml.pref_format);

        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(1)};

        addTextFilter(R.string.k_pref_comment_char, filters);
        addTextFilter(R.string.k_pref_escape_char, filters);
        addTextFilter(R.string.k_pref_quote_char, filters);
        addTextFilter(R.string.k_pref_delimiter, filters);

        swEscaping = (SwitchPreference) findPreference(getString(R.string.k_pref_escape));
        swComment = (SwitchPreference) findPreference(getString(R.string.k_pref_comment));
        swQuote = (SwitchPreference) findPreference(getString(R.string.k_pref_quote));
        swHeaderLine = (SwitchPreference) findPreference(getString(R.string.k_pref_first_line_header));
        swIgnEmptyLines = (SwitchPreference) findPreference(getString(R.string.k_pref_ignore_empty_lines));
        swIgnoreSpaces = (SwitchPreference) findPreference(getString(R.string.k_pref_ignore_spaces));

        tEscaping = (EditTextPreference) findPreference(getString(R.string.k_pref_escape_char));
        tQuote = (EditTextPreference) findPreference(getString(R.string.k_pref_quote_char));
        tDelimtier = (EditTextPreference) findPreference(getString(R.string.k_pref_delimiter));
        tComment = (EditTextPreference) findPreference(getString(R.string.k_pref_comment_char));
    }

    @Override
    public void onStart() {
        super.onStart();
        loadPrefs();
    }



    @Override
    public void onStop() {
        super.onStop();
        if(verifyFormat())
            ExImportActivity.updateCustomFormat(savePrefsToCSVFormat());
    }

    /**
     * Load CSVFormat to preferences
     */
    private void loadPrefs(){
        SharedPreferences pref = getActivity().getSharedPreferences(PREFS_NAME, 0);
        loadPrefsFromCSVFormat(ExImportActivity.getCustomFormat(pref));
    }

    /**
     * Load preferences from CSV format
     *
     * @param format CSVFormat to use
     */
    private void loadPrefsFromCSVFormat(final CSVFormat format) {
        swEscaping.setChecked(format.isEscapeCharacterSet());
        swQuote.setChecked(format.isQuoteCharacterSet());
        swComment.setChecked(format.isCommentMarkerSet());
        swHeaderLine.setChecked(format.getSkipHeaderRecord());
        swIgnEmptyLines.setChecked(format.getIgnoreEmptyLines());
        swIgnoreSpaces.setChecked(format.getIgnoreSurroundingSpaces());

        tEscaping.setText(String.valueOf(format.getEscapeCharacter()));
        tComment.setText(String.valueOf(format.getCommentMarker()));
        tDelimtier.setText(String.valueOf(format.getDelimiter()));
        tQuote.setText(String.valueOf(format.getQuoteCharacter()));
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

        char delimiter = tDelimtier.getText().charAt(CHAR_POS);

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
            errorDiag.setPositiveButton(R.string.GEN_Ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
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
    private CSVFormat savePrefsToCSVFormat() {
        CSVFormat format = CSVFormat.newFormat(tDelimtier.getText().charAt(CHAR_POS));
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

        return format.withAllowMissingColumnNames();
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
                loadPrefsFromCSVFormat(CSVFormat.DEFAULT);
                return true;
            case R.id.fResetPrev:
                Log.d(TAG,"reset to previous");
                loadPrefs();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Adds text filters to specified settings
     *
     * @param key     resource key by which to get the setting
     * @param filters Filters to apply
     */
    private void addTextFilter(final int key, InputFilter[] filters) {
        EditText editText1 = ((EditTextPreference) findPreference(getString(key)))
                .getEditText();
        editText1.setFilters(filters);
    }

    @Override
    public boolean onBackPressed() {
        return verifyFormat();
    }
}
