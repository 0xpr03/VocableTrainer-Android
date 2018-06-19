package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Custom EditTextPreference
 * Created by Aron Heinecke
 */
public class CustomEditTextPreference extends EditTextPreference {
    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                    int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CustomEditTextPreference(Context context) {
        super(context);
    }
}
