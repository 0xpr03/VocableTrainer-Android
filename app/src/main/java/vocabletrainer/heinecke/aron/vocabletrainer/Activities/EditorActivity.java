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
import java.util.Random;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.EntryListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

public class EditorActivity extends AppCompatActivity {
    public static final String PARAM_NEW_TABLE = "NEW_TABLE";
    public static final String PARAM_EDIT_TABLE = "EDIT_TABLE";
    private static final String TAG = "Editor_Acivity";
    private Table table;
    private ArrayList<Entry> entries;
    private EntryListAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        entries = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor__activity);
        Intent intent = getIntent();

        // setup listview
        initListView();


        // handle passed params
        boolean newTable = intent.getBooleanExtra(PARAM_NEW_TABLE, false);
        if (newTable) {
            table = new Table("Column A", "Column B", "List Name");
            Log.d(TAG, "new table mode");
            showTableInfoDialog(true);
        } else {
            Table tbl = intent.getParcelableExtra(PARAM_EDIT_TABLE);
            if (tbl != null) {
                this.table = tbl;
                Log.d(TAG, "edit table mode");
            } else {
                Log.e(TAG, "Edit Table Flag set without passing a table");
            }
        }

        //TODO: remove after debugging
        Random rnd = new Random();
        for (int i = 0; i < 100; i++) {
            entries.add(new Entry("" + rnd.nextInt(), "" + rnd.nextInt(), "" + rnd.nextInt(), table, -1L));
        }
        adapter.notifyDataSetChanged();
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
        Database db = new Database(this.getBaseContext());
        Log.d(TAG,"table: "+table);
        if(db.upsertTable(table)) {
            Log.d(TAG,"table: "+table);
            db.upsertEntries(entries);
        }else{
            Log.w(TAG,"was unable to upsert table! aborting");
        }
    }

    /**
     * Setup listview
     */
    private void initListView() {
        listView = (ListView) findViewById(R.id.listview);

        listView.setLongClickable(true);

        entries = new ArrayList<>();
        adapter = new EntryListAdapter(this, entries);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                int pos = position + 1;
                Toast.makeText(EditorActivity.this, Integer.toString(pos) + " Clicked", Toast.LENGTH_SHORT).show();
                showEntryEditDialog((Entry) adapter.getItem(pos),false);
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                        int pos, long id) {
                showEntryDeleteDialog((Entry) adapter.getItem(pos));
                return true;
            }
        });
    }

    /**
     * Add an entry
     */
    public void addEntry(View view) {

        Entry entry = new Entry("A", "B", "Tip", table, -1);
        adapter.addEntryUnrendered(entry);
        showEntryEditDialog(entry,true);
    }

    private void showEntryDeleteDialog(final Entry entry) {
        if(entry.getId() == ID_RESERVED_SKIP)
            return;
        AlertDialog.Builder delDiag = new AlertDialog.Builder(this);

        delDiag.setTitle("Delete Dntry");
        delDiag.setMessage("Do you want to delete this entry ?\n"+entry.toString());

        delDiag.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                adapter.setDeleted(entry);
                Toast.makeText(EditorActivity.this, entry.toString() + " deleted", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "deleted");
            }
        });

        delDiag.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
        if(entry.getId() == ID_RESERVED_SKIP)
            return;
        AlertDialog.Builder editDiag = new AlertDialog.Builder(this);

        editDiag.setMessage("Edit entry");

        final EditText editA = new EditText(this);
        final EditText editB = new EditText(this);
        final EditText editTipp = new EditText(this);
        editA.setText(entry.getAWord());
        editB.setText(entry.getBWord());
        editTipp.setText(entry.getTip());

        LinearLayout rl = new TableLayout(this);
        rl.addView(editA);
        rl.addView(editB);
        rl.addView(editTipp);

        editDiag.setView(rl);

        editDiag.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                entry.setAWord(editA.getText().toString());
                entry.setBWord(editB.getText().toString());
                entry.setTip(editTipp.getText().toString());
                adapter.notifyDataSetChanged();
                Log.d(TAG, "edited");
            }
        });

        editDiag.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
     */
    private void showTableInfoDialog(boolean selectAll) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("New Table");
        alert.setMessage("Please set the table information");

        // Set an EditText view to get user iName
        final EditText iName = new EditText(this);
        final EditText iColA = new EditText(this);
        final EditText iColB = new EditText(this);
        iName.setText(table.getName());
        iName.setSingleLine();
//        iName.setHint(); // TODO: add resource of XML
        iColA.setText(table.getNameA());
        iColA.setSingleLine();
        iColB.setSingleLine();
        iColB.setText(table.getNameB());
        if (selectAll) {
            iName.setSelectAllOnFocus(true);
            iColA.setSelectAllOnFocus(true);
            iColB.setSelectAllOnFocus(true);
        }

        LinearLayout rl = new TableLayout(this);
        rl.addView(iName);
        rl.addView(iColA);
        rl.addView(iColB);
        alert.setView(rl);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                updateName(iName.getText().toString());
                table.setNameA(iColA.getText().toString());
                table.setNameB(iColB.getText().toString());
                adapter.setTableData(table);
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

}
