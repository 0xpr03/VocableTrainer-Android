package vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * Created by aron on 26.04.17.
 */

/**
 * BaseAdapter for entry list views
 */
public class EntryListAdapter extends BaseAdapter {

    List<Entry> dataItems = null;
    List<Entry> deleted;
    Activity activity;
    TextView colA;
    TextView colB;
    TextView colTipp;

    private Entry header;

    public EntryListAdapter(Activity activity, List<Entry> objects) {
        super();
        this.activity = activity;
        this.dataItems = objects;
        header = new Entry("A", "B", "Tipp", ID_RESERVED_SKIP, new Table(ID_RESERVED_SKIP), -2L);
        dataItems.add(0, header);
        deleted = new ArrayList<>();
    }

    /**
     * Set table data (Column Names)
     *
     * @param tbl
     */
    public void setTableData(Table tbl) {
        header.setAWord(tbl.getNameA());
        header.setBWord(tbl.getNameB());
        this.notifyDataSetChanged();
        Log.d("EntryListAdapter", "setTableData");
    }


    @Override
    public int getCount() {
        return dataItems.size();
    }

    @Override
    public Object getItem(int position) {
        return dataItems.get(position - 1);
        // -1 required as onItemClicked counts from 1 but the list starts a 0
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //TODO: different layout for head column
        LayoutInflater inflater = activity.getLayoutInflater();
        convertView = inflater.inflate(R.layout.table_list_view, null);

        colA = (TextView) convertView.findViewById(R.id.FirstText);
        colB = (TextView) convertView.findViewById(R.id.SecondText);
        colTipp = (TextView) convertView.findViewById(R.id.ThirdText);
        Entry item = dataItems.get(position);

        colA.setText(item.getAWord());
        colB.setText(String.valueOf(item.getBWord()));
        colTipp.setText(String.valueOf(item.getTip()));

        return convertView;
    }

    /**
     * Returns a list of deleted entries
     *
     * @return
     */
    public List<Entry> getDeleted() {
        return deleted;
    }

    /**
     * Set entry as deleted
     *
     * @param entry
     */
    public void setDeleted(Entry entry) {
        entry.setDelete(true);
        dataItems.remove(entry);
        deleted.add(entry);
        notifyDataSetChanged();
    }

    /**
     * Clear the deleted list<br>
     *     To be called after all changes are written to the DB
     */
    public void clearDeleted(){
        this.deleted.clear();
    }

    /**
     * Add a new Entry to the view<br>
     * Does not update the view
     *
     * @param entry
     */
    public void addEntryUnrendered(Entry entry) {
        dataItems.add(entry);
    }

    /**
     * Add an Entry to the view at selected position.<br>
     * Does update the view rendering
     *
     * @param entry    new Entry
     * @param position Position at which it should be inserted
     */
    public void addEntryRendered(Entry entry, int position) {
        dataItems.add(position, entry);
        if (deleted.contains(entry)) {
            deleted.remove(entry);
        }
        this.notifyDataSetChanged();
    }
}
