package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.SessionStorageManager;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

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
    private Button bSolve;
    private TrainerSettings settings;
    private Trainer trainer;
    private TextView tColumnQuestion;
    private TextView tColumnAnswer;
    private MenuItem tTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);
        setTitle(R.string.Trainer_Title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        tExercise = (TextView) findViewById(R.id.tTrainerExercise);
        tHint = (TextView) findViewById(R.id.tTrainerHint);
        tColumnQuestion = (TextView) findViewById(R.id.tTrainerExColumn);
        tColumnAnswer = (TextView) findViewById(R.id.tTrainerInputColumn);
        tInput = (EditText) findViewById(R.id.tTrainerInput);
        bSolve = (Button) findViewById(R.id.bTrainerSolve);
        initTrainer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trainer, menu);

        tTip = menu.findItem(R.id.tMenu_Tip);

        return true;
    }

    /**
     * Initialize trainer
     */
    private void initTrainer() {
        final Database db = new Database(getBaseContext());
        SessionStorageManager ssm = new SessionStorageManager(db);
        Intent intent = getIntent();
        boolean resume = intent.getBooleanExtra(PARAM_RESUME_SESSION_FLAG, false);
        ArrayList<Table> tables;
        if (resume) {
            Log.d(TAG, "resuming");
            settings = ssm.loadSession();
            tables = ssm.loadSessionTbls();
        } else {
            Log.d(TAG, "not resuming");
            tables = (ArrayList<Table>) intent.getSerializableExtra(PARAM_TABLES);
            if (tables == null) {
                Log.wtf(TAG, "Flag for tables passed but no tables received!");
            } else {
                settings = (TrainerSettings) intent.getSerializableExtra(PARAM_TRAINER_SETTINGS);
                if (settings == null) {
                    Log.wtf(TAG, "No trainer settings passed!");
                } else {
                    Log.d(TAG, "saving new session..");
                    if (!db.deleteSession()) {
                        Log.wtf(TAG, "unable to delete past session");
                    } else if (!ssm.saveSession(settings)) {
                        Log.wtf(TAG, "unable to save session meta");
                    } else if (!ssm.saveSessionTbls(tables)) {
                        Log.wtf(TAG, "unable to save session tables");
                    } else {
                        Log.d(TAG, "saved session");
                    }
                }

            }
        }
        trainer = new Trainer(tables, settings, getBaseContext(), !resume);
        showNextVocable();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateTip();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tMenu_Tip:
                showHint();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Show next vocable of trainer
     */
    private void showNextVocable() {
        if (trainer.isFinished()) {
            AlertDialog.Builder finishedDiag = new AlertDialog.Builder(this);

            finishedDiag.setTitle(R.string.Trainer_Diag_finished_Title);
            finishedDiag.setMessage(R.string.Trainer_Diag_finished_MSG);

            finishedDiag.setPositiveButton(R.string.Trainer_Diag_finished_btn_OK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d(TAG, "ok");

                    Intent myIntent = new Intent(TrainerActivity.this, MainActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(myIntent);
                }
            });

            finishedDiag.show();
        } else {
            tExercise.setText(trainer.getQuestion());
            tHint.setText("");
            tInput.setText("");
            tInput.requestFocus();
            bSolve.setEnabled(true);
            updateTip();
            tColumnQuestion.setText(trainer.getColumnNameExercise());
            tColumnAnswer.setText(trainer.getColumnNameSolution());
        }
    }

    /**
     * Verify input against solution
     */
    public void checkInput(View view) {
        if (trainer.checkSolution(tInput.getText().toString())) {
            showNextVocable();
        } else {
            tInput.setSelectAllOnFocus(true);
            tInput.requestFocus();
        }
    }

    /**
     * Solve current vocable
     */
    public void solve(View view) {
        tInput.setText(trainer.getSolution());
        bSolve.setEnabled(false);
    }

    /**
     * Action on hint request
     */
    public void showHint() {
        if (settings.allowTips) {
            tHint.setText(trainer.getTip());
        }
    }

    /**
     * Function updates tTip enabled status
     */
    private void updateTip(){
        if(tTip != null){
            tTip.getIcon().setAlpha(settings.allowTips ? 255 : 155);
            tTip.setEnabled(settings.allowTips);
        }
    }
}
