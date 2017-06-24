package com.tycho.app.primenumberfinder.utils;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.tycho.app.primenumberfinder.StatisticData;

/**
 * A statistics fragment is a {@link Fragment} that display statistics about a particular task
 * (such as time elapsed and numbers scanned per second).
 *
 * @author Tycho Bellers
 *         Date Created: 5/19/2017
 */
public abstract class StatisticFragment extends Fragment{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "StatisticFragment";

    /**
     * This key is used to store the {@link StatisticData} object when saving and restoring the
     * fragment state.
     */
    protected static final String KEY_STATISTIC_DATA = "statisticData";

    /**
     * This {@link StatisticData} holds all of the data for the statistics.
     */
    private StatisticData statisticData;

    //Override methods

    @Override
    public void onSaveInstanceState(Bundle outState){
        if (statisticData != null){
            outState.putString(KEY_STATISTIC_DATA, statisticData.toString());
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        if (statisticData == null){
            updateData(new StatisticData());
        }else{
            updateData(statisticData);
        }
    }

    //Abstract methods

    /**
     * Update the fragment with new data.
     * @param data The new statistic data.
     */
    protected abstract void updateData(StatisticData data);

    //Getters and setters

    public StatisticData getStatisticData(){
        return statisticData;
    }

    public void setStatisticData(StatisticData statisticData){
        this.statisticData = statisticData;
        if (this.statisticData != null){
            updateData(statisticData);
        }
    }
}
