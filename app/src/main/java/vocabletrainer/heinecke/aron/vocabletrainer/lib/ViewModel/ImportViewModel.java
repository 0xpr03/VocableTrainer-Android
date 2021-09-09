package vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.CSVCustomFormat;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.Import.ImportFetcher;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.Import.ImportFetcherBuilder;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.Import.Importer;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.Import.PreviewParser;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Function;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;

import static android.os.AsyncTask.Status.RUNNING;

/**
 * ViewModel for import task<br>
 * Handles background tasks & contains livedata handles
 * @author Aron Heinecke
 */
public class ImportViewModel extends ViewModel {
    private final static String TAG = "ImportViewModel";
    private MutableLiveData<ArrayList<VEntry>> previewList;
    private MutableLiveData<Boolean> reparsing;
    private MutableLiveData<Boolean> importing;
    private MutableLiveData<Integer> progress;
    private MutableLiveData<LogData> importLog; // also preview parsing, see data container
    private MutableLiveData<Boolean> cancelPreview; // cancelPreview parser thread trigger
    private MutableLiveData<Boolean> cancelImport;
    private boolean isMultiList;
    private boolean isRawData;
    // set on preview parsing finished, contains data
    private MutableLiveData<PreviewParser> previewParser;
    // internal observer, resetting reparsing on update
    private Observer<ArrayList<VEntry>> observerPreviewList;
    // internal observer for setting whether it was cancelled
    private Observer<Boolean> observerCancel;
    private AsyncTask parserThread;

    /**
     * Log Data container
     */
    public static class LogData{
        public final String log;
        public final boolean isPreview;

        /**
         * New Log data container
         * @param log Log data
         * @param isPreview whether it's preview data or not
         */
        public LogData(@NonNull String log, boolean isPreview) {
            this.log = log;
            this.isPreview = isPreview;
        }
    }

    /**
     * Start preview parsing
     * @param format
     */
    public void previewParse(@NonNull CSVCustomFormat format, @NonNull Uri impFile, @NonNull Context context, @NonNull ImportFetcher.MessageProvider mp) {
        if(verifyNoParsersRunning())
            return;
        //CSVCustomFormat format = getFormatSelected();
        final PreviewParser dataHandler = new PreviewParser(new ArrayList<>(20));
        Function<Void,String> callback = param -> {
            setMultiList(dataHandler.isMultiList());
            setRawData(dataHandler.isRawData());
            setPreviewParser(dataHandler);
            updateIsReparsing(false);
            importLog.setValue(new LogData(param,true)); // show logged exceptions during preview parsing
            return null;
        };
        Function<Void,String> callbackCancel = param -> {
            resetPreviewData();
            updateIsReparsing(false);
            cancelPreview.setValue(false);
            return null;
        };
        ImportFetcher imp = new ImportFetcherBuilder()
                .setFormat(format)
                .setSource(impFile)
                .setHandler(dataHandler)
                .setLogErrors(false)
                .setContext(context)
                .setMessageProvider(mp)
                .setProgressHandle(progress)
                .setImportCallback(callback)
                .setCancelCallback(callbackCancel)
                .createImportFetcher();
        Log.d(TAG, "starting preview parsing task");
        this.updateIsReparsing(true);
        parserThread = imp.execute(0); // 0 is just to pass something
    }

    public void runImport(@NonNull Importer dataHandler, @NonNull CSVCustomFormat format, @NonNull Uri impFile,
              @NonNull Context context,@NonNull ImportFetcher.MessageProvider mp){
        if(verifyNoParsersRunning())
            return;
        Function<Void,String> callback = param -> {
            importing.setValue(false);
            importLog.setValue(new LogData(param,false));
            cancelImport.setValue(false);
            return null;
        };
        Log.d(TAG, "amount: " + getPreviewParser().getAmountRows());

        ImportFetcher imp = new ImportFetcherBuilder()
                .setFormat(format)
                .setSource(impFile)
                .setHandler(dataHandler)
                .setMessageProvider(mp)
                .setContext(context)
                .setImportCallback(callback)
                .setProgressHandle(progress)
                .setCancelCallback(callback)
                .setLogErrors(true)
                .createImportFetcher();
        Log.d(TAG, "Starting import");
        this.progress.setValue(0); // don't start at max on redo
        this.importing.setValue(true);
        parserThread = imp.execute(0); // 0 is just to pass something
    }

    /**
     * Check that no parser threads are running currently
     * @return false if no thread is running or about to run
     */
    private boolean verifyNoParsersRunning(){
        if(parserThread != null && parserThread.getStatus() != AsyncTask.Status.FINISHED) {
            Log.e(TAG,"Parser thread currently active! Can't start new thread.");
            return true;
        }
        return false;
    }

    @Override
    protected void onCleared() {
        previewList.removeObserver(observerPreviewList);
        cancelPreview.removeObserver(observerCancel);
        cancelImport.removeObserver(observerCancel);
        if(parserThread != null && parserThread.getStatus() == RUNNING){
            parserThread.cancel(true);
        }
    }

    /**
     * Returns import log live data
     * @return
     */
    public LiveData<LogData> getLogHandle() {
        return importLog;
    }

    public boolean isMultiList() {
        return isMultiList;
    }

    /**
     * Returns progress LiveData
     * @return
     */
    public LiveData<Integer> getProgressData() {
        return progress;
    }

    /**
     * Update whether data to be imported is a MultiList
     * @param multiList
     */
    public void setMultiList(boolean multiList) {
        isMultiList = multiList;
    }

    public boolean isRawData() {
        return isRawData;
    }

    /**
     * Update whether data to be import is raw data
     * @param rawData
     */
    public void setRawData(boolean rawData) {
        isRawData = rawData;
    }

    /**
     * Update preview parser & update preview data
     * @param parser
     */
    public void setPreviewParser(@NonNull PreviewParser parser){
        this.previewParser.setValue(parser);
        setPreviewData(parser.getPreviewData());
    }

    /**
     * Returns preview data LiveData
     * @return
     */
    public LiveData<PreviewParser> getPreviewData(){
        return previewParser;
    }

    /**
     * Returns preview parser
     * @return
     */
    @Nullable
    public PreviewParser getPreviewParser() {
        return previewParser.getValue();
    }

    /**
     * New Instance, set default values
     */
    public ImportViewModel() {
        Log.d(TAG,"ViewModel init");
        this.previewList = new MutableLiveData<>();
        this.reparsing = new MutableLiveData<>();
        this.importing = new MutableLiveData<>();
        this.progress = new MutableLiveData<>();
        this.previewParser = new MutableLiveData<>();
        this.importLog = new MutableLiveData<>();
        this.cancelPreview = new MutableLiveData<>();
        this.cancelImport = new MutableLiveData<>();
        previewList.setValue(new ArrayList<>(0));
        reparsing.setValue(false);
        importing.setValue(false);
        progress.setValue(0);
        previewParser.setValue(null);
        cancelPreview.setValue(false);
        cancelImport.setValue(false);
        // new value in preview list -> finished parsing
        observerPreviewList = (val -> reparsing.setValue(false));
        previewList.observeForever(observerPreviewList);
        setRawData(false);
        setMultiList(true);

        observerCancel = (val -> {
            if(val != null && val && parserThread != null && parserThread.getStatus() == RUNNING){
                parserThread.cancel(true);
            }
        });
        cancelPreview.observeForever(observerCancel);
        cancelImport.observeForever(observerCancel);
    }

    /**
     * Get cancelPreview handle
     * @return mutable LiveData handle for cancelling preview parser
     */
    public MutableLiveData<Boolean> getCancelPreviewHandle() { return cancelPreview; }

    /**
     * Get cancelImport handle
     * @return mutable LiveData handle for cancelling import
     */
    public MutableLiveData<Boolean> getCancelImportHandle() { return cancelImport; }

    /**
     * Get preview LiveData
     * @return
     */
    public LiveData<ArrayList<VEntry>> getPreviewList() {
        return previewList;
    }

    /**
     * Set preview data, main thread
     * @param data
     */
    public void setPreviewData(ArrayList<VEntry> data) {
        previewList.setValue(data);
    }

    /**
     * Update reparsing state
     * @param reparsing
     */
    public void updateIsReparsing(boolean reparsing) {
        this.reparsing.setValue(reparsing);
    }

    /**
     * Reset preview data to null
     */
    public void resetPreviewData() {
        this.setMultiList(false);
        this.setRawData(false);
        previewList.setValue(new ArrayList<>(0));
        previewParser.setValue(null);
    }

    /**
     * Returns the LiveData handle to reparsing state
     * @return LiveData handle
     */
    public LiveData<Boolean> getReparsingHandle() {
        return reparsing;
    }

    /**
     * Returns the LiveData handle for importing state
     * @return LiveData handle
     */
    public LiveData<Boolean> getImportingHandle() {
        return importing;
    }

    /**
     * Reset log back to null
     */
    public void resetLog() {
        importLog.setValue(null);
    }
}
