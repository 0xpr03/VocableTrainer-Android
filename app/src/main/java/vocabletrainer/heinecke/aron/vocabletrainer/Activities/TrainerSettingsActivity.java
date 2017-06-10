package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.ListSelector.PARAM_PASSED_SELECTION;
import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.TrainerActivity.PARAM_TABLES;
import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.TrainerActivity.PARAM_TRAINER_SETTINGS;

/**
 * Trainer settings activity
 */
public class TrainerSettingsActivity extends AppCompatActivity {

    private static final String TAG = "TrainerSettings";
    private Spinner spinner;
    public ArrayList<Table> tables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_settings);
        setTitle(R.string.TSettings_Title);
        Intent intent = getIntent();
        tables = (ArrayList<Table>) intent.getSerializableExtra(PARAM_PASSED_SELECTION);
        if(tables == null){
            Log.e(TAG,"No table list passed!");
            return;
        }

        init();
    }

    /**
     * Setup view
     */
    private void init() {
        spinner = (Spinner) findViewById(R.id.tSettingsSpinMode);

        spinner.setAdapter(new ArrayAdapter<Trainer.TEST_MODE>(this, android.R.layout.simple_list_item_1, Trainer.TEST_MODE.values()));
    }

    /**
     * Go to next activity -> start trainer
     * @param view
     */
    public void gotoNext(View view) {
        CheckBox box = (CheckBox) findViewById(R.id.tSettingsChkAllowTips);
        EditText txt = (EditText) findViewById(R.id.tSettingsSolveTimes);
        int timeToSolve = 0;
        try {
            timeToSolve = Integer.valueOf(txt.getText().toString());
        } catch (NumberFormatException e) {
            return;
        }
        Trainer.TEST_MODE mode = (Trainer.TEST_MODE) spinner.getSelectedItem();
        boolean showHints = box.isChecked();
        Log.d(TAG, "" + timeToSolve + " " + mode + " " + showHints);
        TrainerSettings settings = new TrainerSettings(timeToSolve, mode, showHints);

        Intent intent = new Intent(this, TrainerActivity.class);
        intent.putExtra(PARAM_TRAINER_SETTINGS, settings);
        intent.putExtra(PARAM_TABLES,tables);
        startActivity(intent);
    }
}
