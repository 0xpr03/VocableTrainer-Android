package vocabletrainer.heinecke.aron.vocabletrainer.lib.Widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Compatibility class with vectorgraphics utilities
 */
public class VectorImageHelper {
    private final View view;
    private final Context context;

    public VectorImageHelper(Context context, View view){
        this.context = context;
        this.view = view;
    }

    /**
     * Manually initialize buttons with a drawableLeft<br>
     * Workaround for android regression with vector drawable on pre-21 android
     * @param button
     * @param drawableRes
     */
    public void initImageLeft(@IdRes int button, @DrawableRes int drawableRes){
        Drawable drawable = AppCompatResources.getDrawable(
                context,
                drawableRes);
        Button btn = view.findViewById(button);
        btn.setCompoundDrawablesWithIntrinsicBounds(drawable, null,null,null);
    }
}
