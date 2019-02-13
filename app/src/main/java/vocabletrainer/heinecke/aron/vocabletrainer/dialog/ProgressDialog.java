package vocabletrainer.heinecke.aron.vocabletrainer.dialog;

import android.annotation.SuppressLint;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import vocabletrainer.heinecke.aron.vocabletrainer.R;

/**
 * Dialog showing progress during import/export and preview parsing.
 * Capable of two modes: indefinite mode, displaying the actual progress data and actual progress mode<br>
 * Works with a LiveData<Integer>
 * @author Aron Heinecke
 */
public class ProgressDialog extends DialogFragment {
    public static final String TAG = "ProgressDialog";
    private static final String P_KEY_MAX = "max";
    private static final String P_KEY_TITLE = "title";
    private static final String P_KEY_MODE = "mode";
    private static final String P_KEY_PROGRESS = "progress";
    private ProgressBar progressBar;
    private LiveData<Integer> progressData;
    private MutableLiveData<Boolean> cancelHandle;
    private boolean indeterminateMode = true;
    private TextView messageView, titleView;
    private Button btnCancel;
    private int max = 0;
    private @StringRes int title = R.string.Placeholder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(P_KEY_MAX,max);
        outState.putInt(P_KEY_TITLE,title);
        outState.putBoolean(P_KEY_MODE,indeterminateMode);
        outState.putInt(P_KEY_PROGRESS,progressBar.getProgress());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDisplayMode();
        this.setCancelable(false);
    }

    /**
     * Creates a new instance
     */
    public static ProgressDialog newInstance(){
        ProgressDialog dialog = new ProgressDialog();
        Bundle args = new Bundle();
        dialog.setArguments(args);
        return dialog;
    }

    /**
     * Update display mode for progress with option to enable cancel button
     * @param indeterminateMode
     * @param max
     * @param title Title of dialog
     * @param cancelHandle Cancel handle to use, on null the cancel button is disabled
     */
    public void setDisplayMode(final boolean indeterminateMode, final int max, final @StringRes int title,@Nullable MutableLiveData<Boolean> cancelHandle){
        this.indeterminateMode = indeterminateMode;
        this.title = title;
        this.max = max;
        this.cancelHandle = cancelHandle;
        if(this.isVisible()){
            updateDisplayMode();
        }
    }

    /**
     * Update UI to reflect set display mode
     */
    private void updateDisplayMode(){
        progressBar.setIndeterminate(indeterminateMode);
        if(!indeterminateMode){
            progressBar.setMax(max);
        }
        messageView.setVisibility(indeterminateMode ? View.VISIBLE : View.GONE);
        titleView.setText(title);
        btnCancel.setEnabled(cancelHandle != null);
    }

    /**
     * Set progress LiveData to use<br>
     *     removes previous livedata handle
     * @param progressHandle
     */
    @SuppressLint("SetTextI18n")
    public void setProgressHandle(LiveData<Integer> progressHandle){
        if(this.progressData != null){
            this.progressData.removeObservers(this);
        }
        this.progressData = progressHandle;
        progressHandle.observe(this,data -> {
            if(data != null && this.isVisible()){
                if(indeterminateMode){
                    messageView.setText(Integer.toString(data));
                } else {
                    progressBar.setProgress(data);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(savedInstanceState != null){
            max = savedInstanceState.getInt(P_KEY_MAX);
            title = savedInstanceState.getInt(P_KEY_TITLE);
            indeterminateMode = savedInstanceState.getBoolean(P_KEY_MODE);
        }
        View view = View.inflate(getContext(),R.layout.dialog_progress, null);

        progressBar = view.findViewById(R.id.dialog_progressbar);
        messageView = view.findViewById(R.id.dialog_message);
        titleView = view.findViewById(R.id.dialog_title);
        btnCancel = view.findViewById(R.id.button_close);
        btnCancel.setOnClickListener(v -> cancelHandle.setValue(true));
        updateDisplayMode();
        if(savedInstanceState != null)
            progressBar.setProgress(savedInstanceState.getInt(P_KEY_PROGRESS));
        return view;
    }


}
