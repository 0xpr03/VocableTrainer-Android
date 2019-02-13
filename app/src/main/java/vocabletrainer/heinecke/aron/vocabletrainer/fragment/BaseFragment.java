package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import vocabletrainer.heinecke.aron.vocabletrainer.activity.FragmentActivity;

/**
 * Base fragment to be used
 */
public abstract class BaseFragment extends Fragment {
    /**
     * Returns the FragmentActivity<br>
     *     This assumes the parent activity is instance of FragmentActivity
     * @return FragmentActivity
     */
    protected FragmentActivity getFragmentActivity(){
        return (FragmentActivity) getActivity();
    }

    /**
     * Returns the current AppCompatActivity casted via getActivity
     * @return AppCompatActivity
     */
    public AppCompatActivity getACActivity(){
        return (AppCompatActivity) getActivity();
    }
}
