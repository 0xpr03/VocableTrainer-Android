package vocabletrainer.heinecke.aron.vocabletrainer.lib.Adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
    @SuppressWarnings("unused")
    private static final String TAG = "EntryListAdapter";
    private List<VEntry> dataItems;
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
        header = VEntry.Companion.spacer(activity.getString(R.string.Editor_Hint_Column_A),
                activity.getString(R.string.Editor_Hint_Column_B),
                activity.getString(R.string.Editor_Hint_Tip),
                ID_RESERVED_SKIP);
        // don't re-add header double on instance restore
        if(dataItems.size() == 0 || !dataItems.get(0).equals(header)) {
            dataItems.add(0, header);
        }

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

            convertView = inflater.inflate(R.layout.entry_list_view, parent,false);

            holder.colA = convertView.findViewById(R.id.entryFirstText);
            holder.colB = convertView.findViewById(R.id.entrySecondText);
            holder.colTipp = convertView.findViewById(R.id.entryThirdText);
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

        int typeFace;
        if(bold) {
            typeFace = Typeface.BOLD;
            //TODO: remove on drop of API 19 support
        } else if(holder.originTypeface == ANDROID_WORKAROUND_STYLE){
            typeFace = Typeface.NORMAL;
        } else {
            typeFace = holder.originTypeface;
        }

        holder.colA.setTypeface(null, typeFace);
        holder.colB.setTypeface(null, typeFace);
        holder.colTipp.setTypeface(null, typeFace);

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
    public void remove(VEntry entry) {
        dataItems.remove(entry);
        notifyDataSetChanged();
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
        this.notifyDataSetChanged();
    }

    /**
     * View Holder, storing data for re-use
     */
    private class ViewHolder {
        TextView colA;
        TextView colB;
        TextView colTipp;
        int originTypeface;
    }
}
