package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import org.apache.commons.csv.CSVFormat;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.ExImportActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.Activities.FragmentActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.R;

import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.MainActivity.PREFS_NAME;

/**
 * Fragment for custom format preferences
 */
public class FormatFragment extends PreferenceFragment {
    private static final String TAG = "FormatFragment";
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
     * Creates a CSVFormat object out of the settings
     *
     * @return
     */
    private CSVFormat savePrefsToCSVFormat() {
        CSVFormat format = CSVFormat.newFormat(tDelimtier.getText().charAt(0));
        if (swEscaping.isChecked())
            format = format.withEscape(tEscaping.getText().charAt(0));
        if (swComment.isChecked())
            format = format.withCommentMarker(tComment.getText().charAt(0));
        if (swQuote.isChecked())
            format = format.withQuote(tQuote.getText().charAt(0));
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
}
