package com.tycho.app.primenumberfinder.modules.savedfiles.sort;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;

public class SortMethodView implements View.OnClickListener{

    private final Context context;

    private final int drawableRes;
    private final String name;
    protected boolean ascending;

    private boolean selected;

    private ImageView ascendingView;

    public SortMethodView(final Context context, final int drawableRes, final String name){
        this.context = context;
        this.drawableRes = drawableRes;
        this.name = name;
    }

    public View inflate(final ViewGroup parent, final boolean attachToParent){
        final View view = LayoutInflater.from(context).inflate(R.layout.sort_method, parent, attachToParent);

        final TextView nameTextView = view.findViewById(R.id.text);
        nameTextView.setText(name);
        nameTextView.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0);

        ascendingView = view.findViewById(R.id.direction);
        ascendingView.setImageResource(ascending ? R.drawable.ic_keyboard_arrow_up_white_24dp : R.drawable.ic_keyboard_arrow_down_white_24dp);
        ascendingView.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);

        //Apply tint to icons
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            for (Drawable drawable : nameTextView.getCompoundDrawables()) {
                if (drawable != null) {
                    drawable.mutate().setTint(ContextCompat.getColor(context, R.color.white));
                }
            }
        }

        view.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        setSelected(true, !ascending);
    }

    public void setSelected(boolean selected, boolean ascending) {
        this.selected = selected;
        if (selected) this.ascending = ascending;
        if (ascendingView != null){
            ascendingView.setImageResource(ascending ? R.drawable.ic_keyboard_arrow_up_white_24dp : R.drawable.ic_keyboard_arrow_down_white_24dp);
            ascendingView.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public String getName() {
        return name;
    }
}
