package com.tycho.app.primenumberfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tycho.app.primenumberfinder.utils.StatisticFragment;

import org.json.JSONException;

/**
 * @author Tycho Bellers
 *         Date Created: 2/17/2017
 */

public class StatisticsFragment2 extends StatisticFragment{

    /**
     * Views within the fragment.
     */
    private SpeedometerView speedometerViewNumbersPerSecond;
    private SpeedometerView speedometerViewPrimesPerSecond;

    private long lastUpdateTime;

    long frameCap = 1000 / 30;

    //Override methods

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){

        //Inflate the view
        final View rootView = inflater.inflate(R.layout.fragment_statistics_2, viewGroup, false);

        speedometerViewNumbersPerSecond = (SpeedometerView) rootView.findViewById(R.id.numbersPerSecond);
        speedometerViewPrimesPerSecond = (SpeedometerView) rootView.findViewById(R.id.primesPerSecond);

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
            if (System.currentTimeMillis() - lastUpdateTime >= frameCap){
                if (statisticData.optInt(Statistic.NUMBERS_PER_SECOND) > speedometerViewNumbersPerSecond.getMaxValue()){
                    speedometerViewNumbersPerSecond.setMaxValue(statisticData.optInt(Statistic.NUMBERS_PER_SECOND));
                }
                speedometerViewNumbersPerSecond.setValue(statisticData.optInt(Statistic.NUMBERS_PER_SECOND));
                if (statisticData.optInt(Statistic.PRIMES_PER_SECOND) > speedometerViewPrimesPerSecond.getMaxValue()){
                    speedometerViewPrimesPerSecond.setMaxValue(statisticData.optInt(Statistic.PRIMES_PER_SECOND));
                }
                speedometerViewPrimesPerSecond.setValue(statisticData.optInt(Statistic.PRIMES_PER_SECOND));
                lastUpdateTime = System.currentTimeMillis();
            }
        }


    }

    //Getters and setters

    public SpeedometerView getSpeedometerViewNumbersPerSecond(){
        return speedometerViewNumbersPerSecond;
    }

    public SpeedometerView getSpeedometerViewPrimesPerSecond(){
        return speedometerViewPrimesPerSecond;
    }
}
