package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import vocabletrainer.heinecke.aron.vocabletrainer.R;

/**
 * Trainer quick mode fragment
 */
public class TrainerQuickFragment extends TrainerModeFragment {

    private static final String TAG = "TQuickFragment";
    private View view;
    private Button bRevolse;
    private Button bCorrect;
    private Button bWrong;
    private TextView tSolution;
    private TextView tColumnQuestion;
    private TextView tColumnAnswer;
    private int guiState = guiStateInit;
    private static final int guiStateSolution = 1;
    private static final int guiStateQuestion = 0;
    private static final int guiStateInit = -1;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_trainer_quick, container, false);

        bRevolse = (Button) view.findViewById(R.id.bTrainerQResolve);
        bCorrect = (Button) view.findViewById(R.id.bTrainerQCorrect);
        bWrong = (Button) view.findViewById(R.id.bTrainerQWrong);
        tSolution = (TextView) view.findViewById(R.id.tTrainerQOut);
        tColumnQuestion = (TextView) view.findViewById(R.id.tTrainerExColumn);
        tColumnAnswer = (TextView) view.findViewById(R.id.tTrainerInputColumn);

        bRevolse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder builder = new StringBuilder();
                builder.append(trainer.getSolutionUncounted());
                builder.append("\n\n");
                builder.append(trainer.getCurrentAddition());
                tSolution.setText(builder.toString());
                changeView(guiStateSolution);
            }
        });

        bCorrect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextVocable(true);
            }
        });

        bWrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextVocable(false);
            }
        });

        return view;

    }

    /**
     * set vocable as correct/wrong and show next one
     * @param correct
     */
    private void showNextVocable(final boolean correct){
        Log.d(TAG,"showNextVocable(correct:"+correct+")");
        trainer.updateVocable(correct);
        if(trainer.isFinished()){
            trainerActivity.showResultDialog();
        }else {
            showVocable();
        }
    }

    /**
     * Change visibility according to parameter
     * @param newGuiState display mode
     */
    private void changeView(final int newGuiState){
        if(guiState == newGuiState) {
            return;
        }
        boolean showSolution = newGuiState == guiStateSolution;
        tSolution.setVisibility(showSolution ? View.VISIBLE : View.GONE);
        bCorrect.setVisibility(showSolution ? View.VISIBLE : View.GONE);
        bWrong.setVisibility(showSolution ? View.VISIBLE : View.GONE);
        bRevolse.setVisibility(showSolution ? View.GONE : View.VISIBLE);
        guiState = newGuiState;
    }

    @Override
    public void showVocable() {
        Log.d(TAG,"showVocable");
        trainerActivity.updateQuestion();
        tSolution.setText("");
        tColumnAnswer.setText(trainer.getColumnNameSolution());
        changeView(guiStateQuestion);
    }

    @Override
    public void showTip(String tip) {
        tSolution.setVisibility(View.VISIBLE);
        tSolution.setText(tip);
    }
}
