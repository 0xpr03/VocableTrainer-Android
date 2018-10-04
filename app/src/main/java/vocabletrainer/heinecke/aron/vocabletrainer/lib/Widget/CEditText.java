package vocabletrainer.heinecke.aron.vocabletrainer.lib.Widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Custom EditText that does not display setError messages
 */
public class CEditText extends TextInputEditText {

    public CEditText(Context context) {
        super(context);
    }

    public CEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {
        Log.d("CEditText","called setError");
        setCompoundDrawables(null, null, icon, null);
    }
}
