package vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

/**
 * ViewModel for list picker, selector
 */
public class ListPickerViewModel extends ViewModel {
    private final static String TAG = "ListPickerViewModel";
    private MutableLiveData<List<VList>> lists;
    private MutableLiveData<Boolean> loading;
    private MutableLiveData<Boolean> cancelLoading;
    private boolean dataInvalidated;
    private Thread loaderTask;
    private boolean selectAll;

    /**
     * Get select all
     * Used to remember select all swap state
     * @return
     */
    public boolean isSelectAll() {
        return selectAll;
    }

    /**
     * Set select all
     * Used to remember select all swap state
     * @param selectAll
     */
    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }

    /**
     * Returns list data handle
     * @return
     */
    public MutableLiveData<List<VList>> getListsHandle() {
        return lists;
    }

    /**
     * Returns loading indicator handle
     * @return
     */
    public MutableLiveData<Boolean> getLoadingHandle() {
        return loading;
    }

    public ListPickerViewModel() {
        lists = new MutableLiveData<>();
        loading = new MutableLiveData<>();
        dataInvalidated = true;
        selectAll = false;
    }

    /**
     * Returns checked lists
     * @return
     */
    public ArrayList<VList> getSelectedLists(){
        ArrayList<VList> result;
        if(lists.getValue() != null){
            result = new ArrayList<>(lists.getValue().size() / 2);
            for(VList entry : lists.getValue()){
                if(entry.isSelected())
                    result.add(entry);
            }
        }else{
            result = new ArrayList<>(0);
        }
        return result;
    }

    /**
     * Set data as invalidated
     */
    public void setDataInvalidated() {
        Log.d(TAG,"invalidating data");
        this.dataInvalidated = true;
    }

    /**
     * Returns whether data is marked as invalidated (updated in DB)
     * @return
     */
    public boolean isDataInvalidated() {
        return dataInvalidated;
    }

    /**
     * Load lists
     * @param context for DB
     */
    public void loadLists(Context context){
        if(loaderTask != null && loaderTask.isAlive()){
            Log.w(TAG,"prevented parallel loader");
            return;
        }
        loading.setValue(true);
        loaderTask = new Thread(() -> {
            Database db = new Database(context);
            lists.postValue(db.getTables(cancelLoading));
            dataInvalidated = false;
            loading.postValue(false);
        });
        loaderTask.start();
    }
}
