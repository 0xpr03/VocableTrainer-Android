package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.lib.ListTouchHelper;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.lib.ListRecyclerAdapter;
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
public class ListPickerFragment extends PagerFragment implements ListRecyclerAdapter.ItemClickListener, ListTouchHelper.SwipeListener {
    public static final String TAG = "ListPickerFragment";
    private static final String P_KEY_LA_SORT = "LA_sorting";
    private static final String K_MULTISELECT = "multiSelect";
    private static final String K_SHOWOK = "showok";
    private static final String K_PRESELECT = "preselect";
    private static final String K_DELETE = "delete";

    private View view;
    private Database db;
    private boolean multiSelect;
    private boolean showOkButton;
    private RecyclerView recyclerView;
    private ListRecyclerAdapter adapter;
    private boolean delete;
    private Button bOk;
    private int sort_type;
    private GenTableComparator compName;
    private GenTableComparator compA;
    private GenTableComparator compB;
    private GenTableComparator cComp;
    private FinishListener listener;
    private AlertDialog dialog;
    private ListPickerViewModel listPickerViewModel;

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
        if(position < adapter.getItemCount() -1){
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
                                    db.deleteTable(entry);
                                break;
                            }
                        }
                    });
            snackbar.show();
            adapter.removeEntry(entry);
        }
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG,"onActivityCreated "+(savedInstanceState != null));
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView "+(savedInstanceState != null));
        view = inflater.inflate(R.layout.fragment_list_selector,container,false);
        db = new Database(getActivity());

        Bundle bundle = getArguments();
        if(savedInstanceState != null){
            bundle = savedInstanceState;
        } else if(bundle == null){
            bundle = new Bundle();
        }
        // default values required for {@link ExImportActivity.ViewPagerAdapter.class} solution, using no arguments
        multiSelect = bundle.getBoolean(K_MULTISELECT,true);
        showOkButton = bundle.getBoolean(K_SHOWOK,false);
        delete = bundle.getBoolean(K_DELETE,false);

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

        bOk = view.findViewById(R.id.btnOkSelect);
        bOk.setVisibility(showOkButton && multiSelect ? View.VISIBLE : View.GONE);

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        sort_type = settings.getInt(P_KEY_LA_SORT, R.id.lMenu_sort_Name);
        updateComp();

        // setup listview
        initRecyclerView();
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
        outState.putBoolean(K_SHOWOK,showOkButton);
        outState.putBoolean(K_MULTISELECT, multiSelect);
        outState.putBoolean(K_DELETE, delete);
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
        if (!delete){
            adapter.setItemClickListener(this);
        }
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);

        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        if(delete) {
            ListTouchHelper touchHelper = new ListTouchHelper(this);
            new ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView);

            getACActivity().getSupportActionBar().setTitle(R.string.ListSelector_Title_Delete);
        }
    }

    private void deleteList(int position){
        Log.d(TAG,"triggered deletion");
    }

    /**
     * Update enabled state of OK button
     */
    private void updateOkButton() {
        bOk.setEnabled(listPickerViewModel.getSelectedLists().size() > 0);
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
            if(db.deleteTable(listToDelete)) {
                adapter.removeEntry(listToDelete);
            } else{
                Toast.makeText(getContext(),R.string.ListSelector_Diag_delete_error_Toast,Toast.LENGTH_LONG).show();
            }
        });

        finishedDiag.setNegativeButton(R.string.ListSelector_Diag_delete_btn_Cancel, (dialog, whichButton) -> {
            // do nothing
        });

        dialog = finishedDiag.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(dialog != null && dialog.isShowing())
            dialog.dismiss();
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
