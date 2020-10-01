package com.tycho.app.primenumberfinder.modules;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;

import java.util.Map;
import java.util.TreeMap;

public class StatisticsLayout {

    private final Context context;

    private final ViewGroup container;

    private final Map<String, StatisticsItem> statistics = new TreeMap<>();

    public StatisticsLayout(final ViewGroup container) {
        this.container = container;
        this.context = container.getContext();
    }

    public void add(final String tag, final StatisticsItem statisticsItem){
        this.statistics.put(tag, statisticsItem);
    }

    public void add(final String tag, final int drawableRes){
        this.statistics.put(tag, new StatisticsItem(drawableRes));
    }

    public void set(final String tag, final CharSequence content){
        this.statistics.get(tag).textView.setText(content);
    }

    public void remove(final String tag){
        final StatisticsItem statisticsItem = this.statistics.remove(tag);
        container.removeView(statisticsItem.textView);
    }

    public void hide(final String tag){
        this.statistics.get(tag).textView.setVisibility(View.GONE);
    }

    public void show(final String tag){
        this.statistics.get(tag).textView.setVisibility(View.VISIBLE);
    }

    public boolean isVisible(final String tag){
        return this.statistics.get(tag).textView.getVisibility() == View.VISIBLE;
    }

    public boolean contains(final String tag){
        return this.statistics.containsKey(tag);
    }

    public void setVisibility(int visibility){
        container.setVisibility(visibility);
    }

    public View inflate() {
        for (StatisticsItem statisticsItem : statistics.values()){
            container.addView(statisticsItem.inflate(container, false));
        }
        return container;
    }

    public class StatisticsItem{
        private TextView textView;

        private int drawableRes;

        public StatisticsItem(final int drawableRes){
            this.drawableRes = drawableRes;
        }

        public View inflate(final ViewGroup container, final boolean attachToParent) {
            final View rootView = LayoutInflater.from(context).inflate(R.layout.statistics_item, container, attachToParent);
            textView = (TextView) rootView;
            textView.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0);

            //Apply tint to icons
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                for (Drawable drawable : textView.getCompoundDrawables()) {
                    if (drawable != null) {
                        drawable.mutate().setTint(ContextCompat.getColor(context, PreferenceManager.getInt(PreferenceManager.Preference.THEME) ==  0 ? R.color.black : R.color.white));
                    }
                }
            }
            return rootView;
        }
    }
}
