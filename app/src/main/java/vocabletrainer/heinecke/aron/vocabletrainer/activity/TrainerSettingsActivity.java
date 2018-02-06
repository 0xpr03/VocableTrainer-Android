package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;
import static vocabletrainer.heinecke.aron.vocabletrainer.activity.TrainerActivity.PARAM_TABLES;
import static vocabletrainer.heinecke.aron.vocabletrainer.activity.TrainerActivity.PARAM_TRAINER_SETTINGS;

/**
 * Trainer settings activity
 */
public class TrainerSettingsActivity extends AppCompatActivity {

    // shared prefs keys
    private final static String P_KEY_TS_TIMES_VOCABLE = "vocable_repeat";
    private final static String P_KEY_TS_TRAIN_MODE = "training_mode";
    private final static String P_KEY_TS_ALLOW_HINTS = "hints_allowed";
    private final static String P_KEY_TS_CASE_SENSITIVE = "case_sensitive";

    private static final String TAG = "TrainerSettings";
    public ArrayList<VList> lists;
    CheckBox bHints;
    CheckBox bCaseSensitive;
    CheckBox bTrimSpaces;
    EditText tTimesVocable;
    private Trainer.TEST_MODE testMode;
    private RadioButton[] rButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_settings);
        setTitle(R.string.TSettings_Title);
        Intent intent = getIntent();
        lists = (ArrayList<VList>) intent.getSerializableExtra(ListActivity.RETURN_LISTS);
        if (lists == null) {
            Log.wtf(TAG, "No table list passed!");
            return;
        }
        bHints = (CheckBox) findViewById(R.id.tSettingsChkAllowTips);
        bCaseSensitive = (CheckBox) findViewById(R.id.chkTSettingsChkCaseSens);
        bTrimSpaces = (CheckBox) findViewById(R.id.tSettingsChkTrimSpaces);
        tTimesVocable = (EditText) findViewById(R.id.tSettingsSolveTimes);
        RadioButton rbA = (RadioButton) findViewById(R.id.rTSettingsA);
        RadioButton rbB = (RadioButton) findViewById(R.id.rTSettingsB);
        RadioButton rbR = (RadioButton) findViewById(R.id.rTSettingsAB);
        rButtons = new RadioButton[]{rbA,rbB,rbR};


        init();
    }

    /**
     * Radio button clicked handler
     * @param view
     */
    public void onRadioButtonClicked(View view) {
        refreshTestMode(view);
    }

    /**
     * Update testmode based on selected view
     * @param view
     */
    private void refreshTestMode(View view){
        switch (view.getId()) {
            case R.id.rTSettingsA:
                testMode = Trainer.TEST_MODE.A;
                break;
            case R.id.rTSettingsB:
                testMode = Trainer.TEST_MODE.B;
                break;
            case R.id.rTSettingsAB:
                testMode = Trainer.TEST_MODE.RANDOM;
                break;
            default:
                Log.w(TAG,"invalid view passed for mode refresh");
        }
    }


    /**
     * Returns position of the currently checked radio button in the list
     * @return
     */
    private int getChecked(){
        for(int i = 0; i < rButtons.length ; i++){
            if (rButtons[i].isChecked())
                return i;
        }
        Log.w(TAG,"no button selected!");
        return 0;
    }


    /**
     * Setup view
     */
    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Load past values
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        // use string to show the hint at first via empty string
        tTimesVocable.setText(settings.getString(P_KEY_TS_TIMES_VOCABLE, ""));
        bHints.setChecked(settings.getBoolean(P_KEY_TS_ALLOW_HINTS, true));
        bCaseSensitive.setChecked(settings.getBoolean(P_KEY_TS_CASE_SENSITIVE,true));

        int cRB = (settings.getInt(P_KEY_TS_TRAIN_MODE, 0));
        RadioButton rbtn;
        if(cRB < rButtons.length){
            rbtn = rButtons[cRB];
        }else{
            rbtn = rButtons[0];
        }
        rbtn.setChecked(true);
        refreshTestMode(rbtn); // update here to init testmode
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Save values
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(P_KEY_TS_ALLOW_HINTS, bHints.isChecked());
        editor.putInt(P_KEY_TS_TRAIN_MODE, getChecked());
        editor.putString(P_KEY_TS_TIMES_VOCABLE,tTimesVocable.getText().toString());
        editor.putBoolean(P_KEY_TS_CASE_SENSITIVE,bCaseSensitive.isChecked());
        editor.apply();
    }

    /**
     * Go to next activity -> start trainer
     *
     * @param view
     */
    public void gotoNext(View view) {

        int timesToSolve = 0;
        try {
            timesToSolve = Integer.valueOf(tTimesVocable.getText().toString());
        } catch (NumberFormatException e) {
            return;
        }
        boolean showHints = bHints.isChecked();
        boolean caseSensitive = bCaseSensitive.isChecked();
        boolean trimSpaces = bTrimSpaces.isChecked();
        TrainerSettings settings = new TrainerSettings(timesToSolve, testMode,
                showHints, caseSensitive,trimSpaces);

        Intent intent = new Intent(this, TrainerActivity.class);
        intent.putExtra(PARAM_TRAINER_SETTINGS, settings);
        intent.putExtra(PARAM_TABLES, lists);
        startActivity(intent);
    }
}
