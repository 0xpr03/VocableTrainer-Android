package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import androidx.lifecycle.ViewModelProviders;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Objects;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Adapter.EntryListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel.ImportViewModel;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * Import Preview Fragment, displays import preview
 */
public class PreviewFragment extends BaseFragment {

    public static final String TAG = "PagerFragment";
    private ArrayList<VEntry> lst;
    private EntryListAdapter adapter;
    private VEntry ENTRY_LOADING;
    private VEntry ENTRY_EMPTY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ENTRY_LOADING = new VEntry(getString(R.string.Import_Preview_Loading),"","",ID_RESERVED_SKIP);
        ENTRY_EMPTY = new VEntry(getString(R.string.Import_Preview_No_data),"","",ID_RESERVED_SKIP);

        ImportViewModel model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ImportViewModel.class);
        model.getPreviewList().observe(this, previewList -> {
            Log.d(TAG,"preview change");
            lst.clear();
            if(previewList != null && previewList.size() > 0)
                lst.addAll(previewList);
            else
                lst.add(ENTRY_EMPTY);
            adapter.notifyDataSetChanged();
        });

        model.getReparsingHandle().observe(this, isParsing -> {
            if(isParsing != null && isParsing) {
                Log.d(TAG,"parsing state changed");
                lst.clear();
                lst.add(ENTRY_LOADING);
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        lst = new ArrayList<>();
        adapter = new EntryListAdapter(Objects.requireNonNull(getActivity()), lst);
        ListView listView = view.findViewById(R.id.lstImportPreview);
        listView.setAdapter(adapter);

        // fix bug with collapsing toolbar + scrollview
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listView.setNestedScrollingEnabled(true);
            listView.startNestedScroll(View.OVER_SCROLL_ALWAYS);
        }

        return view;
    }

}
