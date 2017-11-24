package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.TrainerResultDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.TrainerClassicFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.TrainerModeFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.TrainerQuickFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.SessionStorageManager;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;

/**
 * Trainer activity
 */
public class TrainerActivity extends FragmentActivity {
    public static final String PARAM_RESUME_SESSION_FLAG = "resume_session";
    public static final String PARAM_TRAINER_SETTINGS = "trainer_settings";
    private static final String KEY_TRAINER_MODE = "trainer_mode";
    public static final String PARAM_TABLES = "lists";
    private static final String TAG = "TrainerActivity";

    private TextView tColumnQuestion;
    private TextView tExercise;
    private TrainerSettings settings;
    private Trainer trainer;
    private MenuItem tTip;
    private SessionStorageManager ssm;
    private TrainerClassicFragment classicFragment;
    private TrainerModeFragment cTrainingFragment;
    private int trainingMode = -1;
    private TrainerModeFragment[] modeStorage = new TrainerModeFragment[2];

    private static final int modeClassicID = 0;
    private static final int modeQuickID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"oncreate0");
        super.onCreate(savedInstanceState);
        Log.d(TAG,"oncreate1");
        setContentView(R.layout.activity_trainer);
        setTitle(R.string.Trainer_Title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        tExercise = (TextView) findViewById(R.id.tTrainerExercise);
        tColumnQuestion = (TextView) findViewById(R.id.tTrainerExColumn);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        trainingMode = -1;

        initTrainer();

        // do not show vocable now, onPostCreate has to handle this
        setTrainingMode(settings.getInt(KEY_TRAINER_MODE, modeClassicID));
    }

    /**
     * Function to be called by fragments to request an update of the exercise question display
     */
    public void updateQuestion(){
        tExercise.setText(trainer.getQuestion());
        tColumnQuestion.setText(trainer.getColumnNameExercise());
    }

    /**
     * Set training mode to specified value<br>
     *     also sets trainingMode<br>
     * @param mode Mode to dispaly
     */
    private void setTrainingMode(final int mode){
        Log.d(TAG,"init fragments");
        if(trainingMode == mode) {
            Log.d(TAG,"GUI mode already set");
            return;
        }
        switch(mode){
            default:
                Log.w(TAG,"unknown training mode! "+mode);
                return;
            case modeClassicID:
                if(modeStorage[modeClassicID] == null) {
                    modeStorage[modeClassicID] = new TrainerClassicFragment();
                }
                cTrainingFragment = modeStorage[modeClassicID];
                break;
            case modeQuickID:
                if(modeStorage[modeQuickID] == null) {
                    modeStorage[modeQuickID] = new TrainerQuickFragment();
                }
                cTrainingFragment = modeStorage[modeQuickID];
                break;
        }
        cTrainingFragment.setTrainer(trainer);
        cTrainingFragment.setTrainerActivity(this);
        setFragment(cTrainingFragment);
        trainingMode = mode;
    }

    /**
     * Shows result dialog on training end
     */
    public void showResultDialog(){
        if(trainer.isFinished()){
            Callable callable = new Callable() {
                @Override
                public Object call() throws Exception {
                    Intent myIntent = new Intent(TrainerActivity.this, MainActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(myIntent);
                    return null;
                }
            };
            TrainerResultDialog resultDialog = TrainerResultDialog.newInstance(trainer,callable);
            resultDialog.show(getFragmentManager(),TAG);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d(TAG,"post create");
        showNextVocable();
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
        ssm = new SessionStorageManager(db);
        Intent intent = getIntent();
        boolean resume = intent.getBooleanExtra(PARAM_RESUME_SESSION_FLAG, false);
        ArrayList<VList> lists;
        if (resume) {
            Log.d(TAG, "resuming");
            settings = ssm.loadSession();
            lists = ssm.loadSessionTbls();
        } else {
            Log.d(TAG, "not resuming");
            lists = (ArrayList<VList>) intent.getSerializableExtra(PARAM_TABLES);
            if (lists == null) {
                Log.wtf(TAG, "Flag for lists passed but no lists received!");
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
                    } else if (!ssm.saveSessionTbls(lists)) {
                        Log.wtf(TAG, "unable to save session lists");
                    } else {
                        Log.d(TAG, "saved session");
                    }
                }

            }
        }
        trainer = new Trainer(lists, settings, getBaseContext(), !resume,ssm);
    }

    @Override
    protected void onStop() {
        super.onStop();
        trainer.saveVocState();

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_TRAINER_MODE,trainingMode);
        editor.apply();
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
                cTrainingFragment.showTip(trainer.getTip());
                return true;
            case R.id.tMenu_Classic:
                setTrainingMode(modeClassicID);
                break;
            case R.id.tMenu_Quick:
                setTrainingMode(modeQuickID);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Show next vocable of trainer
     */
    private void showNextVocable() {
        if (trainer.isFinished()) {
            showResultDialog();
        } else {
            updateTip();
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
