package vocabletrainer.heinecke.aron.vocabletrainer.dialog;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.util.SparseArray;

import vocabletrainer.heinecke.aron.vocabletrainer.R;

/**
 * Item picker dialog for Array Resources<br>
 *     Allows override of specific values for dynamic entries
 */
public class ItemPickerDialog extends DialogFragment {
    private static final String K_OVERRIDES = "overrides";
    private static final String P_TITLE = "title";
    private static final String P_ITEMS = "items";
    private ItemPickerHandler handler;
    private SparseArray<String> overrides;

    /**
     * Interface for handlers of ItemPickerDialog on select
     */
    public interface ItemPickerHandler {
        void onItemPickerSelected(int position);
    }

    /**
     * Set handler for selection
     * @param handler
     */
    public void setItemPickerHandler(ItemPickerHandler handler){
        this.handler = handler;
    }

    /**
     * Creates a new instance
     */
    public static ItemPickerDialog newInstance(@ArrayRes int itemArray, @StringRes int title){
        ItemPickerDialog dialog = new ItemPickerDialog();
        Bundle args = new Bundle();
        args.putInt(P_ITEMS,itemArray);
        args.putInt(P_TITLE,title);
        dialog.setArguments(args);
        return dialog;
    }

    /**
     * Allows to override certain values<br>
     *     Has to be called <b></b>before showing the dialog</b>!
     * @param id
     * @param value
     */
    public void overrideEntry(int id, String value){
        if(overrides == null)
            overrides = new SparseArray<>();
        overrides.put(id,value);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int itemArray = getArguments().getInt(P_ITEMS);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(),R.style.CustomDialog);
        dialog.setTitle(getArguments().getInt(P_TITLE));
        String[] values = getResources().getStringArray(itemArray);
        if(overrides != null) {
            for(int i = 0; i < values.length; i++){
                String override = overrides.get(i);
                if(override != null)
                    values[i] = override;
            }
        }
        dialog.setItems(values, (dialog1, which) -> {
            dialog1.dismiss();
            handler.onItemPickerSelected(which);
        });
        return dialog.create();
    }

}
