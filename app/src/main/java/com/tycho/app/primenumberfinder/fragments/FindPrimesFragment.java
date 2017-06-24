package com.tycho.app.primenumberfinder.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.Adapters_old.NumberAdapter;
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
import com.tycho.app.primenumberfinder.tasks.FindPrimesTask;
import com.tycho.app.primenumberfinder.tasks.PrimalityCheckTask;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Task;

import org.json.JSONException;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;

import static com.tycho.app.primenumberfinder.activities.MainActivity.hideKeyboard;

/**
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public class FindPrimesFragment extends TaskFragment implements Savable, FindPrimesTask.EventListener{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesFragment";

    /**
     * All views.
     */
    private EditText editTextPrimalityInput;
    private EditText editTextSearchRangeStart;
    private EditText editTextSearchRangeEnd;

    private Button buttonCheckPrimality;
    private Button buttonFindPrimes;

    private ImageButton infinityButton;
    private ImageButton pauseButton;
    private ImageButton resetButton;

    private ProgressBar progressBarInfinite;
   // private ProgressBar progressBarTotalProgress;

    private TextView textViewSearchStatus;
    private TextView textViewSearchSubTitle;
    private TextView textViewBulletSeparator;

    private RecyclerView recyclerViewPrimes;

    private ViewPager statisticsViewPager;

    private View cardViewResults;

    /**
     * All UI updates are posted to this {@link Handler}.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Adapter for statistics {@link ViewPager}.
     */
    private StatisticsAdapter statisticsAdapter;

    /**
     * Adapter for the list of prime numbers found.
     */
    private final NumberAdapter primesAdapter = new NumberAdapter();

    /**
     * An array of times certain UI components were last updated. The index of the array corresponds
     * to the UI group the views are in.
     */
    private long[] lastUpdateTimes = new long[3];

    /**
     * The number of items in the adapter at the previous update interval.
     */
    private int prevItemCount = 0;

    private final FindPrimesTask.SearchOptions searchOptions = new FindPrimesTask.SearchOptions(0, -1, FindPrimesTask.SearchOptions.Method.SIEVE_OF_ERATOSTHENES, FindPrimesTask.SearchOptions.MonitorType.SIMPLE);

    private BottomSheetBehavior bottomSheetBehavior;

    private RadioGroup radioGroupSearchMethod;
    private RadioGroup radioGroupMonitorType;

    /**
     * A custom action mode callback that prevents the copy and paste bar from appearing.
     *
     * @return A custom callback that prevents copy and paste events.
     */
    private ActionMode.Callback customActionModeCallback(){
        return new ActionMode.Callback(){
            public boolean onPrepareActionMode(ActionMode mode, Menu menu){
                return false;
            }

            public void onDestroyActionMode(ActionMode mode){
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu){
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item){
                return false;
            }
        };
    }

    //Override methods

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){

        //Inflate the view
        final View rootView = inflater.inflate(R.layout.fragment_find_primes, viewGroup, false);

        cardViewResults = rootView.findViewById(R.id.search_results);

        //Define views
        editTextSearchRangeStart = (EditText) rootView.findViewById(R.id.search_range_start);
        editTextSearchRangeEnd = (EditText) rootView.findViewById(R.id.search_range_end);

        //Check primality input
        editTextPrimalityInput = (EditText) rootView.findViewById(R.id.editText_primality_input);
        editTextPrimalityInput.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void afterTextChanged(Editable editable){
                final String text = editable.toString();

                if (text.trim().length() > 0){
                    final String numberString = text.replace(",", "");
                    final BigInteger number = new BigInteger(numberString);
                    final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(number);

                    if (!text.equals(formattedText)){
                        editTextPrimalityInput.setText(formattedText);
                    }

                    editTextPrimalityInput.setSelection(formattedText.length());

                    if (isValidInput(number, false)){
                        editTextPrimalityInput.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));
                    }else{
                        editTextPrimalityInput.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.red)));
                    }
                }
            }
        });

        buttonCheckPrimality = (Button) rootView.findViewById(R.id.button_check_primality);
        buttonCheckPrimality.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (isValidInput(getPrimalityInput(), true)){

                    hideKeyboard(getActivity());

                    if (getTask() == null){
                        final PrimalityCheckTask primalityCheckTask = new PrimalityCheckTask(Long.valueOf(getPrimalityInput().toString()));
                        primalityCheckTask.addTaskListener(new TaskListener(){
                            @Override
                            public void onTaskStarted(){
                                handler.post(new Runnable(){
                                    @Override
                                    public void run(){
                                        textViewSearchStatus.setText("Searching...");
                                        progressBarInfinite.setVisibility(View.VISIBLE);
                                        textViewSearchSubTitle.setText("");
                                    }
                                });
                            }

                            @Override
                            public void onTaskPaused(){

                            }

                            @Override
                            public void onTaskResumed(){

                            }

                            @Override
                            public void onTaskStopped(){
                            }

                            @Override
                            public void onTaskFinished(){
                                handler.post(new Runnable(){
                                    @Override
                                    public void run(){

                                        updateActionButtons();

                                        final String start = NumberFormat.getInstance().format(Long.valueOf(editTextPrimalityInput.getText().toString().replace(",", "")));
                                        boolean isPrime = primalityCheckTask.isPrime();
                                        String end = isPrime ? "prime" : "not prime";
                                        final String string = getString(R.string.primality_result, start, end);
                                        final SpannableString text = new SpannableString(string);
                                        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.primary_text)), string.indexOf(start), string.indexOf(start) + start.length(), 0);
                                        text.setSpan(new StyleSpan(Typeface.BOLD), string.indexOf(start), string.indexOf(start) + start.length(), 0);
                                        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), (isPrime ? R.color.green : R.color.red))), string.indexOf(end), string.indexOf(end) + end.length(), 0);
                                        text.setSpan(new StyleSpan(Typeface.BOLD), string.indexOf(end), string.indexOf(end) + end.length(), 0);
                                        textViewSearchSubTitle.setText(text);

                                        textViewSearchStatus.setText(getString(R.string.status_finished));
                                        progressBarInfinite.setVisibility(View.GONE);
                                    }
                                });

                            }

                            @Override
                            public void onProgressChanged(float percent){

                            }
                        });
                        setTask(primalityCheckTask);
                        new Thread(getTask()).start();
                    }else{
                        Toast.makeText(getActivity(), "Please wait for another task to finish...", Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });


        //textViewResult = (TextView) rootView.findViewById(R.id.result);

        //textViewSearchRange = (TextView) rootView.findViewById(R.id.search_range);
        textViewSearchStatus = (TextView) rootView.findViewById(R.id.title);
        //textViewElapsedTime = (TextView) rootView.findViewById(R.id.textView_elapsed_time);

        textViewSearchSubTitle = (TextView) rootView.findViewById(R.id.subTitle);

        radioGroupSearchMethod = (RadioGroup) rootView.findViewById(R.id.radio_group_search_method);
        radioGroupSearchMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId){
                switch (checkedId){

                    case R.id.brute_force:
                        searchOptions.setSearchMethod(FindPrimesTask.SearchOptions.Method.BRUTE_FORCE);
                        editTextSearchRangeStart.setEnabled(true);
                        infinityButton.setEnabled(true);
                        infinityButton.setAlpha(1f);
                        break;

                    case R.id.sieve_of_eratosthenes:
                        searchOptions.setSearchMethod(FindPrimesTask.SearchOptions.Method.SIEVE_OF_ERATOSTHENES);
                        editTextSearchRangeStart.setText("0");
                        editTextSearchRangeStart.setEnabled(false);
                        if (getEndValue() < 0){
                            editTextSearchRangeEnd.setText(NumberFormat.getInstance().format(1000000));
                        }
                        infinityButton.setEnabled(false);
                        infinityButton.setAlpha(0.3f);
                        break;

                }

                editTextSearchRangeStart.setText(editTextSearchRangeStart.getText());
                editTextSearchRangeEnd.setText(editTextSearchRangeEnd.getText());
                //editTextSearchRangeStart.clearFocus();
            }
        });

        radioGroupMonitorType = (RadioGroup) rootView.findViewById(R.id.radio_group_monitor_type);
        radioGroupMonitorType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId){
                switch (checkedId){

                    case R.id.none:
                        searchOptions.setMonitorType(FindPrimesTask.SearchOptions.MonitorType.NONE);
                        break;

                    case R.id.simple:
                        searchOptions.setMonitorType(FindPrimesTask.SearchOptions.MonitorType.SIMPLE);
                        break;

                    case R.id.advanced:
                        searchOptions.setMonitorType(FindPrimesTask.SearchOptions.MonitorType.ADVANCED);
                        break;
                }
            }
        });

        pauseButton = (ImageButton) rootView.findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                playClicked();
                //actionButton0Clicked();
            }
        });

        buttonFindPrimes = (Button) rootView.findViewById(R.id.button_find_primes);
        buttonFindPrimes.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                playClicked();
            }
        });

        resetButton = (ImageButton) rootView.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                reset();
            }
        });

        final View bottomSheet = rootView.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight((int) PrimeNumberFinder.dpToPx(getActivity(), 48));
        bottomSheet.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
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
                    bottomSheet.requestLayout();
                    //buttonStart.requestLayout();
                    if (event.getAction() == MotionEvent.ACTION_UP){
                        //editTextSearchRangeStart.clearFocus();
                        //editTextSearchRangeEnd.clearFocus();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        hideKeyboard(getActivity());
                    }
                    return true;
                }
                return false;
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

        //primesAdapter = new NumberAdapter();
        recyclerViewPrimes = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerViewPrimes.setHasFixedSize(true);
        recyclerViewPrimes.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewPrimes.setAdapter(primesAdapter);
        recyclerViewPrimes.setItemAnimator(null);

        //Initialize all views
        initViews(rootView, savedInstanceState);

        Log.e(TAG, "onCreateView");

        return rootView;
    }

    private boolean isValidInput(final BigInteger bigInteger, boolean displayErrors){

        if (bigInteger.compareTo(BigInteger.ONE) < 0){
            if (displayErrors)
                Toast.makeText(getActivity(), "Number must be greater than 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (bigInteger.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) > 0){
            if (displayErrors)
                Toast.makeText(getActivity(), "Maximum input is 9,223,372,036,854,775,807", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public static float map(float value, float a, float b, float c, float d){
        return (value - a) / (b - a) * (d - c) + c;
    }

    private void updateActionButtons(){
        if (getTask() != null){
            switch (getTask().getState()){

                case NOT_STARTED:
                    //pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    resetButton.setEnabled(false);
                    break;

                case RUNNING:
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                    resetButton.setEnabled(false);
                    break;

                case PAUSED:
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    resetButton.setVisibility(View.VISIBLE);
                    resetButton.setEnabled(true);
                    break;

                case STOPPED:
                    pauseButton.setVisibility(View.GONE);
                    resetButton.setVisibility(View.VISIBLE);
                    resetButton.setEnabled(true);
                    break;

                case FINISHED:
                    pauseButton.setVisibility(View.GONE);
                    resetButton.setVisibility(View.VISIBLE);
                    resetButton.setEnabled(true);
                    break;
            }
        }else{
            //pauseButton.setVisibility(View.VISIBLE);
            pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            resetButton.setVisibility(View.GONE);
        }
    }

    private void runUiUpdater(){
        while (getTask() != null && getTask().getState() != Task.State.FINISHED){
            handler.post(new Runnable(){
                @Override
                public void run(){
                    if (getTask() != null){
                        try{
                            //if (getTask() != null && getTask().getState() != Task.State.PAUSED)
                               // textViewElapsedTime.setText(StatisticsFragment0.formatTime(getTask().getElapsedTime()));

                            switch (searchOptions.getMonitorType()){
                                case ADVANCED:
                                    break;

                                case SIMPLE:
                                    //textViewCurrentNumber.setText(NumberFormat.getInstance(Locale.getDefault()).format(((FindPrimesTask) getTask()).getCurrentNumber()));
                                   // progressBarCurrentProgress.setProgress((int) (((FindPrimesTask) getTask()).getCurrentProgress() * 100));
                                    //progressBarTotalProgress.setProgress((int) (getTask().getProgress() * 100));

                                    try{
                                     //   textViewHighestPrime.setText(NumberFormat.getInstance(Locale.getDefault()).format(((FindPrimesTask) getTask()).getPrimeNumbers().get(((FindPrimesTask) getTask()).getPrimeNumbers().size() - 1)));
                                    }catch (ArrayIndexOutOfBoundsException e){
                                    }

                                    //buttonViewAllPrimes.setText("View all (" + NumberFormat.getInstance(Locale.getDefault()).format(primesAdapter.getListNumbers().size()) + ")");

                                    updateStatistics();

                                    updateUiGroup2();
                                    break;

                                case NONE:
                                    break;
                            }

                        }catch (Exception e){
                        }
                    }
                }
            });

            try{
                Thread.sleep(PrimeNumberFinder.UPDATE_LIMIT_MS);
            }catch (InterruptedException e){
            }
        }
    }

   // @Override
    public void onTaskStarted(){

        handler.post(new Runnable(){
            @Override
            public void run(){

                cardViewResults.setVisibility(View.VISIBLE);
                textViewSearchStatus.setText(getString(R.string.status_searching));

                final String start = NumberFormat.getInstance().format(getStartValue());
                String end = NumberFormat.getInstance().format(getEndValue());
                if (getEndValue() == -1){
                    end = "infinity";
                }
                final String string = getString(R.string.search_range, start, end);
                final SpannableString text = new SpannableString(string);
                text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), string.indexOf(start), string.indexOf(start) + start.length(), 0);
                text.setSpan(new StyleSpan(Typeface.BOLD), string.indexOf(start), string.indexOf(start) + start.length(), 0);
                text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), string.indexOf(end), string.indexOf(end) + end.length(), 0);
                text.setSpan(new StyleSpan(Typeface.BOLD), string.indexOf(end), string.indexOf(end) + end.length(), 0);
               // textViewSearchRange.setText(text);
                textViewSearchSubTitle.setText(text);

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                //Progressbar
                if (getEndValue() == /*LIMIT_NO_LIMIT*/ -1){
                    //progressBarTotalProgress.setVisibility(View.GONE);
                }
                progressBarInfinite.setVisibility(View.VISIBLE);

                //Search range
                editTextSearchRangeStart.setEnabled(false);
                editTextSearchRangeEnd.setEnabled(false);
                infinityButton.setEnabled(false);
                infinityButton.setAlpha(0.3f);

                //Search method
                for (int i = 0; i < radioGroupSearchMethod.getChildCount(); i++){
                    radioGroupSearchMethod.getChildAt(i).setEnabled(false);
                }

                //Floating action button
                updateActionButtons();

                //Action view
                ((MainActivity) getActivity()).showActionView(0);

                //Scrollbars
                //recyclerViewPrimes.setVerticalScrollBarEnabled(false);

                //Save button
                ((MainActivity) getActivity()).hideMenuItemSave();
            }
        });

        new Thread(new Runnable(){
            @Override
            public void run(){
                runUiUpdater();
            }
        }).start();
    }

    //@Override
    public void onTaskPaused(){

        handler.post(new Runnable(){
            @Override
            public void run(){

                textViewSearchStatus.setText("Paused");

                //Progressbar
                progressBarInfinite.setVisibility(View.GONE);

                //Search range
                editTextSearchRangeEnd.setEnabled(true);
                infinityButton.setEnabled(true);
                infinityButton.setAlpha(1f);

                //Floating action button
                updateActionButtons();

                //Update the UI immediately
                updateUiNow();

                //Action view
                ((MainActivity) getActivity()).hideActionView(0);

                //Scrollbars
                //recyclerViewPrimes.setVerticalScrollBarEnabled(true);

                //Save button
                ((MainActivity) getActivity()).showMenuItemSave();

                updateStatistics();

            }
        });
    }

   // @Override
    public void onTaskResumed(){

        handler.post(new Runnable(){
            @Override
            public void run(){

                textViewSearchStatus.setText("Searching...");

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                //Progressbar
                if (getEndValue() == -1){
                   // progressBarTotalProgress.setVisibility(View.GONE);
                }else{
                    //progressBarTotalProgress.setVisibility(View.VISIBLE);
                }
                progressBarInfinite.setVisibility(View.VISIBLE);

                //Search range
                editTextSearchRangeStart.setEnabled(false);
                editTextSearchRangeEnd.setEnabled(false);

                //Floating action button
                updateActionButtons();

                //Action view
                ((MainActivity) getActivity()).showActionView(0);

                //Scrollbars
                //recyclerViewPrimes.setVerticalScrollBarEnabled(false);

                //Save button
                ((MainActivity) getActivity()).hideMenuItemSave();
            }
        });
    }

    //@Override
    public void onTaskStopped(){

        handler.post(new Runnable(){
            @Override
            public void run(){

                //searchActiveView.setVisibility(View.INVISIBLE);
                textViewSearchStatus.setText("Stopped");

                //Progressbar
                progressBarInfinite.setVisibility(View.GONE);

                //Search range
                editTextSearchRangeStart.setEnabled(false);
                editTextSearchRangeEnd.setEnabled(true);

                //Search method
                for (int i = 0; i < radioGroupSearchMethod.getChildCount(); i++){
                    radioGroupSearchMethod.getChildAt(i).setEnabled(true);
                }

                //Floating action button
                updateActionButtons();

                //Update the UI immediately
                updateUiNow();

                //Action view
                ((MainActivity) getActivity()).hideActionView(0);

                //Scrollbars
                //recyclerViewPrimes.setVerticalScrollBarEnabled(true);

                //Save button
                ((MainActivity) getActivity()).showMenuItemSave();
            }
        });
    }

   // @Override
    public void onTaskFinished(){

        handler.post(new Runnable(){
            @Override
            public void run(){

                //searchActiveView.setVisibility(View.VISIBLE);
                textViewSearchStatus.setText("Finished");

                //Progressbar
                progressBarInfinite.setVisibility(View.GONE);

                //Search range
                editTextSearchRangeStart.setEnabled(false);
                editTextSearchRangeEnd.setEnabled(true);

                //Floating action button
                updateActionButtons();

                //Update the UI immediately
                updateUiNow();

                //Action view
                ((MainActivity) getActivity()).hideActionView(0);

                //Scrollbars
                //recyclerViewPrimes.setVerticalScrollBarEnabled(true);

                //Save button
                ((MainActivity) getActivity()).showMenuItemSave();

                updateStatistics();

                //TEST STUFF
                final String start = NumberFormat.getInstance().format(getStartValue());
                final String end = NumberFormat.getInstance().format(getEndValue());
                //final String number = NumberFormat.getInstance().format(getNumberToFactor());
                final String primeCount = NumberFormat.getInstance().format(((FindPrimesTask) getTask()).getPrimeNumbers().size());
                final String string = getString(R.string.find_primes_result, primeCount, start, end);
                final SpannableString text = new SpannableString(string);
                final int firstIndexStart = string.indexOf(primeCount);
                final int firstIndexEnd = firstIndexStart + primeCount.length();
                text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.primary_text)), firstIndexStart, firstIndexEnd, 0);

                final int secondIndexStart = string.indexOf(start, firstIndexEnd);
                final int secondIndexEnd = secondIndexStart + start.length();
                text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.primary_text)), secondIndexStart, secondIndexEnd, 0);

                final int thirdIndexStart = string.indexOf(end, secondIndexEnd);
                final int thirdIndexEnd = thirdIndexStart + end.length();
                text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.primary_text)), thirdIndexStart, thirdIndexEnd, 0);
                //textViewResult.setVisibility(View.VISIBLE);
                //textViewResult.setText(text);
            }
        });
    }

   // @Override
    public void onProgressChanged(float percent){

        //Request a UI update
        requestUiUpdate();
    }

    @Override
    public void onPrimeFound(long prime){
        primesAdapter.getListNumbers().add(prime);
        requestUiUpdate();
    }

    @Override
    public void onSaveClicked(){

        Toast.makeText(getActivity(), "Saving file...", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable(){
            @Override
            public void run(){
                final boolean success = FileManager.getInstance(getActivity()).savePrimes(getStartValue(), ((FindPrimesTask) getTask()).getCurrentNumber(), ((FindPrimesTask) getTask()).getPrimeNumbers());
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

    //Initializations

    /**
     * Initialize all views and apply the default state. If there is a saved instance state bundle,
     * restore views to that state instead of the default state.
     *
     * @param rootView           The root view that contains all of the child views
     * @param savedInstanceState A bundle containing data from the last saved instance state, or
     *                           null if there was no previous state.
     */
    private void initViews(final View rootView, Bundle savedInstanceState){

        statisticsAdapter = new StatisticsAdapter(getFragmentManager());

        statisticsViewPager = (ViewPager) rootView.findViewById(R.id.statistics_viewpager);
        statisticsViewPager.setAdapter(statisticsAdapter);
        statisticsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){

            }

            @Override
            public void onPageSelected(int position){
                for (int i = 0; i < ((LinearLayout) rootView.findViewById(R.id.page_indicator)).getChildCount(); i++){
                    ((LinearLayout) rootView.findViewById(R.id.page_indicator)).getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                }
                ((LinearLayout) rootView.findViewById(R.id.page_indicator)).getChildAt(position).setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));
            }

            @Override
            public void onPageScrollStateChanged(int state){

            }
        });
        statisticsViewPager.setCurrentItem(1);
        statisticsViewPager.setCurrentItem(0);


        //TODO: Hide cursors on task start

      /*  Log.e(TAG, "progressBarCurrentProgress was " + progressBarCurrentProgress);
        Log.e(TAG, "textViewCurrentNumber was    " + textViewCurrentNumber);

        String text = "";

        if (textViewCurrentNumber != null){
            Log.e(TAG, "textViewCurrentNumber value was " + textViewCurrentNumber.getText());
            //textViewCurrentNumber.setText(textViewCurrentNumber.getText());
            text = textViewCurrentNumber.getText().toString();
            savedInstanceState = new Bundle();
        }*/

        //Define views
       /* if (textViewCurrentNumber == null)
            textViewCurrentNumber = (TextView) rootView.findViewById(R.id.textView_currentNumber);*/
        progressBarInfinite = (ProgressBar) rootView.findViewById(R.id.progressBar_infinite);
      //  progressBarCurrentProgress = (ProgressBar) rootView.findViewById(R.id.progressBar_currentProgress);
        //progressBarTotalProgress = (ProgressBar) rootView.findViewById(R.id.progressBar_totalProgress);
      //  textViewHighestPrime = (TextView) rootView.findViewById(R.id.textView_highest_prime);
      //  buttonViewAllPrimes = (Button) rootView.findViewById(R.id.button_view_all_primes);

       /* buttonViewAllPrimes.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.e(TAG, buttonViewAllPrimes.getText() + " Primes");
            }
        });*/

        //textViewCurrentNumber.setText(text);

       //Log.e(TAG, "progressBarCurrentProgress is now " + progressBarCurrentProgress);
       // Log.e(TAG, "textViewCurrentNumber is now " + textViewCurrentNumber);

       // viewPagerStatistics = (ViewPager) rootView.findViewById(R.id.viewPager_statistics);
        //viewPagerStatistics.setOffscreenPageLimit(2);
       // viewPagerStatistics.setAdapter(statisticsAdapter);
      /*  viewPagerStatistics.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){

            }

            @Override
            public void onPageSelected(int position){
                for (int i = 0; i < ((LinearLayout) rootView.findViewById(R.id.page_indicator)).getChildCount(); i++){
                    ((LinearLayout) rootView.findViewById(R.id.page_indicator)).getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                }
                ((LinearLayout) rootView.findViewById(R.id.page_indicator)).getChildAt(position).setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));
            }

            @Override
            public void onPageScrollStateChanged(int state){

            }
        });*/

        infinityButton = (ImageButton) rootView.findViewById(R.id.infinity_button);
        infinityButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                editTextSearchRangeEnd.setText("infinity");
            }
        });

        editTextSearchRangeStart.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void afterTextChanged(Editable editable){

                if (editTextSearchRangeStart.isEnabled()){
                    final String text = editable.toString();

                    final String numberString = text.replace(",", "");

                    try{
                        final long number = Long.valueOf(numberString);

                        final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(number);

                        if (!text.equals(formattedText)){
                            editTextSearchRangeStart.setText(formattedText);
                        }

                        editTextSearchRangeStart.setSelection(formattedText.length());
                    }catch (RuntimeException e){
                    }

                    if (isRangeValid()){
                        editTextSearchRangeStart.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));
                        editTextSearchRangeEnd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));
                    }else{
                        editTextSearchRangeStart.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.red)));
                    }
                }

            }
        });
        /*editTextSearchRangeStart.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    editTextSearchRangeStart.setText("");
                }
                return false;
            }
        });*/
        editTextSearchRangeEnd.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void afterTextChanged(Editable editable){

                final String text = editable.toString();

                final String numberString = text.replace(",", "");

                try{
                    final long number = Long.valueOf(numberString);

                    final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(number);

                    if (!text.equals(formattedText)){
                        editTextSearchRangeEnd.setText(formattedText);
                    }

                    editTextSearchRangeEnd.setSelection(formattedText.length());
                }catch (RuntimeException e){
                }

                if (isRangeValid()){
                    editTextSearchRangeStart.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));
                    editTextSearchRangeEnd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));
                }else{
                    editTextSearchRangeEnd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.red)));
                }
            }
        });
        editTextSearchRangeEnd.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){
                if (editTextSearchRangeEnd.getText().toString().equals("infinity") && motionEvent.getAction() == MotionEvent.ACTION_UP){
                    editTextSearchRangeEnd.setText("");
                }
                return false;
            }
        });

        //If the saved instance state is not null then restore it
        if (savedInstanceState != null){

            //Set the search range
            Log.e(TAG, "SavedInstanceState was not null.");

        }else{

            Log.e(TAG, "Apply defaults.");

            //Set defaults
            applyDefaults();

        }
    }

    public Bundle getSavedInstanceState(){
        final Bundle bundle = new Bundle();
        return bundle;
    }

    //User interface

    /**
     * Update the user interface only if the update limit has been passed.
     */
    private void requestUiUpdate(){

        //Check if the UI should be updated
        if (System.currentTimeMillis() - lastUpdateTimes[0] >= PrimeNumberFinder.UPDATE_LIMIT_MS){

            //Update the UI on the main thread
            handler.post(new Runnable(){
                @Override
                public void run(){
                    updateUi();
                }
            });

            //The last UI update was now
            lastUpdateTimes[0] = System.currentTimeMillis();

            //Log.e(TAG, "UI update");
        }
    }

    /**
     * Update all user interface components immediately.
     */
    private void updateUiNow(){
        //updateUiGroup0();
        //updateUiGroup1();
        //updateUiGroup2();
        updateUi();
    }

    /**
     * Update all user interface components. This will make sure each UI group's refresh is synced.
     */
    private void updateUi(){

        //if (getTask() != null && getTask().getState() != Task.State.PAUSED)
        //    textViewElapsedTime.setText(StatisticsFragment0.formatTime(getTask().getElapsedTime()));
        //textViewCurrentNumber.setText(NumberFormat.getInstance(Locale.getDefault()).format(((FindPrimesTask) getTask()).getCurrentNumber()));
       // progressBarCurrentProgress.setProgress((int) (((FindPrimesTask) getTask()).getCurrentProgress() * 100));
       // progressBarTotalProgress.setProgress((int) (getTask().getProgress() * 100));

        try{
         //   textViewHighestPrime.setText(NumberFormat.getInstance(Locale.getDefault()).format(((FindPrimesTask) getTask()).getPrimeNumbers().get(((FindPrimesTask) getTask()).getPrimeNumbers().size() - 1)));
        }catch (ArrayIndexOutOfBoundsException e){
        }

      //  buttonViewAllPrimes.setText("View all (" + NumberFormat.getInstance(Locale.getDefault()).format(primesAdapter.getListNumbers().size()) + ")");

        updateStatistics();

        updateUiGroup2();

       /* //Update UI group 2
        if (System.currentTimeMillis() - lastUpdateTimes[2] >= 500){
            updateUiGroup2();
            lastUpdateTimes[2] = System.currentTimeMillis();
        }*/

    }

    private void updateStatistics(){

        final StatisticData statisticData = new StatisticData();

        if (getTask() != null){
            try{
                statisticData.put(Statistic.TIME_ELAPSED, getTask().getElapsedTime());
                statisticData.put(Statistic.FACTORS_PER_SECOND, 0);
                statisticData.put(Statistic.NUMBERS_PER_SECOND, 0);
            }catch (JSONException e){
                e.printStackTrace();
            }

            ((StatisticsFragment0) statisticsAdapter.getItem(0)).setStatisticData(statisticData);
            ((StatisticsFragment1) statisticsAdapter.getItem(1)).setStatisticData(statisticData);
            ((StatisticsFragment2) statisticsAdapter.getItem(2)).setStatisticData(statisticData);
        }
    }

    /**
     * Update the views in group 2.
     */
    private void updateUiGroup2(){

        final int itemCount = primesAdapter.getItemCount();

        //Refresh adapter data and scroll to the bottom
        if (prevItemCount < itemCount){
            primesAdapter.notifyItemRangeInserted(prevItemCount, itemCount - prevItemCount);
            prevItemCount = itemCount;
            recyclerViewPrimes.scrollToPosition(itemCount - 1);
        }
    }

    //Button clicks

    private void playClicked(){

        //Check if the range is valid
        if (isRangeValid()){

            //Check if the runnable is null
            if (getTask() == null){

                //Create a new runnable
                searchOptions.setStartValue(getStartValue());
                searchOptions.setEndValue(getEndValue());
                setTask(new FindPrimesTask(searchOptions));
                PrimeNumberFinder.getTasks().put("findPrimes", getTask());
                //getTask().addTaskListener(this);
                getTask().addTaskListener(new TaskListener(){
                    @Override
                    public void onTaskStarted(){
                        FindPrimesFragment.this.onTaskStarted();
                    }

                    @Override
                    public void onTaskPaused(){
FindPrimesFragment.this.onTaskPaused();
                    }

                    @Override
                    public void onTaskResumed(){
FindPrimesFragment.this.onTaskResumed();
                    }

                    @Override
                    public void onTaskStopped(){
FindPrimesFragment.this.onTaskStopped();
                    }

                    @Override
                    public void onTaskFinished(){
FindPrimesFragment.this.onTaskFinished();
                    }

                    @Override
                    public void onProgressChanged(float percent){
FindPrimesFragment.this.onProgressChanged(percent);
                    }
                });
                ((FindPrimesTask) getTask()).addEventListener(this);

                //Start the runnable in a new thread
                new Thread(getTask()).start();

            }else{

                //Switch based on state
                switch (getTask().getState()){

                    case RUNNING:
                        getTask().pause();
                        break;

                    case PAUSED:
                        ((FindPrimesTask) getTask()).setEndValue(getEndValue());
                        getTask().resume();
                        break;
                }
            }
        }else{
            //Invalid range
            Toast.makeText(getActivity(), "Invalid range", Toast.LENGTH_SHORT).show();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void pauseClicked(){
        getTask().pause();
    }

    //Utility methods

    @Override
    protected void reset(){
        setTask(null);
        PrimeNumberFinder.getTasks().remove("findPrimes");

        applyDefaults();

        lastUpdateTimes[1] = 0;

        //Floating action button
        updateActionButtons();

        //Save button
        ((MainActivity) getActivity()).hideMenuItemSave();

        updateStatistics();
    }

    private void applyDefaults(){

        switch (searchOptions.getSearchMethod()){

            case BRUTE_FORCE:
                editTextSearchRangeStart.setText("0");
                editTextSearchRangeStart.setEnabled(true);
                editTextSearchRangeEnd.setText("infinity");
                editTextSearchRangeEnd.setEnabled(true);
                infinityButton.setEnabled(true);
                infinityButton.setAlpha(1f);
                break;

            case SIEVE_OF_ERATOSTHENES:
                editTextSearchRangeStart.setText("0");
                editTextSearchRangeStart.setEnabled(false);
                editTextSearchRangeEnd.setText(NumberFormat.getInstance().format(1000000));
                editTextSearchRangeEnd.setEnabled(true);
                infinityButton.setEnabled(false);
                infinityButton.setAlpha(0.3f);
                break;
        }

        cardViewResults.setVisibility(View.INVISIBLE);

        editTextSearchRangeStart.setCustomSelectionActionModeCallback(customActionModeCallback());
        editTextSearchRangeEnd.setCustomSelectionActionModeCallback(customActionModeCallback());

        for (int i = 0; i < radioGroupSearchMethod.getChildCount(); i++){
            radioGroupSearchMethod.getChildAt(i).setEnabled(true);
        }


        //textViewElapsedTime.setText(StatisticsFragment0.formatTime(0));
      //  textViewCurrentNumber.setText("-");
        progressBarInfinite.setVisibility(View.GONE);
       // progressBarCurrentProgress.setProgress(0);
        //progressBarTotalProgress.setVisibility(View.VISIBLE);
       // progressBarTotalProgress.setProgress(0);

        // textViewPrimesLabel.setText("Prime numbers");
        final int adapterSize = primesAdapter.getListNumbers().size();
        primesAdapter.getListNumbers().clear();
        primesAdapter.notifyItemRangeRemoved(0, adapterSize);
        //textViewHighestPrime.setText("-");
        //buttonViewAllPrimes.setText("View all (0)");
        //searchActiveView.setVisibility(View.VISIBLE);
        textViewSearchStatus.setText(getString(R.string.status_ready));


        //textViewResult.setVisibility(View.INVISIBLE);


    }

    private long getStartValue(){
        final BigInteger startValue = new BigInteger(editTextSearchRangeStart.getText().toString().replace(",", ""));

        if (startValue.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1){
            return -1;
        }

        return startValue.longValue();
    }

    private long getEndValue(){

        final String endValueString = editTextSearchRangeEnd.getText().toString();

        if (endValueString.equals("infinity")){
            return /*LIMIT_NO_LIMIT*/ -1;
        }

        final BigInteger endValue = new BigInteger(editTextSearchRangeEnd.getText().toString().replace(",", ""));

        if (endValue.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1){
            return -2;
        }

        return endValue.longValue();
    }

    private BigInteger getPrimalityInput(){
        final String primalityInputString = editTextPrimalityInput.getText().toString().replace(",", "").trim();

        if (primalityInputString.length() > 0){
            return new BigInteger(primalityInputString);
        }else{
            return BigInteger.ZERO;
        }
    }

    private boolean isRangeValid(){

        //Validate the start value
        try{
            final long startValue = getStartValue();
            final long endValue = getEndValue();

            //The start value must be at least 0
            if (startValue < 0){
                return false;
            }

            Log.e(TAG, "Start: " + startValue);
            Log.e(TAG, "End: " + endValue);

            if (searchOptions.getSearchMethod() == FindPrimesTask.SearchOptions.Method.SIEVE_OF_ERATOSTHENES){
                if (startValue != 0) return false;
            }

            if (searchOptions.getSearchMethod() == FindPrimesTask.SearchOptions.Method.SIEVE_OF_ERATOSTHENES){
                if (endValue == -1) return false;
            }

            if (endValue != /*LIMIT_NO_LIMIT*/ -1){

                //The start value must be less than the end value
                if (startValue > endValue){
                    return false;
                }

                //The end value must be greater than the current number
                if (getTask() != null && getTask() instanceof FindPrimesTask){
                    if (endValue <= ((FindPrimesTask) getTask()).getCurrentNumber()){
                        return false;
                    }
                }
            }

        }catch (NumberFormatException e){
            return false;
        }

        return true;
    }
}
