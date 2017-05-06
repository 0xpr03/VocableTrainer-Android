package vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib;

/**
 * Created by aron on 29.04.17.
 */

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

/**
 * ArrayAdapter for table listviews
 */
public class TableListAdapter extends ArrayAdapter<Table> {

    ArrayList<Table> dataItem;
    Context context;
    int resLayout;
    TextView colName;
    TextView colA;
    TextView colB;
    private final boolean displayCheckbox;

    public TableListAdapter(Context context, int textViewResourceId, ArrayList<Table> table, final boolean displayCheckbox) {
        super(context, textViewResourceId, table);
        this.dataItem = table;
        resLayout = textViewResourceId;
        this.context = context;
        this.displayCheckbox = displayCheckbox;
    }

    /**
     * Add multiple tables to the list<br>
     * updates the view
     *
     * @param entries
     */
    public void addAllUpdated(List<Table> entries) {
        dataItem.addAll(entries);
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(resLayout, parent,false);

        colName = (TextView) convertView.findViewById(R.id.tableFirstText);
        colA = (TextView) convertView.findViewById(R.id.tableSecondText);
        colB = (TextView) convertView.findViewById(R.id.tableThirdText);
        if(!displayCheckbox)
            ((CheckBox) convertView.findViewById(R.id.tblCheckBox)).setVisibility(View.GONE);

        Table item = dataItem.get(position);

        Log.d("TableListAdapter","setting text: "+item.getName()+" on "+colName.toString());
        colName.setText(item.getName());
        colA.setText(item.getNameA());
        colB.setText(String.valueOf(item.getNameB()));

        return convertView;
    }
}
