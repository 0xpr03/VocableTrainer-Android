package vocabletrainer.heinecke.aron.vocabletrainer.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import java.util.concurrent.Callable;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

/**
 * Dialog for VEntry editing
 */
public class VEntryEditorDialog extends DialogFragment {
    public static final String TAG = "VEntryEditorDialog";
    private final static String PARAM_ENTRY = "entry";
    private Callable<Void> okAction;
    private Callable<Void> cancelAction;
    private VEntry entry;

    /**
     * Creates a new instance
     * @param entry VEntry to edit
     * @return VListEditorDialog
     */
    public static VEntryEditorDialog newInstance(final VEntry entry){
        VEntryEditorDialog dialog = new VEntryEditorDialog();

        Bundle bundle = new Bundle();

        bundle.putSerializable(PARAM_ENTRY, entry);
        dialog.setArguments(bundle);

        return dialog;
    }

    /**
     * Set ok action to run afterwards
     * @param okAction
     */
    public void setOkAction(Callable<Void> okAction) {
        this.okAction = okAction;
    }

    /**
     * Set cancel action to run afterwards
     * @param cancelAction
     */
    public void setCancelAction(Callable<Void> cancelAction) {
        this.cancelAction = cancelAction;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            entry = (VEntry) getArguments().getSerializable(PARAM_ENTRY);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setMessage(R.string.Editor_Diag_edit_Title);
        final EditText iColA = new EditText(getActivity());
        final EditText iColB = new EditText(getActivity());
        final EditText iColTip = new EditText(getActivity());
        iColA.setText(entry.getAWord());
        iColA.setSingleLine();
        iColA.setHint(R.string.Editor_Default_Column_A);
        iColB.setHint(R.string.Editor_Default_Column_B);
        iColTip.setHint(R.string.Editor_Default_Tip);
        iColB.setText(entry.getBWord());
        iColB.setSingleLine();
        iColTip.setSingleLine();
        iColTip.setText(entry.getTip());

        LinearLayout rl = new TableLayout(getActivity());
        rl.addView(iColA);
        rl.addView(iColB);
        rl.addView(iColTip);
        alertDialog.setView(rl);

        alertDialog.setPositiveButton(R.string.Editor_Diag_edit_btn_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (iColB.getText().length() == 0 || iColTip.length() == 0 || iColA.getText().length() == 0) {
                    Log.d(TAG, "empty insert");
                }

                entry.setAWord(iColA.getText().toString());
                entry.setBWord(iColB.getText().toString());
                entry.setTip(iColTip.getText().toString());
                if(okAction != null){
                    try {
                        okAction.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        alertDialog.setNegativeButton(R.string.Editor_Diag_edit_btn_CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callCancelAction();
            }
        });
        return alertDialog.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callCancelAction();
    }

    /**
     * Calls cancel action
     */
    private void callCancelAction(){
        if(cancelAction != null){
            try {
                cancelAction.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
