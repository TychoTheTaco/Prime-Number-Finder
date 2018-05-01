package com.tycho.app.primenumberfinder.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.FloatingActionButtonHost;
import com.tycho.app.primenumberfinder.FloatingActionButtonListener;
import com.tycho.app.primenumberfinder.IntentReceiver;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.about.AboutPageFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.fragments.FindFactorsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.fragments.FindPrimesFragment;
import com.tycho.app.primenumberfinder.modules.primefactorization.fragments.PrimeFactorizationFragment;
import com.tycho.app.primenumberfinder.modules.savedfiles.SavedFilesFragment;
import com.tycho.app.primenumberfinder.settings.SettingsFragment;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

import static com.tycho.app.primenumberfinder.utils.NotificationManager.REQUEST_CODE_FIND_FACTORS;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.REQUEST_CODE_FIND_PRIMES;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.REQUEST_CODE_PRIME_FACTORIZATION;
import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 * Date Created: 1/10/2016
 */
public class MainActivity extends AbstractActivity implements FloatingActionButtonHost {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The main {@link DrawerLayout} of this activity.
     */
    private DrawerLayout drawerLayout;

    /**
     * The {@link NavigationView} of {@link #drawerLayout}.
     */
    private NavigationView navigationView;

    /**
     * The current fragment being displayed.
     */
    private Fragment currentFragment;

    /**
     * This handler is used for posting to the UI thread.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * This is the main {@linkplain FloatingActionButton} of the activity. It can be used by any
     * fragments that implement the {@linkplain FloatingActionButtonListener} interface.
     */
    private FloatingActionButton floatingActionButton;

    /**
     * Maps drawer item ids to the corresponding fragment tag. The ids and tags are used to find
     * the corresponding fragment when a new drawer item is selected. This is a {@linkplain Map}
     * instead of a {@linkplain android.util.SparseArray} because fragment ID's can be any integer
     * value, meaning a {@linkplain android.util.SparseArray} would cause a lot of wasted space for
     * fragments with large ID's.
     */
    private Map<Integer, String> fragmentIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize analytics
        if (PrimeNumberFinder.getPreferenceManager().isAllowAnalytics()) {
            Fabric.with(this, new Crashlytics());
        }else{
            Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().build()).build());
        }

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Initialize the fragment IDs
        fragmentIds.put(R.id.drawer_item_find_primes, "findPrimes");
        fragmentIds.put(R.id.drawer_item_find_factors, "findFactors");
        fragmentIds.put(R.id.drawer_item_factor_tree, "primeFactorization");
        fragmentIds.put(R.id.drawer_item_saved_files, "savedFiles");
        fragmentIds.put(R.id.drawer_item_settings, "settings");
        fragmentIds.put(R.id.drawer_item_about, "about");

        //Set up navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.app_version)).setText(getString(R.string.app_version_name, PrimeNumberFinder.getVersionName(this)));
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            try {
                setActionViewVisibility(i, View.GONE);
            } catch (NullPointerException e) {
                //Ignore NPE because there is no action view
            }
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });

        //Set up floating action button
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFragment instanceof FloatingActionButtonListener) {
                    ((FloatingActionButtonListener) currentFragment).onClick(v);
                }
            }
        });

        //Select the correct drawer item
        final int taskType = getIntent().getIntExtra("taskType", 0);
        switch (taskType) {
            default:
                selectDrawerItem(0);
                break;

            case REQUEST_CODE_FIND_PRIMES:
                selectDrawerItem(0);
                break;

            case REQUEST_CODE_FIND_FACTORS:
                selectDrawerItem(1);
                break;

            case REQUEST_CODE_PRIME_FACTORIZATION:
                selectDrawerItem(2);
                break;
        }

        //Show a dialog while upgrading to the newest version
        if (PrimeNumberFinder.getPreferenceManager().getFileVersion() < PreferenceManager.CURRENT_VERSION) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Updating...");
            progressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Update file system
                    FileManager.getInstance().updateFileSystem(getBaseContext());
                    FileManager.getInstance().upgradeTo1_3_0();

                    progressDialog.dismiss();
                }
            }).start();
        }

        //FileManager.saveDebugFile(1, 10_000, new File(FileManager.getInstance().getSavedPrimesDirectory() + File.separator + "debug"));
    }

    @Override
    public void onBackPressed() {
        //Close the drawer if it was open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            //Menu button
            case android.R.id.home:
                hideKeyboard(this);

                //Show or hide the drawer
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                break;
        }
        return true;
    }

    @Override
    public FloatingActionButton getFab(int index) {
        if (index == 0) {
            return this.floatingActionButton;
        }
        return null;
    }

    /**
     * Change the visibility of a menu item's action view.
     *
     * @param index      The index of the {@linkplain MenuItem}.
     * @param visibility The visibility to set.
     */
    private void setActionViewVisibility(final int index, final int visibility) {
        navigationView.getMenu().getItem(index).getActionView().setVisibility(visibility);
    }

    /**
     * Get the fragment with the specified ID. If the fragment has not been created yet, it will
     * create it now.
     *
     * @param id The ID of the fragment.
     * @return The fragment with the corresponding ID.
     */
    private Fragment getFragment(int id) {
        //Check if fragment exists already
        if (getFragmentManager().findFragmentByTag(fragmentIds.get(id)) == null) {
            switch (id) {
                case R.id.drawer_item_find_primes:
                    final FindPrimesFragment findPrimesFragment = new FindPrimesFragment();
                    findPrimesFragment.addActionViewListener(getActionViewListener(0));
                    return findPrimesFragment;
                case R.id.drawer_item_find_factors:
                    final FindFactorsFragment findFactorsFragment = new FindFactorsFragment();
                    findFactorsFragment.addActionViewListener(getActionViewListener(1));
                    return findFactorsFragment;
                case R.id.drawer_item_factor_tree:
                    final PrimeFactorizationFragment primeFactorizationFragment = new PrimeFactorizationFragment();
                    primeFactorizationFragment.addActionViewListener(getActionViewListener(2));
                    return primeFactorizationFragment;
                case R.id.drawer_item_saved_files:
                    return new SavedFilesFragment();
                case R.id.drawer_item_settings:
                    return new SettingsFragment();
                case R.id.drawer_item_about:
                    return new AboutPageFragment();
            }
        }

        return getSupportFragmentManager().findFragmentByTag(fragmentIds.get(id));
    }

    private ActionViewListener getActionViewListener(final int index) {
        return new ActionViewListener() {
            @Override
            public void onTaskStatesChanged(final boolean active) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setActionViewVisibility(index, active ? View.VISIBLE : View.GONE);
                    }
                });
            }
        };
    }

    private void selectDrawerItem(final MenuItem menuItem) {

        //Start the fragment transaction
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        //Hide the existing fragment
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment);
        }

        //Get the new fragment
        currentFragment = getFragment(menuItem.getItemId());

        //Add the new fragment if it doesn't exist yet
        if (getFragmentManager().findFragmentByTag(fragmentIds.get(menuItem.getItemId())) == null) {
            fragmentTransaction.add(R.id.main_content_frame, currentFragment, fragmentIds.get(menuItem.getItemId()));
        }

        //Show the new fragment
        fragmentTransaction.show(currentFragment);

        //Commit changes
        fragmentTransaction.commit();
        menuItem.setChecked(true);

        //Tell the fragment to initialize the floating action button
        if (currentFragment instanceof FloatingActionButtonListener) {
            ((FloatingActionButtonListener) currentFragment).initFab(floatingActionButton);
        } else {
            floatingActionButton.setVisibility(View.GONE);
        }

        //Give Intent to the fragment
        if (currentFragment instanceof IntentReceiver) {
            ((IntentReceiver) currentFragment).giveIntent(getIntent());
        }

        //Apply theme to activity based on current fragment
        switch (menuItem.getItemId()) {

            default:
                setTitle(fragmentIds.get(menuItem.getItemId()));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.accent)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.accent_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.primary));
                break;

            case R.id.drawer_item_find_primes:
                setTitle(getString(R.string.title_find_primes));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.purple)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.purple_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));
                break;

            case R.id.drawer_item_find_factors:
                setTitle(getString(R.string.title_find_factors));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.orange)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.orange_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.orange_dark), ContextCompat.getColor(this, R.color.orange));
                break;

            case R.id.drawer_item_factor_tree:
                setTitle(getString(R.string.title_factor_tree));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.green)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.green_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.green_dark), ContextCompat.getColor(this, R.color.green));
                break;

            case R.id.drawer_item_saved_files:
                setTitle(getString(R.string.title_saved_files));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.accent)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.accent_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.primary));
                break;

            case R.id.drawer_item_settings:
                setTitle(getString(R.string.title_settings));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.accent)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.accent_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.primary));
                break;

            case R.id.drawer_item_about:
                setTitle(getString(R.string.title_about));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.accent)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.accent_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.primary));
                break;
        }

        //Close the drawer
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void selectDrawerItem(final int index) {
        selectDrawerItem(navigationView.getMenu().getItem(index));
    }

    private ColorStateList createColorStateList(final int defaultColor, final int selectedColor) {
        return new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}},
                new int[]{
                        defaultColor,
                        selectedColor
                });
    }
}
