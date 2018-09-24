package vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.fragment.ExportFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Exporter;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Function;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

/**
 * Export ViewModel containinig relevant data & handles background tasking
 */
public class ExportViewModel extends ViewModel {
    private final static String TAG = "ExportViewModel";
    private int exportListAmount; // TODO: temporary until selector has own viewmodel
    private MutableLiveData<Integer> progressExport;
    private MutableLiveData<Boolean> exporting;
    private AsyncTask task;

    public ExportViewModel() {
        exportListAmount = 0;
        progressExport = new MutableLiveData<>();
        exporting = new MutableLiveData<>();
        exporting = new MutableLiveData<>();
        progressExport.setValue(0);
    }

    /**
     * Returns exporting status handle
     * @return LiveData handle
     */
    public LiveData<Boolean> getExportingHandles(){
        return exporting;
    }

    /**
     * Returns progress export handle
     * @return LiveData handle
     */
    public LiveData<Integer> getProgressExportHandle() {
        return progressExport;
    }

    /**
     * Returns size of export
     * @return amount VLists
     */
    public int getExportSize(){
        return exportListAmount;
    }

    /**
     * Run Import task
     * @param context
     */
    public void runImport(Context context, ExportFragment.ExportStorage exportStorage){
        if(task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            Log.w(TAG,"preventing second running export");
            return;
        }
        //post export
        Function<Void,String> callback = param -> {
            exporting.setValue(false);
            exportListAmount = 0;
            return null;
        };

        Exporter exporter = new Exporter(exportStorage,progressExport,callback,context);
        this.exportListAmount = exportStorage.lists.size();
        this.progressExport.setValue(0); // don't start on max on redo
        this.exporting.setValue(true);
        task = exporter.execute(0); // 0 is just to pass something
    }

}
