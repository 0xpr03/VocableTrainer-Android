package vocabletrainer.heinecke.aron.vocabletrainer.dialog;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel.ImportViewModel;

/**
 * Dialog showing the log after importing has finished.
 * @author Aron Heinecke
 */
public class ImportLogDialog extends DialogFragment {
    public static final String TAG = "LogFragment";
    private static final String LOG = "log";
    private static final String PREVIEW = "preview";
    /**
     * Creates a new instance
     */
    public static ImportLogDialog newInstance(final ImportViewModel.LogData log){
        ImportLogDialog dialog = new ImportLogDialog();
        Bundle args = new Bundle();
        args.putString(LOG, log.log);
        args.putBoolean(PREVIEW,log.isPreview);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getArguments().getBoolean(PREVIEW) ?
                R.string.Import_Preview_Log_Title : R.string.Import_Finished_Title);
        alertDialog.setMessage(getArguments().getString(LOG));

        alertDialog.setPositiveButton(R.string.GEN_OK, (dialog, whichButton) -> this.dismiss());
        return alertDialog.create();
    }
}
