package vocabletrainer.heinecke.aron.vocabletrainer.lib.Adapter;

import android.graphics.Canvas;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;

/**
 * VList touch helper for recyclerview
 */
public class ListTouchHelper extends ItemTouchHelper.SimpleCallback {
    /**
     * Swipe listener
     */
    public interface SwipeListener{
        /**
         * Called on swipe
         * @param viewHolder
         * @param position adapter position
         */
        void onSwiped(ListRecyclerAdapter.VListViewHolder viewHolder, int position);
    }

    private SwipeListener swipeListener;

    /**
     * Create new list touch helper with specified swipe listener
     * @param swipeListener
     */
    public ListTouchHelper(SwipeListener swipeListener) {
        super(0, ItemTouchHelper.RIGHT);
        this.swipeListener = swipeListener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        swipeListener.onSwiped((ListRecyclerAdapter.VListViewHolder) viewHolder,viewHolder.getAdapterPosition());
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            final View foregroundView = ((ListRecyclerAdapter.VListViewHolder) viewHolder).viewForeground;

            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((ListRecyclerAdapter.VListViewHolder) viewHolder).viewForeground;
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((ListRecyclerAdapter.VListViewHolder) viewHolder).viewForeground;
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((ListRecyclerAdapter.VListViewHolder) viewHolder).viewForeground;

        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }
}
