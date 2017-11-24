package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.View;

import vocabletrainer.heinecke.aron.vocabletrainer.activity.TrainerActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

/**
 * Base class for trainer mode fragments<br>
 *     The following variables are provided: trainerActivity trainer
 */
public abstract class TrainerModeFragment extends BaseFragment {
    protected TrainerActivity trainerActivity;
    protected Trainer trainer;
    /**
     * Force call to show the specified vocable & update the gui accordingly<br>
     *     called upon view created
     */
    public abstract void showVocable();

    @Override
    @CallSuper
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        showVocable();
    }

    /**
     * Show specified tip, called by appbar handler
     * @param tip
     */
    public abstract void showTip(String tip);

    /**
     * Set trainer to use
     * @param trainer
     */
    public final void setTrainer(final Trainer trainer){
        this.trainer = trainer;
    }

    /**
     * Sets trainer activity to callback
     * @param trainerActivity
     */
    public final void setTrainerActivity (final TrainerActivity trainerActivity) {
        this.trainerActivity = trainerActivity;
    }
}
