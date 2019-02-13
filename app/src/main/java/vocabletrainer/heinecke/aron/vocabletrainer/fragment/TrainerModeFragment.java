package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.Trainer;

/**
 * Base class for trainer mode fragments<br>
 *     The following variables are provided: trainerActivity trainer
 */
public abstract class TrainerModeFragment extends BaseFragment {
    protected TrainingFragmentHolder trainerActivity;
    protected Trainer trainer;
    protected TrainerSettings settings;
    protected String INPUT_CORRECT;
    protected String INPUT_DOUBLED;
    protected String INPUT_INVALID;

    /**
     * Force call to show the specified vocable & update the gui accordingly<br>
     *     called upon view created
     */
    public abstract void showVocable();

    @Override
    @CallSuper
    public void onAttach(Context context) {
        super.onAttach(context);
        trainerActivity = (TrainingFragmentHolder) context;
    }

    @CallSuper
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        trainer = trainerActivity.getTrainer();
        settings = trainerActivity.getTrainerSettings();
        showVocable();
    }

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        INPUT_CORRECT = getString(R.string.Trainer_Input_Correct);
        INPUT_DOUBLED = getString(R.string.Trainer_Input_Double);
        INPUT_INVALID = getString(R.string.Trainer_Input_Invalid);
    }

    /**
     * Show specified tip, called by appbar handler
     * @param tip
     */
    public abstract void showTip(String tip);

    public interface TrainingFragmentHolder {
        Trainer getTrainer();
        TrainerSettings getTrainerSettings();
        void showResultDialog();
        void updateQuestion();
    }
}
