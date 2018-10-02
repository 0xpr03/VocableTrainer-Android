package vocabletrainer.heinecke.aron.vocabletrainer.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import java.util.concurrent.Callable;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.Trainer;

/**
 * Trainer result dialog fragment<br>
 *     Shows result data about the finished training
 *
 */
public class TrainerResultDialog extends DialogFragment {
    public final static String TAG = "TrainerResultDialog";
    private Trainer trainer;
    private Callable finishAction;

    /**
     * Creates a new instance
     * @param trainer Trainer instance to use
     * @return VListEditorDialog
     */
    public static TrainerResultDialog newInstance(final Trainer trainer, final Callable callable){
        TrainerResultDialog dialog = new TrainerResultDialog();
        dialog.setTrainer(trainer);
        dialog.setFinishAction(callable);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    /**
     * Set finish action
     * @param callable
     */
    private void setFinishAction(final Callable callable){
        this.finishAction = callable;
    }

    /**
     * Set trainer to use for data gathering<br>
     *     should not be called directly
     * @param trainer
     */
    private void setTrainer(Trainer trainer){
        this.trainer = trainer;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.Trainer_Diag_finished_Title);
        alertDialog.setMessage(R.string.Trainer_Diag_finished_MSG);

        alertDialog.setPositiveButton(R.string.Trainer_btn_ok, (dialog, whichButton) -> callFinishAction());
        return alertDialog.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callFinishAction();
    }

    /**
     * Calls finish action
     */
    private void callFinishAction(){
        if(finishAction != null){
            try {
                finishAction.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
