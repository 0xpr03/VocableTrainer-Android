package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.MainActivity.PREFS_NAME;
import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.TrainerActivity.PARAM_TABLES;
import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.TrainerActivity.PARAM_TRAINER_SETTINGS;

/**
 * Trainer settings activity
 */
public class TrainerSettingsActivity extends AppCompatActivity {

    // shared prefs keys
    private final static String P_KEY_TS_TIMES_VOCABLE = "vocable_repeat";
    private final static String P_KEY_TS_TRAIN_MODE = "training_mode";
    private final static String P_KEY_TS_ALLOW_HINTS = "hints_allowed";

    private static final String TAG = "TrainerSettings";
    public ArrayList<Table> tables;
    CheckBox bHints;
    EditText tTimesVocable;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_settings);
        setTitle(R.string.TSettings_Title);
        Intent intent = getIntent();
        tables = (ArrayList<Table>) intent.getSerializableExtra(ListActivity.RETURN_LISTS);
        if (tables == null) {
            Log.wtf(TAG, "No table list passed!");
            return;
        }
        bHints = (CheckBox) findViewById(R.id.tSettingsChkAllowTips);
        tTimesVocable = (EditText) findViewById(R.id.tSettingsSolveTimes);

        init();
    }

    /**
     * Setup view
     */
    private void init() {
        spinner = (Spinner) findViewById(R.id.tSettingsSpinMode);

        spinner.setAdapter(new ArrayAdapter<Trainer.TEST_MODE>(this, android.R.layout.simple_list_item_1, Trainer.TEST_MODE.values()));

        // Load past values
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        // use string to show the hint at first via empty string
        tTimesVocable.setText(settings.getString(P_KEY_TS_TIMES_VOCABLE, ""));
        spinner.setSelection(settings.getInt(P_KEY_TS_TRAIN_MODE, 0));
        bHints.setChecked(settings.getBoolean(P_KEY_TS_ALLOW_HINTS, false));
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Save values
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(P_KEY_TS_ALLOW_HINTS, bHints.isChecked());
        editor.putInt(P_KEY_TS_TRAIN_MODE, spinner.getSelectedItemPosition());
        editor.putString(tTimesVocable.getText().toString(), P_KEY_TS_TIMES_VOCABLE);
        editor.apply();
    }

    /**
     * Go to next activity -> start trainer
     *
     * @param view
     */
    public void gotoNext(View view) {

        int timeToSolve = 0;
        try {
            timeToSolve = Integer.valueOf(tTimesVocable.getText().toString());
        } catch (NumberFormatException e) {
            return;
        }
        Trainer.TEST_MODE mode = (Trainer.TEST_MODE) spinner.getSelectedItem();
        boolean showHints = bHints.isChecked();
        Log.d(TAG, "" + timeToSolve + " " + mode + " " + showHints);
        TrainerSettings settings = new TrainerSettings(timeToSolve, mode, showHints);

        Intent intent = new Intent(this, TrainerActivity.class);
        intent.putExtra(PARAM_TRAINER_SETTINGS, settings);
        intent.putExtra(PARAM_TABLES, tables);
        startActivity(intent);
    }
}
