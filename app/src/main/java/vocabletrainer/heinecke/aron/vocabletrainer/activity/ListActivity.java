package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.lib.TableListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenTableComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenericComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.MIN_ID_TRESHOLD;

/**
 * List selector activity
 */
public class ListActivity extends AppCompatActivity {

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
     * Expect a {@link List} of {@link VList}
     */
    public static final String PARAM_SELECTED = "selected";
    private static final String TAG = "ListActivity";
    private static final String P_KEY_LA_SORT = "LA_sorting";
    Database db;
    List<VList> lists;
    private boolean multiselect;
    private ListView listView;
    private TableListAdapter adapter;
    private boolean delete;
    private Button bOk;
    private int sort_type;
    private GenTableComparator compName;
    private GenTableComparator compA;
    private GenTableComparator compB;
    private GenTableComparator cComp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this.getBaseContext());
        setContentView(R.layout.activity_list_selector);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // lambdas without lambdas
        compName = new GenTableComparator(
                new GenericComparator.ValueRetriever[]{GenTableComparator.retName,
                        GenTableComparator.retA, GenTableComparator.retB}
        ,ID_RESERVED_SKIP);
        compA = new GenTableComparator(
                new GenericComparator.ValueRetriever[]{GenTableComparator.retA,
                        GenTableComparator.retB, GenTableComparator.retName}
        ,ID_RESERVED_SKIP);
        compB = new GenTableComparator(
                new GenericComparator.ValueRetriever[]{GenTableComparator.retB,
                        GenTableComparator.retA, GenTableComparator.retName}
        ,ID_RESERVED_SKIP);

        Intent intent = getIntent();
        // handle passed params
        multiselect = intent.getBooleanExtra(PARAM_MULTI_SELECT, false);
        delete = intent.getBooleanExtra(PARAM_DELETE_FLAG, false);
        bOk = (Button) findViewById(R.id.btnOkSelect);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        sort_type = settings.getInt(P_KEY_LA_SORT, R.id.lMenu_sort_Name);
        updateComp();

        // setup listview
        initListView();
        loadTables((ArrayList<VList>) intent.getSerializableExtra(PARAM_SELECTED));
        updateOkButton();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.lMenu_sort_Name:
            case R.id.lMenu_sort_A:
            case R.id.lMenu_sort_B:
                sort_type = item.getItemId();
                updateComp();
                adapter.updateSorting(cComp);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Update sorting type
     */
    private void updateComp() {
        switch (sort_type) {
            case R.id.lMenu_sort_A:
                cComp = compA;
                break;
            case R.id.lMenu_sort_B:
                cComp = compB;
                break;
            case R.id.lMenu_sort_Name:
                cComp = compName;
                break;
            default:
                cComp = compName;
                sort_type = R.id.lMenu_sort_Name;
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTables(null);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }


    /**
     * Load lists from db
     *
     * @param tickedLists already selected lists, can be null
     */
    private void loadTables(List<VList> tickedLists) {
        lists = db.getTables();
        adapter.setAllUpdated(lists, cComp);
        if (tickedLists != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                VList tbl = adapter.getItem(i);
                if (tbl.getId() >= MIN_ID_TRESHOLD && tickedLists.contains(tbl)) {
                    listView.setItemChecked(i, true);
                }
            }
        }
    }

    /**
     * Setup list view
     */
    private void initListView() {
        listView = (ListView) findViewById(R.id.listVIewLstSel);

        ArrayList<VList> lists = new ArrayList<>();
        adapter = new TableListAdapter(this, R.layout.table_list_view, lists, multiselect);

        listView.setAdapter(adapter);

        if (multiselect) {
            // TODO: title; setTitle(R.string.ListSelector_Title_Training);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setItemsCanFocus(false);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    updateOkButton();
                }
            });
            bOk.setVisibility(View.VISIBLE);
            bOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<VList> selectedLists = new ArrayList<VList>(10);
                    final SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                    int chkItemsCount = checkedItems.size();

                    for (int i = 0; i < chkItemsCount; ++i) {
                        if (checkedItems.valueAt(i)) {
                            selectedLists.add(adapter.getItem(checkedItems.keyAt(i)));
                        }
                    }

                    Log.d(TAG, "returning with " + selectedLists.size() + " selected items");

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(RETURN_LISTS, selectedLists);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            });
        } else {
            if (delete) {
                setTitle(R.string.ListSelector_Title_Delete);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                        VList list = adapter.getItem(position);
                        if (list.getId() != ID_RESERVED_SKIP) {
                            showDeleteDialog(list);
                        }
                    }

                });
            } else {
                setTitle(R.string.ListSelector_Title_Edit);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                        VList list = adapter.getItem(position);
                        if (list.getId() != ID_RESERVED_SKIP) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(RETURN_LISTS, list);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                    }

                });
            }
        }
    }

    /**
     * Update enabled state of OK button
     */
    private void updateOkButton() {
        bOk.setEnabled(listView.getCheckedItemCount() > 0);
    }

    /**
     * Show delete dialog for table
     *
     * @param listToDelete
     */
    private void showDeleteDialog(final VList listToDelete) {
        final AlertDialog.Builder finishedDiag = new AlertDialog.Builder(this);

        finishedDiag.setTitle(R.string.ListSelector_Diag_delete_Title);
        finishedDiag.setMessage(String.format(getText(R.string.ListSelector_Diag_delete_Msg).toString(),
                listToDelete.getName(), listToDelete.getNameA(), listToDelete.getNameB()));

        finishedDiag.setPositiveButton(R.string.ListSelector_Diag_delete_btn_Delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                db.deleteTable(listToDelete);
                adapter.removeEntryUpdated(listToDelete);
            }
        });

        finishedDiag.setNegativeButton(R.string.ListSelector_Diag_delete_btn_Cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // do nothing
            }
        });

        finishedDiag.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Save values
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(P_KEY_LA_SORT, sort_type);
        editor.apply();
    }
}
