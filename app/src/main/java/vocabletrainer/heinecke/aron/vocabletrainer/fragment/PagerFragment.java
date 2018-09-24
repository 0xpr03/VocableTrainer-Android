package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.support.annotation.CallSuper;

/**
 * Pager Fragment which implements PagerListener, listening on viewpager changes
 */
public abstract class PagerFragment extends BaseFragment{
    @CallSuper
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(this.isResumed()){
            if(isVisibleToUser)
                onFragmentVisible();
            else
                onFragmentInvisible();
        }
    }

    /**
     * Called when fragment goes invisible (viewpager)<br>
     *     Does not replace onPause/onStop/onCreate, as these are no page changes.
     */
    protected void onFragmentInvisible(){};

    /**
     * Called when fragment goes visible (viewpager)<br>
     *     Does not replace onPause/onStop/onCreate, as these are no page changes.
     */
    protected void onFragmentVisible(){};
}
