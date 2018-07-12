package com.tycho.app.primenumberfinder.modules.savedfiles.sort;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.tycho.app.primenumberfinder.R;

import java.util.ArrayList;
import java.util.List;

public class SortPopupWindow extends PopupWindow {

    private final Context context;

    private final List<SortMethodView> sortMethodViews = new ArrayList<>();

    public SortPopupWindow(final Context context, final SortMethod... sortMethods){
        super(LayoutInflater.from(context).inflate(R.layout.sort_dialog_menu, null), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;

        for (final SortMethod methods : sortMethods){
            sortMethodViews.add(new SortMethodView(context, methods.drawableResource, methods.name){
                @Override
                public void onClick(View v) {
                    deselectAll();
                    super.onClick(v);
                    onSortMethodSelected(methods, ascending);
                }
            });
        }

        for (SortMethodView sortMethodView : sortMethodViews){
            ((ViewGroup) getContentView()).addView(sortMethodView.inflate((ViewGroup) getContentView(), false));
        }
    }

    private void deselectAll(){
        for (SortMethodView sortMethodView : sortMethodViews){
            sortMethodView.setSelected(false, false);
        }
    }

    public void setSearchMethod(final SortMethod searchMethod, final boolean ascending){
        for (SortMethodView view : sortMethodViews){
            if (view.getName().equals(searchMethod.name)){
                view.setSelected(true, ascending);
                return;
            }
        }
    }

    protected void onSortMethodSelected(final SortMethod sortMethod, final boolean ascending){

    }

    public enum SortMethod{
        DATE("Date", R.drawable.round_date_range_24),
        FILE_SIZE("Size", R.drawable.round_sort_24),
        SEARCH_RANGE_START("Search Range", R.drawable.ic_delete_white_24dp),
        SEARCH_RANGE_END("Search Range", R.drawable.ic_delete_white_24dp),
        NUMBER("Number", R.drawable.ic_delete_white_24dp);

        private final String name;

        private final int drawableResource;

        SortMethod(String name, int drawableResource) {
            this.name = name;
            this.drawableResource = drawableResource;
        }
    }
}
