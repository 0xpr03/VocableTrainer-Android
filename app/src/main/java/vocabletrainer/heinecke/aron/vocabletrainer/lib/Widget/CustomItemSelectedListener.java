package vocabletrainer.heinecke.aron.vocabletrainer.lib.Widget;

import android.view.View;
import android.widget.AdapterView;

/**
 * ItemSelectedListener which allows for disabling the first call (initial item set)
 */
public abstract class CustomItemSelectedListener implements AdapterView.OnItemSelectedListener {
    private boolean firstSelect = true;
    @Override
    public final void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(firstSelect){
            firstSelect = false;
        } else {
            itemSelected(parent, view, position, id);
        }
    }

    /**
     * Set firstSelect as undone, disabling the next itemSelect call from triggering itemSelected
     */
    public void disableNextEvent() {
        this.firstSelect = true;
    }

    /**
     * Called on item select
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    public abstract void itemSelected(AdapterView<?> parent, View view, int position, long id);

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
