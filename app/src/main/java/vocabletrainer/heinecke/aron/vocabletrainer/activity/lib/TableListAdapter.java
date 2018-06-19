package vocabletrainer.heinecke.aron.vocabletrainer.activity.lib;

import android.content.Context;
import android.graphics.Typeface;
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
    public final static int STARTING_ITEM = 1;
    private final boolean displayCheckbox;
    ArrayList<VList> dataItem;
    Context context;
    int resLayout;
    TextView colName;
    TextView colA;
    TextView colB;
    VList header;

    /**
     * New lists list adapter
     *
     * @param context context to use, should be "this" for activities, avoiding style problems
     * @param textViewResourceId row resource XML
     * @param lists
     * @param displayCheckbox set to true to show checkbox for multi select
     */
    public TableListAdapter(Context context, int textViewResourceId, ArrayList<VList> lists, final boolean displayCheckbox) {
        super(context, textViewResourceId, lists);
        this.dataItem = lists;
        header = new VList(ID_RESERVED_SKIP,context.getString(R.string.Editor_Hint_Column_A), context.getString(R.string.Editor_Hint_Column_B), context.getString(R.string.Editor_Hint_List_Name),null);
        dataItem.add(STARTING_ITEM - 1, header);
        resLayout = textViewResourceId;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(resLayout, parent, false);

        colName = (TextView) convertView.findViewById(R.id.tableFirstText);
        colA = (TextView) convertView.findViewById(R.id.tableSecondText);
        colB = (TextView) convertView.findViewById(R.id.tableThirdText);

        if (!displayCheckbox) {
            convertView.findViewById(R.id.tblCheckBox).setVisibility(View.GONE);
        }


        VList item = dataItem.get(position);
        if (item.getId() == ID_RESERVED_SKIP) {
            if (displayCheckbox)
                convertView.findViewById(R.id.tblCheckBox).setVisibility(View.INVISIBLE);

            colName.setTypeface(null, Typeface.BOLD);
            colA.setTypeface(null, Typeface.BOLD);
            colB.setTypeface(null, Typeface.BOLD);
        }

        colName.setText(item.getName());
        colA.setText(item.getNameA());
        colB.setText(String.valueOf(item.getNameB()));

        return convertView;
    }
}
