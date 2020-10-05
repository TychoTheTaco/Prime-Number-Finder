package com.tycho.app.primenumberfinder;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VerticalItemDecorationLast extends RecyclerView.ItemDecoration{
    private int space;

    public VerticalItemDecorationLast(int space){
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1){
            outRect.bottom = space;
        }
    }
}
