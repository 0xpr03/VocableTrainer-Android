package vocabletrainer.heinecke.aron.vocabletrainer.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;

/**
 * Dialog for VEntry editing
 */
public class VEntryEditorDialog extends DialogFragment {

    private final static int IMG_ADD = R.drawable.ic_add_black_24dp;
    private final static int IMG_REMOVE = R.drawable.ic_remove_black_24dp;
    public static final String TAG = "VEntryEditorDialog";
    private final static String PARAM_ENTRY = "entry";
    private Callable<Void> okAction;
    private Callable<Void> cancelAction;
    private VEntry entry;

    private LinearLayout meaningsA;
    private LinearLayout meaningsB;
    private TextInputLayout wrapperHint;
    private TextInputLayout wrapperAddition;
    private TextInputEditText tHint;
    private TextInputEditText tAddition;
    private int tagCounter = 0;


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

        View view = View.inflate(getActivity(), R.layout.dialog_entry, null);

        meaningsA = (LinearLayout) view.findViewById(R.id.meaningsA);
        meaningsB = (LinearLayout) view.findViewById(R.id.meaningsB);
        wrapperHint = (TextInputLayout) view.findViewById(R.id.wrapper_Hint);
        wrapperAddition = (TextInputLayout) view.findViewById(R.id.wrapper_Addition);
        tHint = (TextInputEditText) view.findViewById(R.id.tHint);
        tAddition = (TextInputEditText) view.findViewById(R.id.tAddition);

        alertDialog.setTitle(R.string.Editor_Diag_edit_Title);
        alertDialog.setView(view);

        generateMeanings(entry.getAMeanings(), entry.getList().getNameA(),meaningsA);
        generateMeanings(entry.getBMeanings(), entry.getList().getNameB(),meaningsB);

        tHint.setSingleLine();
        tHint.setText(entry.getTip());

        tAddition.setSingleLine();
        tAddition.setText(entry.getAddition());

        alertDialog.setPositiveButton(R.string.Editor_Diag_edit_btn_OK, (dialog, whichButton) -> {
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
                    okAction.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        alertDialog.setNegativeButton(R.string.Editor_Diag_edit_btn_CANCEL, (dialog, which) -> callCancelAction());
        return alertDialog.create();
    }

    /**
     * Retrieves meanings from layout input
     * @param layout Layout to traverse
     * @return
     */
    private List<String> getMeanings(LinearLayout layout) {
        List<String> lst = new ArrayList<>(3);

        for(int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            TextInputEditText text = (TextInputEditText) child.findViewById(R.id.meaning);
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
    private void generateMeanings(List<String> meanings, String hint, LinearLayout layout) {
        final String descAdd = getString(R.string.Editor_Meaning_Btn_Desc_Add);
        final String descRemove = getString(R.string.Editor_Meaning_Btn_Desc_Remove);
        final View.OnClickListener addListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.addView(generateMeaning("", hint,IMG_ADD, descAdd,this,true));

                for(int i = 0; i < layout.getChildCount() -1; i++) {
                    View child = layout.getChildAt(i);
                    ImageButton childBtn = (ImageButton) child.findViewById(R.id.btnMeaning);
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
            layout.addView(generateMeaning("", hint, IMG_ADD, descAdd, addListener,true));
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
        public DeleteAction(Object tag, ViewGroup group){
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
        View view = View.inflate(getActivity(),R.layout.editor_meaning,null);

        view.setTag(tagCounter);
        tagCounter++;
        TextInputLayout layout = (TextInputLayout) view.findViewById(R.id.wrapper_meaning);
        TextInputEditText text = (TextInputEditText) view.findViewById(R.id.meaning);
        ImageButton btn = (ImageButton) view.findViewById(R.id.btnMeaning);
        text.setSingleLine();

        if(focus)
            layout.requestFocus();

        layout.setHint(hint);
        text.setText(meaning);

        btn.setImageResource(image);
        btn.setContentDescription(description);
        btn.setOnClickListener(listener);

        return view;
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
