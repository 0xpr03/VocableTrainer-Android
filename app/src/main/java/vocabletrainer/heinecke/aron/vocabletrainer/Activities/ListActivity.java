package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.TableListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

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
     * This key contains a {@link Table} object or a {@link List} of {@link Table}
     */
    public static final String RETURN_LISTS = "selected";
    /**
     * Pass this flag as true to call this as an deletion activity
     */
    public static final String PARAM_DELETE_FLAG = "delete";
    /**
     * Optional Param key for already selected lists, available when multiselect is set<br>
     * Expect a {@link List} of {@link Table}
     */
    public static final String PARAM_SELECTED = "selected";
    private static final String TAG = "ListActivity";
    Database db;
    List<Table> tables;
    private boolean multiselect;
    private ListView listView;
    private TableListAdapter adapter;
    private boolean delete;
    private Button bOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this.getBaseContext());
        setContentView(R.layout.activity_list_selector);
        Intent intent = getIntent();
        // handle passed params
        multiselect = intent.getBooleanExtra(PARAM_MULTI_SELECT, false);
        delete = intent.getBooleanExtra(PARAM_DELETE_FLAG, false);
        bOk = (Button) findViewById(R.id.btnOkSelect);

        // setup listview
        initListView();
        loadTables((ArrayList<Table>) intent.getSerializableExtra(PARAM_SELECTED));
        updateOkButton();
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
     * Load tables from db
     *
     * @param tickedTables already selected tables, can be null
     */
    private void loadTables(List<Table> tickedTables) {
        tables = db.getTables();
        adapter.setAllUpdated(tables);
        if (tickedTables != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                Table tbl = adapter.getItem(i);
                if (tbl.getId() >= MIN_ID_TRESHOLD && tickedTables.contains(tbl)) {
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

        ArrayList<Table> tables = new ArrayList<>();
        adapter = new TableListAdapter(this, R.layout.table_list_view, tables, multiselect);

        listView.setAdapter(adapter);

        if (multiselect) {
            setTitle(R.string.ListSelector_Title_Training);
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
                    ArrayList<Table> selectedTables = new ArrayList<Table>(10);
                    final SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                    int chkItemsCount = checkedItems.size();

                    for (int i = 0; i < chkItemsCount; ++i) {
                        if (checkedItems.valueAt(i)) {
                            selectedTables.add(adapter.getItem(checkedItems.keyAt(i)));
                        }
                    }

                    Log.d(TAG, "returning with " + selectedTables.size() + " selected items");

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(RETURN_LISTS, selectedTables);
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
                        Table table = adapter.getItem(position);
                        if (table.getId() != ID_RESERVED_SKIP) {
                            showDeleteDialog(table);
                        }
                    }

                });
            } else {
                setTitle(R.string.ListSelector_Title_Edit);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                        Table table = adapter.getItem(position);
                        if (table.getId() != ID_RESERVED_SKIP) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(RETURN_LISTS, table);
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
    private void updateOkButton(){
        bOk.setEnabled(listView.getCheckedItemCount() > 0);
    }

    /**
     * Show delete dialog for table
     *
     * @param tableToDelete
     */
    private void showDeleteDialog(final Table tableToDelete) {
        final AlertDialog.Builder finishedDiag = new AlertDialog.Builder(this);

        finishedDiag.setTitle(R.string.ListSelector_Diag_delete_Title);
        finishedDiag.setMessage(String.format(getText(R.string.ListSelector_Diag_delete_Msg).toString(),
                tableToDelete.getName(), tableToDelete.getNameA(), tableToDelete.getNameB()));

        finishedDiag.setPositiveButton(R.string.ListSelector_Diag_delete_btn_Delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                db.deleteTable(tableToDelete);
                adapter.removeEntryUpdated(tableToDelete);
            }
        });

        finishedDiag.setNegativeButton(R.string.ListSelector_Diag_delete_btn_Cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // do nothing
            }
        });

        finishedDiag.show();
    }
}
