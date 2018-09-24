package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import org.apache.commons.csv.CSVFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.lib.ViewPagerAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.ExportFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.FormatFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.ImportFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.ListPickerFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.PreviewFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVCustomFormat;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.GenericSpinnerEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel.FormatViewModel;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;

/**
 * Activity for import/export
 */
public class ExImportActivity extends FragmentActivity implements ListPickerFragment.FinishListener, ExportFragment.ExportListProvider {
    private final static String P_KEY_S_CSV_FORMAT = "csv_format";

    private static final String TAG = "ExImportActivity";
    /**
     * This permission is required for this activity to work
     */
    public static final String REQUIRED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    /**
     * Pass this as false to show export options
     */
    public static final String PARAM_IMPORT = "show_import";
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private ArrayList<VList> selectedExportLists = new ArrayList<>(0);
    private FormatViewModel formatViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");

        setContentView(R.layout.activity_expimp);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        formatViewModel = ViewModelProviders.of(this).get(FormatViewModel.class);

        viewPager = (ViewPager) findViewById(R.id.pager);

        setFragmentContainer(R.id.pager);

        initViewPager();
        Log.d(TAG,"Amount: "+viewPagerAdapter.getCount());

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Init ViewPager
     */
    private void initViewPager(){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),this);
        Intent intent = getIntent();
        boolean showImport = intent.getBooleanExtra(PARAM_IMPORT, true);
        if(showImport){
            getSupportActionBar().setTitle(R.string.Import_Title);
            viewPagerAdapter.addFragment(ImportFragment.class, R.string.Import_Tab_Main);
            viewPagerAdapter.addFragment(PreviewFragment.class, R.string.Import_Tab_Preview);
        } else {
            getSupportActionBar().setTitle(R.string.Export_Title);
            viewPagerAdapter.addFragment(ExportFragment.class, R.string.Export_Tab_Main);
            viewPagerAdapter.addFragment(ListPickerFragment.class,R.string.Export_Tab_List);
        }
        viewPagerAdapter.addFragment(FormatFragment.class, R.string.ExImport_Tab_CustomFormat);
        viewPager.setAdapter(viewPagerAdapter);

        // expand appbar on tab change
        AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                appBarLayout.setExpanded(true);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCustomFormat();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                Log.d(TAG,"onOptionsItemSelected:home");
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Returns the custom format<br>
     * This can be user defined
     *
     * @param settings required to load preferences
     * @return
     */
    public static CSVCustomFormat loadCustomFormat(SharedPreferences settings) {
        CSVCustomFormat format;
        String serialized = settings.getString(P_KEY_S_CSV_FORMAT, null);
        if (serialized == null) {
            format = CSVCustomFormat.DEFAULT;
        } else {
            try {
                byte b[] = Base64.decode(serialized.getBytes(),Base64.DEFAULT);
                ByteArrayInputStream bi = new ByteArrayInputStream(b);
                ObjectInputStream si = new ObjectInputStream(bi);
                format = (CSVCustomFormat) si.readObject();
                si.close();
                bi.close();
                Log.d(TAG,"decoded format");
            } catch (Exception e) {
                Log.w(TAG, "unable to load custom format ", e);
                format = CSVCustomFormat.DEFAULT;
            }
        }
        return format;
    }

    /**
     * Save custom format settings
     *
     */
    private void saveCustomFormat() {
        CSVCustomFormat format = formatViewModel.getCustomFormatData();
        if(format == null)
            return;
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(formatViewModel.getCustomFormatData());
            so.flush();
            SharedPreferences.Editor editor = getSharedPreferences(MainActivity.PREFS_NAME,0).edit();
            editor.putString(P_KEY_S_CSV_FORMAT, new String(Base64.encode(bo.toByteArray(), Base64.DEFAULT)));
            editor.apply();
            so.close();
            bo.close();
            Log.d(TAG,"saved custom format");
        } catch (IOException e) {
            Log.e(TAG, "unable to save format ", e);
        }

    }

    /**
     * Method to (re)populate format spinner adapters
     *
     * @param adapter Adapter to populate
     * @return CustomFormat spinner entry
     */
    public GenericSpinnerEntry<CSVCustomFormat> populateFormatSpinnerAdapter(ArrayAdapter<GenericSpinnerEntry<CSVCustomFormat>> adapter) {
        adapter.clear();
        adapter.add(new GenericSpinnerEntry<>(CSVCustomFormat.DEFAULT, getString(R.string.CSV_Format_Default)));
        adapter.add(new GenericSpinnerEntry<>(new CSVCustomFormat(CSVFormat.EXCEL), getString(R.string.CSV_Format_EXCEL)));
        adapter.add(new GenericSpinnerEntry<>(new CSVCustomFormat(CSVFormat.RFC4180), getString(R.string.CSV_Format_RFC4180)));
        adapter.add(new GenericSpinnerEntry<>(new CSVCustomFormat(CSVFormat.TDF), getString(R.string.CSV_Format_Tabs)));
        adapter.add(new GenericSpinnerEntry<>(new CSVCustomFormat(CSVFormat.MYSQL), getString(R.string.CSV_Format_Mysql)));
        adapter.add(new GenericSpinnerEntry<>(new CSVCustomFormat(CSVFormat.INFORMIX_UNLOAD), getString(R.string.CSV_Format_INFORMIX_UNLOAD)));
        adapter.add(new GenericSpinnerEntry<>(new CSVCustomFormat(CSVFormat.INFORMIX_UNLOAD_CSV), getString(R.string.CSV_Format_INFORMIX_UNLOAD_CSV)));
        GenericSpinnerEntry<CSVCustomFormat> spinnerEntry  = new GenericSpinnerEntry<>(formatViewModel.getCustomFormatData(), getString(R.string.CSV_Format_Custom_Format));
        adapter.add(spinnerEntry);
        adapter.notifyDataSetChanged();
        return spinnerEntry;
    }

    @Override
    public void selectionUpdate(ArrayList<VList> selected) {
        this.selectedExportLists = selected;
    }

    @Override
    public void cancel() {

    }

    /**
     * Returns selected VLists for export
     * @return
     */
    @Override
    public ArrayList<VList> getExportLists() {
        return selectedExportLists;
    }

}
