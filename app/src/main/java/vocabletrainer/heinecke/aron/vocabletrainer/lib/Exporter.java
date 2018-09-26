package vocabletrainer.heinecke.aron.vocabletrainer.lib;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.fragment.ExportFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVHeaders.CSV_METADATA_COMMENT;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVHeaders.CSV_METADATA_START;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * Export task running in background & reporting progress
 */
public class Exporter extends AsyncTask<Integer, Integer, String> {
    private final static String TAG = "ExportTask";
    private final ExportFragment.ExportStorage es;
    private final Database db;
    private MutableLiveData<Integer> progressHandle;
    private final Function<Void,String> exportCallback;
    private final Function<Void,String> cancelCallback;

    /**
     * Creates a new ExportOperation
     *
     * @param es
     */
    public Exporter(ExportFragment.ExportStorage es, MutableLiveData<Integer> progressHandle,
            Function<Void,String> exportCallback,Function<Void,String> cancelCallback, Context context) {
        this.es = es;
        this.progressHandle = progressHandle;
        this.exportCallback = exportCallback;
        this.cancelCallback = cancelCallback;
        db = new Database(context);
    }

    @Override
    protected String doInBackground(Integer... params) {
        Log.d(TAG, "Starting background task");
        try (FileWriter fw = new FileWriter(es.file);
             BufferedWriter writer = new BufferedWriter(fw);
             CSVPrinter printer = new CSVPrinter(writer, es.cFormat.getFormat())
        ) {
            MultiMeaningHandler handler = new MultiMeaningHandler(es.cFormat);
            int i = 0;
            for (VList list : es.lists) {
                if(isCancelled()){
                    break;
                }
                if (list.getId() == ID_RESERVED_SKIP) {
                    continue;
                }
                Log.d(TAG, "exporting list " + list.toString());
                if (es.exportTableInfo) {
                    printer.printRecord((Object[]) CSV_METADATA_START);
                    printer.printComment(CSV_METADATA_COMMENT);
                    printer.print(list.getName());
                    printer.print(list.getNameA());
                    printer.print(list.getNameB());
                    printer.println();
                }
                List<VEntry> vocables = db.getVocablesOfTable(list);

                for (VEntry ent : vocables) {
                    if(isCancelled()) {
                        break;
                    }
                    List<String> mA = ent.getAMeanings();
                    List<String> mB = ent.getBMeanings();
                    printer.print(handler.formatMultiMeaning(mA));
                    printer.print(handler.formatMultiMeaning(mB));
                    printer.print(ent.getTip());
                    printer.print(ent.getAddition());
                    printer.println();
                }
                i++;
                publishProgress(i);
            }
        } catch (Exception e) {
            Log.wtf(TAG, e);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        progressHandle.setValue(values[0]);
    }

    @Override
    protected void onPostExecute(String result) {
         if(isCancelled())
             cancelCallback.function(result);
         else
             exportCallback.function(result);
    }
}
