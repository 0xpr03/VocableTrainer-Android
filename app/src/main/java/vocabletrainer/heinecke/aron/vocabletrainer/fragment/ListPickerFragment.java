package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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

/**
 * List selector fragment<br>
 *     This can be used externally in other fragments<br>
 *     Requires a toolbar
 */
public class ListPickerFragment extends BaseFragment {
    public static final String TAG = "ListPickerFragment";
    private static final String P_KEY_LA_SORT = "LA_sorting";
    private static final String K_MULTISELECT = "multiselect";
    private static final String K_SHOWOK = "showok";
    private static final String K_PRESELECT = "preselect";
    private static final String K_DELETE = "delete";

    private View view;
    private Database db;
    private boolean multiselect;
    private boolean showOkButton;
    private ListView listView;
    private TableListAdapter adapter;
    private boolean delete;
    private Button bOk;
    private int sort_type;
    private GenTableComparator compName;
    private GenTableComparator compA;
    private GenTableComparator compB;
    private GenTableComparator cComp;
    private FinishListener listener;

    /**
     * Interface for list picker finish
     */
    public interface FinishListener {
        /**
         * Called when ok button is pressed
         * @param selected Selected lists<br>
         *        Contains one element if multiselect is disabled
         */
        void selectionFinished(ArrayList<VList> selected);

        /**
         * Called when list picker got canceled.
         */
        void cancel();
    }

    /**
     * Create new ListPickerFragment instance
     * @param multiSelect Multi select enabled
     * @param delete Delete mode
     * @param selected List of pre-selected VList
     * @param showOkButton True for ok-submit button
     * @return ListPickerFragment
     */
    @NonNull
    public static ListPickerFragment newInstance(final boolean multiSelect, final boolean delete,
                                                 final ArrayList<VList> selected,
                                                 final boolean showOkButton){
        ListPickerFragment lpf = new ListPickerFragment();
        Bundle args = new Bundle();
        args.putBoolean(K_DELETE, delete);
        args.putBoolean(K_MULTISELECT, multiSelect);
        args.putBoolean(K_SHOWOK, showOkButton);
        args.putParcelableArrayList(K_PRESELECT, selected);
        lpf.setArguments(args);
        return lpf;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = null;
        if(context instanceof FinishListener){
            listener = (FinishListener) context;
        } else {
            Log.d(TAG,context.toString() + " does not implement FinishListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG,"onActivityCreated "+(savedInstanceState != null));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView "+(savedInstanceState != null));
        view = inflater.inflate(R.layout.fragment_list_selector,container,false);
        db = new Database(getActivity());

        Bundle bundle = getArguments();
        if(savedInstanceState != null){
            bundle = savedInstanceState;
        }
        multiselect = bundle.getBoolean(K_MULTISELECT);
        showOkButton = bundle.getBoolean(K_SHOWOK);
        delete = bundle.getBoolean(K_DELETE);
        List<VList> preselected = bundle.getParcelableArrayList(K_PRESELECT);

        ActionBar ab = getACActivity().getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        setHasOptionsMenu(true);

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

        bOk = (Button) view.findViewById(R.id.btnOkSelect);
        bOk.setVisibility(showOkButton && multiselect ? View.VISIBLE : View.GONE);

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        sort_type = settings.getInt(P_KEY_LA_SORT, R.id.lMenu_sort_Name);
        updateComp();

        // setup listview
        initListView();
        loadTables(preselected);
        updateOkButton();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.list,menu);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"onSaveInstanceState");
        ArrayList<VList> lst;
        if(listView.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE){
            lst = getSelectedItems();
        } else {
            lst = new ArrayList<>(0);
        }
        outState.putParcelableArrayList(K_PRESELECT,lst);
        outState.putBoolean(K_SHOWOK,showOkButton);
        outState.putBoolean(K_MULTISELECT, multiselect);
        outState.putBoolean(K_DELETE, delete);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        loadTables(null);
    }

    /**
     * Load lists from db
     *
     * @param tickedLists already selected lists, can be null
     */
    private void loadTables(List<VList> tickedLists) {
        List<VList> lists = db.getTables();
        adapter.setAllUpdated(lists, cComp);
        if (tickedLists != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                VList tbl = adapter.getItem(i);
                if (tbl.isExisting() && tickedLists.contains(tbl)) {
                    listView.setItemChecked(i, true);
                }
            }
        }
    }

    /**
     * Setup list view
     */
    private void initListView() {
        listView = (ListView) view.findViewById(R.id.listVIewLstSel);

        ArrayList<VList> lists = new ArrayList<>();
        adapter = new TableListAdapter(getActivity(), R.layout.table_list_view, lists, multiselect);

        listView.setAdapter(adapter);

        if (multiselect) {
            // TODO: title; setTitle(R.string.ListSelector_Title_Training);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setItemsCanFocus(false);
            listView.setOnItemClickListener((adapterView, view, position, id) -> {
                if (adapter.getItem(position).getId() != ID_RESERVED_SKIP) {
                    updateOkButton();
                }
            });
            Log.d(TAG,"visible button!");
            bOk.setOnClickListener(v -> listener.selectionFinished(getSelectedItems()));
        } else {
            if (delete) {
//                setTitle(R.string.ListSelector_Title_Delete);
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    VList list = adapter.getItem(position);
                    if (list.getId() != ID_RESERVED_SKIP) {
                        showDeleteDialog(list);
                    }
                });
            } else {
//                setTitle(R.string.ListSelector_Title_Edit);
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    VList list = adapter.getItem(position);
                    if (list.getId() != ID_RESERVED_SKIP) {
                        ArrayList<VList> result = new ArrayList<>(1);
                        result.add(list);
                        listener.selectionFinished(result);
                    }
                });
            }
        }
    }

    /**
     * Get selected items
     * Can be used to query the selected items at any time
     * @return List of VList which are selected
     */
    public ArrayList<VList> getSelectedItems(){
        ArrayList<VList> selectedLists = new ArrayList<>(10);
        Log.d(TAG,"listView: "+(listView!= null) + " mode: "+listView.getChoiceMode());
        final SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
        int chkItemsCount = checkedItems.size();

        for (int i = 0; i < chkItemsCount; ++i) {
            if (checkedItems.valueAt(i)) {
                VList item = adapter.getItem(checkedItems.keyAt(i));
                if(item.isExisting())
                    selectedLists.add(item);
                else
                    Log.d(TAG,"ignoring item");
            }
        }
        return selectedLists;
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
        final AlertDialog.Builder finishedDiag = new AlertDialog.Builder(getActivity());

        finishedDiag.setTitle(R.string.ListSelector_Diag_delete_Title);
        finishedDiag.setMessage(String.format(getText(R.string.ListSelector_Diag_delete_Msg).toString(),
                listToDelete.getName(), listToDelete.getNameA(), listToDelete.getNameB()));

        finishedDiag.setPositiveButton(R.string.ListSelector_Diag_delete_btn_Delete, (dialog, whichButton) -> {
            db.deleteTable(listToDelete);
            adapter.removeEntryUpdated(listToDelete);
        });

        finishedDiag.setNegativeButton(R.string.ListSelector_Diag_delete_btn_Cancel, (dialog, whichButton) -> {
            // do nothing
        });

        finishedDiag.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Save values
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(P_KEY_LA_SORT, sort_type);
        editor.apply();
    }
}
