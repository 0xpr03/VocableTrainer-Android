package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.TableListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * List selector activity
 */
public class ListSelector extends AppCompatActivity {

    private static final String TAG = "ListSelector";

    /**
     * Set whether multi-select is enabled or not<br>
     * Boolean expected
     */
    public static final String PARAM_MULTI_SELECT = "multiselect";

    /**
     * Param which activity should called upon this one<br>
     * A {@link Class} is expect for this param
     */
    public static final String PARAM_NEW_ACTIVITY = "activity";

    /**
     * Param under which the selected table / tables are passed<br>
     * This is a {@link Table} object or a {@link List} of {@link Table}
     */
    public static final String PARAM_PASSED_SELECTION = "selected";

    /**
     * Pass this flag as true to call this as an deletion activity
     */
    public static final String PARAM_DELETE_FLAG = "delete";

    private Class nextActivity;
    private boolean multiselect;
    private ListView listView;
    private TableListAdapter adapter;
    private boolean delete;
    Database db;
    List<Table> tables;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this.getBaseContext());
        setContentView(R.layout.activity_list_selector);
        Intent intent = getIntent();
        setTitle("List selector");
        // handle passed params
        multiselect = intent.getBooleanExtra(PARAM_MULTI_SELECT, false);
        nextActivity = (Class) intent.getSerializableExtra(PARAM_NEW_ACTIVITY);
        delete = intent.getBooleanExtra(PARAM_DELETE_FLAG, false);

        // setup listview
        initListView();

        loadTables();
    }

    /**
     * Load tables from db
     */
    private void loadTables() {
        tables = db.getTables();
        adapter.addAllUpdated(tables);
    }

    /**
     * Setup list view
     */
    private void initListView() {
        listView = (ListView) findViewById(R.id.listVIewLstSel);
//        listView.setLongClickable(true);

        ArrayList<Table> tables = new ArrayList<>();
        adapter = new TableListAdapter(this, R.layout.table_list_view, tables, multiselect);

        listView.setAdapter(adapter);

        if (multiselect) {
            setTitle("Select lists for training");
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setItemsCanFocus(false);

            Button btn = (Button) findViewById(R.id.btnOkSelect);
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Table> selectedTables = new ArrayList<Table>(10);
                    final SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                    int chkItemsCount = checkedItems.size();

                    for (int i = 0; i < chkItemsCount; ++i) {
                        int position = checkedItems.keyAt(i);
                        if (checkedItems.valueAt(i)) {
                            selectedTables.add(adapter.getItem(position));
                        }
                    }

                    Log.d(TAG, "Going to: " + nextActivity.toString() + " with " + selectedTables.size() + " selected items");

                    Intent intent = new Intent(ListSelector.this, nextActivity);
                    intent.putExtra(PARAM_PASSED_SELECTION, selectedTables);
                    ListSelector.this.startActivity(intent);
                }
            });
        } else {
            if (delete) {
                setTitle("Delete selected Table");
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                        Table table = (Table) adapter.getItem(position);
                        if (table.getId() != ID_RESERVED_SKIP) {
                            showDeleteDialog(table);
                        }
                    }

                });
            } else {
                setTitle("Select list to edit");
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                        Table table = (Table) adapter.getItem(position);
                        if (table.getId() != ID_RESERVED_SKIP) {
                            Log.d(TAG, nextActivity.toString());
                            Intent intent = new Intent(ListSelector.this, nextActivity);
                            intent.putExtra(PARAM_PASSED_SELECTION, table);
                            ListSelector.this.startActivity(intent);
                        }
                    }

                });
            }
        }

    }

    /**
     * Show delete dialog for table
     *
     * @param tableToDelete
     */
    private void showDeleteDialog(final Table tableToDelete) {
        final AlertDialog.Builder finishedDiag = new AlertDialog.Builder(this);

        finishedDiag.setTitle("Delete Table");
        finishedDiag.setMessage(String.format("Do you really want to delete the following table: %s: %s, %s",
                tableToDelete.getName(),tableToDelete.getNameA(),tableToDelete.getNameB()));

        finishedDiag.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                db.deleteTable(tableToDelete);
                tables.remove(tableToDelete);
                adapter.notifyDataSetChanged();
            }
        });

        finishedDiag.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // do nothing
            }
        });

        finishedDiag.show();
    }
}
