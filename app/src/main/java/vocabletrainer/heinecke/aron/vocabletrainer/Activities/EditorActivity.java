package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.EntryListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

import static vocabletrainer.heinecke.aron.vocabletrainer.Activities.ListSelector.PARAM_PASSED_SELECTION;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * List editor activity
 */
public class EditorActivity extends AppCompatActivity {
    public static final String PARAM_NEW_TABLE = "NEW_TABLE";
    private static final String TAG = "EditorActivity";
    private Table table;
    private ArrayList<Entry> entries;
    private EntryListAdapter adapter;
    private ListView listView;
    private Database db;
    private View undoContainer;
    private Entry lastDeleted;
    private int deletedPosition;
    private boolean edited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        entries = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor__activity);
        db = new Database(getBaseContext());

        Intent intent = getIntent();
        undoContainer = findViewById(R.id.undobar);

        // setup listview
        initListView();

        edited = false;

        // handle passed params
        boolean newTable = intent.getBooleanExtra(PARAM_NEW_TABLE, false);
        if (newTable) {
            table = new Table(getString(R.string.Editor_Default_Column_A),getString(R.string.Editor_Default_Column_B),getString(R.string.Editor_Default_List_Name));
            Log.d(TAG, "new table mode");
            showTableInfoDialog(true);
        } else {
            Table tbl = (Table) intent.getSerializableExtra(PARAM_PASSED_SELECTION);
            if (tbl != null) {
                this.table = tbl;
                entries.addAll(db.getVocablesOfTable(table));
                adapter.setTableData(tbl);
                Log.d(TAG, "edit table mode");
            } else {
                Log.e(TAG, "Edit Table Flag set without passing a table");
            }
        }
    }

    @Override
    public void onPause(){
        saveTable();
        super.onPause();
    }
    
    /**
     * Called upon click on save changes
     */
    public void onSaveChangesClicked(View view){
        saveTable();
    }

    /**
     *  Save the table to disk
     */
    private void saveTable(){
        Log.d(TAG,"table: "+table);
        if(db.upsertTable(table)) {
            Log.d(TAG,"table: "+table);
            if(db.upsertEntries(adapter.getAllEntries())){
                adapter.clearDeleted();
                edited = false;
            }else{
                Log.e(TAG,"unable to upsert entries! aborting");
            }
        }else{
            Log.e(TAG,"unable to upsert table! aborting");
        }
    }

    /**
     * Setup listview
     */
    private void initListView() {
        listView = (ListView) findViewById(R.id.listviewEditor);

        listView.setLongClickable(true);

        entries = new ArrayList<>();
        adapter = new EntryListAdapter(this, entries,this);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int pos, long id) {
                Toast.makeText(EditorActivity.this, Integer.toString(pos) + " Clicked", Toast.LENGTH_SHORT).show();
                showEntryEditDialog((Entry) adapter.getItem(pos),false);
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                        int pos, long id) {
                showEntryDeleteDialog((Entry) adapter.getItem(pos),pos);
                return true;
            }
        });
    }

    /**
     * Add an entry
     */
    public void addEntry(View view) {
        Entry entry = new Entry("","","", table, -1);
        adapter.addEntryUnrendered(entry);
        showEntryEditDialog(entry,true);
    }

    /**
     * Show entry delete dialog
     * @param entry
     * @param position
     */
    private void showEntryDeleteDialog(final Entry entry, final int position) {
        if(entry.getId() == ID_RESERVED_SKIP)
            return;
        AlertDialog.Builder delDiag = new AlertDialog.Builder(this);

        delDiag.setTitle(R.string.Editor_Diag_delete_Title);
        delDiag.setMessage(String.format(getString(R.string.Editor_Diag_delete_MSG_part)+"\n %s %s %s",entry.getAWord(),entry.getBWord(),entry.getTip()));

        delDiag.setPositiveButton(R.string.Editor_Diag_delete_btn_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                lastDeleted = entry;
                deletedPosition = position;
                adapter.setDeleted(entry);
                Toast.makeText(EditorActivity.this, entry.toString() + " deleted", Toast.LENGTH_SHORT).show();
                edited = true;
                showUndo();
                Log.d(TAG, "deleted");
            }
        });

        delDiag.setNegativeButton(R.string.Editor_Diag_delete_btn_CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, "canceled");
            }
        });

        delDiag.show();
    }

    /**
     * Show entry edit dialog
     * @param entry Entry to edit
     * @param deleteOnCancel True if entry should be deleted on cancel
     */
    private void showEntryEditDialog(final Entry entry,final boolean deleteOnCancel) {
        if(entry.getId() == ID_RESERVED_SKIP){
            showTableInfoDialog(false);
            return;
        }
        AlertDialog.Builder editDiag = new AlertDialog.Builder(this);

        editDiag.setTitle(R.string.Editor_Diag_edit_Title);

        final EditText editA = new EditText(this);
        final EditText editB = new EditText(this);
        final EditText editTipp = new EditText(this);
        editA.setText(entry.getAWord());
        editB.setText(entry.getBWord());
        editTipp.setText(entry.getTip());
        editA.setHint(R.string.Editor_Default_Column_A);
        editB.setHint(R.string.Editor_Default_Column_B);
        editTipp.setHint(R.string.Editor_Default_Tip);

        LinearLayout rl = new TableLayout(this);
        rl.addView(editA);
        rl.addView(editB);
        rl.addView(editTipp);

        editDiag.setView(rl);

        editDiag.setPositiveButton(R.string.Editor_Diag_edit_btn_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                entry.setAWord(editA.getText().toString());
                entry.setBWord(editB.getText().toString());
                entry.setTip(editTipp.getText().toString());
                adapter.notifyDataSetChanged();
                edited = true;
                Log.d(TAG, "edited");
            }
        });

        editDiag.setNegativeButton(R.string.Editor_Diag_edit_btn_CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(deleteOnCancel){
                    adapter.setDeleted(entry);
                    adapter.notifyDataSetChanged();
                }
                Log.d(TAG, "canceled");
            }
        });

        editDiag.show();
    }

    /**
     * Show table title editor dialog
     * @param newTbl set to true if this is a new table
     */
    private void showTableInfoDialog(boolean newTbl) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if(newTbl) {
            alert.setTitle(R.string.Editor_Diag_table_Title_New);
        }else{
            alert.setTitle(R.string.Editor_Diag_table_Title_Edit);
        }
        alert.setMessage("Please set the table information");

        // Set an EditText view to get user iName
        final EditText iName = new EditText(this);
        final EditText iColA = new EditText(this);
        final EditText iColB = new EditText(this);
        iName.setText(table.getName());
        iName.setSingleLine();
        iName.setHint(R.string.Editor_Default_List_Name);
        iColA.setHint(R.string.Editor_Default_Column_A);
        iColB.setHint(R.string.Editor_Default_Column_B);
        iColA.setText(table.getNameA());
        iColA.setSingleLine();
        iColB.setSingleLine();
        iColB.setText(table.getNameB());
        if (newTbl) {
            iName.setSelectAllOnFocus(true);
            iColA.setSelectAllOnFocus(true);
            iColB.setSelectAllOnFocus(true);
        }

        LinearLayout rl = new TableLayout(this);
        rl.addView(iName);
        rl.addView(iColA);
        rl.addView(iColB);
        alert.setView(rl);

        alert.setPositiveButton(R.string.Editor_Diag_table_btn_Ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(iColA.getText().length() == 0 || iColB.length() == 0 || iName.getText().length() == 0){
                   Log.d(TAG,"empty insert");
                }
                updateName(iName.getText().toString());
                table.setNameA(iColA.getText().toString());
                table.setNameB(iColB.getText().toString());
                adapter.setTableData(table);
                edited = true;
                Log.d(TAG, "set table info");
            }
        });

        alert.show();
    }

    /**
     * Update table name<br>
     * handles title renaming
     *
     * @param name
     */
    private void updateName(final String name) {
        setTitle("VocableTrainer - " + name);
        table.setName(name);
    }

    /**
     * Show undo view
     */
    private void showUndo() {
        undoContainer.setVisibility(View.VISIBLE);
        undoContainer.setAlpha(1);
        undoContainer.animate().alpha(0.4f).setDuration(5000)
                .withEndAction(new Runnable() {

                    @Override
                    public void run() {
                        undoContainer.setVisibility(View.GONE);
                    }
                });
        undoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"undoing");
                lastDeleted.setDelete(false);
                adapter.addEntryRendered(lastDeleted,deletedPosition);
                undoContainer.setVisibility(View.GONE);
            }
        });
    }
}
