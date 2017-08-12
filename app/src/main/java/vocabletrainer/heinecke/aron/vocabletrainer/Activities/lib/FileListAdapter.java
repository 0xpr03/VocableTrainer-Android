package vocabletrainer.heinecke.aron.vocabletrainer.Activities.lib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Table;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.ID_RESERVED_SKIP;

/**
 * BaseAdapter for file list views
 */
public class FileListAdapter extends BaseAdapter {

    private static final String TAG = "FileListAdapter";
    private List<BasicFileEntry> dataItems = null;
    private Activity activity;
    private LayoutInflater inflater;

    private class ViewHolder {
        protected TextView colA;
        protected TextView colB;
        protected int originPaintFlags;
    }

    private Entry header;

    /**
     * Creates a new entry list adapter
     *
     * @param activity
     * @param items
     */
    public FileListAdapter(Activity activity, List<BasicFileEntry> items, Context context) {
        super();
        this.activity = activity;
        this.dataItems = items;
        inflater = activity.getLayoutInflater();
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
    public View getView(final int position,View convertView,final ViewGroup parent) {
        final ViewHolder holder;
        final BasicFileEntry item = dataItems.get(position);

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.file_list_view, parent,false);

            holder.colA = (TextView) convertView.findViewById(R.id.entryFirstText);
            holder.colB = (TextView) convertView.findViewById(R.id.entrySecondText);
            holder.originPaintFlags = holder.colA.getPaintFlags();

            convertView.setTag(holder);
            convertView.setTag(R.id.entryFirstText, holder.colA);
            convertView.setTag(R.id.entrySecondText, holder.colB);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.colA.setText(item.getName());
        holder.colB.setText(item.getSize());
        if(item.isUnderline()){
            holder.colA.setPaintFlags(holder.colA.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else{
            holder.colA.setPaintFlags(holder.originPaintFlags);
        }

        //TODO: fix selection rendering on scrolling

        return convertView;
    }
}
