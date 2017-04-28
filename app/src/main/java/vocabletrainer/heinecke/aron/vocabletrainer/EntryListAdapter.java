package vocabletrainer.heinecke.aron.vocabletrainer;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aron on 26.04.17.
 */


public class EntryListAdapter extends BaseAdapter {

    List<Entry> dataItems = null;
    List<Entry> deleted;
    Activity activity;
    TextView colA;
    TextView colB;
    TextView colTipp;

    boolean setIsChanging = false;

    private Entry header;


    public EntryListAdapter(Activity activity, List<Entry> objects) {
//        super(context, R.layout.list_items, objects);
        super();
        this.activity = activity;
        this.dataItems = objects;
        header = new Entry("A","B","Tipp",-2,-2L);
        dataItems.add(0,header);
        deleted = new ArrayList<>();
    }

    @Override
    public void notifyDataSetChanged(){
        setIsChanging = true;
        super.notifyDataSetChanged();
        setIsChanging = false;
    }

    /**
     * Set table data (Column Names)
     * @param tbl
     */
    public void setTableData(Table tbl){
        header.setAWord(tbl.getNameA());
        header.setBWord(tbl.getNameB());
        this.notifyDataSetChanged();
        Log.d("EntryListAdapter","setTableData");
    }


    @Override
    public int getCount() {
        return dataItems.size();
    }

    @Override
    public Object getItem(int position) {
        return dataItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //TODO: different layout for head column
        LayoutInflater inflater = activity.getLayoutInflater();
//        if (convertView == null || setIsChanging) {
            convertView = inflater.inflate(R.layout.table_list_view, null);

            colA = (TextView) convertView.findViewById(R.id.FirstText);
            colB = (TextView) convertView.findViewById(R.id.SecondText);
            colTipp = (TextView) convertView.findViewById(R.id.ThirdText);
//        }
        Entry item = dataItems.get(position);

        colA.setText(item.getAWord());
        colB.setText(String.valueOf(item.getBWord()));
        colTipp.setText(String.valueOf(item.getTip()));

        return convertView;
    }

    /**
     * Returns a list of deleted entries
     * @return
     */
    public List<Entry> getDeleted(){
        return deleted;
    }

    /**
     * Set entry as deleted
     * @param entry
     */
    public void setDeleted(Entry entry) {
        entry.setDelete(true);
        dataItems.remove(entry);
        deleted.add(entry);
        notifyDataSetChanged();
    }

    /**
     * Add a new Entry to the view<br>
     *     Does not update the view
     * @param entry
     */
    public void addEntryUnrendered(Entry entry){
        dataItems.add(entry);
    }
}
