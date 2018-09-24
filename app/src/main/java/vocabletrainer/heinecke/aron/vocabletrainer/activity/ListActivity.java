package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.ListPickerFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

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
     * Pass this flag as true to call this as an deletion activity
     */
    public static final String PARAM_DELETE_FLAG = "delete";
    /**
     * Optional Param key for already selected lists, available when multiselect is set<br>
     * Expects a {@link List} of {@link VList}<br>
     *     This can be null, if nothing is selected
     */
    public static final String PARAM_SELECTED = "selected";
    /**
     * Param, if set runs in editor mode, calling EditorActivity & not returning
     */
    public static final String PARAM_RUN_EDITOR = "editorMode";
    private boolean multiselect;
    ListPickerFragment listPickerFragment;
    private boolean editorMode;

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
        boolean delete = intent.getBooleanExtra(PARAM_DELETE_FLAG, false);
        editorMode = intent.getBooleanExtra(PARAM_RUN_EDITOR,false);
        if(editorMode){
            multiselect = false;
            delete = false;
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
            listPickerFragment = ListPickerFragment.newInstance(multiselect, delete,preselected,true);
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
        if(editorMode) {
            super.onBackPressed();
        } else {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        }

    }

    @Override
    public void selectionUpdate(ArrayList<VList> selected) {
        if(editorMode) {
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
