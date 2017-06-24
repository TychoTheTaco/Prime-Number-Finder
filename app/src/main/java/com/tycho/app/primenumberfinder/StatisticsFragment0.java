package com.tycho.app.primenumberfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.utils.StatisticFragment;

import org.json.JSONException;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 2/17/2017
 */

public class StatisticsFragment0 extends StatisticFragment{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "StatisticsFragment0";

    /**
     * Views within the fragment.
     */
    private TextView textViewElapsedTime;
    private TextView textViewNumbersPerSecond;
    private TextView textViewPrimesPerSecond;
    private TextView textViewAverageCheckTime;

    //Override methods

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){

        //Inflate the view
        final View rootView = inflater.inflate(R.layout.fragment_statistics_0, viewGroup, false);

        textViewElapsedTime = (TextView) rootView.findViewById(R.id.textView_elapsed_time);
        textViewNumbersPerSecond = (TextView) rootView.findViewById(R.id.textView_numbers_per_second);
        textViewPrimesPerSecond = (TextView) rootView.findViewById(R.id.textView_primes_per_second);
        textViewAverageCheckTime = (TextView) rootView.findViewById(R.id.textView_average_check_time);

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
            setTimeElapsed(statisticData.optLong(Statistic.TIME_ELAPSED));
            textViewNumbersPerSecond.setText(NumberFormat.getInstance().format(statisticData.optInt(Statistic.NUMBERS_PER_SECOND)));
            textViewPrimesPerSecond.setText(NumberFormat.getInstance().format(statisticData.optInt(Statistic.PRIMES_PER_SECOND)));
        }

        //textViewAverageCheckTime.setText(NumberFormat.getInstance().format((float) statisticData.getAverageCheckTime() / 1000000));
    }

    public void setTimeElapsed(final long millis){
        textViewElapsedTime.setText(formatTime(millis));
    }

    public static String formatTime(final long millis){

        int milliseconds = (int) (millis % 1000);
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) (millis / (1000 * 60)) % 60;
        int hours = (int) (millis / (1000 * 60 * 60)) % 24;
        int days = (int) (millis / (1000 * 60 * 60 * 24)) % 7;

        final String time;

        if (days > 0){
            time = String.format(Locale.getDefault(),"%03d:%02d:%02d:%02d.%03d", days, hours, minutes, seconds, milliseconds);
        }else if (hours > 0){
            time = String.format(Locale.getDefault(),"%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
        }else{
            time = String.format(Locale.getDefault(),"%02d:%02d.%03d", minutes, seconds, milliseconds);
        }

        return time;
    }
}
