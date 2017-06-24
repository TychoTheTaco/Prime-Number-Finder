package com.tycho.app.primenumberfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tycho.app.primenumberfinder.utils.StatisticFragment;
import com.tycho.app.simplegraphs.ui.ScrollingLineGraph;

import org.json.JSONException;

/**
 * @author Tycho Bellers
 *         Date Created: 2/17/2017
 */

public class StatisticsFragment1 extends StatisticFragment{

    /**
     * Views within the fragment.
     */
    private ScrollingLineGraph graphNumbersPerSecond;
    private ScrollingLineGraph graphPrimesPerSecond;

    //Override methods

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){

        //Inflate the view
        final View rootView = inflater.inflate(R.layout.fragment_statistics_1, viewGroup, false);

        graphNumbersPerSecond = (ScrollingLineGraph) rootView.findViewById(R.id.graphNumbersPerSecond);
        graphPrimesPerSecond = (ScrollingLineGraph) rootView.findViewById(R.id.graphPrimesPerSecond);

        if (savedInstanceState != null){
            try{
                final String statisticDataJson = savedInstanceState.getString(KEY_STATISTIC_DATA);
                if (statisticDataJson != null){
                    setStatisticData(new StatisticData(statisticDataJson));
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        return rootView;
    }

    @Override
    protected void updateData(StatisticData statisticData){
        if (getView() != null){
            graphNumbersPerSecond.setValue(statisticData.optInt(Statistic.NUMBERS_PER_SECOND));
            graphPrimesPerSecond.setValue(statisticData.optInt(Statistic.PRIMES_PER_SECOND));
        }

    }

    //Getters and setters

    public ScrollingLineGraph getGraphNumbersPerSecond(){
        return graphNumbersPerSecond;
    }

    public ScrollingLineGraph getGraphPrimesPerSecond(){
        return graphPrimesPerSecond;
    }
}
