package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.EditorActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.ItemPickerDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Adapter.ListRecyclerAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Adapter.ListTouchHelper;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenTableComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Comparator.GenericComparator;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel.ListPickerViewModel;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * List selector fragment<br>
 *     This can be used externally in other fragments<br>
 *     Requires a toolbar
 */
public class ListPickerFragment extends PagerFragment implements ListRecyclerAdapter.ItemClickListener,
        ListTouchHelper.SwipeListener, ItemPickerDialog.ItemPickerHandler {
    public static final String TAG = "ListPickerFragment";
    private static final String P_KEY_LA_SORT = "LA_sorting";
    private static final String P_KEY_SORTING_DIALOG = "sorting_dialog_list";
    private static final String K_PRESELECT = "preselect";
    private static final String K_MULTISELECT = "multiSelect";
    public static final String K_SELECT_ONLY = "select_only";
    private static final int CODE_NEW_LIST = 1001;

    private View view;
    private boolean multiSelect;
    private boolean showOkButton;
    private RecyclerView recyclerView;
    private ListRecyclerAdapter adapter;
    private boolean selectOnly;
    private int sort_type;
    private GenTableComparator compName;
    private GenTableComparator compA;
    private GenTableComparator compB;
    private GenTableComparator cComp;
    private FinishListener listener;
    private ListPickerViewModel listPickerViewModel;
    private ItemPickerDialog sortingDialog;
    private FloatingActionButton bNewList;

    @Override
    protected void onFragmentInvisible() {
        //listener.selectionUpdate(getSelectedItems());
    }

    @Override
    public void onItemClick(View view, int position) {
        if(!multiSelect){
            VList list = adapter.getItemAt(position);
            if (list.getId() != ID_RESERVED_SKIP) {
                ArrayList<VList> result = new ArrayList<>(1);
                result.add(list);
                listener.selectionUpdate(result);
            }
        }
        Log.d(TAG,"item licked at: "+ position);
    }

    @Override
    public void onSwiped(ListRecyclerAdapter.VListViewHolder viewHolder, int position) {
        if(position < adapter.getItemCount()){
            VList entry = adapter.getItemAt(position);
            Snackbar snackbar = Snackbar
                    .make(recyclerView, R.string.List_Deleted_Message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.GEN_Undo, view -> {
                        adapter.restoreEntry(entry,position);
                        recyclerView.scrollToPosition(position);
                    })
                    .addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            switch(event){
                                case DISMISS_EVENT_CONSECUTIVE: // second deletion
                                case DISMISS_EVENT_TIMEOUT: // timeout
                                case DISMISS_EVENT_MANUAL: // dismiss() -> view change
                                case DISMISS_EVENT_SWIPE: // swiped away
                                    Log.d(TAG,"deleting list");
                                    Database db = new Database(getContext());
                                    db.deleteList(entry);
                                break;
                            }
                        }
                    });
            snackbar.show();
            adapter.removeEntry(entry);
        }
    }

    @Override
    public void onItemPickerSelected(int position) {
        sort_type = position;
        updateComp();
        adapter.updateSorting(cComp);
        sortingDialog = null;
    }

    /**
     * Interface for list picker finish
     */
    public interface FinishListener {
        /**
         * Called when ok button is pressed or list picker goes invisible
         * @param selected Selected lists<br>
         *        Contains one element if multiSelect is disabled
         */
        void selectionUpdate(ArrayList<VList> selected);

        /**
         * Called when list picker got canceled.
         */
        void cancel();
    }

    /**
     * Create new ListPickerFragment instance
     * @param multiSelect Multi select enabled
     * @param selectOnly Whether only selection is allowed, no delete / creation
     * @param selected List of pre-selected VList
     * @return ListPickerFragment
     */
    @NonNull
    public static ListPickerFragment newInstance(final boolean multiSelect, final boolean selectOnly,
                                                 final ArrayList<VList> selected){
        ListPickerFragment lpf = new ListPickerFragment();
        Bundle args = new Bundle();
        args.putBoolean(K_SELECT_ONLY, selectOnly);
        args.putBoolean(K_MULTISELECT, multiSelect);
        args.putParcelableArrayList(K_PRESELECT, selected);
        lpf.setArguments(args);
        return lpf;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            sortingDialog = (ItemPickerDialog) getACActivity().getSupportFragmentManager().getFragment(savedInstanceState, P_KEY_SORTING_DIALOG);
            Log.d(TAG,"sortingDialog: "+sortingDialog);
            if(sortingDialog != null)
                sortingDialog.setItemPickerHandler(this);
        }

        listPickerViewModel = ViewModelProviders.of(getActivity()).get(ListPickerViewModel.class);

        listPickerViewModel.getListsHandle().observe(this, lists -> {
            if(lists != null) {
                // effectively nothing happens if lists == current
                adapter.submitList(lists, cComp);
                Log.d(TAG,"retrieved data size:"+lists.size());
            }
        });

        // check to not override checked items on viewport change (export)
        if(listPickerViewModel.isDataInvalidated()){
            loadTables();
        }
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
    public void onResume() {
        super.onResume();
        if(listPickerViewModel.isDataInvalidated()){
            // effectively submitList doesn't do anything if list==list
            listPickerViewModel.loadLists(getContext());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CODE_NEW_LIST){
            listPickerViewModel.loadLists(getContext());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView "+(savedInstanceState != null));
        view = inflater.inflate(R.layout.fragment_list_selector,container,false);

        Bundle bundle = getArguments();
        if(savedInstanceState != null){
            bundle = savedInstanceState;
        } else if(bundle == null){
            bundle = new Bundle();
        }
        // default values required for {@link ExImportActivity.ViewPagerAdapter.class} solution, using no arguments
        multiSelect = bundle.getBoolean(K_MULTISELECT,true);
        selectOnly = bundle.getBoolean(K_SELECT_ONLY,false);

        bNewList = view.findViewById(R.id.bListNew);
        bNewList.setVisibility(selectOnly ? View.GONE : View.VISIBLE);
        bNewList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getActivity(), EditorActivity.class);
                myIntent.putExtra(EditorActivity.PARAM_NEW_TABLE, true);
                startActivityForResult(myIntent,CODE_NEW_LIST);
            }
        });

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

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        sort_type = settings.getInt(P_KEY_LA_SORT, 0);
        updateComp();

        initRecyclerView();
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem selectAll = menu.findItem(R.id.lMenu_select_all);
        // FIX pre-v21 devices, on rotation called twice, one time without selectAll
        if(selectAll != null)
            selectAll.setVisible(multiSelect);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.list,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.lMenu_sort:
                sortingDialog = ItemPickerDialog.newInstance(R.array.sort_lists,R.string.GEN_Sort);
                sortingDialog.setItemPickerHandler(this);
                sortingDialog.show(getACActivity().getSupportFragmentManager(),P_KEY_SORTING_DIALOG);
                return true;
            case R.id.lMenu_select_all:
                listPickerViewModel.setSelectAll(!listPickerViewModel.isSelectAll());
                adapter.selectAll(listPickerViewModel.isSelectAll());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Update sorting type
     */
    private void updateComp() {
        switch (sort_type) {
            case 1:
                cComp = compA;
                break;
            case 2:
                cComp = compB;
                break;
            case 0:
                cComp = compName;
                break;
            default:
                cComp = compName;
                sort_type = 0;
                break;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"onSaveInstanceState");
        outState.putBoolean(K_MULTISELECT, multiSelect);
        outState.putBoolean(K_SELECT_ONLY, selectOnly);
        if(sortingDialog != null && sortingDialog.isAdded()){
            getACActivity().getSupportFragmentManager().putFragment(outState, P_KEY_SORTING_DIALOG, sortingDialog);
        }
    }

    /**
     * Load lists from db
     */
    private void loadTables() {
        Log.d(TAG,"loading lists");
        listPickerViewModel.loadLists(getContext());
    }

    /**
     * Setup list view
     */
    private void initRecyclerView() {
        recyclerView = view.findViewById(R.id.listViewRecyclerView);
        // fix bug with collapsing toolbar + scrollview
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            listView.setNestedScrollingEnabled(true);
//            listView.startNestedScroll(View.OVER_SCROLL_ALWAYS);
//        }
        ArrayList<VList> lists = new ArrayList<>();
        adapter = new ListRecyclerAdapter(lists, multiSelect, getContext());
        adapter.setItemClickListener(this);
        bNewList.setEnabled(!selectOnly);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);

        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        if(!selectOnly) {
            ListTouchHelper touchHelper = new ListTouchHelper(this);
            new ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView);

            getACActivity().getSupportActionBar().setTitle(R.string.Lists_Title);
        }
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
