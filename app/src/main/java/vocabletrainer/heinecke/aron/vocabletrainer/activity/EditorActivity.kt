package vocabletrainer.heinecke.aron.vocabletrainer.activity

import android.content.DialogInterface
import android.database.SQLException
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import vocabletrainer.heinecke.aron.vocabletrainer.R
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.ItemPickerDialog
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.ItemPickerDialog.ItemPickerHandler
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.VEntryEditorDialog
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.VEntryEditorDialog.EditorDialogDataProvider
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.VListEditorDialog
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.VListEditorDialog.ListEditorDataProvider
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Adapter.EntryListAdapter
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenEntryComparator
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList.Companion.isIDValid
import java.util.*

/**
 * List editor activity
 */
class EditorActivity : AppCompatActivity(), EditorDialogDataProvider, ListEditorDataProvider,
    ItemPickerHandler {
    private var list: VList? = null
    private var entries: ArrayList<VEntry>? = null
    private var adapter: EntryListAdapter? = null
    private var listView: ListView? = null
    private var db: Database? = null
    private var sortSetting = 0
    private var cComp: GenEntryComparator? = null
    private var compA: GenEntryComparator? = null
    private var compB: GenEntryComparator? = null
    private var compTip: GenEntryComparator? = null
    private var sortingDialog: ItemPickerDialog? = null
    private var editorDialog: VEntryEditorDialog? = null
    private var listEditorDialog: VListEditorDialog? = null

    // current edit
    private var editPosition: Int =
        (Database.MIN_ID_TRESHOLD - 1).toInt() // store position for viewport change, shared object
    private var editorEntry: VEntry? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        entries = ArrayList()
        setContentView(R.layout.activity_editor)
        db = Database(baseContext)
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
        clearEdit()
        compA = GenEntryComparator(
            arrayOf(
                GenEntryComparator.retA, GenEntryComparator.retB,
                GenEntryComparator.retTip
            ), Database.ID_RESERVED_SKIP
        )
        compB = GenEntryComparator(
            arrayOf(
                GenEntryComparator.retB, GenEntryComparator.retA,
                GenEntryComparator.retTip
            ), Database.ID_RESERVED_SKIP
        )
        compTip = GenEntryComparator(
            arrayOf(
                GenEntryComparator.retTip, GenEntryComparator.retA,
                GenEntryComparator.retB
            ), Database.ID_RESERVED_SKIP
        )
        val intent = intent
        val bNewEntry = findViewById<FloatingActionButton>(R.id.bEditorNewEntry)
        bNewEntry.setOnClickListener { v: View? -> addEntry() }

        // setup listview
        initListView()
        val settings = getSharedPreferences(MainActivity.PREFS_NAME, 0)
        sortSetting = settings.getInt(P_KEY_EA_SORT, 0)
        updateComp()

        // handle passed params
        var newTable = intent.getBooleanExtra(PARAM_NEW_TABLE, false)
        if (savedInstanceState != null) {
            newTable = savedInstanceState.getBoolean(PARAM_NEW_TABLE, false)
            listEditorDialog = supportFragmentManager.getFragment(
                savedInstanceState,
                VListEditorDialog.TAG
            ) as VListEditorDialog?
        }
        if (newTable) {
            list = if (savedInstanceState != null) // viewport changed during creation phase
                savedInstanceState.getParcelable(PARAM_TABLE) else VList.blank(
                getString(R.string.Editor_Hint_Column_A), getString(
                    R.string.Editor_Hint_Column_B
                ), getString(R.string.Editor_Hint_List_Name)
            )
            Log.d(TAG, "new list mode")
            if (savedInstanceState == null) showTableInfoDialog()
        } else {
            val tbl: VList? = intent.getParcelableExtra(PARAM_TABLE)
            if (tbl != null) {
                list = tbl
                // do not call updateColumnNames as we've to wait for onCreateOptionsMenu, calling it
                entries!!.addAll(db!!.getVocablesOfTable(list!!))
                adapter!!.updateSorting(cComp)
                Log.d(TAG, "edit list mode")
            } else {
                Log.e(TAG, "Edit VList Flag set without passing a list")
            }
        }
        if (listEditorDialog != null) setListEditorActions()
        if (savedInstanceState != null) {
            editorDialog = supportFragmentManager.getFragment(
                savedInstanceState,
                VEntryEditorDialog.TAG
            ) as VEntryEditorDialog?
            if (editorDialog != null) {
                // DialogFragment re-adds itself
                editPosition = savedInstanceState.getInt(KEY_EDITOR_POSITION)
                editorEntry =
                    if (isIDValid(editPosition)) adapter!!.getItem(editPosition) as VEntry else savedInstanceState.getParcelable(
                        KEY_EDITOR_ENTRY
                    )
                setEditorDialogActions()
            }
            sortingDialog = supportFragmentManager.getFragment(
                savedInstanceState,
                P_KEY_SORTING_DIALOG
            ) as ItemPickerDialog?
            if (sortingDialog != null) {
                setSortingDialogParams()
            }
        }
        this.title = list!!.name
    }

    /**
     * Set handlers & overrides for sortingDialog
     */
    private fun setSortingDialogParams() {
        sortingDialog!!.run {
            setItemPickerHandler(this@EditorActivity)
            overrideEntry(0, list!!.nameA)
            overrideEntry(1, list!!.nameB)
        }
    }

    /**
     * Clear current edit state
     */
    private fun clearEdit() {
        editPosition = (Database.MIN_ID_TRESHOLD - 1).toInt() // clear
        editorEntry = null
    }

    /**
     * Handles list column name changes
     */
    private fun updateColumnNames() {
        adapter!!.setTableData(list)
    }

    /**
     * Changes cComp to current selection
     */
    private fun updateComp() {
        when (sortSetting) {
            0 -> cComp = compA
            1 -> cComp = compB
            2 -> cComp = compTip
            else -> {
                cComp = compA
                sortSetting = 0
            }
        }
        adapter!!.updateSorting(cComp)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.editor, menu)
        updateColumnNames()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.tEditorListEdit -> {
                showTableInfoDialog()
                return true
            }
            R.id.eMenu_sort -> {
                sortingDialog =
                    ItemPickerDialog.newInstance(R.array.sort_entries, R.string.GEN_Sort)
                setSortingDialogParams()
                sortingDialog!!.show(supportFragmentManager, P_KEY_SORTING_DIALOG)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Save editorEntry to DB & update listview
     */
    private fun saveEdit() {
        val lst = ArrayList<VEntry>(1)
        editorEntry!!.let {
            lst.add(it)
            val isExisting = it.isExisting
            if (!isExisting) {
                adapter!!.addEntryUnrendered(it)
            }
            db!!.upsertEntries(lst)
            adapter!!.notifyDataSetChanged()
            if (!isExisting) {
                addEntry()
            }
        }
    }

    /**
     * Setup listview
     */
    private fun initListView() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
        listView = findViewById(R.id.listviewEditor)
        entries = ArrayList()
        this@EditorActivity.adapter = EntryListAdapter(this@EditorActivity, entries)
        listView!!.run {
            isLongClickable = true
            adapter = this@EditorActivity.adapter
            onItemClickListener =
                OnItemClickListener { parent: AdapterView<*>?, view: View?, pos: Int, id: Long ->
                    showEntryEditDialog(
                        adapter!!.getItem(pos) as VEntry, pos, false
                    )
                }
            onItemLongClickListener =
                OnItemLongClickListener { arg0: AdapterView<*>?, arg1: View?, pos: Int, id: Long ->
                    showEntryDeleteDialog(
                        adapter!!.getItem(pos) as VEntry, pos
                    )
                    true
                }
        }
    }

    /**
     * Add new VEntry
     */
    private fun addEntry() {
        editorEntry = VEntry.blankFromList(list!!)
        showEntryEditDialog(editorEntry!!)
    }

    /**
     * Show entry delete dialog
     *
     * @param entry
     * @param position
     */
    private fun showEntryDeleteDialog(entry: VEntry, position: Int) {
        if (entry.id == Database.ID_RESERVED_SKIP) return
        val delDiag = AlertDialog.Builder(this, R.style.CustomDialog)
        delDiag.setTitle(R.string.Editor_Diag_delete_Title)
        delDiag.setMessage(
            String.format(
                "${getString(R.string.Editor_Diag_delete_MSG_part)}\n %s %s %s", entry.aString, entry.bString, entry.tip
            )
        )
        delDiag.setPositiveButton(R.string.Editor_Diag_delete_btn_OK) { dialog: DialogInterface?, whichButton: Int ->
            deleteEntry(
                position,
                entry
            )
        }
        delDiag.setNegativeButton(R.string.Editor_Diag_delete_btn_CANCEL) { dialog: DialogInterface?, whichButton: Int ->
            Log.d(
                TAG, "canceled"
            )
        }
        delDiag.show()
    }

    /**
     * Perform entry deletion with undo possibility
     * @param deletedPosition
     * @param entry
     */
    private fun deleteEntry(deletedPosition: Int, entry: VEntry) {
        adapter!!.remove(entry)
        val snackbar = Snackbar
            .make(listView!!, R.string.Editor_Entry_Deleted_Message, Snackbar.LENGTH_LONG)
            .setAction(R.string.GEN_Undo) { view: View? ->
                adapter!!.addEntryRendered(
                    entry,
                    deletedPosition
                )
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                    when (event) {
                        DISMISS_EVENT_CONSECUTIVE, DISMISS_EVENT_TIMEOUT, DISMISS_EVENT_MANUAL, DISMISS_EVENT_SWIPE -> {
                            Log.d(TAG, "deleting entry")
                            entry.isDelete = true
                            val lst = ArrayList<VEntry>(1)
                            lst.add(entry)
                            val db = Database(applicationContext)
                            db.upsertEntries(lst) // TODO make a single function
                        }
                    }
                }
            })
        snackbar.show()
    }

    /**
     * Show entry edit dialog for new vocable
     * @param entry
     */
    private fun showEntryEditDialog(entry: VEntry) {
        showEntryEditDialog(entry, (Database.MIN_ID_TRESHOLD - 1).toInt(), true)
    }

    /**
     * Show entry edit dialog
     *
     * @param entry VEntry to edit/create
     * @param position edit position in list, if existing
     */
    @Synchronized
    private fun showEntryEditDialog(entry: VEntry, position: Int, forceDialog: Boolean) {
        if (entry.id == Database.ID_RESERVED_SKIP) {
            showTableInfoDialog()
            return
        }
        if (!forceDialog && editorDialog != null && editorDialog!!.isAdded) {
            return
        }
        editPosition = position
        editorEntry = entry
        editorDialog = VEntryEditorDialog.newInstance()
        setEditorDialogActions()
        editorDialog!!.show(supportFragmentManager, VEntryEditorDialog.TAG)
    }

    /**
     * Setup editor dialog actions
     */
    private fun setEditorDialogActions() {
        editorDialog!!.setOkAction { e: VEntry? ->
            saveEdit()
            null
        }
        editorDialog!!.setCancelAction { e: VEntry? ->
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(0, 0)
            null
        }
    }

    /**
     * Setup list editor actions
     */
    private fun setListEditorActions() {
        listEditorDialog!!.setCancelAction {
            if (!list!!.isExisting) {
                finish()
            }
            null
        }
        listEditorDialog!!.setOkAction {
            try {
                db!!.upsertVList(list!!)
                title = list!!.name
                updateColumnNames()
            } catch (e: SQLException) {
                Toast.makeText(
                    this, "Unable to save list!",
                    Toast.LENGTH_LONG
                ).show()
                finish() // TODO: is this the right place ?
            }
            null
        }
    }

    /**
     * Show list title editor dialog<br></br>
     * Exit editor when newTbl is set and user cancels the dialog
     */
    @Synchronized
    private fun showTableInfoDialog() {
        if (listEditorDialog != null && listEditorDialog!!.isAdded) {
            return
        }
        listEditorDialog = VListEditorDialog.newInstance(!list!!.isExisting)
        setListEditorActions()
        listEditorDialog!!.show(supportFragmentManager, VListEditorDialog.TAG)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (editorDialog != null && editorDialog!!.isAdded) supportFragmentManager.putFragment(
            outState,
            VEntryEditorDialog.TAG,
            editorDialog!!
        )
        if (listEditorDialog != null && listEditorDialog!!.isAdded) supportFragmentManager.putFragment(
            outState,
            VListEditorDialog.TAG,
            listEditorDialog!!
        )
        outState.putInt(KEY_EDITOR_POSITION, editPosition)
        if (editorEntry != null && !editorEntry!!.isExisting) // unsaved new entry (empty entry as filled by editor)
            outState.putParcelable(KEY_EDITOR_ENTRY, editorEntry)
        if (!list!!.isExisting) { // unsaved new table, still in creation dialog
            outState.putBoolean(PARAM_NEW_TABLE, true)
            outState.putParcelable(PARAM_TABLE, list)
        }
        if (sortingDialog != null && sortingDialog!!.isAdded) supportFragmentManager.putFragment(
            outState,
            P_KEY_SORTING_DIALOG,
            sortingDialog!!
        )
    }

    override fun onStop() {
        super.onStop()
        val settings = getSharedPreferences(MainActivity.PREFS_NAME, 0)
        val editor = settings.edit()
        editor.putInt(P_KEY_EA_SORT, sortSetting)
        editor.apply()
    }

    override fun getEditVEntry(): VEntry {
        return editorEntry!!
    }

    override fun getList(): VList {
        return list!!
    }

    override fun onItemPickerSelected(position: Int) {
        sortSetting = position
        updateComp()
    }

    companion object {
        /**
         * Param key for new list, default is false
         */
        const val PARAM_NEW_TABLE = "NEW_TABLE"

        /**
         * Param key for list to load upon new_table false
         */
        const val PARAM_TABLE = "list"
        const val TAG = "EditorActivity"
        private const val P_KEY_EA_SORT = "EA_sorting"
        private const val P_KEY_SORTING_DIALOG = "sorting_dialog_editor"
        private const val KEY_EDITOR_POSITION = "editorPosition"
        private const val KEY_EDITOR_ENTRY = "editorEntry"
    }
}