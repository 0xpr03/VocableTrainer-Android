package vocabletrainer.heinecke.aron.vocabletrainer.lib.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * ArrayAdapter for table listviews
 */
public class TableListAdapter extends ArrayAdapter<VList> {

    /**
     * items >= starting item are real items and no header etc
     */
    private final static int STARTING_ITEM = 1;
    private final boolean displayCheckbox;
    private ArrayList<VList> dataItem;
    private Context context;
    private int resLayout;
    private VList header;

    /**
     * New lists list adapter
     *
     * @param context context to use, should be "this" for activities, avoiding style problems
     * @param lists
     * @param displayCheckbox set to true to show checkbox for multi select
     */
    public TableListAdapter(Context context, @NonNull ArrayList<VList> lists, final boolean displayCheckbox) {
        super(context, R.layout.list_recycler_item, lists);
        this.dataItem = lists;
        header = VList.Companion.withId(ID_RESERVED_SKIP,context.getString(R.string.Editor_Hint_Column_A), context.getString(R.string.Editor_Hint_Column_B), context.getString(R.string.Editor_Hint_List_Name));
        // don't re-add header on restore
        if(dataItem.size() == 0 || !dataItem.get(0).equals(header))
            dataItem.add(STARTING_ITEM - 1, header);
        resLayout = R.layout.list_recycler_item;
        this.context = context;
        this.displayCheckbox = displayCheckbox;
    }

    /**
     * Set list as new list<br>
     * updates the view
     *
     * @param entries
     */
    public void setAllUpdated(List<VList> entries, Comparator<VList> comparator) {
        dataItem.clear();
        dataItem.add(header);
        dataItem.addAll(entries);
        Collections.sort(dataItem,comparator);
        this.notifyDataSetChanged();
    }

    /**
     * Updates sort order
     * @param comparator Comparator to use
     */
    public void updateSorting(Comparator<VList> comparator){
        Collections.sort(dataItem,comparator);
        this.notifyDataSetChanged();
    }

    /**
     * Remove element from list<br>
     * updates the view
     *
     * @param tbl
     */
    public void removeEntryUpdated(VList tbl) {
        dataItem.remove(tbl);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null)
            convertView = inflater.inflate(resLayout, parent, false);

        TextView colName = convertView.findViewById(R.id.tListEntryName);
        TextView colA = convertView.findViewById(R.id.tListEntryColA);
        TextView colB = convertView.findViewById(R.id.tListEntryColB);

        if (!displayCheckbox) {
            convertView.findViewById(R.id.chkListEntrySelect).setVisibility(View.GONE);
        }


        VList item = dataItem.get(position);
        int typeface = Typeface.NORMAL;
        boolean reserved = item.getId() == ID_RESERVED_SKIP;
        if (reserved) {
            typeface = Typeface.BOLD;
        }

        if (displayCheckbox)
            convertView.findViewById(R.id.chkListEntrySelect).setVisibility(reserved ? View.INVISIBLE : View.VISIBLE);

        colName.setTypeface(null, typeface);
        colA.setTypeface(null, typeface);
        colB.setTypeface(null, typeface);

        colName.setText(item.getName());
        colA.setText(item.getNameA());
        colB.setText(String.valueOf(item.getNameB()));

        return convertView;
    }
}
