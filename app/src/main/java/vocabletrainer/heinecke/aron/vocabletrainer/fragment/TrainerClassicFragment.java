package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
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

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.TrainerActivity.MAX;
import static vocabletrainer.heinecke.aron.vocabletrainer.activity.TrainerActivity.MS_SEC;

/**
 * Trainer classic mode fragment
 */
public class TrainerClassicFragment extends TrainerModeFragment {
    private static final String TAG = "TClassicFragment";
    private static final String KEY_INPUT = "input";

    private TextView tHint;
    private EditText tInput;
    private Button bSolve;
    private Button bShowNext;
    private TextView tColumnAnswer;
    private MenuItem tTip;
    private SessionStorageManager ssm;
    private View view;
    private Button bCheckInput;
    private CountDownTimer timer;
    private String strShowNext;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_trainer_classic, container, false);
        strShowNext = getString(R.string.Trainer_btn_Show_Next);
        tHint = (TextView) view.findViewById(R.id.tTrainerQOut);
        tColumnAnswer = (TextView) view.findViewById(R.id.tTrainerInputColumn);
        tInput = (EditText) view.findViewById(R.id.tTrainerInput);
        bSolve = (Button) view.findViewById(R.id.bTrainerSolve);
        bCheckInput = (Button) view.findViewById(R.id.bTrainerEnter);
        bShowNext = (Button) view.findViewById(R.id.bTrainerShowNext);

        bCheckInput.setOnClickListener(v -> checkInput());

        bSolve.setOnClickListener(v -> solve());

        bShowNext.setOnClickListener(v -> showNextVocable());

        if(savedInstanceState != null)
            tInput.setText(savedInstanceState.getString(KEY_INPUT));

        return view;
    }

    /**
     * Show next vocable of trainer
     */
    private void showNextVocable() {
        Log.d(TAG,"showNextVocable()");
        if(timer != null)
            timer.cancel();
            timer = null;
        if(trainer.isFinished()){
            trainerActivity.showResultDialog();
        }else {
            showAdditionView(false);
            trainerActivity.updateQuestion();
            tInput.setError(null);
            tHint.setText("");
            tInput.setText("");
            tInput.requestFocus();
            bSolve.setEnabled(true);
            updateTip();
            tColumnAnswer.setText(trainer.getColumnNameSolution());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_INPUT,tInput.getText().toString());
    }

    /**
     * Show vocable addition field view
     * @param show
     */
    private void showAdditionView(boolean show){
        bSolve.setVisibility(show ? View.GONE : View.VISIBLE);
        bCheckInput.setVisibility(show ? View.GONE : View.VISIBLE);
        bShowNext.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Display addition view with timeout
     */
    private void displayAdditionTimed() {
        showAdditionView(true);

        timer = new CountDownTimer(MAX * 1000, MS_SEC) {
            @Override
            public void onTick(long l) {
                if(bShowNext != null && isAdded()) // rotation during countdown
                    bShowNext.setText(getString(R.string.Trainer_btn_Show_Next_Auto,l/MS_SEC));
                else
                    this.cancel();
            }

            @Override
            public void onFinish() {
                showNextVocable();
            }
        };
        timer.start();
    }

    /**
     * Verify input against solution
     */
    private void checkInput() {
        if (trainer.checkSolution(tInput.getText().toString())) {
            if(trainer.hasLastAddition()) {
                if(settings.additionAuto){
                    displayAdditionTimed();
                } else {
                    showAdditionView(true);
                    bShowNext.setText(R.string.Trainer_btn_Show_Next);
                }
                tHint.setText(trainer.getLastAddition());
            } else {
                showNextVocable();
            }
        } else {
            tInput.setSelectAllOnFocus(true);
            tInput.setError(INPUT_INVALID);
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
