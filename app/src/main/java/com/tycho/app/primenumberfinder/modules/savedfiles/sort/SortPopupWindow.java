package com.tycho.app.primenumberfinder.modules.savedfiles.sort;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.core.content.res.ResourcesCompat;

import com.tycho.app.primenumberfinder.R;

import java.util.ArrayList;
import java.util.List;

public class SortPopupWindow extends PopupWindow {

    private final Context context;

    private final List<SortMethodView> sortMethodViews = new ArrayList<>();

    public SortPopupWindow(final Context context, final int backgroundColor, final List<SortMethod> sortMethods){
        super(LayoutInflater.from(context).inflate(R.layout.sort_dialog_menu, null), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;

        setFocusable(true);
        setOutsideTouchable(true);

        /*
        TODO: WTF
        For some reason these are required on API < 23. It will not set the color to red, but it will
        keep the correct background tint list.
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            getContentView().setBackgroundColor(Color.RED); //Required for setBackgroundTintList()
            setBackgroundDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.round_rectangle_white, null)); //Required for setOutsideTouchable()
        }

        getContentView().setBackgroundTintList(new ColorStateList(
                new int[][]{
                        new int[]{}
                        },
                new int[]{
                        backgroundColor
                }));

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
        FILE_SIZE("Size", R.drawable.ic_sort),
        NUMBER("Number", R.drawable.ic_numeric);

        private final String name;

        private final int drawableResource;

        SortMethod(String name, int drawableResource) {
            this.name = name;
            this.drawableResource = drawableResource;
        }
    }
}
