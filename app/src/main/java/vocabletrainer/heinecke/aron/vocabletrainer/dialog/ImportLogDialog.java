package vocabletrainer.heinecke.aron.vocabletrainer.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import vocabletrainer.heinecke.aron.vocabletrainer.R;

/**
 * Dialog showing the log after importing has finished.
 * @author Aron Heinecke
 */
public class ImportLogDialog extends DialogFragment {
    public static final String TAG = "DialogFragment";
    private static final String LOG = "log";
    /**
     * Creates a new instance
     */
    public static ImportLogDialog newInstance(final String log){
        ImportLogDialog dialog = new ImportLogDialog();
        Bundle args = new Bundle();
        args.putString(LOG, log);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.Import_Finished_Title);
        alertDialog.setMessage(getArguments().getString(LOG));

        alertDialog.setPositiveButton(R.string.GEN_Ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                getActivity().finish();
            }
        });
        return alertDialog.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getActivity().finish();
    }
}
