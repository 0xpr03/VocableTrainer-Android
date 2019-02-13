package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.app.Activity;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.ListPickerFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel.ListPickerViewModel;

/**
 * List selector activity
 */
public class ListActivity extends FragmentActivity implements ListPickerFragment.FinishListener {

    /**
     * Set whether multi-select is enabled or not<br>
     * Boolean expected
     */
    public static final String PARAM_MULTI_SELECT = "multiselect";
    /**
     * Param key for return of selected lists<br>
     * This key contains a {@link VList} object or a {@link List} of {@link VList}
     */
    public static final String RETURN_LISTS = "selected";
    /**
     * Pass this flag as true to call this with creation & deletion capabilities
     */
    public static final String PARAM_FULL_FEATURESET = "full_features";
    /**
     * Optional Param key for already selected lists, available when multiselect is set<br>
     * Expects a {@link List} of {@link VList}<br>
     *     This can be null, if nothing is selected
     */
    public static final String PARAM_SELECTED = "selected";
    private boolean multiselect;
    private boolean fullFeatures;
    ListPickerFragment listPickerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        // handle passed params
        multiselect = intent.getBooleanExtra(PARAM_MULTI_SELECT, false);
        fullFeatures = intent.getBooleanExtra(PARAM_FULL_FEATURESET, false);
        if(fullFeatures){
            ab.setTitle(R.string.Lists_Title);
        } else {
            ab.setTitle(R.string.List_Select_Title);
        }


        ArrayList<VList> preselected;
        if (intent.hasExtra(PARAM_SELECTED)) {
            preselected = intent.getParcelableArrayListExtra(PARAM_SELECTED);
        } else {
            preselected = new ArrayList<>();
        }

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            listPickerFragment = (ListPickerFragment) getSupportFragmentManager().getFragment(savedInstanceState, ListPickerFragment.TAG);
        } else {
            listPickerFragment = ListPickerFragment.newInstance(multiselect, !fullFeatures,preselected);
        }

        setFragment(listPickerFragment);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, ListPickerFragment.TAG, listPickerFragment);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if(fullFeatures) {
            super.onBackPressed();
        } else {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        }

    }

    @Override
    public void selectionUpdate(ArrayList<VList> selected) {
        if(fullFeatures) {
            ListPickerViewModel listPickerViewModel = ViewModelProviders.of(this).get(ListPickerViewModel.class);
            listPickerViewModel.setDataInvalidated(); // editor changed entry

            Intent myIntent = new Intent(this, EditorActivity.class);
            myIntent.putExtra(EditorActivity.PARAM_NEW_TABLE, false);
            VList lst = selected.get(0);
            myIntent.putExtra(EditorActivity.PARAM_TABLE, lst);
            this.startActivity(myIntent);
        } else {
            Intent returnIntent = new Intent();
            if (multiselect) {
                returnIntent.putExtra(RETURN_LISTS, selected);
            } else {
                returnIntent.putExtra(RETURN_LISTS, selected.get(0));
            }
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    @Override
    public void cancel() {
        onBackPressed();
    }
}
