package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Helper class
 */
public class DPIHelper {
    /**
     * Convert DP to Pixels
     * @param r Resrouce
     * @param dp dp to convert
     * @return pixels float
     */
    public static float DPIToPixels(Resources r, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}
