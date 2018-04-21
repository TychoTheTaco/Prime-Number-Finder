package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.FabAnimator;
import com.tycho.app.primenumberfinder.FloatingActionButtonListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.activities.MainActivity;
import com.tycho.app.primenumberfinder.adapters.FragmentAdapter;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FindFactorsTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.Task;
import easytasks.TaskAdapter;
import easytasks.TaskListener;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * This {@linkplain Fragment} allows the user to input a number they want to factor. This fragment
 * will display the progress and statistics of the factorization, along with a list of factors that
 * are found.
 *
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public class FindFactorsFragment extends Fragment implements FloatingActionButtonListener{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsFragment";

    /**
     * All UI updates are posted to this {@link Handler} on the main thread.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    /**
     * All views
     */
    private EditText editTextNumberToFactor;
    private Button buttonFindFactors;

    private final FindFactorsTask.SearchOptions searchOptions = new FindFactorsTask.SearchOptions(0);

    private final FindFactorsTaskListFragment taskListFragment = new FindFactorsTaskListFragment();
    private final FindFactorsResultsFragment resultsFragment = new FindFactorsResultsFragment();

    //Override methods

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_factors_fragment, viewGroup, false);

        //Set up tab layout for results and statistics
        final FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager());
        final ViewPager viewPager = rootView.findViewById(R.id.view_pager);
        fragmentAdapter.add("Tasks", taskListFragment);
        taskListFragment.addEventListener(new FindFactorsTaskListAdapter.EventListener() {
            @Override
            public void onTaskSelected(Task task) {
                resultsFragment.setTask(task);
            }

            @Override
            public void onPausePressed(Task task) {

            }

            @Override
            public void onTaskRemoved(Task task) {
                if (resultsFragment.getTask() == task) {
                    resultsFragment.setTask(null);
                }

                taskListFragment.update();
            }

            @Override
            public void onEditPressed(Task task) {
                final Intent intent = new Intent(getActivity(), FindFactorsConfigurationActivity.class);
                intent.putExtra("searchOptions", ((FindFactorsTask) task).getSearchOptions());
                intent.putExtra("taskId", task.getId());
                startActivityForResult(intent, 0);
            }
        });
        fragmentAdapter.add("Results", resultsFragment);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.addOnPageChangeListener(new FabAnimator(((MainActivity) getActivity()).getFab()));
        final TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        //Set up factor input
        editTextNumberToFactor = rootView.findViewById(R.id.editText_input_number);
        editTextNumberToFactor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                //Check if the number is valid
                if (Validator.isValidFactorInput(getNumberToFactor())) {
                    editTextNumberToFactor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));

                    final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(getNumberToFactor());
                    if (!editable.toString().equals(formattedText)) {
                        editTextNumberToFactor.setText(formattedText);
                    }
                    editTextNumberToFactor.setSelection(formattedText.length());

                } else {
                    editTextNumberToFactor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.red)));
                }
            }
        });
        editTextNumberToFactor.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                editTextNumberToFactor.getText().clear();
                return false;
            }
        });

        //Set up start button
        buttonFindFactors = rootView.findViewById(R.id.button_find_factors);
        buttonFindFactors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if the number is valid
                if (Validator.isValidFactorInput(getNumberToFactor())) {

                    //Create a new task
                    searchOptions.setNumber(getNumberToFactor().longValue());
                    try {
                        startTask((FindFactorsTask.SearchOptions) searchOptions.clone());
                    }catch (CloneNotSupportedException e){
                        e.printStackTrace();
                    }

                    hideKeyboard(getActivity());
                    taskListFragment.scrollToBottom();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private static final int REQUEST_CODE_NEW_TASK = 0;

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), FindFactorsConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final FindFactorsTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    final FindFactorsTask task = (FindFactorsTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    if (task == null) {
                        startTask(searchOptions);
                    } else {
                        task.setSearchOptions(searchOptions);
                    }
                }
                break;
        }
    }

    private void startTask(final FindFactorsTask.SearchOptions searchOptions){
        final FindFactorsTask task = new FindFactorsTask(searchOptions);
        task.addTaskListener(new TaskAdapter() {

            @Override
            public void onTaskStopped() {

                //Auto-save
                if (task.getSearchOptions().isAutoSave()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean success = FileManager.getInstance().saveFactors(task.getFactors(), task.getNumber());
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), success ? getString(R.string.successfully_saved_file) : getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }

                //Notify when finished
                if (task.getSearchOptions().isNotifyWhenFinished()) {
                    final String CHANNEL_ID = "default";

                    //Create notification
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.circle_white)
                            .setContentTitle("Task Finished")
                            .setContentText("Task \"Factors of " + NUMBER_FORMAT.format(task.getNumber()) + "\" finished.")
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    //Register notification channel
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Default", NotificationManager.IMPORTANCE_DEFAULT);
                        channel.setDescription("Default notification channel.");
                        final NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.createNotificationChannel(channel);
                    }

                    final NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());
                }
                task.removeTaskListener(this);
            }
        });
        taskListFragment.addTask(task);
        PrimeNumberFinder.getTaskManager().registerTask(task);

        //Start the task
        task.startOnNewThread();

        //Post to a handler because "java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState"
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                taskListFragment.setSelected(task);
            }
        });
    }

    private BigInteger getNumberToFactor() {
        if (editTextNumberToFactor.getText().length() > 0){
            return new BigInteger(editTextNumberToFactor.getText().toString().replace(",", ""));
        }
        return BigInteger.valueOf(-1);
    }

    public void addActionViewListener(final ActionViewListener actionViewListener){
        taskListFragment.addActionViewListener(actionViewListener);
    }
}
