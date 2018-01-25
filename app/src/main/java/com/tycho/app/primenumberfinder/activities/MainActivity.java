package com.tycho.app.primenumberfinder.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.Fragments_old.AboutPageFragment;
import com.tycho.app.primenumberfinder.settings.SettingsFragment;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findfactors.fragments.FindFactorsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.fragments.FindPrimesFragment;
import com.tycho.app.primenumberfinder.modules.primefactorization.fragments.PrimeFactorizationFragment;
import com.tycho.app.primenumberfinder.modules.savedfiles.SavedFilesFragment;

import java.util.HashMap;
import java.util.Map;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 *         Date Created: 1/10/2016
 */
public class MainActivity extends AppCompatActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "MainActivity";

    /**
     * The main {@link DrawerLayout} of this activity.
     */
    private DrawerLayout drawerLayout;

    /**
     * The {@link NavigationView} of {@link #drawerLayout}.
     */
    private NavigationView navigationView;

    /**
     * The {@link Menu} of this activity (typically the {@link ActionBar}). This reference is used for dynamically adding and
     * removing items.
     */
    private Menu menu;

    /**
     * The current fragment being displayed.
     */
    private Fragment currentFragment;

    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Maps drawer item ids to the corresponding fragment tag. The ids and tags are used to find
     * the corresponding fragment when a new drawer item is selected.
     */
    private Map<Integer, String> fragmentIds = new HashMap<>();
    //TODO: This should be a map instead because these IDs are high numbers, the VM needs to create an array of lots of wasted space

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Initialize the fragment IDs
        fragmentIds.put(R.id.drawer_item_find_primes, "findPrimes");
        fragmentIds.put(R.id.drawer_item_find_factors, "findFactors");
        fragmentIds.put(R.id.drawer_item_saved_files, "savedFiles");
        fragmentIds.put(R.id.drawer_item_settings, "settings");
        fragmentIds.put(R.id.drawer_item_about, "about");

        //Set up navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.app_version)).setText(getString(R.string.app_version_name, PrimeNumberFinder.getVersionName(this)));
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            setActionViewVisibility(i, View.GONE);
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });

        //Select the first drawer item
        selectDrawerItem(0);
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

        //Inflate the menu
        getMenuInflater().inflate(R.menu.main, menu);

        //Assign menu
        this.menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Switch based on item ID
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

    /**
     * Change the visibility of a menu item's action view.
     * @param index
     * @param visibility
     */
    private void setActionViewVisibility(final int index, final int visibility){
        try {
            navigationView.getMenu().getItem(index).getActionView().setVisibility(visibility);
        }catch (NullPointerException e){
            //Ignore exception
        }
    }

    private Fragment getFragment(int id) {

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

        return getFragmentManager().findFragmentByTag(fragmentIds.get(id));
    }

    private ActionViewListener getActionViewListener(final int index){
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

    private void selectDrawerItem(final int index) {
        selectDrawerItem(navigationView.getMenu().getItem(index));
    }

    private void selectDrawerItem(final MenuItem menuItem) {

        //Get fragment transaction object
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

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

        //Switch based on item id
        switch (menuItem.getItemId()) {

            default:
                setTitle(fragmentIds.get(menuItem.getItemId()));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.accent)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.accent_dark)));
                applyThemeColor(ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.primary));
                break;

            case R.id.drawer_item_find_primes:
                setTitle(getString(R.string.title_scan_for_primes));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.purple)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.purple_dark)));
                applyThemeColor(ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));
                break;

            case R.id.drawer_item_find_factors:
                setTitle(getString(R.string.title_find_factors));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.orange)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.orange_dark)));
                applyThemeColor(ContextCompat.getColor(this, R.color.orange_dark), ContextCompat.getColor(this, R.color.orange));
                break;

            case R.id.drawer_item_factor_tree:
                setTitle(getString(R.string.title_factor_tree));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.green)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.green_dark)));
                applyThemeColor(ContextCompat.getColor(this, R.color.green_dark), ContextCompat.getColor(this, R.color.green));
                break;

            case R.id.drawer_item_saved_files:
                setTitle(getString(R.string.title_saved_files));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.accent)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.accent_dark)));
                applyThemeColor(ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.primary));
                break;

            case R.id.drawer_item_settings:
                setTitle(getString(R.string.title_settings));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.accent)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.accent_dark)));
                applyThemeColor(ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.primary));
                break;

            case R.id.drawer_item_about:
                setTitle(getString(R.string.title_about));
                navigationView.setItemIconTintList(createColorStateList(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.accent)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.accent_dark)));
                applyThemeColor(ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.primary));
                break;
        }

        //Close the drawer
        drawerLayout.closeDrawer(GravityCompat.START);
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

    private void applyThemeColor(final int statusBarColor, final int actionBarColor) {

        //Set status bar color
        getWindow().setStatusBarColor(statusBarColor);

        //Set actionbar color
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrimeNumberFinder.getTaskManager().resumeAllTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!PrimeNumberFinder.getPreferenceManager().isAllowBackgroundTasks()) {
            PrimeNumberFinder.getTaskManager().pauseAllTasks();
        }
    }
}
