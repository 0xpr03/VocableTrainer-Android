package vocabletrainer.heinecke.aron.vocabletrainer.lib.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.FileEntry;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry.TYPE_DIR;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry.TYPE_INTERNAL;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry.TYPE_SD_CARD;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.BasicFileEntry.TYPE_UP;

/**
 * BaseAdapter for file list views
 */
public class FileRecyclerAdapter extends ListAdapter<BasicFileEntry,FileRecyclerAdapter.FileViewHolder> {

    private static final DiffUtil.ItemCallback<BasicFileEntry> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<BasicFileEntry>() {
                @Override
                public boolean areItemsTheSame(BasicFileEntry oldItem, BasicFileEntry newItem) {
                    return oldItem.getName().equals(newItem.getName());
                }

                @Override
                public boolean areContentsTheSame(BasicFileEntry oldItem, BasicFileEntry newItem) {
                    if(oldItem instanceof FileEntry){
                        if(newItem instanceof FileEntry){
                            File oldFile = ((FileEntry) oldItem).getFile();
                            File newFile = ((FileEntry) newItem).getFile();
                            return oldFile.getAbsolutePath().equals(newFile.getAbsolutePath());
                        }else{
                            return false;
                        }
                    } else if(newItem instanceof FileEntry){
                        return false;
                    }
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.isSelected() == newItem.isSelected()
                            && oldItem.getISize() == newItem.getISize();
                }
            };

    @SuppressWarnings("unused")
    private static final String TAG = "FileRecyclerAdapter";
    private List<BasicFileEntry> data;
    private ItemClickListener itemClickListener;
    private BasicFileEntry lastSelected = null;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * Select single entry, de-selecting previous selection
     * @param position
     */
    public void selectSingleEntry(int position){
        BasicFileEntry newEntry = data.get(position);
        if(lastSelected != null && lastSelected != newEntry){
            int lastIndex = data.indexOf(lastSelected);
            if(lastIndex >= 0){
                lastSelected.setSelected(false);
                notifyItemChanged(lastIndex);
            }
        }
        newEntry.setSelected(true);
        notifyItemChanged(position);
        lastSelected = newEntry;
    }

    /**
     * Position of the specified entry in adapter
     * @param entry
     * @return -1 if there is no such entry, position otherwise
     */
    public int getPositionOfElement(@NonNull BasicFileEntry entry){
        return data.indexOf(entry);
    }

    /**
     * Clear current selection
     */
    public void clearSelection() {
        lastSelected = null;
    }

    /**
     * Submit data with sorting
     * @param newData
     * @param comparator
     */
    public void submitList(List<BasicFileEntry> newData, @Nullable Comparator<BasicFileEntry> comparator) {
        if(comparator != null)
            Collections.sort(newData,comparator);
        this.submitList(newData);
    }

    @Override
    public void submitList(List<BasicFileEntry> newData){
        // don't rely on this being checked by super.submitList
        // essential for selection persistence on viewport change (re-trigger of LiveData)
        if(newData == data){
            return;
        }
        data = newData;
        super.submitList(newData);
    }

    /**
     * Creates a new entry list adapter
     *
     * @param data
     * @param context Context for color resolution
     */
    public FileRecyclerAdapter(@NonNull List<BasicFileEntry> data,@NonNull Context context) {
        super(DIFF_CALLBACK);
        this.data = data;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public BasicFileEntry getItem(int position) {
        return data.get(position);
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_recycler_item,parent,false);
        return new FileViewHolder(view,itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        final BasicFileEntry entry = data.get(position);
        holder.colName.setText(entry.getName());
        holder.colSize.setText(entry.getSize());
        holder.view.setSelected(entry.isSelected());
        holder.image.setVisibility(entry.isUnderline() ? View.VISIBLE : View.INVISIBLE);
        if(entry.getTypeID() < 0){
            int drawable;
            switch(entry.getTypeID()){
                case TYPE_UP:
                    drawable = R.drawable.ic_arrow_upward_24dp;
                    break;
                default:
                case TYPE_DIR:
                    drawable = R.drawable.ic_folder_black_24dp;
                    break;
                case TYPE_INTERNAL:
                case TYPE_SD_CARD:
                    drawable = R.drawable.ic_sd_storage_24dp;
                    break;
            }
            holder.image.setImageResource(drawable);
        }
        if(entry.isUnderline()){
            holder.colName.setPaintFlags(holder.colName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            holder.colName.setPaintFlags(holder.originPaintFlags);
        }

    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Updated sorting
     * @param comparator Comparator to use for sorting
     */
    public void updateSorting(Comparator<BasicFileEntry> comparator){
        Collections.sort(data,comparator);
        this.notifyDataSetChanged();
    }

    /**
     * Set lastSelected to param, used for pre-selection
     * @param preselectedElement
     */
    public void setInitialSelectedElement(FileEntry preselectedElement) {
        lastSelected = preselectedElement;
    }

    /**
     * Item click listener used for recycler
     */
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * File entry view holder
     */
    public class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView colName;
        final TextView colSize;
        final View view;
        ImageView image;
        final int originPaintFlags;
        private ItemClickListener itemClickListener;

        FileViewHolder(View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            itemView.setClickable(true);
            view = itemView;
            colName = itemView.findViewById(R.id.entryFirstText);
            colSize = itemView.findViewById(R.id.entrySecondText);
            image = itemView.findViewById(R.id.fileImage);
            originPaintFlags = colName.getPaintFlags();
            this.itemClickListener = itemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(itemClickListener != null) {
                int pos = getAdapterPosition();
                if(pos >= 0) // prevent invalid clicks
                    itemClickListener.onItemClick(v, pos);
            }
        }
    }
}
