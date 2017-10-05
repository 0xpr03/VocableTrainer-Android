package vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Checkable relative layout
 *
 * @see {http://adanware.blogspot.de/2012/04/android-multiple-selection-listview.html}
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private boolean isChecked;
    private List<Checkable> checkableViews;

    /**
     * New checkable relative layout
     *
     * @param context
     * @param attrs
     * @param defStyle style to use
     */
    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialise(attrs);
    }

    /**
     * New checkable relative layout<br>
     * using context style
     *
     * @param context
     * @param attrs
     */
    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(attrs);
    }

    /**
     * @see android.widget.Checkable#isChecked()
     */
    public boolean isChecked() {
        return isChecked;
    }

    /**
     * @see android.widget.Checkable#setChecked(boolean)
     */
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        for (Checkable c : checkableViews) {
            c.setChecked(isChecked);
        }
    }

    /**
     * @see android.widget.Checkable#toggle()
     */
    public void toggle() {
        this.isChecked = !this.isChecked;
        for (Checkable c : checkableViews) {
            c.toggle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final int childCount = this.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            findCheckableChildren(this.getChildAt(i));
        }
    }

    /**
     * Read the custom XML attributes
     */
    private void initialise(AttributeSet attrs) {
        this.isChecked = false;
        this.checkableViews = new ArrayList<Checkable>(5);
    }

    /**
     * Add to our checkable list all the children of the view that implement the
     * interface Checkable
     */
    private void findCheckableChildren(View v) {
        if (v instanceof Checkable) {
            this.checkableViews.add((Checkable) v);
        }

        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            final int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                findCheckableChildren(vg.getChildAt(i));
            }
        }
    }
}

