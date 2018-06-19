package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.SessionStorageManager;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.TrainerInput;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.TrainerActivity.MAX;
import static vocabletrainer.heinecke.aron.vocabletrainer.activity.TrainerActivity.MS_SEC;

/**
 * Trainer classic mode, multi meaning fragment
 */
public class TrainerClassicMMFragment extends TrainerModeFragment implements TrainerInput{
    private static final String TAG = "TClassicMMFragment";

    private TextView tHint;
    private LinearLayout inputLayout;
    private Button bSolve;
    private Button bShowNext;
    private TextView tColumnAnswer;
    private CountDownTimer timer;
    private MenuItem tTip;
    private SessionStorageManager ssm;
    private View view;
    private ScrollView mainContainer;
    private TextInputLayout[] inputList;
    private Button bCheckInput;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_trainer_classic_mm, container, false);

        tHint = (TextView) view.findViewById(R.id.tTrainerQOut);
        tColumnAnswer = (TextView) view.findViewById(R.id.tTrainerInputColumn);
        //tInput = (EditText) view.findViewById(R.id.tTrainerInput);
        inputLayout = (LinearLayout) view.findViewById(R.id.tInputLayout);
        bSolve = (Button) view.findViewById(R.id.bTrainerSolve);
        mainContainer = (ScrollView) view.findViewById(R.id.tTrainerModeScroller);
        bCheckInput = (Button) view.findViewById(R.id.bTrainerEnter);
        bShowNext = (Button) view.findViewById(R.id.bTrainerShowNext);
        //mainContainer.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        bCheckInput.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) {
                Log.d(TAG,"hiding keyboard");
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
            for(TextInputLayout i : inputList)
                i.clearFocus();
            checkInput();
        });

        bSolve.setOnClickListener(v -> solve());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initInputs();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
                bShowNext.setText(getString(R.string.Trainer_btn_Show_Next_Auto,l/MS_SEC));
            }

            @Override
            public void onFinish() {
                showNextVocable();
            }
        };
        timer.start();
    }

    /**
     * Input input list/group
     */
    private void initInputs(){
        Log.d(TAG,"initInputs");
        inputLayout.removeAllViews(); // delete previous views
        final int max = trainer.getAmountSolutionMeanings();
        inputList = new TextInputLayout[max];
        for(int i = 0; i < max; i++){
            View elem = generateInput();
            TextInputLayout input = (TextInputLayout) elem.findViewById(R.id.tTrainerInput_wrapper);
            inputList[i] = input;
            inputLayout.addView(elem);
        }
    }

    /**
     * Generate input view with element
     * @return View with input element
     */
    private View generateInput(){
        View tView = View.inflate(getActivity(),R.layout.trainer_input_elem,null);
        return tView;
    }

    /**
     * Show next vocable of trainer
     */
    private void showNextVocable() {
        Log.d(TAG,"showNextVocable()");
        if(timer != null)
            timer.cancel();
        if(trainer.isFinished()){
            trainerActivity.showResultDialog();
        }else {
            showAdditionView(false);
            trainerActivity.updateQuestion();
            tHint.setText("");
            initInputs();
            inputList[0].getEditText().requestFocus();
            bSolve.setEnabled(true);
            updateTip();
            tColumnAnswer.setText(trainer.getColumnNameSolution());
        }
    }

    /**
     * Verify input against solution
     */
    private void checkInput() {
        if (trainer.checkSolutions(this)) {
            if(trainer.hasLastAddition()) {
                if (settings.additionAuto) {
                    displayAdditionTimed();
                } else {
                    showAdditionView(true);
                    bShowNext.setText(R.string.Trainer_btn_Show_Next);
                }
                tHint.setText(trainer.getLastAddition());
                tHint.requestFocus();
                mainContainer.smoothScrollTo(0,tHint.getBottom());
            } else {
                showNextVocable();
            }
        } else {
//            tInput.setSelectAllOnFocus(true);
//            tInput.requestFocus(); TODO
        }
    }

    /**
     * Solve current vocable
     */
    private void solve() {
        bSolve.setEnabled(false);
        trainer.getSolutions(this);
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

    @Override
    public List<String> getData() {
        List<String> lst = new ArrayList<>(inputList.length);
        for(TextInputLayout input : inputList){
            lst.add(input.getEditText().getText().toString());
        }
        return lst;
    }

    @Override
    public void setInputState(int inputNo, INPUT_STATE newState) {
        TextInputLayout input = inputList[inputNo];
        switch (newState) {
            case VALID:
                input.setError(null);
                input.getEditText().setError(null);
                break;
            case INVALID:
                input.setError(null);
                input.getEditText().setError(INPUT_INVALID);
                break;
            case DUPLICATE:
                input.setError(INPUT_DOUBLED);
                input.getEditText().setError(INPUT_DOUBLED);
                break;
        }
    }

    private void clearInput(){
        for(TextInputLayout input : inputList){
            input.getEditText().setText("");
        }
    }

    @Override
    public void setInputValue(int inputNo, String newValue) {
        inputList[inputNo].getEditText().setText(newValue);
    }

    @Override
    public void setAmountInputs(int newAmount) {
        initInputs();
    }
}
