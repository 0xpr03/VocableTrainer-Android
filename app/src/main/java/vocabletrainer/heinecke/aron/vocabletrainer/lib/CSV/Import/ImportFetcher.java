package vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.Import;

import androidx.lifecycle.MutableLiveData;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.CSVCustomFormat;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.MultiMeaningHandler;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Function;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.CSVHeaders.CSV_METADATA_START;

/**
 * ImportFetcher class<br>
 * Does the basic parsing work, passes data to specified ImportHandler<br>
 * Supports callbacks for finish & cancel of execution
 */
public class ImportFetcher extends AsyncTask<Integer, Integer, String> {
    private final static String TAG = "ImportFetcher";
    private final static int MAX_RECORD_SIZE = 4;
    private final static int ADDITION_RECORD_SIZE = 4;
    private final static int MIN_RECORD_SIZE = 2;
    private final static int REC_V1 = 0; // meanings A / name
    private final static int REC_V2 = 1; // meanings B / colA
    private final static int REC_V3 = 2; // tip / colB
    private final static int REC_V4 = 3; // addition (voc only)
    private final Uri source;
    private final CSVCustomFormat cformat;
    private final ImportHandler handler;
    private final MutableLiveData<Integer> progressHandle;
    private final MessageProvider messageProvider;
    private final Function<Void,String> importCallback;
    private final Function<Void,String> cancelCallback;
    private final boolean logEverything;
    private long lastUpdate = 0;
    private final StringBuilder log;
    private InputStream in;

    /**
     * Creates a new importer<br>
     *     It takes a dialog with a progressbar as input to display progress.
     *     Alternative you can use
     *
     * @param cformat      Format to use
     * @param source      Source for parsing
     * @param handler     Data handler
     * @param progressHandle Progress handle<br>
     * @param messageProvider Message provider for logs
     * @param importCallback callback after successful import, given import log as param, return ignored
     * @param logEverything   whether to log all or exceptions only (preview vs import)
     * @param cancelCallback Callback to be executed on cancel action
     */
    ImportFetcher(final CSVCustomFormat cformat, final Uri source, final ImportHandler handler,
                  final MutableLiveData<Integer> progressHandle,
                  final MessageProvider messageProvider, final Function<Void,String> importCallback,
                  final boolean logEverything, @Nullable final Function<Void,String> cancelCallback,
                  final Context context) {
        this.source = source;
        this.cformat = cformat;
        this.handler = handler;
        this.progressHandle = progressHandle;
        this.log = new StringBuilder();
        this.messageProvider = messageProvider;
        this.importCallback = importCallback;
        this.logEverything = logEverything;
        this.cancelCallback = cancelCallback;
        try {
            in = context.getContentResolver().openInputStream(source);
        } catch (FileNotFoundException e) {
            log.append("Exception: ");
            log.append(e.toString());
            Log.e(TAG, "Import exception", e);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        progressHandle.setValue(values[0]);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
        try{
            if(cancelCallback != null)
                cancelCallback.function(s);
        } catch (Exception e){
            Log.e(TAG,"cancelCallback crash",e);
        }
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            if(importCallback != null)
                importCallback.function(s);
        } catch (Exception e){
            Log.e(TAG,"importCallback crash",e);
        }
    }

    /**
     * Format record to string with all infos
     * @param record
     * @return
     */
    private String recordToString(@NonNull CSVRecord record) {
        return record.getRecordNumber()+": ["+ TextUtils.join(",",record.toList())+"]";
    }

    @Override
    protected String doInBackground(Integer... params) {
        try (
//                FileReader reader = new FileReader(source);
//                InputStream in = getContentResolver().openInputStream(source);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                CSVParser parser = new CSVParser(bufferedReader, cformat.getFormat())
        ) {
            handler.start();
            MultiMeaningHandler multiMeaningHandler = new MultiMeaningHandler(cformat);
            boolean tbl_start = false;
            final String empty_v = "";
            int vocableAmount = 0;
            for (CSVRecord record : parser) {
                if(this.isCancelled()) {
                    log.append(messageProvider.I_IMPORT_CANCEL);
                    break;
                }
                if (System.currentTimeMillis() - lastUpdate > 250) { // don't spam the UI thread
                    publishProgress(vocableAmount);
                    lastUpdate = System.currentTimeMillis();
                }

                if (record.size() < MIN_RECORD_SIZE) { // ignore, not enough values
                    if(logEverything) {
                        Log.w(TAG, "ignoring entry, missing values: " + record.toString());
                        log.append(messageProvider.E_NOT_ENOUGH_VALUES);
                        log.append(recordToString(record));
                        log.append('\n');
                    }
                    continue;
                } else if (record.size() > MAX_RECORD_SIZE && logEverything) { // warn, too many values
                    Log.w(TAG, "entry longer then necessary: " + record.toString());
                    log.append(messageProvider.W_TOO_MANY_VALUES);
                    log.append(recordToString(record));
                    log.append('\n');
                }
                String v1 = record.get(REC_V1);
                String v2 = record.get(REC_V2);
                String v3 = record.size() == MIN_RECORD_SIZE ? empty_v : record.get(REC_V3);
                if (tbl_start) {
                    handler.newTable(v1, v2, v3);
                    tbl_start = false;
                } else //noinspection StatementWithEmptyBody
                {
                    if (tbl_start = (v1.equals(CSV_METADATA_START[0]) && v2.equals(CSV_METADATA_START[1]) && v3.equals(CSV_METADATA_START[2]))) {
                        //do nothing
                    } else {
                        vocableAmount++;
                        List<String> mA = multiMeaningHandler.parseMultiMeaning(v1);
                        List<String> mB = multiMeaningHandler.parseMultiMeaning(v2);
                        String addition = record.size() < ADDITION_RECORD_SIZE ? empty_v : record.get(REC_V4);
                        handler.newEntry(mA, mB, v3,addition);
                    }
                }
            }
            //prepend to start
            if(!isCancelled() && logEverything)
                log.insert(0,messageProvider.formatIMPORTED_AMOUNT(vocableAmount));
        } catch (Exception e) {
            log.append("Exception: ");
            log.append(e.toString());
            Log.e(TAG, "Import exception", e);
        } finally {
            if(this.isCancelled())
                handler.cancel();
            else
                handler.finish();
        }
        return log.toString();
    }

    /**
     * Message provider for the import log
     */
    public static class MessageProvider {
        final String E_NOT_ENOUGH_VALUES;
        final String W_TOO_MANY_VALUES;
        final String I_IMPORTED_AMOUNT;
        final String I_IMPORT_CANCEL;

        /**
         * Creates a new MessageProvider
         * @param fragment Fragment for context resource retrieval
         */
        public MessageProvider(final Fragment fragment){
            E_NOT_ENOUGH_VALUES = fragment.getString(R.string.Import_Error_MIN_RECORD_SIZE);
            W_TOO_MANY_VALUES = fragment.getString(R.string.Import_Warn_MAX_RECORD_SIZE);
            I_IMPORTED_AMOUNT = fragment.getString(R.string.Import_Info_Import);
            I_IMPORT_CANCEL = fragment.getString(R.string.Import_Cancel_Log);
        }

        /**
         * Returns formatted IMPORT_AMOUNT with param
         * @param amount
         * @return formatted String
         */
        String formatIMPORTED_AMOUNT(final int amount){
            return I_IMPORTED_AMOUNT.replace("%d",String.valueOf(amount)) + '\n';
        }
    }
}
