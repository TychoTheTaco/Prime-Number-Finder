package com.tycho.app.primenumberfinder.Fragments_old;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.Statistic;
import com.tycho.app.primenumberfinder.StatisticData;
import com.tycho.app.primenumberfinder.StatisticsAdapter;
import com.tycho.app.primenumberfinder.StatisticsFragment0;
import com.tycho.app.primenumberfinder.StatisticsFragment1;
import com.tycho.app.primenumberfinder.StatisticsFragment2;
import com.tycho.app.primenumberfinder.TaskFragment;
import com.tycho.app.primenumberfinder.TaskListener;
import com.tycho.app.primenumberfinder.activities.MainActivity;
import com.tycho.app.primenumberfinder.adapters.FactorsAdapter;
import com.tycho.app.primenumberfinder.tasks.FindFactorsTask;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Task;

import org.json.JSONException;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;

import static com.tycho.app.primenumberfinder.fragments.FindPrimesFragment.map;

/**
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public class FindFactorsFragment extends TaskFragment implements Savable, FindFactorsTask.EventListener{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsFragment";

    /**
     * All views
     */
    private EditText editTextNumberToFactor;

    private ImageButton pauseButton;
    private ImageButton resetButton;
    
    private ProgressBar progressBarInfinite;
    //private ProgressBar progressBarTotalProgress;

    private TextView textViewSearchProgress;
    private TextView textViewBullet;
    
    private RecyclerView recyclerViewFactors;
    
    /**
     * Adapter for the list of factors found.
     */
    private FactorsAdapter factorsAdapter;

    /**
     * An array of times certain UI components were last updated. The index of the array corresponds
     * to the UI group the views are in.
     */
    private long[] lastUpdateTimes = new long[3];

    /**
     * All UI updates are posted to this {@link Handler}.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * The number of items in the adapter at the previous update interval.
     */
    private int prevItemCount = 0;

    private TextView textViewSearchStatus;

    private BottomSheetBehavior bottomSheetBehavior;

    //private TextView textViewResult;
    private TextView textViewElapsedTime;

    //private TextView textViewFactors;
    private String factors = "";

    private TextView textViewFactorsTitle;

    private StatisticsAdapter statisticsAdapter;

    private RadioGroup radioGroupMonitorType;

    private ViewPager statisticsViewPager;

    private View cardViewResults;

    private final FindFactorsTask.SearchOptions searchOptions = new FindFactorsTask.SearchOptions(0, FindFactorsTask.SearchOptions.MonitorType.SIMPLE);

    //Override methods

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){

        //Inflate the view
        final View rootView = inflater.inflate(R.layout.fragment_find_factors, viewGroup, false);

        cardViewResults = rootView.findViewById(R.id.search_results);

        //Define views
        editTextNumberToFactor = (EditText) rootView.findViewById(R.id.editText_input_number);
        //textViewResult = (TextView) rootView.findViewById(R.id.result);
        radioGroupMonitorType = (RadioGroup) rootView.findViewById(R.id.radio_group_monitor_type);
       // textViewCurrentNumber = (TextView) rootView.findViewById(R.id.textView_currentNumber);
        textViewSearchStatus = (TextView) rootView.findViewById(R.id.title);
        textViewElapsedTime = (TextView) rootView.findViewById(R.id.textView_elapsed_time);
        progressBarInfinite = (ProgressBar) rootView.findViewById(R.id.progressBar_infinite);
        textViewBullet = (TextView) rootView.findViewById(R.id.textView_bullet);
     //   progressBarTotalProgress = (ProgressBar) rootView.findViewById(R.id.progressBar_totalProgress);
      //  textViewFactorsLabel = (TextView) rootView.findViewById(R.id.textView_label_factors);
        textViewSearchProgress = (TextView) rootView.findViewById(R.id.textView_search_progress);

        textViewFactorsTitle = (TextView) rootView.findViewById(R.id.subTitle);

        //textViewFactors = (TextView) rootView.findViewById(R.id.factors);

        statisticsAdapter = new StatisticsAdapter(getFragmentManager());

        statisticsViewPager = (ViewPager) rootView.findViewById(R.id.statistics_viewpager);
        statisticsViewPager.setAdapter(statisticsAdapter);
        statisticsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){

            }

            @Override
            public void onPageSelected(int position){
               /* for (int i = 0; i < ((LinearLayout) rootView.findViewById(R.id.page_indicator)).getChildCount(); i++){
                    ((LinearLayout) rootView.findViewById(R.id.page_indicator)).getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                }
                ((LinearLayout) rootView.findViewById(R.id.page_indicator)).getChildAt(position).setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));*/
            }

            @Override
            public void onPageScrollStateChanged(int state){

            }
        });

        final Button buttonFindFactors = (Button) rootView.findViewById(R.id.button_find_factors);
        buttonFindFactors.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                playClicked();
            }
        });

        //Initialize the recycler view
        factorsAdapter = new FactorsAdapter(getActivity());
        recyclerViewFactors = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerViewFactors.setHasFixedSize(true);
        recyclerViewFactors.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewFactors.setAdapter(factorsAdapter);
        recyclerViewFactors.setItemAnimator(null);

        editTextNumberToFactor.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void afterTextChanged(Editable editable){

                final String text = editable.toString();

                final String numberString = text.replace(",","");

                try{
                    final long number = Long.valueOf(numberString);

                    final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(number);

                    if (!text.equals(formattedText)){
                        editTextNumberToFactor.setText(formattedText);
                    }

                    editTextNumberToFactor.setSelection(formattedText.length());
                }catch (RuntimeException e){}

                if (isNumberValid()){
                    editTextNumberToFactor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.orange_light)));
                }else{
                    editTextNumberToFactor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.red)));
                }
            }
        });
        editTextNumberToFactor.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){
                editTextNumberToFactor.setText("");
                return false;
            }
        });

        final View bottomSheet = rootView.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight((int) PrimeNumberFinder.dpToPx(getActivity(), 48));
        bottomSheet.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.e(TAG, "Clicked! State: " + bottomSheetBehavior.getState());
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        final View screenDim = rootView.findViewById(R.id.screenDim);
        screenDim.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if (screenDim.getAlpha() > 0){
                    if (event.getAction() == MotionEvent.ACTION_UP){
                        editTextNumberToFactor.clearFocus();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        MainActivity.hideKeyboard(getActivity());
                    }
                    return true;
                }
                return false;
            }
        });

        radioGroupMonitorType = (RadioGroup) rootView.findViewById(R.id.radio_group_monitor_type);
        radioGroupMonitorType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId){
                switch (checkedId){

                    case R.id.none:
                        searchOptions.setMonitorType(FindFactorsTask.SearchOptions.MonitorType.NONE);
                        break;

                    case R.id.simple:
                        searchOptions.setMonitorType(FindFactorsTask.SearchOptions.MonitorType.SIMPLE);
                        break;

                    case R.id.advanced:
                        searchOptions.setMonitorType(FindFactorsTask.SearchOptions.MonitorType.ADVANCED);
                        break;
                }
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback(){
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState){

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset){
                screenDim.setAlpha(map(slideOffset, 0, 1, 0, 0.7f));
            }
        });

        pauseButton = (ImageButton) rootView.findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                actionButton0Clicked();
            }
        });

        resetButton = (ImageButton) rootView.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                reset();
            }
        });

        //Set defaults
        applyDefaults();

        return rootView;
    }

    //Task events

    //@Override
    public void onTaskStarted(){

        factorsAdapter.setNumber(getNumberToFactor());

        handler.post(new Runnable(){
            @Override
            public void run(){

                cardViewResults.setVisibility(View.VISIBLE);

                final String start =  NumberFormat.getInstance(Locale.getDefault()).format(getNumberToFactor());
                final String string = getString(R.string.find_factors_result_list, start);
                final SpannableString text = new SpannableString(string);
                text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.primary_text)), string.indexOf(start), string.indexOf(start) + start.length(), 0);
                text.setSpan(new StyleSpan(Typeface.BOLD), string.indexOf(start), string.indexOf(start) + start.length(), 0);
                //textViewSearchRange.setText(text);

                //final SpannableString spannableString = new SpannableString(NumberFormat.getInstance(Locale.getDefault()).format(getNumberToFactor()));
                //spannableString.setSpan();
                textViewFactorsTitle.setText(text);

                //Update search status
                textViewSearchStatus.setText("Searching...");
                progressBarInfinite.setVisibility(View.VISIBLE);
                textViewBullet.setVisibility(View.VISIBLE);

                //Update bottom sheet
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                editTextNumberToFactor.setEnabled(false);

                //Update action buttons
                updateActionButtons();

                ((MainActivity) getActivity()).hideMenuItemSave();
            }
        });

        if (searchOptions.getMonitorType() == FindFactorsTask.SearchOptions.MonitorType.NONE){
            new Thread(new Runnable(){
                @Override
                public void run(){
                    while (getTask() != null && getTask().getState() != Task.State.FINISHED){
                        handler.post(new Runnable(){
                            @Override
                            public void run(){
                                //textViewCurrentNumber.setText(NumberFormat.getInstance(Locale.getDefault()).format(((FindPrimesTask) getTask()).getCurrentNumber()));
                                //textViewHighestPrime.setText(NumberFormat.getInstance(Locale.getDefault()).format(((FindPrimesTask) getTask()).getPrimeNumbers().get(((FindPrimesTask) getTask()).getPrimeNumbers().size() - 1)));
                                textViewElapsedTime.setText(StatisticsFragment0.formatTime(getTask().getElapsedTime()));
                            }
                        });

                        try{
                            Thread.sleep(16);
                        }catch (InterruptedException e){}
                    }
                }
            }).start();
        }
    }

   // @Override
    public void onTaskPaused(){
        handler.post(new Runnable(){
            @Override
            public void run(){

                //Update search status
                textViewSearchStatus.setText("Paused");
                progressBarInfinite.setVisibility(View.GONE);

                //Update action buttons
                updateActionButtons();

                ((MainActivity) getActivity()).showMenuItemSave();
            }
        });
    }

   // @Override
    public void onTaskResumed(){
        handler.post(new Runnable(){
            @Override
            public void run(){

                //Update search status
                textViewSearchStatus.setText("Searching...");
                progressBarInfinite.setVisibility(View.VISIBLE);

                //Update bottom sheet
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                //Update action buttons
                updateActionButtons();

                ((MainActivity) getActivity()).hideMenuItemSave();
            }
        });
    }

   // @Override
    public void onTaskStopped(){
        handler.post(new Runnable(){
            @Override
            public void run(){

                //Update search status
                textViewSearchStatus.setText("Stopped");
                progressBarInfinite.setVisibility(View.GONE);

                //Update action buttons
                updateActionButtons();

                ((MainActivity) getActivity()).showMenuItemSave();
            }
        });
    }

    //@Override
    public void onTaskFinished(){
        handler.post(new Runnable(){
            @Override
            public void run(){

                updateStatistics();

                //Update search status
                textViewSearchStatus.setText("Finished ");
                progressBarInfinite.setVisibility(View.GONE);

                //Update action buttons
                updateActionButtons();

                //TEST STUFF
                final String number = NumberFormat.getInstance().format(getNumberToFactor());
                final String factorCount  = NumberFormat.getInstance().format(((FindFactorsTask) getTask()).getFactors().size());
                final String string = getString(R.string.find_factors_result, number, factorCount);
                final SpannableString text = new SpannableString(string);
                final int firstIndexStart = string.indexOf(number);
                final int firstIndexEnd = firstIndexStart + number.length();
                text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.primary_text)), firstIndexStart, firstIndexEnd, 0);
                final int secondIndexStart = string.indexOf(factorCount, firstIndexEnd);
                text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.primary_text)), secondIndexStart, secondIndexStart + factorCount.length(), 0);
              //  textViewResult.setVisibility(View.VISIBLE);
               // textViewResult.setText(text);

                //numberAdapter.notifyDataSetChanged();
                factorsAdapter.notifyDataSetChanged();

                ((MainActivity) getActivity()).showMenuItemSave();
            }
        });
    }

    //@Override
    public void onProgressChanged(float percent){
        requestUiUpdate();
    }

    @Override
    public void onFactorFound(long factor){

        if (factor != getNumberToFactor()){
            factors += factor + ", ";
        }else{
            factors += factor;
        }


       // numberAdapter.getListNumbers().add(factor);
        factorsAdapter.getListNumbers().add(factor);
        requestUiUpdate();
    }

    private void updateActionButtons(){
        if (getTask() != null){
            switch (getTask().getState()){

                case NOT_STARTED:
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    resetButton.setVisibility(View.GONE);
                    break;

                case RUNNING:
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                    resetButton.setVisibility(View.GONE);
                    break;

                case PAUSED:
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    resetButton.setVisibility(View.VISIBLE);
                    break;

                case STOPPED:
                    pauseButton.setVisibility(View.GONE);
                    resetButton.setVisibility(View.VISIBLE);
                    break;

                case FINISHED:
                    pauseButton.setVisibility(View.GONE);
                    resetButton.setVisibility(View.VISIBLE);
                    break;
            }
        }else{
            pauseButton.setVisibility(View.VISIBLE);
            pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            resetButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void reset(){

        setTask(null);
        PrimeNumberFinder.getTasks().remove("findFactors");

        applyDefaults();

        updateActionButtons();

        //Save button
        ((MainActivity) getActivity()).hideMenuItemSave();
    }

    //Utility methods

    public void onSaveClicked(){

        Toast.makeText(getActivity(), "Saving file...", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable(){
            @Override
            public void run(){
                final boolean success = FileManager.getInstance(getActivity()).saveFactors(((FindFactorsTask) getTask()).getFactors());
                handler.post(new Runnable(){
                    @Override
                    public void run(){
                        if (success){
                            Toast.makeText(getActivity(), "Successfully saved file", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(), "Error saving file", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void actionButton0Clicked(){

        final Task.State taskState;
        if (getTask() == null){
            taskState = Task.State.NOT_STARTED;
        }else{
            taskState = getTask().getState();
        }

        switch (taskState){

            case NOT_STARTED:
            case STOPPED:
            case PAUSED:
                playClicked();
                break;

            case RUNNING:
                pauseClicked();
                break;

            case FINISHED:
                reset();
                break;
        }
    }

    private void playClicked(){

        //Check if the number is valid
        if (isNumberValid()){

            //Check if the runnable is null
            if (getTask() == null){

                setTask(new FindFactorsTask(getNumberToFactor()));
                PrimeNumberFinder.getTasks().put("findFactors", getTask());
               // getTask().addTaskListener(this);
                getTask().addTaskListener(new TaskListener(){
                    @Override
                    public void onTaskStarted(){
                        FindFactorsFragment.this.onTaskStarted();
                    }

                    @Override
                    public void onTaskPaused(){
                        FindFactorsFragment.this.onTaskPaused();
                    }

                    @Override
                    public void onTaskResumed(){
                        FindFactorsFragment.this.onTaskResumed();
                    }

                    @Override
                    public void onTaskStopped(){
                        FindFactorsFragment.this.onTaskStopped();
                    }

                    @Override
                    public void onTaskFinished(){
                        FindFactorsFragment.this.onTaskFinished();
                    }

                    @Override
                    public void onProgressChanged(float percent){
                        FindFactorsFragment.this.onProgressChanged(percent);
                    }
                });
                ((FindFactorsTask) getTask()).addEventListener(this);

                //Start the runnable in a new thread
                new Thread(getTask()).start();
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        while (getTask() != null && getTask().getState() != Task.State.STOPPED && getTask().getState() != Task.State.FINISHED){
                            handler.post(new Runnable(){
                                @Override
                                public void run(){
                                    updateStatistics();
                                }
                            });

                            try{
                                Thread.sleep(PrimeNumberFinder.UPDATE_LIMIT_MS);
                            }catch (InterruptedException e){}
                        }
                    }
                }).start();

            }else{

                //Switch based on state
                switch (getTask().getState()){

                    case RUNNING:
                        getTask().pause();
                        break;

                    case PAUSED:
                        getTask().resume();
                        break;
                }
            }
        }else{
            //Invalid range
            Toast.makeText(getActivity(), "Invalid number", Toast.LENGTH_SHORT).show();

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void pauseClicked(){
        getTask().pause();
    }

    private boolean isNumberValid(){

        try{

            final long number = getNumberToFactor();

            //The number must be greater than 0
            if (number <= 0){
                return false;
            }

        }catch (NumberFormatException e){
            return false;
        }

        return true;
    }

    //User interface

    /**
     * Update the user interface only if the update limit has been passed.
     */
    private void requestUiUpdate(){

        //Check if the UI should be updated
        if (System.currentTimeMillis() - lastUpdateTimes[0] >= PrimeNumberFinder.UPDATE_LIMIT_MS){

            //Update the UI on the main thread
            new Handler(Looper.getMainLooper()).post(new Runnable(){
                @Override
                public void run(){
                    updateUi();
                }
            });

            //The last UI update was now
            lastUpdateTimes[0] = System.currentTimeMillis();
        }
    }

    /**
     * Update all user interface components immediately.
     */
    private void updateUiNow(){
        updateUiGroup0();
        updateUiGroup1();
        updateUiGroup2();
    }

    /**
     * Update all user interface components. This will make sure each UI group's refresh is synced.
     */
    private void updateUi(){

//        textViewFactors.setText(factors);
        textViewSearchProgress.setText(String.format(Locale.getDefault(), "%.2f%%", getTask().getProgress() * 100));

        //Update these items every UI update
        updateUiGroup0();

        //Update these items every second
        if (System.currentTimeMillis() - lastUpdateTimes[1] >= 1000){
            updateUiGroup1();
            lastUpdateTimes[1] = System.currentTimeMillis();
        }

        //Update these items every 200 ms
        if (System.currentTimeMillis() - lastUpdateTimes[2] >= 200){
            updateUiGroup2();
            lastUpdateTimes[2] = System.currentTimeMillis();
        }

    }

    /**
     * Update the views in group 0. These are updated every UI refresh.
     */
    private void updateUiGroup0(){
        updateStatistics();
        //textViewCurrentNumber.setText(NumberFormat.getInstance(Locale.getDefault()).format(((RunnableFindFactors)PrimeNumberFinder.getRunnables()[1]).getCurrentNumber()));
        //progressBarTotalProgress.setProgress((int) (((RunnableFindFactors)PrimeNumberFinder.getRunnables()[1]).getTotalProgress() * 100));
        //textViewFactorsLabel.setText("Factors (" + NumberFormat.getInstance(Locale.getDefault()).format(numberAdapter.getItemCount()) + ")");
    }

    private void updateStatistics(){

        final StatisticData statisticData = new StatisticData();

        if (getTask() != null){
            try{
                statisticData.put(Statistic.TIME_ELAPSED.getKey(), getTask().getElapsedTime());
                statisticData.put(Statistic.FACTORS_PER_SECOND.getKey(), 0);
                statisticData.put(Statistic.NUMBERS_PER_SECOND.getKey(), 0);
            }catch (JSONException e){
                e.printStackTrace();
            }

            ((StatisticsFragment0) statisticsAdapter.getItem(0)).setStatisticData(statisticData);
            ((StatisticsFragment1) statisticsAdapter.getItem(1)).setStatisticData(statisticData);
            ((StatisticsFragment2) statisticsAdapter.getItem(2)).setStatisticData(statisticData);
        }
    }

    /**
     * Update the views in group 1.
     */
    private void updateUiGroup1(){

        //Factors found per second
        /*if (factorsSinceLast > speedometerViewFactorsPerSecond.getMaxSpeed()){
            speedometerViewFactorsPerSecond.setMaxSpeed(factorsSinceLast);
        }
        speedometerViewFactorsPerSecond.setSpeed((int) factorsSinceLast);*/
        //factorsSinceLast = 0;

        //Elapsed time
        //stopwatchViewElapsedTime.setTime(PrimeNumberFinder.getTasks()[1].getElapsedTime());
    }

    /**
     * Update the views in group 1.
     */
    private void updateUiGroup2(){

        final int itemCount = factorsAdapter.getItemCount();

        //Refresh adapter data and scroll to the bottom
        if (prevItemCount < itemCount){
            factorsAdapter.notifyItemRangeInserted(prevItemCount, itemCount - prevItemCount);
            prevItemCount = itemCount;
            recyclerViewFactors.scrollToPosition(itemCount - 1);
        }
    }

    private long getNumberToFactor(){
        final String numberString = editTextNumberToFactor.getText().toString();

        final BigInteger number = new BigInteger(editTextNumberToFactor.getText().toString().replace(",", ""));

        if (number.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1){
            return -1;
        }

        return number.longValue();
    }

    private void applyDefaults(){

        cardViewResults.setVisibility(View.INVISIBLE);

        //Update search status
        textViewSearchStatus.setText(getString(R.string.status_ready));

       // textViewResult.setVisibility(View.INVISIBLE);
        textViewBullet.setVisibility(View.GONE);

        editTextNumberToFactor.setEnabled(true);
        editTextNumberToFactor.setText("");
       // textViewCurrentNumber.setText("-");
        progressBarInfinite.setVisibility(View.GONE);
       // progressBarTotalProgress.setVisibility(View.VISIBLE);
       // progressBarTotalProgress.setProgress(0);

       // textViewFactorsLabel.setText("Factors");
        final int adapterSize = factorsAdapter.getListNumbers().size();
        factorsAdapter.getListNumbers().clear();
        factorsAdapter.notifyItemRangeRemoved(0, adapterSize);
    }
}
