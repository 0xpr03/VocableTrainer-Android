package vocabletrainer.heinecke.aron.vocabletrainer.activity.lib;

import android.app.Activity;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * BaseAdapter for entry list views
 */
public class EntryListAdapter extends BaseAdapter {

    private static final int ANDROID_WORKAROUND_STYLE = -1;
    private static final String TAG = "EntryListAdapter";
    private List<VEntry> dataItems = null;
    private List<VEntry> deleted;
    private LayoutInflater inflater;
    private VEntry header;

    /**
     * Creates a new entry list adapter
     *
     * @param items
     */
    public EntryListAdapter(Activity activity,List<VEntry> items) {
        super();
        this.dataItems = items;
        header = new VEntry(activity.getString(R.string.Editor_Hint_Column_A),
                activity.getString(R.string.Editor_Hint_Column_B),
                activity.getString(R.string.Editor_Hint_Tip),
                ID_RESERVED_SKIP);
        dataItems.add(0, header);
        deleted = new ArrayList<>();
        inflater = activity.getLayoutInflater();
    }

    /**
     * Set table data (Column Names)
     *
     * @param tbl
     */
    public void setTableData(VList tbl) {
        header.getAMeanings().set(0,tbl.getNameA());
        header.getBMeanings().set(0,tbl.getNameB());
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
        final VEntry item = dataItems.get(position);

        if (convertView == null) {
            holder = new ViewHolder();


            convertView = inflater.inflate(R.layout.entry_list_view, null);

            holder.colA = (TextView) convertView.findViewById(R.id.entryFirstText);
            holder.colB = (TextView) convertView.findViewById(R.id.entrySecondText);
            holder.colTipp = (TextView) convertView.findViewById(R.id.entryThirdText);
            if(holder.colA.getTypeface() == null){
                holder.originTypeface = ANDROID_WORKAROUND_STYLE;
            }else {
                holder.originTypeface = holder.colA.getTypeface().getStyle();
            }

            convertView.setTag(holder);
            convertView.setTag(R.id.entryFirstText, holder.colA);
            convertView.setTag(R.id.entrySecondText, holder.colB);
            convertView.setTag(R.id.entryThirdText, holder.colTipp);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        boolean bold = item.getId() == ID_RESERVED_SKIP;
        //TODO: remove on drop of API 19 support
        if(holder.originTypeface == ANDROID_WORKAROUND_STYLE){
            holder.colA.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);
            holder.colB.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);
            holder.colTipp.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);
        }else {
            holder.colA.setTypeface(null, bold ? Typeface.BOLD : holder.originTypeface);
            holder.colB.setTypeface(null, bold ? Typeface.BOLD : holder.originTypeface);
            holder.colTipp.setTypeface(null, bold ? Typeface.BOLD : holder.originTypeface);
        }

        holder.colA.setText(item.getAString());
        holder.colB.setText(item.getBString());
        holder.colTipp.setText(item.getTip());

        return convertView;
    }

    /**
     * Update sorting
     * @param comp Comparator to use for sorting
     */
    public void updateSorting(Comparator<VEntry> comp){
        Collections.sort(dataItems,comp);
        this.notifyDataSetChanged();
    }

    /**
     * Set entry as deleted
     *
     * @param entry
     */
    public void setDeleted(VEntry entry) {
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
     * Add a new VEntry to the view<br>
     * Does not update the view
     *
     * @param entry
     */
    public void addEntryUnrendered(VEntry entry) {
        dataItems.add(entry);
    }

    /**
     * Add an VEntry to the view at selected position.<br>
     * Does update the view rendering
     *
     * @param entry    new VEntry
     * @param position Position at which it should be inserted
     */
    public void addEntryRendered(VEntry entry, int position) {
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
    public List<VEntry> getAllEntries() {
        ArrayList<VEntry> entries = new ArrayList<>(this.dataItems);
        entries.addAll(deleted);
        return entries;
    }

    /**
     * View Holder, storing data for re-use
     */
    private class ViewHolder {
        protected TextView colA;
        protected TextView colB;
        protected TextView colTipp;
        protected int originTypeface;
    }
}
