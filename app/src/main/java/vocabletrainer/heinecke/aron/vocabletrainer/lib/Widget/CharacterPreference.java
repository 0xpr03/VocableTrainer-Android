package vocabletrainer.heinecke.aron.vocabletrainer.lib.Widget;

import android.content.Context;
import androidx.preference.EditTextPreference;
import android.util.AttributeSet;

import vocabletrainer.heinecke.aron.vocabletrainer.R;

/**
 * Custom EditTextPreference for single character settings
 * Created by Aron Heinecke
 */
public class CharacterPreference extends EditTextPreference {
    public CharacterPreference(Context context, AttributeSet attrs, int defStyleAttr,
                               int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public CharacterPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CharacterPreference(Context context) {
        super(context);
    }

    public CharacterPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.dialog_character_preference;
    }
}
