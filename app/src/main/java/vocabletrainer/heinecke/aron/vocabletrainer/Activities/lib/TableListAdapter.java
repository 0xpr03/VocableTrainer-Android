package vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib;

/**
 * Created by aron on 29.04.17.
 */

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

/**
 * BaseAdapter for table listviews
 */
public class TableListAdapter extends BaseAdapter {

    List<Table> dataItem;
    Activity activity;
    TextView colName;
    TextView colA;
    TextView colB;

    public TableListAdapter(Activity activity,List<Table> table){
        this.dataItem = table;
        this.activity = activity;
    }

    /**
     * Add multiple tables to the list<br>
     *     updates the view
     * @param entries
     */
    public void addAllUpdated(List<Table> entries){
        dataItem.addAll(entries);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataItem.size();
    }

    @Override
    public Object getItem(int position) {
        return dataItem.get(+1);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();

        convertView = inflater.inflate(R.layout.table_list_view, null);

        colName = (TextView) convertView.findViewById(R.id.FirstText);
        colA = (TextView) convertView.findViewById(R.id.SecondText);
        colB= (TextView) convertView.findViewById(R.id.ThirdText);

        Table item = dataItem.get(position);

        colName.setText(item.getName());
        colA.setText(item.getNameA());
        colB.setText(String.valueOf(item.getNameB()));

        return convertView;
    }
}
