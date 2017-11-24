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
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

/**
 * Dialog for list metadata editing
 */
public class VListEditorDialog extends DialogFragment {
    public static final String TAG = "VListEditorDialog";
    private final static String PARAM_LIST = "list";
    private final static String PARAM_NEW = "is_new";
    private Callable<Void> okAction;
    private Callable<Void> cancelAction;
    private boolean newList;
    private VList list;

    /**
     * Creates a new instance
     * @param isNew true if a new list is created
     * @param list List to edit
     * @return VListEditorDialog
     */
    public static VListEditorDialog newInstance(final boolean isNew, final VList list){
        VListEditorDialog dialog = new VListEditorDialog();

        Bundle bundle = new Bundle();

        bundle.putSerializable(PARAM_LIST, list);
        bundle.putBoolean(PARAM_NEW, isNew);
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
            list = (VList) getArguments().getSerializable(PARAM_LIST);
            newList = getArguments().getBoolean(PARAM_NEW);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setMessage(newList ? R.string.Editor_Diag_table_Title_New : R.string.Editor_Diag_table_Title_Edit );
        final EditText iName = new EditText(getActivity());
        final EditText iColA = new EditText(getActivity());
        final EditText iColB = new EditText(getActivity());
        iName.setText(list.getName());
        iName.setSingleLine();
        iName.setHint(R.string.Editor_Default_List_Name);
        iColA.setHint(R.string.Editor_Default_Column_A);
        iColB.setHint(R.string.Editor_Default_Column_B);
        iColA.setText(list.getNameA());
        iColA.setSingleLine();
        iColB.setSingleLine();
        iColB.setText(list.getNameB());
        if (newList) {
            iName.setSelectAllOnFocus(true);
            iColA.setSelectAllOnFocus(true);
            iColB.setSelectAllOnFocus(true);
        }

        LinearLayout rl = new TableLayout(getActivity());
        rl.addView(iName);
        rl.addView(iColA);
        rl.addView(iColB);
        alertDialog.setView(rl);

        alertDialog.setPositiveButton(R.string.Editor_Diag_table_btn_Ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (iColA.getText().length() == 0 || iColB.length() == 0 || iName.getText().length() == 0) {
                    Log.d(TAG, "empty insert");
                }

                list.setNameA(iColA.getText().toString());
                list.setNameB(iColB.getText().toString());
                list.setName(iName.getText().toString());
                if(okAction != null){
                    try {
                        okAction.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        alertDialog.setNegativeButton(R.string.Editor_Diag_table_btn_Canel, new DialogInterface.OnClickListener() {
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
