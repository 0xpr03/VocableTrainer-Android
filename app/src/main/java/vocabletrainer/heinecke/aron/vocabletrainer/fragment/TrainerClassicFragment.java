package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.SessionStorageManager;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;

/**
 * Trainer activity
 */
public class TrainerClassicFragment extends TrainerModeFragment {
    public static final String PARAM_RESUME_SESSION_FLAG = "resume_session";
    public static final String PARAM_TRAINER_SETTINGS = "trainer_settings";
    public static final String PARAM_TABLES = "lists";
    private static final String TAG = "TrainerActivity";

    private TextView tHint;
    private EditText tInput;
    private Button bSolve;
    private TrainerSettings settings;
    private TextView tColumnAnswer;
    private MenuItem tTip;
    private SessionStorageManager ssm;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_trainer_classic, container, false);

        tHint = (TextView) view.findViewById(R.id.tTrainerQOut);
        tColumnAnswer = (TextView) view.findViewById(R.id.tTrainerInputColumn);
        tInput = (EditText) view.findViewById(R.id.tTrainerInput);
        bSolve = (Button) view.findViewById(R.id.bTrainerSolve);
        Button bCheckInput = (Button) view.findViewById(R.id.bTrainerEnter);

        bCheckInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInput();
            }
        });

        bSolve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solve();
            }
        });

        return view;
    }

    /**
     * Show next vocable of trainer
     */
    private void showNextVocable() {
        Log.d(TAG,"showNextVocable()");
        if(trainer.isFinished()){
            trainerActivity.showResultDialog();
        }else {
            trainerActivity.updateQuestion();
            tHint.setText("");
            tInput.setText("");
            tInput.requestFocus();
            bSolve.setEnabled(true);
            updateTip();
            tColumnAnswer.setText(trainer.getColumnNameSolution());
        }
    }

    /**
     * Verify input against solution
     */
    private void checkInput() {
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
    private void solve() {
        tInput.setText(trainer.getSolution());
        bSolve.setEnabled(false);
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

    @Override
    public void showVocable() {
        Log.d(TAG,"showVocable");
        showNextVocable();
    }

    @Override
    public void showTip(String tip) {
        tHint.setText(tip);
    }
}
