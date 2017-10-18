package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.BaseFragment;

/**
 * Base class for fragment activities<br>
 *     This includes back stack function & callbacks as well as helper functions
 */
public abstract class FragmentActivity extends AppCompatActivity{

    private static final String TAG = "FragmentActivity";
    private Fragment currentFragment;
    private Fragment rootFragment;

    /**
     * Interface to implement by fragments that want to be notifified
     */
    public interface BackButtonListner {
        /**
         * Called when back button is pressed<br>
         *     Used to communicate between activity & fragment
         * @return true indicates that the activity can be closed
         */
        boolean onBackPressed();
    }

    BackButtonListner backButtonListner;

    /**
     * Returns the action bar<br>
     *     used by fragments
     * @return
     */
    public ActionBar getSupportActionBar(){
        return super.getSupportActionBar();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(null);
        }
    }

    @Override
    public void onBackPressed() {
        if(backButtonListner != null) {
            if (backButtonListner.onBackPressed()) {
                super.onBackPressed();
            }
        } else if(!handleFragmentBack()){
            super.onBackPressed();
        }
    }

    /**
     * Goes back one stack
     * @return false when it's impossible to go back
     */
    protected boolean handleFragmentBack(){
        Log.d(TAG,"handling fragment back "+ getFragmentManager().getBackStackEntryCount());
        if(getFragmentManager().getBackStackEntryCount() > 0){
            Log.d(TAG,"popping stack");
            currentFragment = getCurrentFragment();
            if(getFragmentManager().popBackStackImmediate()) {
                if (currentFragment instanceof BaseFragment) {
                    currentFragment.onResume();
                }
                return true;
            } else {
                Log.w(TAG,"unable to pop backstack");
                return false;
            }
        }else{
            return false;
        }
    }

    /**
     * Returns the current fragment
     * @return
     */
    private Fragment getCurrentFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        int amount;
        Log.d(TAG,"fragment stack:" + (amount = fragmentManager.getBackStackEntryCount()));
        Fragment fr = fragmentManager.findFragmentById(R.id.frame);
        if(fr == null) {
            fr = rootFragment;
        }
        return fr;
    }

    /**
     * Set fragment to show<br>
     *     Replaces current fragment
     * @param fragment
     */
    public void setFragment(final Fragment fragment){
        Log.w(TAG,(fragment instanceof BaseFragment)+""+fragment);
        checkBackButtonListener(fragment);
        getFragmentManager().beginTransaction()
                .replace(R.id.frame, fragment).commit();
        currentFragment = fragment;
        rootFragment = fragment;
    }

    /**
     * Check back button listener
     * @param fragment
     */
    private void checkBackButtonListener(final Fragment fragment){
        if(fragment instanceof BackButtonListner) {
            backButtonListner = (BackButtonListner) fragment;
        }else{
            backButtonListner = null;
        }
    }

    /**
     * Adds a new fragment as top element
     * @param fragment
     */
    public void addFragment(final Fragment caller, final Fragment fragment){
        checkBackButtonListener(fragment);
        getFragmentManager().beginTransaction()
                .add(R.id.frame,fragment).addToBackStack(null).remove(caller).commit();
        currentFragment = fragment;
    }
}
