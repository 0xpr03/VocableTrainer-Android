package vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
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
 * BaseAdapter for entry list views
 */
public class EntryListAdapter extends BaseAdapter {

    private static final String TAG = "EntryListAdapter";
    private List<Entry> dataItems = null;
    private List<Entry> deleted;
    private Activity activity;
    private LayoutInflater inflater;
    private Entry header;

    /**
     * Creates a new entry list adapter
     *
     * @param activity
     * @param items
     */
    public EntryListAdapter(Activity activity, List<Entry> items, Context context) {
        super();
        this.activity = activity;
        this.dataItems = items;
        header = new Entry(context.getString(R.string.Editor_Default_Column_A),
                context.getString(R.string.Editor_Default_Column_B),
                context.getString(R.string.Editor_Default_Tip),
                ID_RESERVED_SKIP, new Table(ID_RESERVED_SKIP), -2L);
        dataItems.add(0, header);
        deleted = new ArrayList<>();
        inflater = activity.getLayoutInflater();
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
        return dataItems.get(position);
        // -1 required as onItemClicked counts from 1 but the list starts a 0
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        final Entry item = dataItems.get(position);

        if (convertView == null) {
            holder = new ViewHolder();


            convertView = inflater.inflate(R.layout.entry_list_view, null);

            holder.colA = (TextView) convertView.findViewById(R.id.entryFirstText);
            holder.colB = (TextView) convertView.findViewById(R.id.entrySecondText);
            holder.colTipp = (TextView) convertView.findViewById(R.id.entryThirdText);

            convertView.setTag(holder);
            convertView.setTag(R.id.entryFirstText, holder.colA);
            convertView.setTag(R.id.entrySecondText, holder.colB);
            convertView.setTag(R.id.entryThirdText, holder.colTipp);

            if (item.getId() == ID_RESERVED_SKIP) {
                holder.colA.setTypeface(null, Typeface.BOLD);
                holder.colB.setTypeface(null, Typeface.BOLD);
                holder.colTipp.setTypeface(null, Typeface.BOLD);
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.colA.setText(item.getAWord());
        holder.colB.setText(item.getBWord());
        holder.colTipp.setText(item.getTip());

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
     * To be called after all changes are written to the DB
     */
    public void clearDeleted() {
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

    /**
     * Returns all entries, existing and deleted
     *
     * @return
     */
    public List<Entry> getAllEntries() {
        ArrayList<Entry> entries = new ArrayList<>(this.dataItems);
        entries.addAll(deleted);
        return entries;
    }

    private class ViewHolder {
        protected TextView colA;
        protected TextView colB;
        protected TextView colTipp;
    }
}
