package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.app.Fragment;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.FragmentActivity;

/**
 * Base fragment to be used
 */
public abstract class BaseFragment extends Fragment {
    /**
     * Returns the FragmentAcitivity<br>
     *     This assumes the parent activity is instance of FragmentAcitivty
     * @return FragmentActivity
     */
    protected FragmentActivity getFragmentActivity(){
        return (FragmentActivity) getActivity();
    }
}
