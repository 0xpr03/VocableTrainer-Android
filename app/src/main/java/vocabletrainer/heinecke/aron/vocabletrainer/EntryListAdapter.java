package vocabletrainer.heinecke.aron.vocabletrainer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by aron on 26.04.17.
 */
/*

public class EntryListAdapter extends ArrayAdapter<Entry> {

    List<Entry> dataItems = null;
    Context context;


    public EntryListAdapter(Context context, List<Entry> objects) {
//        super(context, R.layout.list_items, objects);
        this.context = context;
        this.dataItems = objects;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_items, parent, false);

        TextView colA = (TextView) convertView.findViewById(R.id.name);
        TextView colB = (TextView) convertView.findViewById(R.id.price);
        TextView colTipp = (TextView) convertView.findViewById(R.id.id);
        colA.setText(dataItems.get(position).getAWord());
        colB.setText(String.valueOf(dataItems.get(position).getBWord()));
        colTipp.setText(String.valueOf(dataItems.get(position).getTip()));
        return convertView;
    }
}*/
