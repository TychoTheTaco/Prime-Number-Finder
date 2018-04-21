package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
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

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.FabAnimator;
import com.tycho.app.primenumberfinder.FloatingActionButtonListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.MainActivity;
import com.tycho.app.primenumberfinder.adapters.FragmentAdapter;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.adapters.PrimeFactorizationTaskListAdapter;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import easytasks.Task;
import easytasks.TaskAdapter;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 * Date Created: 3/2/2017
 */

public class PrimeFactorizationFragment extends Fragment implements FloatingActionButtonListener{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "PrimeFactorizationFrgmt";

    /**
     * All UI updates are posted to this {@link Handler} on the main thread.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    private EditText editTextInput;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private Button buttonFactorize;

    private final PrimeFactorizationTaskListFragment taskListFragment = new PrimeFactorizationTaskListFragment();
    private final PrimeFactorizationResultsFragment resultsFragment = new PrimeFactorizationResultsFragment();

    private final PrimeFactorizationTask.SearchOptions searchOptions = new PrimeFactorizationTask.SearchOptions(0);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.prime_factorization_fragment, container, false);

        //Set up tab layout for results and statistics
        final FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager());
        final ViewPager viewPager = rootView.findViewById(R.id.view_pager);
        fragmentAdapter.add("Tasks", taskListFragment);
        taskListFragment.addEventListener(new PrimeFactorizationTaskListAdapter.EventListener() {
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
                final Intent intent = new Intent(getActivity(), PrimeFactorizationConfigurationActivity.class);
                intent.putExtra("searchOptions", ((PrimeFactorizationTask) task).getSearchOptions());
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
        editTextInput = (EditText) rootView.findViewById(R.id.editText_input_number);
        editTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                //Check if the number is valid
                if (isNumberValid()) {
                    editTextInput.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));

                    final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(getNumberToFactor());
                    if (!editable.toString().equals(formattedText)) {
                        editTextInput.setText(formattedText);
                    }
                    editTextInput.setSelection(formattedText.length());

                } else {
                    editTextInput.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.red)));
                }
            }
        });
        editTextInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                editTextInput.getText().clear();
                return false;
            }

        });

        //Set up start button
        buttonFactorize = rootView.findViewById(R.id.button_generate_factor_tree);
        buttonFactorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if the number is valid
                if (isNumberValid()) {

                    //Create a new task
                    searchOptions.setNumber(getNumberToFactor());
                    startTask(searchOptions);

                    hideKeyboard(getActivity());

                } else {
                    Toast.makeText(getActivity(), "Invalid number", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return rootView;
    }

    private boolean isNumberValid() {

        try {

            final long number = getNumberToFactor();

            //The number must be greater than 0
            if (number <= 0) {
                return false;
            }

        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private long getNumberToFactor() {
        final BigInteger number = new BigInteger(editTextInput.getText().toString().replace(",", ""));

        if (number.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1) {
            return -1;
        }

        return number.longValue();
    }

    public void addActionViewListener(final ActionViewListener actionViewListener) {
        taskListFragment.addActionViewListener(actionViewListener);
    }

    private static final int REQUEST_CODE_NEW_TASK = 0;

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), PrimeFactorizationConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final PrimeFactorizationTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    final PrimeFactorizationTask task = (PrimeFactorizationTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    if (task == null) {
                        startTask(searchOptions);
                    } else {
                        task.setSearchOptions(searchOptions);
                    }
                }
                break;
        }
    }

    private void startTask(final PrimeFactorizationTask.SearchOptions searchOptions){
        final PrimeFactorizationTask task = new PrimeFactorizationTask(searchOptions);
        task.addTaskListener(new TaskAdapter() {

            @Override
            public void onTaskStopped() {

                //Auto-save
                if (task.getSearchOptions().isAutoSave()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean success = FileManager.getInstance().saveTree(task.getFactorTree());
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
                            .setContentText("Task \"Prime factorization of " + NUMBER_FORMAT.format(task.getNumber()) + "\" finished.")
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
}
