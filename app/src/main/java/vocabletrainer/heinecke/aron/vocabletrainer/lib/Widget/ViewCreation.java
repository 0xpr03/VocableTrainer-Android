package vocabletrainer.heinecke.aron.vocabletrainer.lib.Widget;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Function;

/**
 * Helper Class for view creation
 * @author Aron Heinecke
 */
public class ViewCreation {
    /**
     * Init radio group of buttons onCheckedChangeListener
     * @param group Group to init
     * @param fn Function to call on button checked change
     */
    public static void initRadioGroup(RadioGroup group,Function<Void,View> fn){
        for (int i = 0; i < group.getChildCount();i++) {
            View view = group.getChildAt(i);
            if(view instanceof RadioButton){
                ((RadioButton)view).setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) // don't double fire (B de-check => A check)
                        fn.function(buttonView);
                });
            }
        }
    }
}
