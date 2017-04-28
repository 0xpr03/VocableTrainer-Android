package vocabletrainer.heinecke.aron.vocabletrainer;

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
import java.util.List;

public class Editor_Activity extends AppCompatActivity {
    public static final String PARAM_NEW_TABLE = "NEW_TABLE";
    public static final String PARAM_EDIT_TABLE = "EDIT_TABLE";
    private static final String TAG = "Editor_Acivity";
    private Table table;
    private ArrayList<Entry> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        entries = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor__activity);
        Intent intent = getIntent();
        boolean newTable = intent.getBooleanExtra(PARAM_NEW_TABLE, false);

        ListView listView = (ListView)findViewById(R.id.listview);

        List<Entry> list = new ArrayList<>();
        list.add(new Entry("A","B","C",1,-1L));
        EntryListAdapter adapter = new EntryListAdapter(this,list);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                int pos=position+1;
                Toast.makeText(Editor_Activity.this, Integer.toString(pos)+" Clicked", Toast.LENGTH_SHORT).show();
            }

        });

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
    }

    private void updateRow(Entry entry){

    }

    /**
     * Add an entry
     */
    public void addEntry(View view) {

        Entry entry = new Entry("A","B","Tip",table.getId(),-1);

        showEntryEditDialog(entry);
    }

    private void showEntryEditDialog(final Entry entry) {
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

                Log.d(TAG, "edited");
            }
        });

        editDiag.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                entry.setAWord(editA.getText().toString());
                entry.setBWord(editB.getText().toString());
                entry.setTip(editTipp.getText().toString());
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
                Log.d(TAG, "set table info");
            }
        });
//
//        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                // Canceled.
//            }
//        });

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
