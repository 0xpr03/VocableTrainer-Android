package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import vocabletrainer.heinecke.aron.vocabletrainer.activity.EditorActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.FileActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.ListActivity;
import vocabletrainer.heinecke.aron.vocabletrainer.activity.lib.EntryListAdapter;
import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.dialog.VListEditorDialog;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Import.ImportFetcher;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Import.Importer;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Import.PreviewParser;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.GenericSpinnerEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.ExImportActivity.populateFormatSpinnerAdapter;
import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.DPIHelper.DPIToPixels;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.MIN_ID_TRESHOLD;

/*
 * Import Activity
 */
public class ImportFragment extends BaseFragment {

    private static final String P_KEY_I_IMP_MULTI = "import_sp_multi";
    private static final String P_KEY_I_IMP_SINGLE = "import_sp_single";
    private static final String P_KEY_I_IMP_RAW = "import_sp_raw";
    private static final String P_KEY_I_IMP_FORMAT = "import_sp_format";
    private static final int REQUEST_FILE_RESULT_CODE = 1;
    private static final int REQUEST_LIST_SELECT_CODE = 2;
    private static final String TAG = "ImportFragment";
    File impFile;
    List<VEntry> lst;
    EntryListAdapter adapter;
    VList targetList;
    ArrayAdapter<GenericSpinnerEntry<CSVFormat>> spAdapterFormat;
    ArrayAdapter<GenericSpinnerEntry<Importer.IMPORT_LIST_MODE>> spAdapterMultilist;
    ArrayAdapter<GenericSpinnerEntry<Importer.IMPORT_LIST_MODE>> spAdapterSinglelist;
    ArrayAdapter<GenericSpinnerEntry<Importer.IMPORT_LIST_MODE>> spAdapterRawlist;
    private Spinner spFormat;
    private Spinner spSingleList;
    private Spinner spSingelRaw;
    private Spinner spMultilist;
    private Button bSelectList;
    private EditText etList;
    private EditText etFile;
    private Button bImportOk;
    private ListView list;
    private ConstraintLayout singleLayout;
    private TextView tInfo;
    private boolean isRawData = false;
    private boolean isMultilist = true;
    private PreviewParser previewParser;
    private TextView tMsg;
    private View view;

    private boolean showedCustomFormatFragment = false;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_import, container, false);

        setHasOptionsMenu(true);

        lst = new ArrayList<>();
        adapter = new EntryListAdapter(getActivity(), lst);

        spSingelRaw = (Spinner) view.findViewById(R.id.spImportSingleRaw);
        spSingleList = (Spinner) view.findViewById(R.id.spImportSingleMetadata);
        spMultilist = (Spinner) view.findViewById(R.id.spImportMultiple);
        singleLayout = (ConstraintLayout) view.findViewById(R.id.cImportNonMultilist);
        tInfo = (TextView) view.findViewById(R.id.tImportInfo);
        etList = (EditText) view.findViewById(R.id.tImportList);
        bSelectList = (Button) view.findViewById(R.id.bImportSelectList);
        etFile = (EditText) view.findViewById(R.id.tImportPath);
        bImportOk = (Button) view.findViewById(R.id.bImportOk);
        list = (ListView) view.findViewById(R.id.lstImportPreview);
        spFormat = (Spinner) view.findViewById(R.id.spImportFormat);
        tMsg = (TextView) view.findViewById(R.id.tImportMsg);

        bSelectList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectList();
            }
        });

        bImportOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImport();
            }
        });

        Button bSelectFile = (Button) view.findViewById(R.id.bImportFile);
        bSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });

        tMsg.setMovementMethod(LinkMovementMethod.getInstance());
        list.setAdapter(adapter);

        bImportOk.setEnabled(false);
        etList.setKeyListener(null);
        etFile.setKeyListener(null);

        initSpinner();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.exp_import, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentActivity().finish();
                return true;
            case R.id.tCustomFormat:
                showedCustomFormatFragment = true;
                FormatFragment formatFragment = new FormatFragment();
                getFragmentActivity().addFragment(this,formatFragment);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup spinners
     */
    private void initSpinner() {
        spAdapterFormat = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        spAdapterMultilist = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        spAdapterSinglelist = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        spAdapterRawlist = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);

        spAdapterMultilist.add(new GenericSpinnerEntry<>(Importer.IMPORT_LIST_MODE.REPLACE, getString(R.string.Import_Multilist_REPLACE)));
        spAdapterMultilist.add(new GenericSpinnerEntry<>(Importer.IMPORT_LIST_MODE.ADD, getString(R.string.Import_Multilist_ADD)));
        spAdapterMultilist.add(new GenericSpinnerEntry<>(Importer.IMPORT_LIST_MODE.IGNORE, getString(R.string.Import_Multilist_IGNORE)));

        spAdapterRawlist.add(new GenericSpinnerEntry<>(Importer.IMPORT_LIST_MODE.CREATE, getString(R.string.Import_Rawlist_CREATE)));
        spAdapterRawlist.add(new GenericSpinnerEntry<>(Importer.IMPORT_LIST_MODE.ADD, getString(R.string.Import_Rawlist_MERGE)));

        spAdapterSinglelist.add(new GenericSpinnerEntry<>(Importer.IMPORT_LIST_MODE.REPLACE, getString(R.string.Import_Singlelist_REPLACE)));
        spAdapterSinglelist.add(new GenericSpinnerEntry<>(Importer.IMPORT_LIST_MODE.ADD, getString(R.string.Import_Singlelist_ADD)));
        spAdapterSinglelist.add(new GenericSpinnerEntry<>(Importer.IMPORT_LIST_MODE.CREATE, getString(R.string.Import_Singlelist_CREATE)));

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);

        populateFormatSpinnerAdapter(spAdapterFormat, getActivity(), settings);

        spFormat.setAdapter(spAdapterFormat);
        spMultilist.setAdapter(spAdapterMultilist);
        spSingleList.setAdapter(spAdapterSinglelist);
        spSingelRaw.setAdapter(spAdapterRawlist);

        spFormat.setSelection(settings.getInt(P_KEY_I_IMP_FORMAT, 0));
        spSingelRaw.setSelection(settings.getInt(P_KEY_I_IMP_RAW, 0));
        spSingleList.setSelection(settings.getInt(P_KEY_I_IMP_SINGLE, 0));
        spMultilist.setSelection(settings.getInt(P_KEY_I_IMP_MULTI, 0));

        spFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshParsing();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spSingleList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spMultilist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spSingelRaw.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(P_KEY_I_IMP_MULTI, spMultilist.getSelectedItemPosition());
        editor.putInt(P_KEY_I_IMP_SINGLE, spSingleList.getSelectedItemPosition());
        editor.putInt(P_KEY_I_IMP_RAW, spSingelRaw.getSelectedItemPosition());
        editor.putInt(P_KEY_I_IMP_FORMAT, spFormat.getSelectedItemPosition());
        editor.apply();
    }

    /**
     * Returns the selected CSVFormat
     *
     * @return CSVFormat to be used to parsing
     */
    private CSVFormat getFormatSelected() {
        return spAdapterFormat.getItem(spFormat.getSelectedItemPosition()).getObject();
    }

    /**
     * Refresh preview parsing, change view accordingly
     */
    private void refreshParsing() {
        if (impFile != null && impFile.exists()) {
            CSVFormat format = getFormatSelected();
            final PreviewParser dataHandler = new PreviewParser(lst);
            final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setCancelable(false);
            alert.setMessage("");
            alert.setTitle(R.string.Import_Preview_Update_Title);
            final ProgressBar tw = new ProgressBar(getActivity());
            Space sp = new Space(getActivity());
            sp.setMinimumHeight((int) DPIToPixels(getResources(), 10)); // little space downside
            LinearLayout rl = new TableLayout(getActivity());
            rl.addView(tw);
            rl.addView(sp);
            alert.setView(rl);

            /*alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //TODO: add cancel option
                }
            });*/
            final AlertDialog dialog = alert.show();
            Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
//                    dialog.dismiss();
                    isMultilist = dataHandler.isMultiList();
                    isRawData = dataHandler.isRawData();
                    refreshView();
                    adapter.notifyDataSetChanged();
                    previewParser = dataHandler;
                    return null;
                }
            };
            ImportFetcher imp = new ImportFetcher(format, impFile, dataHandler, 0, dialog, tw, callable);
            lst.clear();
            Log.d(TAG, "Starting task");
            imp.execute(0); // 0 is just to pass smth
        }
    }

    /**
     * Returns the {@link Importer.IMPORT_LIST_MODE} of the relevant adapter
     *
     * @return
     */
    private Importer.IMPORT_LIST_MODE getListMode() {
        if (isMultilist) {
            return spAdapterMultilist.getItem(spMultilist.getSelectedItemPosition()).getObject();
        } else if (isRawData) {
            return spAdapterRawlist.getItem(spSingelRaw.getSelectedItemPosition()).getObject();
        } else {
            return spAdapterSinglelist.getItem(spSingleList.getSelectedItemPosition()).getObject();
        }
    }

    /**
     * Called when import was clickeds
     */
    public void onImport() {
        CSVFormat format = getFormatSelected();
        final Importer dataHandler = new Importer(getActivity().getApplicationContext(), previewParser, getListMode(), targetList);
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setCancelable(false);
        alert.setTitle(R.string.Import_Importing_Title);
        final ProgressBar pg = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
        pg.setIndeterminate(false);
        LinearLayout rl = new TableLayout(getActivity());
        rl.addView(pg);
        alert.setView(rl);
        /*alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //TODO: add cancel option
            }
        });*/
        final AlertDialog dialog = alert.show();
        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getActivity().finish();
                return null;
            }
        };
        Log.d(TAG, "amount: " + previewParser.getAmountRows());
        ImportFetcher imp = new ImportFetcher(format, impFile, dataHandler, previewParser.getAmountRows(), dialog, pg, callable);
        lst.clear();
        Log.d(TAG, "Starting task");
        imp.execute(0); // 0 is just to pass smth
    }

    /**
     * Refresh visibility of all options based on the input<br>
     * also calls checkInput
     */
    private void refreshView() {
        singleLayout.setVisibility(isMultilist ? View.GONE : View.VISIBLE);
        spMultilist.setVisibility(isMultilist ? View.VISIBLE : View.GONE);
        spSingelRaw.setVisibility(isRawData ? View.VISIBLE : View.GONE);
        spSingleList.setVisibility(isRawData ? View.GONE : View.VISIBLE);
        if (!isMultilist) {
            boolean hideListSelect = !isRawData && getListMode() != Importer.IMPORT_LIST_MODE.CREATE;
            etList.setVisibility(hideListSelect ? View.GONE : View.VISIBLE);
            bSelectList.setVisibility(hideListSelect ? View.GONE : View.VISIBLE);
        }

        int text;
        if (isRawData)
            text = R.string.Import_Info_rawlist;
        else if (isMultilist)
            text = R.string.Import_Info_multilist;
        else
            text = R.string.Import_Info_singlelist;
        tInfo.setText(text);

        checkInput();
    }

    /**
     * Called on file select click
     */
    public void selectFile() {
        Intent myIntent = new Intent(getActivity(), FileActivity.class);
        myIntent.putExtra(FileActivity.PARAM_WRITE_FLAG, false);
        myIntent.putExtra(FileActivity.PARAM_MESSAGE, getString(R.string.Import_File_select_Info));
        myIntent.putExtra(FileActivity.PARAM_DEFAULT_FILENAME, "list.csv");
        startActivityForResult(myIntent, REQUEST_FILE_RESULT_CODE);
    }

    /**
     * Called on list select click
     */
    public void selectList() {
        if (getListMode() == Importer.IMPORT_LIST_MODE.CREATE) {
            targetList = new VList("", "", "");
            Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    etList.setText(targetList.getName());
                    checkInput();
                    return null;
                }
            };
            VListEditorDialog dialog = VListEditorDialog.newInstance(true, targetList);
            dialog.setOkAction(callable);
            dialog.show(getFragmentManager(),VListEditorDialog.TAG);
        } else {
            Intent myIntent = new Intent(getActivity(), ListActivity.class);
            myIntent.putExtra(ListActivity.PARAM_MULTI_SELECT, false);
            myIntent.putExtra(ListActivity.PARAM_DELETE_FLAG, false);
            myIntent.putExtra(ListActivity.PARAM_SELECTED, targetList);
            startActivityForResult(myIntent, REQUEST_LIST_SELECT_CODE);
        }
    }

    /**
     * Verify user input and enable import button if appropriate
     */
    private void checkInput() {
        boolean is_ok = true;
        if (impFile == null) {
            is_ok = false;
        }
        Importer.IMPORT_LIST_MODE mode = getListMode();
        //noinspection StatementWithEmptyBody
        if (isMultilist) {
            //don't check the rest
        } else if (isRawData && targetList == null) {
            is_ok = false;
        } else if (mode == Importer.IMPORT_LIST_MODE.CREATE) { // single list
            if (targetList == null) {
                is_ok = false;
            } else if (targetList.getId() >= MIN_ID_TRESHOLD) {
                is_ok = false;
            }
        } else if (isRawData && (mode == Importer.IMPORT_LIST_MODE.ADD || mode == Importer.IMPORT_LIST_MODE.REPLACE) && targetList.getId() < MIN_ID_TRESHOLD) {
            if (targetList.getId() < MIN_ID_TRESHOLD) {
                is_ok = false;
            }
        }

        bImportOk.setEnabled(is_ok);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (showedCustomFormatFragment) {
            showedCustomFormatFragment = false;
            SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
            populateFormatSpinnerAdapter(spAdapterFormat, getActivity(), settings);
            refreshParsing();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_FILE_RESULT_CODE:
                    Log.d(TAG, "got file:" + data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                    impFile = (File) data.getSerializableExtra(FileActivity.RETURN_FILE);
                    etFile.setText(data.getStringExtra(FileActivity.RETURN_FILE_USER_NAME));
                    checkInput();
                    refreshParsing();
                    break;
                case REQUEST_LIST_SELECT_CODE:
                    Log.d(TAG, "got list");
                    targetList = (VList) data.getSerializableExtra(ListActivity.RETURN_LISTS);
                    etList.setText(targetList.getName());
                    checkInput();
                    break;
            }
        }
    }
}
