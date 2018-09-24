package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.lib.EntryListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.VEntryEditorDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.VListEditorDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenEntryComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenericComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.MIN_ID_TRESHOLD;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList.isIDValid;

/**
 * List editor activity
 */
public class EditorActivity extends AppCompatActivity implements VEntryEditorDialog.EditorDialogDataProvider, VListEditorDialog.ListEditorDataProvider {
    /**
     * Param key for new list, default is false
     */
    public static final String PARAM_NEW_TABLE = "NEW_TABLE";
    /**
     * Param key for list to load upon new_table false
     */
    public static final String PARAM_TABLE = "list";
    public static final String TAG = "EditorActivity";
    private static final String P_KEY_EA_SORT = "EA_sorting";
    private static final String KEY_EDITOR_POSITION = "editorPosition";
    private static final String KEY_EDITOR_ENTRY = "editorEntry";
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
    private MenuItem mSort_ColA;
    private MenuItem mSort_ColB;
    VEntryEditorDialog editorDialog;
    VListEditorDialog listEditorDialog;

    // current edit
    private int editPosition = MIN_ID_TRESHOLD -1; // store position for viewport change, shared object
    private VEntry editorEntry = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        entries = new ArrayList<>();
        setContentView(R.layout.activity_editor);
        db = new Database(getBaseContext());

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        clearEdit();

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

        FloatingActionButton bNewEntry = findViewById(R.id.bEditorNewEntry);
        bNewEntry.setOnClickListener(v -> addEntry());

        // setup listview
        initListView();


        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        sortSetting = settings.getInt(P_KEY_EA_SORT, R.id.eMenu_sort_A);
        updateComp();

        // handle passed params
        boolean newTable = intent.getBooleanExtra(PARAM_NEW_TABLE, false);
        if(savedInstanceState != null) {
            newTable = savedInstanceState.getBoolean(PARAM_NEW_TABLE,false);
            listEditorDialog = (VListEditorDialog) getSupportFragmentManager().getFragment(savedInstanceState, VListEditorDialog.TAG);
        }
        if (newTable) {
            if(savedInstanceState != null) // viewport changed during creation phase
                list = savedInstanceState.getParcelable(PARAM_TABLE);
            else
                list = new VList(getString(R.string.Editor_Hint_Column_A), getString(R.string.Editor_Hint_Column_B), getString(R.string.Editor_Hint_List_Name));
            Log.d(TAG, "new list mode");
            if(savedInstanceState == null)
                showTableInfoDialog();
        } else {
            VList tbl = intent.getParcelableExtra(PARAM_TABLE);
            if (tbl != null) {
                this.list = tbl;
                // do not call updateColumnNames as we've to wait for onCreateOptionsMenu, calling it
                entries.addAll(db.getVocablesOfTable(list));
                adapter.updateSorting(cComp);
                Log.d(TAG, "edit list mode");
            } else {
                Log.e(TAG, "Edit VList Flag set without passing a list");
            }
        }

        if(listEditorDialog != null)
            setListEditorActions();

        if(savedInstanceState != null ) {
            editorDialog = (VEntryEditorDialog) getSupportFragmentManager().getFragment(savedInstanceState, VEntryEditorDialog.TAG);
            if(editorDialog != null) {
                // DialogFragment re-adds itself
                editPosition = savedInstanceState.getInt(KEY_EDITOR_POSITION);
                if (isIDValid(editPosition))
                    editorEntry = (VEntry) adapter.getItem(editPosition);
                else
                    editorEntry = savedInstanceState.getParcelable(KEY_EDITOR_ENTRY);
                setEditorDialogActions();
            }
        }

        this.setTitle(list.getName());
    }

    /**
     * Clear current edit state
     */
    private void clearEdit() {
        editPosition = MIN_ID_TRESHOLD -1; // clear
        editorEntry = null;
    }

    /**
     * Handles list column name changes
     */
    private void updateColumnNames(){
        mSort_ColB.setTitle(list.getNameB());
        mSort_ColA.setTitle(list.getNameA());
        adapter.setTableData(list);
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
        adapter.updateSorting(cComp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor, menu);
        mSort_ColA = menu.findItem(R.id.eMenu_sort_A);
        mSort_ColB = menu.findItem(R.id.eMenu_sort_B);
        updateColumnNames();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.tEditorListEdit:
                showTableInfoDialog();
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
     * Save editorEntry to DB & update listview
     */
    private void saveEdit() {
        ArrayList<VEntry> lst = new ArrayList<>(1);
        lst.add(editorEntry);

        if (!editorEntry.isExisting()) {
            adapter.addEntryUnrendered(editorEntry);
        }
        db.upsertEntries(lst);
        adapter.notifyDataSetChanged();
    }

    /**
     * Setup listview
     */
    private void initListView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        listView = findViewById(R.id.listviewEditor);

        listView.setLongClickable(true);

        entries = new ArrayList<>();
        adapter = new EntryListAdapter(this, entries);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, pos, id) -> showEntryEditDialog((VEntry) adapter.getItem(pos), pos));

        listView.setOnItemLongClickListener((arg0, arg1, pos, id) -> {
            showEntryDeleteDialog((VEntry) adapter.getItem(pos), pos);
            return true;
        });
    }

    /**
     * Add new VEntry
     */
    public void addEntry() {
        editorEntry = new VEntry(list);
        showEntryEditDialog(editorEntry);
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
        delDiag.setMessage(String.format(getString(R.string.Editor_Diag_delete_MSG_part) + "\n %s %s %s", entry.getAString(), entry.getBString(), entry.getTip()));

        delDiag.setPositiveButton(R.string.Editor_Diag_delete_btn_OK, (dialog, whichButton) -> {
            lastDeleted = entry;
            deletedPosition = position;
            adapter.remove(entry);
            showUndo();
            Log.d(TAG, "deleted");
        });

        delDiag.setNegativeButton(R.string.Editor_Diag_delete_btn_CANCEL, (dialog, whichButton) -> Log.d(TAG, "canceled"));

        delDiag.show();
    }

    /**
     * Show entry edit dialog for new vocable
     * @param entry
     */
    private void showEntryEditDialog(final VEntry entry) {
        showEntryEditDialog(entry,MIN_ID_TRESHOLD-1);
    }

    /**
     * Show entry edit dialog
     *
     * @param entry VEntry to edit/create
     * @param position edit position in list, if existing
     */
    private void showEntryEditDialog(final VEntry entry, final int position) {
        if (entry.getId() == ID_RESERVED_SKIP) {
            showTableInfoDialog();
            return;
        }

        this.editPosition = position;
        this.editorEntry = entry;
        editorDialog = VEntryEditorDialog.newInstance();
        setEditorDialogActions();

        editorDialog.show(getSupportFragmentManager(), VEntryEditorDialog.TAG);
    }

    /**
     * Setup editor dialog actions
     */
    private void setEditorDialogActions(){
        editorDialog.setOkAction(e -> {
            saveEdit();
            Log.d(TAG,"edited");
            return null;
        });
        editorDialog.setCancelAction(e -> {
            Log.d(TAG,"canceled");
            return null;
        });
    }

    /**
     * Setup list editor actions
     */
    private void setListEditorActions() {
        listEditorDialog.setCancelAction(() -> {
            if(!list.isExisting()) {
                finish();
            }
            return null;
        });
        listEditorDialog.setOkAction(() -> {
            if(db.upsertVList(list)) {
                setTitle(list.getName());
                updateColumnNames();
            } else {
                Toast.makeText(this, "Unable to save list!",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return null;
        });
    }

    /**
     * Show list title editor dialog<br>
     *     Exit editor when newTbl is set and user cancels the dialog
     */
    private void showTableInfoDialog() {
        listEditorDialog = VListEditorDialog.newInstance(!list.isExisting());
        setListEditorActions();
        listEditorDialog.show(getSupportFragmentManager(), VListEditorDialog.TAG);
    }

    /**
     * Show undo view<br>
     * On viewchange during the animation we're not deleting the vocable
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
                        lastDeleted.setDelete(true);
                        ArrayList<VEntry> lst = new ArrayList<>(1);
                        lst.add(lastDeleted);
                        db.upsertEntries(lst);
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

        undoContainer.setOnClickListener(v -> {
            Log.d(TAG, "undoing");
            undoContainer.clearAnimation();
            adapter.addEntryRendered(lastDeleted, deletedPosition);
            undoContainer.setVisibility(View.GONE);
            listView.setFocusable(true);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(editorDialog != null && editorDialog.isAdded())
            getSupportFragmentManager().putFragment(outState, VEntryEditorDialog.TAG,editorDialog);
        if(listEditorDialog != null && listEditorDialog.isAdded())
            getSupportFragmentManager().putFragment(outState, VListEditorDialog.TAG,listEditorDialog);
        outState.putInt(KEY_EDITOR_POSITION,editPosition);
        if(editorEntry != null && !editorEntry.isExisting()) // unsaved new entry (empty entry as filled by editor)
            outState.putParcelable(KEY_EDITOR_ENTRY,editorEntry);
        if(!list.isExisting()) { // unsaved new table, still in creation dialog
            outState.putBoolean(PARAM_NEW_TABLE,true);
            outState.putParcelable(PARAM_TABLE,list);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(P_KEY_EA_SORT, sortSetting);
        editor.apply();
    }

    @NonNull
    @Override
    public VEntry getEditVEntry() {
        return editorEntry;
    }

    @NonNull
    @Override
    public VList getList() {
        return list;
    }
}
