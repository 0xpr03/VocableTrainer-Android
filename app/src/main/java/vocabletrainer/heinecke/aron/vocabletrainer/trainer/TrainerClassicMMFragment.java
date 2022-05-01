package vocabletrainer.heinecke.aron.vocabletrainer.trainer;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
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

import vocabletrainer.heinecke.aron.vocabletrainer.R;

import static vocabletrainer.heinecke.aron.vocabletrainer.trainer.TrainerActivity.MAX;
import static vocabletrainer.heinecke.aron.vocabletrainer.trainer.TrainerActivity.MS_SEC;

/**
 * Trainer classic mode, multi meaning fragment
 */
public class TrainerClassicMMFragment extends TrainerModeFragment implements TrainerInput{
    private static final String TAG = "TClassicMMFragment";
    private static final String KEY_INPUT = "input";

    private TextView tHint;
    private LinearLayout inputLayout;
    private Button bSolve;
    private Button bShowNext;
    private TextView tColumnAnswer;
    private CountDownTimer timer;
    private MenuItem tTip;
    private ScrollView mainContainer;
    private TextInputLayout[] inputList;
    private Button bCheckInput;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trainer_classic_mm, container, false);

        tHint = view.findViewById(R.id.tTrainerQOut);
        tColumnAnswer = view.findViewById(R.id.tTrainerInputColumn);
        //tInput = (EditText) view.findViewById(R.id.tTrainerInput);
        inputLayout = view.findViewById(R.id.tInputLayout);
        bSolve = view.findViewById(R.id.bTrainerSolve);
        mainContainer = view.findViewById(R.id.tTrainerModeScroller);
        bCheckInput = view.findViewById(R.id.bTrainerEnter);
        bShowNext = view.findViewById(R.id.bTrainerShowNext);
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

        bShowNext.setOnClickListener(v -> showNextVocable());

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(KEY_INPUT,getData());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initInputs();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // happens after onActivityCreated
        // has to wait for inputs being created in the view
        if(savedInstanceState != null){
            ArrayList<String> savedInput = savedInstanceState.getStringArrayList(KEY_INPUT);
            int i = 0;
            while(i < inputList.length && i < savedInput.size()) {
                setInputValue(i,savedInput.get(i));
                i++;
            }
            Log.d(TAG,"inputs:"+inputList.length+" saved:"+savedInput.size()+" i:"+i);
        }else{
            Log.d(TAG,"no saved instance state");
        }
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
     * Input input list/group
     */
    private void initInputs(){
        Log.d(TAG,"initInputs");
        inputLayout.removeAllViews(); // delete previous views
        final int max = trainer.getAmountSolutionMeanings();
        inputList = new TextInputLayout[max];
        for(int i = 0; i < max; i++){
            View elem = generateInput();
            TextInputLayout input = elem.findViewById(R.id.tTrainerInput_wrapper);
            inputList[i] = input;
            inputLayout.addView(elem);
        }
    }

    /**
     * Generate input view with element
     * @return View with input element
     */
    private View generateInput(){
        return View.inflate(getActivity(),R.layout.trainer_input_elem,null);
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
    public ArrayList<String> getData() {
        ArrayList<String> lst = new ArrayList<>(inputList.length);
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

    @Override
    public void setInputValue(int inputNo, String newValue) {
        inputList[inputNo].getEditText().setText(newValue);
    }

    @Override
    public void setAmountInputs(int newAmount) {
        initInputs();
    }
}
