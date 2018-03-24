package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Statistic;
import com.tycho.app.primenumberfinder.StatisticData;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.StatisticsFragment;

/**
 * Created by tycho on 12/13/2017.
 */

public class GeneralStatisticsFragment extends StatisticsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "GeneralStatsFragment";

    private StatisticsFragment content;

    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.general_statistics_fragment, container, false);
        this.container = (LinearLayout) rootView;
        updateContent();
        return rootView;
    }

    @Override
    protected void updateData(StatisticData data) {

    }

    public void setContent(final StatisticsFragment statisticsFragment){
       this.content = statisticsFragment;
       updateContent();
   }

    public StatisticsFragment getContent(){
       return this.content;
   }

    private void updateContent(){
        if (content != null && container != null){
            getChildFragmentManager().beginTransaction().replace(container.getId(), content).commit();
        }
    }

    @Override
    protected void packStatistics(StatisticData statisticData) {

    }
}
