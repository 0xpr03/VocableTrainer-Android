package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.ItemPickerDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.TrainerResultDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.TrainerClassicFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.TrainerClassicMMFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.TrainerModeFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.TrainerQuickFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.SessionStorageManager;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.Trainer;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;

/**
 * Trainer activity
 */
public class TrainerActivity extends FragmentActivity implements TrainerModeFragment.TrainingFragmentHolder, ItemPickerDialog.ItemPickerHandler {
    public static final String PARAM_TRAINER_SETTINGS = "trainer_settings";
    private static final String KEY_TRAINER_MODE = "trainer_mode";
    private static final String P_KEY_MODE_DIALOG = "mode_dialog";
    public static final String PARAM_TABLES = "lists";
    private static final String TAG = "TrainerActivity";
    private static final String KEY_TRAINER = "trainer";
    private static final String KEY_FRAGMENT = "fragment";
    private static final String KEY_FRAGMENT_NR = "fragment_nr";

    public static final int MAX = 4;
    public static final int MS_SEC = 1000;

    private TextView tColumnQuestion;
    private TextView tExercise;
    private TrainerSettings settings;
    private Trainer trainer;
    private MenuItem tTip;
    private SessionStorageManager ssm;
    private TrainerModeFragment cTrainingFragment;
    private int trainingMode = -1;
    private TrainerModeFragment[] modeStorage = new TrainerModeFragment[3];
    private ItemPickerDialog modeDialog;

    private static final int modeClassicID = 0;
    private static final int modeQuickID = 1;
    private static final int modeClassicMMID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"oncreate0");
        super.onCreate(savedInstanceState);
        Log.d(TAG,"oncreate1");
        setContentView(R.layout.activity_trainer);
        setTitle(R.string.Trainer_Title);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if(savedInstanceState != null){
            modeDialog = (ItemPickerDialog) getSupportFragmentManager().getFragment(savedInstanceState,P_KEY_MODE_DIALOG);
            if(modeDialog != null)
                modeDialog.setItemPickerHandler(this);
        }

        tExercise = findViewById(R.id.tTrainerExercise);
        tColumnQuestion = findViewById(R.id.tTrainerExColumn);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        trainingMode = -1;

        initTrainer(savedInstanceState);

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
     * @param mode Mode to display
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
            case modeClassicMMID:
                if(modeStorage[modeClassicMMID] == null) {
                    modeStorage[modeClassicMMID] = new TrainerClassicMMFragment();
                }
                cTrainingFragment = modeStorage[modeClassicMMID];
                break;
        }
        setFragment(cTrainingFragment);
        trainingMode = mode;
    }

    /**
     * Shows result dialog on training end
     */
    public void showResultDialog(){
        if(trainer.isFinished()){
            Callable<?> callable = () -> {
                Intent myIntent = new Intent(TrainerActivity.this, MainActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myIntent);
                return null;
            };
            TrainerResultDialog resultDialog = TrainerResultDialog.newInstance(trainer,callable);
            resultDialog.show(getSupportFragmentManager(),TAG);
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
    private void initTrainer(@Nullable Bundle savedInstanceState) {
        final Database db = new Database(getBaseContext());
        ssm = new SessionStorageManager(db);
        settings = ssm.loadSession();
        trainer = new Trainer(settings, getBaseContext(), ssm);
        if (savedInstanceState != null){
            int fragmentNr = savedInstanceState.getInt(KEY_FRAGMENT_NR);
            modeStorage[fragmentNr] = (TrainerModeFragment) getSupportFragmentManager().getFragment(savedInstanceState,KEY_FRAGMENT);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_FRAGMENT_NR,trainingMode);
        getSupportFragmentManager().putFragment(outState,KEY_FRAGMENT,cTrainingFragment);
        if(modeDialog != null && modeDialog.isAdded())
            getSupportFragmentManager().putFragment(outState,P_KEY_MODE_DIALOG,modeDialog);
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
        int id = item.getItemId();
        if (id == R.id.tMenu_Tip) {
                cTrainingFragment.showTip(trainer.getTip());
                return true;
        } else if (id == R.id.tMenu_Mode) {
                modeDialog = ItemPickerDialog.newInstance(R.array.training_modes,R.string.Trainer_Menu_Mode);
                modeDialog.setItemPickerHandler(this);
                modeDialog.show(getSupportFragmentManager(),P_KEY_MODE_DIALOG);
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
            if(settings.allowTips && trainer != null){
                tTip.setEnabled(trainer.hasTip());
            } else {
                tTip.setEnabled(false);
            }
        }
    }

    @Override
    public Trainer getTrainer() {
        return trainer;
    }

    @Override
    public TrainerSettings getTrainerSettings() {
        return settings;
    }

    @Override
    public void onItemPickerSelected(int position) {
        switch(position){
            default:
            case 0:
                setTrainingMode(modeClassicID);
                break;
            case 2:
                setTrainingMode(modeQuickID);
                break;
            case 1:
                setTrainingMode(modeClassicMMID);
                break;
        }
    }
}
