package vocabletrainer.heinecke.aron.vocabletrainer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by aron on 26.04.17.
 */


public class EntryListAdapter extends BaseAdapter {

    List<Entry> dataItems = null;
    Activity activity;
    TextView colA;
    TextView colB;
    TextView colTipp;


    public EntryListAdapter(Activity activity, List<Entry> objects) {
//        super(context, R.layout.list_items, objects);
        super();
        this.activity = activity;
        this.dataItems = objects;

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
        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.table_list_view, null);

            colA = (TextView) convertView.findViewById(R.id.FirstText);
            colB = (TextView) convertView.findViewById(R.id.SecondText);
            colTipp = (TextView) convertView.findViewById(R.id.ThirdText);
        }

        colA.setText(dataItems.get(position).getAWord());
        colB.setText(String.valueOf(dataItems.get(position).getBWord()));
        colTipp.setText(String.valueOf(dataItems.get(position).getTip()));
        return convertView;
    }
}
