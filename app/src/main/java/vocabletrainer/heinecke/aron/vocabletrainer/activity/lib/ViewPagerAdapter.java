package vocabletrainer.heinecke.aron.vocabletrainer.activity.lib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;

/**
 * Viewpager adapter that allows dynamically adding fragments
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final static String TAG = "ViewPagerAdapter";

    private final ArrayList<Class<? extends Fragment>> mFragmentClassList = new ArrayList<>();
    private final ArrayList<String> mFragmentTitleList = new ArrayList<>();
    private Context context;

    /**
     * Creates a new ViewPagerAdapter
     * @param manager FragmentManager
     * @param context Context required for StringRes resolve
     */
    public ViewPagerAdapter(FragmentManager manager, Context context) {
        super(manager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment frag = null;
        try {
            frag = mFragmentClassList.get(position).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            Log.wtf(TAG,"Error on fragment instantiation in ViewPagerAdapter",e);
        }
        return frag;
    }

    /**
     * Add Fragment instance to ViewPagerAdapter<br>
     *     <b>Note:</b> the Fragment passed is not used directly, a new Instance will be used
     * @param frag
     * @param title
     */
    @SuppressWarnings("unused")
    public void addFragment(Class<? extends Fragment> frag,@StringRes int title ){
        addFragment(frag,context.getString(title));
    }

    /**
     * Add Fragment instance to ViewPagerAdapter<br>
     *     <b>Note:</b> the Fragment passed is not used directly, a new Instance will be used
     * @param frag
     * @param title
     */
    @SuppressWarnings("unused")
    public void addFragment(Class<? extends Fragment> frag, @NonNull String title) {
        mFragmentClassList.add(frag);
        mFragmentTitleList.add(title);
    }

    @Override
    public int getCount() {
        return mFragmentClassList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
