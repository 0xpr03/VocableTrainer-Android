package vocabletrainer.heinecke.aron.vocabletrainer.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Function;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;

/**
 * Dialog for VEntry editing
 */
public class VEntryEditorDialog extends DialogFragment {

    private final static int IMG_ADD = R.drawable.ic_add_black_24dp;
    private final static int IMG_REMOVE = R.drawable.ic_remove_black_24dp;
    public static final String TAG = "VEntryEditorDialog";
    private final static String KEY_INPUT_A = "inputA";
    private final static String KEY_INPUT_A_COUNT = "inputACount";
    private final static String KEY_INPUT_B = "inputB";
    private final static String KEY_INPUT_B_COUNT = "inputBCount";
    private final static String KEY_INPUT_HINT = "inputH";
    private final static String KEY_INPUT_ADDITION = "inputAd";
    private Function<Void,VEntry> okAction;
    private Function<Void,VEntry> cancelAction;
    private VEntry entry;

    private LinearLayout meaningsA;
    private LinearLayout meaningsB;
    private TextInputEditText tHint;
    private TextInputEditText tAddition;
    private int tagCounter = 0;

    /**
     * Creates a new instance
     * @return VListEditorDialog
     */
    public static VEntryEditorDialog newInstance(){
        return new VEntryEditorDialog();
    }

    /**
     * Set ok action to run afterwards
     * @param okAction
     */
    public void setOkAction(Function<Void,VEntry> okAction) {
        this.okAction = okAction;
    }

    /**
     * Set cancel action to run afterwards
     * @param cancelAction
     */
    public void setCancelAction(Function<Void,VEntry> cancelAction) {
        this.cancelAction = cancelAction;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<String> meaning = getMeanings(meaningsA);
        outState.putStringArrayList(KEY_INPUT_A,meaning);
        outState.putInt(KEY_INPUT_A_COUNT,meaning.size());
        meaning = getMeanings(meaningsB);
        outState.putStringArrayList(KEY_INPUT_B,meaning);
        outState.putInt(KEY_INPUT_B_COUNT,meaning.size());
        outState.putString(KEY_INPUT_HINT,tHint.getText().toString());
        outState.putString(KEY_INPUT_ADDITION,tAddition.getText().toString());
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View view = View.inflate(getActivity(), R.layout.dialog_entry, null);
        builder.setTitle(R.string.Editor_Diag_edit_Title);
        builder.setView(view);

        meaningsA = view.findViewById(R.id.meaningsA);
        meaningsB = view.findViewById(R.id.meaningsB);
        tHint = view.findViewById(R.id.tHint);
        tAddition = view.findViewById(R.id.tAddition);

        List<String> mLstA;
        List<String> mLstB;
        String tip;
        String addition;
        EditorDialogDataProvider provider = (EditorDialogDataProvider) getActivity();
        entry = provider.getEditVEntry();
        if(savedInstanceState == null){
            mLstA = entry.getAMeanings();
            mLstB = entry.getBMeanings();
            tip = entry.getTip();
            addition = entry.getAddition();
        } else {
            mLstA = savedInstanceState.getStringArrayList(KEY_INPUT_A);
            mLstB = savedInstanceState.getStringArrayList(KEY_INPUT_B);
            tip = savedInstanceState.getString(KEY_INPUT_HINT);
            addition = savedInstanceState.getString(KEY_INPUT_ADDITION);
        }

        assert mLstA != null;
        assert mLstB != null;
        generateMeanings(mLstA, entry.getList().getNameA(),meaningsA, true);
        generateMeanings(mLstB, entry.getList().getNameB(),meaningsB, false);
        tHint.setSingleLine();
        tHint.setText(tip);

        tAddition.setSingleLine();
        tAddition.setText(addition);

        builder.setPositiveButton(R.string.GEN_OK, (dialog, whichButton) -> {
            List<String> mA = getMeanings(meaningsA);
            List<String> mB = getMeanings(meaningsB);

            if (mA.size() == 0 || mB.size() == 0) {
                Log.d(TAG, "empty insert");
            }

            entry.setAMeanings(mA);
            entry.setBMeanings(mB);
            entry.setTip(tHint.getText().toString());
            entry.setAddition(tAddition.getText().toString());
            if(okAction != null){
                try {
                    okAction.function(entry);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(R.string.Editor_Diag_edit_btn_CANCEL, (dialog, which) -> callCancelAction());
        return builder.create();
    }

    /**
     * Retrieves meanings from layout input
     * @param layout Layout to traverse
     * @return List of meanings found in layout
     */
    private ArrayList<String> getMeanings(LinearLayout layout) {
        ArrayList<String> lst = new ArrayList<>(layout.getChildCount());

        for(int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            TextInputEditText text = child.findViewById(R.id.meaning);
            if(text.getText().length() > 0)
                lst.add(text.getText().toString());
        }

        return lst;
    }

    /**
     * Generate view with all meanings for specified list
     * @param meanings List of meanings to process
     * @param hint Hint for input
     * @param layout Layout to add views into
     */
    private void generateMeanings(List<String> meanings, String hint, LinearLayout layout, final boolean allowFocus) {
        final String descAdd = getString(R.string.Editor_Meaning_Btn_Desc_Add);
        final String descRemove = getString(R.string.Editor_Meaning_Btn_Desc_Remove);
        final View.OnClickListener addListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.addView(generateMeaning("", hint,IMG_ADD, descAdd,this,true));

                for(int i = 0; i < layout.getChildCount() -1; i++) {
                    View child = layout.getChildAt(i);
                    ImageButton childBtn = child.findViewById(R.id.btnMeaning);
                    childBtn.setImageResource(IMG_REMOVE);
                    childBtn.setOnClickListener(new DeleteAction(child.getTag(),layout));
                }
            }
        };

        if(meanings.size() > 0) {
            for (int i = 0; i < meanings.size() - 1; i++) {
                layout.addView(generateMeaning(meanings.get(i), hint, IMG_REMOVE, descRemove, new DeleteAction(tagCounter,layout),false));
            }
            layout.addView(generateMeaning(meanings.get(meanings.size() - 1), hint, IMG_ADD, descAdd, addListener,false));
        }else{
            layout.addView(generateMeaning("", hint, IMG_ADD, descAdd, addListener,allowFocus));
        }
    }

    /**
     * {@link android.view.View.OnClickListener} for meaning delete action
     */
    private class DeleteAction implements View.OnClickListener {
        private final Object tag;
        private final ViewGroup group;

        /**
         * Create a new delete action
         * @param tag Tag of view to delete on click
         * @param group parent in which to delete
         */
        DeleteAction(Object tag, ViewGroup group){
            this.tag = tag;
            this.group = group;
        }

        @Override
        public void onClick(View v) {
            group.removeView(group.findViewWithTag(tag));
        }
    }

    /**
     * Generate view for meaning entry
     * @param meaning
     * @param hint
     * @param image Button image resource ID
     * @param description Button description
     * @param listener button listener
     * @return View
     */
    private View generateMeaning(final String meaning, final String hint,int image, String description, View.OnClickListener listener,
                                 boolean focus){
        final RelativeLayout container = (RelativeLayout) View.inflate(getActivity(),R.layout.editor_meaning,null);

        container.setTag(tagCounter);
        tagCounter++;
        final TextInputLayout layout = container.findViewById(R.id.wrapper_meaning);
        final TextInputEditText text = container.findViewById(R.id.meaning);
        ImageButton btn = container.findViewById(R.id.btnMeaning);
        text.setSingleLine();

        if(focus)
            layout.requestFocus();

        layout.setHint(hint);
        text.setText(meaning);

        btn.setImageResource(image);
        btn.setContentDescription(description);
        btn.setOnClickListener(listener);

        return container;
    }

    /**
     * Get editor VEntry
     * @return
     */
    public VEntry getEntry() {
        return entry;
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
                cancelAction.function(entry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Required interface for parent
     */
    public interface EditorDialogDataProvider {
        @NonNull VEntry getEditVEntry();
    }
}
