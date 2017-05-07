package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.ListSelector.PARAM_NEW_ACTIVITY;

/**
 * Trainer activity
 */
public class TrainerActivity extends AppCompatActivity {
    public static final String PARAM_RESUME_SESSION_FLAG = "resume_session";
    public static final String PARAM_TRAINER_SETTINGS = "trainer_settings";
    public static final String PARAM_TABLES = "tables";
    private static final String TAG = "TrainerActivity";

    private TextView tExercise;
    private TextView tHint;
    private EditText tInput;
    private Button bHint;
    private Button bSolve;
    private TrainerSettings settings;
    private Trainer trainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);
        setTitle("Vocabletrainer - Training");
        tExercise = (TextView) findViewById(R.id.tTrainerExercise);
        tHint = (TextView) findViewById(R.id.tTrainerHint);
        tInput = (EditText) findViewById(R.id.tTrainerInput);
        bHint = (Button) findViewById(R.id.bTrainerHint);
        bSolve = (Button) findViewById(R.id.bTrainerSolve);

        initTrainer();
    }

    /**
     * Initialize trainer
     */
    private void initTrainer() {
        Intent intent = getIntent();
        boolean resume = intent.getBooleanExtra(PARAM_RESUME_SESSION_FLAG, false);
        ArrayList<Table> tables;
        if (resume) {
            //TODO: load recent session
            tables = null;
        } else {
            tables = (ArrayList<Table>) intent.getSerializableExtra(PARAM_TABLES);
            if (tables == null) {
                Log.e(TAG, "Flag for tables passed but no tables received!");
            } else {
                settings = (TrainerSettings) intent.getSerializableExtra(PARAM_TRAINER_SETTINGS);
                if(settings == null){
                    Log.e(TAG,"No trainer settings passed!");
                }

            }
        }
        trainer = new Trainer(tables, settings, getBaseContext(), !resume);
        showNextVocable();
    }

    /**
     * Show next vocable of trainer
     */
    private void showNextVocable(){
        tExercise.setText(trainer.getQuestion());
        tHint.setText("");
        tInput.setText("");
        tInput.requestFocus();
        bSolve.setEnabled(true);
        bHint.setEnabled(settings.allowTps);

        if(trainer.isFinished()){
            AlertDialog.Builder finishedDiag = new AlertDialog.Builder(this);

            finishedDiag.setTitle("Finished");
            finishedDiag.setMessage("You've finished this training session.");

            finishedDiag.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d(TAG, "ok");

                    Intent myIntent = new Intent(TrainerActivity.this, MainAcitivity.class);
                    startActivity(myIntent);
                }
            });

            finishedDiag.show();
        }
    }

    /**
     * Verify input against solution
     */
    public void checkInput(View view){
        if(trainer.checkSolution(tInput.getText().toString())){
            showNextVocable();
        }else{
            tInput.setSelectAllOnFocus(true);
            tInput.requestFocus();
        }
    }

    /**
     * Solve current vocable
     */
    public void solve(View view){
        tInput.setText(trainer.getSolution());
        bSolve.setEnabled(false);
    }

    /**
     * Action on hint request
     */
    public void showHint(View view){
        if(settings.allowTps){
            tHint.setText(trainer.getTip());
        }
    }
}
