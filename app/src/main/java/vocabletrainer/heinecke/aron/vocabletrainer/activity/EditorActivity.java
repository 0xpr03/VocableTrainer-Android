package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import vocabletrainer.heinecke.aron.vocabletrainer.activity.lib.EntryListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.VEntryEditorDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.VListEditorDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenEntryComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenericComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * List editor activity
 */
public class EditorActivity extends AppCompatActivity {
    private final static int LIST_SELECT_REQUEST_CODE = 10;
    /**
     * Param key for new list, default is false
     */
    public static final String PARAM_NEW_TABLE = "NEW_TABLE";
    /**
     * Param key for list to load upon new_table false
     */
    public static final String PARAM_TABLE = "list";
    private static final String TAG = "EditorActivity";
    private static final String P_KEY_EA_SORT = "EA_sorting";
    private VList list;
    private ArrayList<VEntry> entries;
    private EntryListAdapter adapter;
    private ListView listView;
    private Database db;
    private View undoContainer;
    private VEntry lastDeleted;
    private int deletedPosition;
    private int sortSetting;
    private GenEntryComparator cComp;
    private GenEntryComparator compA;
    private GenEntryComparator compB;
    private GenEntryComparator compTip;

    /**
     * data save will be ignored when set
     */
    private boolean noDataSave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        entries = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        db = new Database(getBaseContext());

        compA = new GenEntryComparator(new GenericComparator.ValueRetriever[] {
                GenEntryComparator.retA,GenEntryComparator.retB,
                GenEntryComparator.retTip
        },ID_RESERVED_SKIP);
        compB = new GenEntryComparator(new GenericComparator.ValueRetriever[] {
                GenEntryComparator.retB,GenEntryComparator.retA,
                GenEntryComparator.retTip
        },ID_RESERVED_SKIP);
        compTip = new GenEntryComparator(new GenericComparator.ValueRetriever[] {
                GenEntryComparator.retTip,GenEntryComparator.retA,
                GenEntryComparator.retB
        },ID_RESERVED_SKIP);

        Intent intent = getIntent();
        undoContainer = findViewById(R.id.undobar);
        undoContainer.setVisibility(View.GONE);

        FloatingActionButton bNewEntry = (FloatingActionButton) findViewById(R.id.bEditorNewEntry);
        bNewEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEntry();
            }
        });

        // setup listview
        initListView();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        sortSetting = settings.getInt(P_KEY_EA_SORT, R.id.eMenu_sort_A);
        updateComp();

        // handle passed params
        boolean newTable = intent.getBooleanExtra(PARAM_NEW_TABLE, false);
        if (newTable) {
            list = new VList(getString(R.string.Editor_Default_Column_A), getString(R.string.Editor_Default_Column_B), getString(R.string.Editor_Default_List_Name));
            Log.d(TAG, "new list mode");
            showTableInfoDialog(true);
        } else {
            VList tbl = (VList) intent.getSerializableExtra(PARAM_TABLE);
            if (tbl != null) {
                this.list = tbl;
                entries.addAll(db.getVocablesOfTable(list));
                adapter.setTableData(tbl);
                adapter.updateSorting(cComp);
                Log.d(TAG, "edit list mode");
            } else {
                Log.e(TAG, "Edit VList Flag set without passing a list");
            }
        }
        this.setTitle(list.getName());
    }

    /**
     * Changes cComp to current selection
     */
    private void updateComp(){
        switch(sortSetting){
            case R.id.eMenu_sort_A:
                cComp = compA;
                break;
            case R.id.eMenu_sort_B:
                cComp = compB;
                break;
            case R.id.eMenu_sort_Tip:
                cComp = compTip;
                break;
            default:
                cComp = compA;
                sortSetting = R.id.eMenu_sort_A;
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor, menu);
        return true;
    }

    @Override
    public void onPause() {
        saveTable();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tEditorListEdit:
                showTableInfoDialog(false);
                return true;
            case R.id.eMenu_sort_A:
            case R.id.eMenu_sort_B:
            case R.id.eMenu_sort_Tip:
                sortSetting = item.getItemId();
                updateComp();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called upon click on save changes
     */
    public void onSaveChangesClicked(View view) {
        saveTable();
    }

    /**
     * Save the list to disk
     */
    private void saveTable() {
        if(noDataSave){
            return;
        }
        Log.d(TAG, "list: " + list);
        if (db.upsertTable(list)) {
            Log.d(TAG, "list: " + list);
            if (db.upsertEntries(adapter.getAllEntries())) {
                adapter.clearDeleted();
            } else {
                Log.e(TAG, "unable to upsert entries! aborting");
            }
        } else {
            Log.e(TAG, "unable to upsert list! aborting");
        }
    }

    /**
     * Setup listview
     */
    private void initListView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        listView = (ListView) findViewById(R.id.listviewEditor);

        listView.setLongClickable(true);

        entries = new ArrayList<>();
        adapter = new EntryListAdapter(this, entries);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int pos, long id) {
                showEntryEditDialog((VEntry) adapter.getItem(pos), false);
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                showEntryDeleteDialog((VEntry) adapter.getItem(pos), pos);
                return true;
            }
        });
    }

    /**
     * Add an entry
     */
    public void addEntry() {
        VEntry entry = new VEntry("", "", "", list, -1);
        adapter.addEntryUnrendered(entry);
        showEntryEditDialog(entry, true);
    }

    /**
     * Show entry delete dialog
     *
     * @param entry
     * @param position
     */
    private void showEntryDeleteDialog(final VEntry entry, final int position) {
        if (entry.getId() == ID_RESERVED_SKIP)
            return;
        AlertDialog.Builder delDiag = new AlertDialog.Builder(this);

        delDiag.setTitle(R.string.Editor_Diag_delete_Title);
        delDiag.setMessage(String.format(getString(R.string.Editor_Diag_delete_MSG_part) + "\n %s %s %s", entry.getAWord(), entry.getBWord(), entry.getTip()));

        delDiag.setPositiveButton(R.string.Editor_Diag_delete_btn_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                lastDeleted = entry;
                deletedPosition = position;
                adapter.setDeleted(entry);
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
     *
     * @param entry          VEntry to edit
     * @param deleteOnCancel True if entry should be deleted on cancel
     */
    private void showEntryEditDialog(final VEntry entry, final boolean deleteOnCancel) {
        if (entry.getId() == ID_RESERVED_SKIP) {
            showTableInfoDialog(false);
            return;
        }
        VEntryEditorDialog dialog = VEntryEditorDialog.newInstance(entry);
        dialog.setOkAction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                adapter.notifyDataSetChanged();
                Log.d(TAG,"edited");
                return null;
            }
        });
        dialog.setCancelAction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if(deleteOnCancel){
                    adapter.setDeleted(entry);
                    adapter.notifyDataSetChanged();
                }
                Log.d(TAG,"canceled");
                return null;
            }
        });

        dialog.show(getFragmentManager(), VEntryEditorDialog.TAG);
    }

    /**
     * Show list title editor dialog<br>
     *     Exit editor when newTbl is set and user cancels the dialog
     *
     * @param newTbl set to true if this is a new list
     */
    private void showTableInfoDialog(final boolean newTbl) {
        Callable<Void> callableOk = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                setTitle(list.getName());
                adapter.setTableData(list);
                return null;
            }
        };
        Callable<Void> callableCancel = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if(newTbl) {
                    noDataSave = true;
                    finish();
                }
                return null;
            }
        };
        VListEditorDialog dialog = VListEditorDialog.newInstance(newTbl, list);
        dialog.setCancelAction(callableCancel);
        dialog.setOkAction(callableOk);
        dialog.show(getFragmentManager(), VListEditorDialog.TAG);
    }

    /**
     * Show undo view
     */
    private void showUndo() {
        undoContainer.setVisibility(View.VISIBLE);
        undoContainer.bringToFront();
        ScaleAnimation scaleAnimation = new ScaleAnimation(0f,1f,1f,1f,
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f);
        final AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f,1.0f);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setDuration(500);
        animationSet.setFillEnabled(true);

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                AnimationSet animationSetOut = new AnimationSet(true);
                AlphaAnimation alphaAnimation1 = new AlphaAnimation(1f,0f);
                ScaleAnimation scaleAnimation1 = new ScaleAnimation(1f,0f,1f,1f,
                        Animation.RELATIVE_TO_SELF, 1f,
                        Animation.RELATIVE_TO_SELF, 1f);
                ScaleAnimation scaleAnimation2 = new ScaleAnimation(1f,0f,1f,0f,
                        Animation.RELATIVE_TO_SELF, 1f,
                        Animation.RELATIVE_TO_SELF, 1f);

                scaleAnimation2.setStartOffset(500);
                animationSetOut.addAnimation(alphaAnimation1);
                animationSetOut.addAnimation(scaleAnimation1);
                animationSetOut.addAnimation(scaleAnimation2);
                animationSetOut.setDuration(2000);
                animationSetOut.setStartOffset(2000);
                animationSetOut.setFillEnabled(true);
                animationSetOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        undoContainer.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                undoContainer.setAnimation(animationSetOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        undoContainer.clearAnimation();
        undoContainer.setAnimation(animationSet);

        undoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "undoing");
                lastDeleted.setDelete(false);
                undoContainer.clearAnimation();
                adapter.addEntryRendered(lastDeleted, deletedPosition);
                undoContainer.setVisibility(View.GONE);
                listView.setFocusable(true);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(P_KEY_EA_SORT, sortSetting);
        editor.apply();
    }

}
