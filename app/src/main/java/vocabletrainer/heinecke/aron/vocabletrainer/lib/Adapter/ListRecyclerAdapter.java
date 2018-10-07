package vocabletrainer.heinecke.aron.vocabletrainer.lib.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

/**
 * Recycler adapter for VList recycler with optional multi selection
 * @author Aron Heinecke
 */
public class ListRecyclerAdapter extends ListAdapter<VList,ListRecyclerAdapter.VListViewHolder> {
    @SuppressWarnings("unused")
    private static final String TAG = "ListRecyclerAdapter";

    private static final DiffUtil.ItemCallback<VList> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<VList>() {
                @Override
                public boolean areItemsTheSame(VList oldItem, VList newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(VList oldItem, VList newItem) {
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.getNameA().equals(newItem.getNameA())
                            && oldItem.getNameB().equals(newItem.getNameB());
                }
            };

    /**
     * Item click listener used for recycler
     */
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * Item long click listener used for recycler
     */
    public interface ItemLongClickListener {
        void onItemLongClick(View view, int postion);
    }

    private List<VList> data;
    private final boolean multiselect;
    private DateFormat dateFormat;
    private ItemLongClickListener itemLongClickListener;
    private ItemClickListener itemClickListener;

    public void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * New VList recycler adapter
     * @param data initial data
     * @param multiSelect enable checkbox mode
     * @param context context for date formatting etc
     */
    public ListRecyclerAdapter(List<VList> data, final boolean multiSelect, Context context){
        super(DIFF_CALLBACK);
        this.data = data;
        this.multiselect = multiSelect;
        this.dateFormat = android.text.format.DateFormat.getDateFormat(context);
    }

    /**
     * Set all elements as select
     * @param select
     */
    public void selectAll(final boolean select){
        for(VList entry : data){
            entry.setSelected(select);
        }
        notifyDataSetChanged();
    }

    /**
     * Submit data with sorting
     * @param newData
     * @param comparator
     */
    public void submitList(List<VList> newData, @Nullable Comparator<VList> comparator) {
        if(comparator != null)
            Collections.sort(newData,comparator);
        this.submitList(newData);
    }

    @Override
    public void submitList(List<VList> newData) {
        // don't rely on this being checked by super.submitList
        // essential for selection persistence on viewport change (re-trigger of LiveData)
        if(newData == data){
            return;
        }
        this.data = newData;
        super.submitList(newData);
    }

    /**
     * Remove entry<br>
     *     Does not actually delete the item from the Database
     * @param entry
     */
    public void removeEntry(VList entry){
        int pos = data.indexOf(entry);
        data.remove(entry);
        this.notifyItemRemoved(pos);
    }

    /**
     * Restore entry in list
     * @param entry
     * @param position
     */
    public void restoreEntry(VList entry, int position){
        data.add(position,entry);
        notifyItemInserted(position);
    }

    @NonNull
    @Override
    public VListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_recycler_item,parent,false);
        return new VListViewHolder(view,itemLongClickListener,itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull VListViewHolder holder, int position) {
        final VList entry = data.get(position);
        holder.checkBox.setVisibility(multiselect ? View.VISIBLE : View.GONE);
        holder.checkBox.setChecked(entry.isSelected());
        holder.colB.setText(entry.getNameB());
        holder.colA.setText(entry.getNameA());
        holder.name.setText(entry.getName());
        holder.created.setText(dateFormat.format(entry.getCreated()));
        if(multiselect) {
            holder.viewForeground.setOnClickListener(v -> {
                entry.setSelected(!entry.isSelected());
                this.notifyItemChanged(position);
            });
        }
    }

    /**
     * Update sorting, forcing a redraw
     * @param comparator
     */
    public void updateSorting(@NonNull Comparator<VList> comparator){
        Collections.sort(data,comparator);
        // required, submitList wouldn't trigger when same list is used
        this.notifyDataSetChanged();
    }

    /**
     * Get item at position
     * @param pos
     * @return
     */
    public VList getItemAt(int pos) {
        return data.get(pos);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    /**
     * VLIst view holder with click & long click capabilities
     */
    public class VListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        private CheckBox checkBox;
        private TextView colA,colB,name,created;
        private ItemClickListener itemClickListener;
        private ItemLongClickListener itemLongClickListener;
        final RelativeLayout viewBackground;
        final ConstraintLayout viewForeground;

        private VListViewHolder(View itemView,ItemLongClickListener itemLongClickListener, ItemClickListener itemClickListener) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.chkListEntrySelect);
            colA = itemView.findViewById(R.id.tListEntryColA);
            colB = itemView.findViewById(R.id.tListEntryColB);
            name = itemView.findViewById(R.id.tListEntryName);
            created = itemView.findViewById(R.id.tListEntryCreated);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            this.itemClickListener = itemClickListener;
            this.itemLongClickListener = itemLongClickListener;
            if(!multiselect){
                viewForeground.setOnClickListener(this);
                viewForeground.setOnLongClickListener(itemLongClickListener != null ? this : null);
            }
        }

        @Override
        public void onClick(View v) {
            if(itemClickListener != null)
                itemClickListener.onItemClick(v,getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if(itemLongClickListener != null)
                itemLongClickListener.onItemLongClick(v,getAdapterPosition());
            return true;
        }
    }
}
