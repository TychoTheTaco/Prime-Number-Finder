package com.tycho.app.primenumberfinder.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.FloatingActionButtonHost;
import com.tycho.app.primenumberfinder.FloatingActionButtonListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.about.AboutPageFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.fragments.FindFactorsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.fragments.FindPrimesFragment;
import com.tycho.app.primenumberfinder.modules.lcm.LeastCommonMultipleTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.fragments.PrimeFactorizationFragment;
import com.tycho.app.primenumberfinder.modules.savedfiles.SavedFilesFragment;
import com.tycho.app.primenumberfinder.settings.SettingsFragment;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.OneToOneMap;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 * Date Created: 1/10/2016
 */
public class MainActivity extends AbstractActivity implements FloatingActionButtonHost, ActionViewListener {

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
     * This is the main {@linkplain FloatingActionButton} of the activity. It can be used by any
     * fragments that implement the {@linkplain FloatingActionButtonListener} interface.
     */
    private FloatingActionButton floatingActionButton;

    /**
     * Maps drawer item ids to the corresponding fragment tag. The ids and tags are used to find
     * the corresponding fragment when a new drawer item is selected.
     */
    private OneToOneMap<Integer, String> fragmentIds = new OneToOneMap<>();

    private final int defaultDrawerIconTint = Color.BLACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
            //Apply icon tint
            /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                navigationView.setItemIconTintList(createColorStateList(Color.BLACK, ContextCompat.getColor(this, R.color.accent)));
            }*/

            //Hide action view
            try {
                setActionViewVisibility(navigationView.getMenu().getItem(i), false);
            } catch (NullPointerException e) {
                //Ignore NPE because there is no action view
            }
        }
        navigationView.setNavigationItemSelectedListener(item -> {
            selectDrawerItem(item);
            return true;
        });

        //Set up drawer icon animation
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        //Set up floating action button
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> {
            if (currentFragment instanceof FloatingActionButtonListener) {
                ((FloatingActionButtonListener) currentFragment).onClick(v);
            }
        });

        //Select the correct drawer item
        if (savedInstanceState != null) {
            //Restore fragment
            final String currentFragmentTag = savedInstanceState.getString("currentFragmentTag");
            currentFragment = getFragment(currentFragmentTag);
            selectDrawerItem(navigationView.getMenu().findItem(fragmentIds.getKey(currentFragment.getTag())));
        } else {
            selectDrawerItem(getIntent().getIntExtra("taskType", 0));
        }

        //Show a dialog while upgrading to the newest version
        if (PreferenceManager.getInt(PreferenceManager.Preference.FILE_VERSION) < PreferenceManager.CURRENT_VERSION) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Updating...");
            progressDialog.show();

            new Thread(() -> {
                //Update file system
                FileManager.getInstance().updateFileSystem(getBaseContext());
                FileManager.getInstance().upgradeTo1_3_0();

                progressDialog.dismiss();
            }).start();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentFragmentTag", currentFragment.getTag());
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

    @Override
    public void onTaskStatesChanged(final int taskType, final boolean active) {
        if (navigationView != null) {
            navigationView.post(() -> setActionViewVisibility(navigationView.getMenu().getItem(taskType), active));
        }
    }

    /**
     * Change the visibility of the navigation view's action view.
     * @param menuItem The menu item with an action view.
     * @param visible
     */
    private void setActionViewVisibility(final MenuItem menuItem, final boolean visible){
        menuItem.getActionView().setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Get the fragment with the corresponding tag. This will create a new instance of the fragment
     * if it does not yet exist in the fragment transaction. Note this method will not add it to the
     * fragment transaction meaning if it is called twice with the same tag before the returned
     * fragment was added, it will return two different instances.
     *
     * @param tag The fragment tag.
     * @return A fragment with the corresponding tag.
     */
    private Fragment getFragment(final String tag) {
        //Check if fragment exists already
        if (getSupportFragmentManager().findFragmentByTag(tag) == null) {
            switch (tag) {
                case "findPrimes":
                    return new FindPrimesFragment();

                case "findFactors":
                    return new FindFactorsFragment();

                case "primeFactorization":
                    return new PrimeFactorizationFragment();

                case "savedFiles":
                    return new SavedFilesFragment();

                case "settings":
                    return new SettingsFragment();

                case "about":
                    return new AboutPageFragment();
            }
        }

        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    private void selectDrawerItem(final MenuItem menuItem) {

        //Start the fragment transaction
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        //Hide the existing fragment
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment);
        }

        //Get the new fragment
        currentFragment = getFragment(fragmentIds.get(menuItem.getItemId()));

        //Add the new fragment if it doesn't exist yet
        if (getSupportFragmentManager().findFragmentByTag(fragmentIds.get(menuItem.getItemId())) == null) {
            fragmentTransaction.add(R.id.main_content_frame, currentFragment, fragmentIds.get(menuItem.getItemId()));
        }

        //Show the new fragment
        fragmentTransaction.show(currentFragment);

        //Commit changes
        fragmentTransaction.commitNow();
        menuItem.setChecked(true);

        //Tell the fragment to initialize the floating action button
        if (currentFragment instanceof FloatingActionButtonListener) {
            ((FloatingActionButtonListener) currentFragment).initFab(floatingActionButton);
        } else {
            floatingActionButton.setVisibility(View.GONE);
        }

        //Apply theme to activity based on current fragment
        switch (menuItem.getItemId()) {

            default:
                setTitle(fragmentIds.get(menuItem.getItemId()));
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, ContextCompat.getColor(this, R.color.accent)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.accent_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.primary));
                break;

            case R.id.drawer_item_find_primes:
                setTitle(getString(R.string.title_find_primes));
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, ContextCompat.getColor(this, R.color.purple)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.purple_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));
                break;

            case R.id.drawer_item_find_factors:
                setTitle(getString(R.string.title_find_factors));
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, ContextCompat.getColor(this, R.color.orange)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.orange_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.orange_dark), ContextCompat.getColor(this, R.color.orange));
                break;

            case R.id.drawer_item_factor_tree:
                setTitle(getString(R.string.title_factor_tree));
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, ContextCompat.getColor(this, R.color.green)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.green_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.green_dark), ContextCompat.getColor(this, R.color.green));
                break;

            case R.id.drawer_item_saved_files:
                setTitle(getString(R.string.title_saved_files));
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, ContextCompat.getColor(this, R.color.accent)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.accent_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.primary));
                break;

            case R.id.drawer_item_settings:
                setTitle(getString(R.string.title_settings));
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, ContextCompat.getColor(this, R.color.accent)));
                navigationView.setItemTextColor(createColorStateList(ContextCompat.getColor(this, R.color.primary_text), ContextCompat.getColor(this, R.color.accent_dark)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.primary));
                break;

            case R.id.drawer_item_about:
                setTitle(getString(R.string.title_about));
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, ContextCompat.getColor(this, R.color.accent)));
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
