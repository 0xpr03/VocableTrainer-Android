package vocabletrainer.heinecke.aron.vocabletrainer.Activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib.TableListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

/**
 * List selector activity
 */
public class ListSelector extends AppCompatActivity {

    private static final String TAG = "ListSelector";

    /**
     * Set whether multi-select is enabled or not<br>
     *     Boolean expected
     */
    public static final String PARAM_MULTI_SELECT = "multiselect";

    /**
     * Param which activity should called upon this one<br>
     * A {@link Class} is expect for this param
     */
    public static final String PARAM_NEW_ACTIVITY = "activity";

    /**
     * Param under which the selected table / tables are passed<br>
     *     This is a {@link Table} object or a {@link List} of {@link Table}
     */
    public static final String PARAM_PASSED_SELECTION = "selected";

    private Class nextActivity;
    private boolean multiselect;
    private ListView listView;
    private TableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_selector);
        Intent intent = getIntent();

        // handle passed params
        multiselect = intent.getBooleanExtra(PARAM_MULTI_SELECT, false);
        nextActivity = (Class) intent.getSerializableExtra(PARAM_NEW_ACTIVITY);

        // setup listview
        initListView();

        loadTables();
    }

    private void loadTables(){
        Database db = new Database(this.getBaseContext());
        List<Table> tables = db.getTables();
        adapter.addAllUpdated(tables);
    }

    /**
     * Setup list view
     */
    private void initListView() {
        listView = (ListView) findViewById(R.id.listVIewLstSel);

        listView.setLongClickable(true);

        List<Table> tables = new ArrayList<>();
        adapter = new TableListAdapter(this, tables);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                int pos = position + 1;
                Toast.makeText(ListSelector.this, Integer.toString(pos) + " Clicked", Toast.LENGTH_SHORT).show();
                if(multiselect){
                    Log.d(TAG,"Multiselect..");
                }else{
                    Log.d(TAG,nextActivity.toString());
                    Intent intent = new Intent(ListSelector.this,nextActivity);
                    intent.putExtra(PARAM_PASSED_SELECTION,(Table) adapter.getItem(pos));
                    ListSelector.this.startActivity(intent);
                }
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
//                showEntryDeleteDialog((Entry) adapter.getItem(pos),pos-1);
                return true;
            }
        });
    }
}
