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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.FloatingActionButtonHost;
import com.tycho.app.primenumberfinder.FloatingActionButtonListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.about.AboutPageFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.fragments.FindFactorsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.fragments.FindPrimesFragment;
import com.tycho.app.primenumberfinder.modules.gcf.fragments.GreatestCommonFactorFragment;
import com.tycho.app.primenumberfinder.modules.lcm.fragments.LeastCommonMultipleFragment;
import com.tycho.app.primenumberfinder.modules.primefactorization.fragments.PrimeFactorizationFragment;
import com.tycho.app.primenumberfinder.modules.savedfiles.SavedFilesFragment;
import com.tycho.app.primenumberfinder.settings.SettingsFragment;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.util.ArrayList;
import java.util.List;

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
     * The current {@link Fragment} being displayed.
     */
    private Fragment currentFragment;

    /**
     * This is the main {@linkplain FloatingActionButton} of the activity. It can be used by any
     * fragments that implement the {@linkplain FloatingActionButtonListener} interface.
     */
    private FloatingActionButton floatingActionButton;

    /**
     * List of modules that can be selected from the drawer.
     */
    private final List<Module> modules = new ArrayList<>();

    private final int defaultDrawerIconTint = PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 0 ? Color.BLACK : Color.WHITE;

    private class Module{
        private final int drawerId;
        private final String tag;
        private final Class<? extends Fragment> fragmentClass;

        public Module(int drawerId, String tag, Class<? extends Fragment> fragmentClass) {
            this.drawerId = drawerId;
            this.tag = tag;
            this.fragmentClass = fragmentClass;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initialize modules
        modules.add(new Module(R.id.drawer_item_find_primes, "findPrimes", FindPrimesFragment.class));
        modules.add(new Module(R.id.drawer_item_find_factors, "findFactors", FindFactorsFragment.class));
        modules.add(new Module(R.id.drawer_item_factor_tree, "primeFactorization", PrimeFactorizationFragment.class));
        modules.add(new Module(R.id.drawer_item_lcm, "lcm", LeastCommonMultipleFragment.class));
        modules.add(new Module(R.id.drawer_item_gcf, "gcf", GreatestCommonFactorFragment.class));
        modules.add(new Module(R.id.drawer_item_saved_files, "savedFiles", SavedFilesFragment.class));
        modules.add(new Module(R.id.drawer_item_settings, "settings", SettingsFragment.class));
        modules.add(new Module(R.id.drawer_item_about, "about", AboutPageFragment.class));

        //Set up navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        switch (PreferenceManager.getInt(PreferenceManager.Preference.THEME)){
            case 1:
                navigationView.setBackgroundResource(R.color.background_dark_3);
                break;
        }
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

        /*
        Set up the drawer icon animation. Note that this prevents onOptionsItemSelected() from
        being called for R.id.home. Instead, we can listen for drawer open/close events using a
        DrawerListener.
         */
        final ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                hideKeyboard(MainActivity.this);
            }
        });
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
            selectDrawerItem(navigationView.getMenu().findItem(findModule(currentFragment.getTag()).drawerId));
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
        if (currentFragment != null) outState.putString("currentFragmentTag", currentFragment.getTag());
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
    public FloatingActionButton getFab(int index) {
        if (index == 0) {
            return this.floatingActionButton;
        }
        return null;
    }

    @Override
    public void onTaskStatesChanged(final int taskType, final boolean active) {
        if (navigationView != null && taskType != -1) {
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
            return Fragment.instantiate(this, findModule(tag).fragmentClass.getName());
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

        //Find the correct module
        final Module module = findModule(menuItem.getItemId());
        if (module != null){
            //Get the new fragment
            currentFragment = getFragment(module.tag);

            //Add the new fragment if it doesn't exist yet
            if (getSupportFragmentManager().findFragmentByTag(module.tag) == null) {
                fragmentTransaction.add(R.id.main_content_frame, currentFragment, module.tag);
            }

            //Show the new fragment
            fragmentTransaction.show(currentFragment);
        }else{
            currentFragment = null;
            Log.e(TAG, "Module \"" + menuItem.getTitle() + "\" not found!");
        }

        //Commit changes
        fragmentTransaction.commitNow();
        menuItem.setChecked(true);

        //Tell the fragment to initialize the floating action button
        if (currentFragment instanceof FloatingActionButtonListener) {
            ((FloatingActionButtonListener) currentFragment).initFab(floatingActionButton);
        } else {
            floatingActionButton.setVisibility(View.GONE);
        }

        setTitle(menuItem.getTitle());

        //Apply theme to activity based on current fragment
        switch (menuItem.getItemId()) {
            default:
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, Utils.applyAlpha(ContextCompat.getColor(this, R.color.accent), 0.9f)));
                navigationView.setItemTextColor(createColorStateList(Utils.getColor(android.R.attr.textColorPrimary, this), ContextCompat.getColor(this, PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 0 ? R.color.accent_dark : R.color.accent_light)));
                Utils.applyTheme(this, Utils.getColor(android.R.attr.colorPrimaryDark, this), Utils.getColor(android.R.attr.colorPrimary, this));
                break;

            case R.id.drawer_item_find_primes:
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, Utils.applyAlpha(ContextCompat.getColor(this, R.color.purple), 1f)));
                navigationView.setItemTextColor(createColorStateList(Utils.getColor(android.R.attr.textColorPrimary, this), ContextCompat.getColor(this, PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 0 ? R.color.purple_dark : R.color.purple_light)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));
                break;

            case R.id.drawer_item_find_factors:
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, Utils.applyAlpha(ContextCompat.getColor(this, R.color.orange), 0.75f)));
                navigationView.setItemTextColor(createColorStateList(Utils.getColor(android.R.attr.textColorPrimary, this), ContextCompat.getColor(this, PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 0 ? R.color.orange_dark : R.color.orange_light)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.orange_dark), ContextCompat.getColor(this, R.color.orange));
                break;

            case R.id.drawer_item_factor_tree:
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, Utils.applyAlpha(ContextCompat.getColor(this, R.color.green), 0.9f)));
                navigationView.setItemTextColor(createColorStateList(Utils.getColor(android.R.attr.textColorPrimary, this), ContextCompat.getColor(this, PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 0 ? R.color.green_dark : R.color.green_light)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.green_dark), ContextCompat.getColor(this, R.color.green));
                break;

            case R.id.drawer_item_lcm:
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, Utils.applyAlpha(ContextCompat.getColor(this, R.color.yellow), 0.85f)));
                navigationView.setItemTextColor(createColorStateList(Utils.getColor(android.R.attr.textColorPrimary, this), ContextCompat.getColor(this, PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 0 ? R.color.yellow_dark : R.color.yellow_light)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.yellow_dark), ContextCompat.getColor(this, R.color.yellow));
                break;

            case R.id.drawer_item_gcf:
                navigationView.setItemIconTintList(createColorStateList(defaultDrawerIconTint, Utils.applyAlpha(ContextCompat.getColor(this, R.color.lt_blue), 0.8f)));
                navigationView.setItemTextColor(createColorStateList(Utils.getColor(android.R.attr.textColorPrimary, this), ContextCompat.getColor(this, PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 0 ? R.color.blue_dark : R.color.blue_light)));
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.blue_dark), ContextCompat.getColor(this, R.color.lt_blue));
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

    private Module findModule(final int id){
        for (Module module : modules){
            if (module.drawerId == id) return module;
        }
        return null;
    }

    private Module findModule(final String tag){
        for (Module module : modules){
            if (module.tag.equals(tag)) return module;
        }
        return null;
    }

    private Module findModule(final Class<? extends Fragment> cls){
        for (Module module : modules){
            if (module.fragmentClass == cls) return module;
        }
        return null;
    }
}
