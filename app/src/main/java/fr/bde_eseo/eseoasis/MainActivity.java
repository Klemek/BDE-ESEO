/**
 * Copyright (C) 2016 - François LEPAROUX
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.bde_eseo.eseoasis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;

import fr.bde_eseo.eseoasis.clubs.ClubsListFragment;
import fr.bde_eseo.eseoasis.events.EventsListFragment;
import fr.bde_eseo.eseoasis.family.StudentSearchFragment;
import fr.bde_eseo.eseoasis.gcmpush.QuickstartPreferences;
import fr.bde_eseo.eseoasis.gcmpush.RegistrationIntentService;
import fr.bde_eseo.eseoasis.rooms.RoomsListFragment;
import fr.bde_eseo.eseoasis.sponsors.SponsoFragment;
import fr.bde_eseo.eseoasis.interfaces.OnItemAddToCart;
import fr.bde_eseo.eseoasis.interfaces.OnUserProfileChange;
import fr.bde_eseo.eseoasis.lacommande.DataManager;
import fr.bde_eseo.eseoasis.lacommande.OrderHistoryFragment;
import fr.bde_eseo.eseoasis.lacommande.OrderTabsFragment;
import fr.bde_eseo.eseoasis.news.NewsListFragment;
import fr.bde_eseo.eseoasis.profile.ConnectProfileFragment;
import fr.bde_eseo.eseoasis.profile.UserProfile;
import fr.bde_eseo.eseoasis.profile.ViewProfileFragment;
import fr.bde_eseo.eseoasis.settings.SettingsFragment;
import fr.bde_eseo.eseoasis.slidingmenu.NavDrawerItem;
import fr.bde_eseo.eseoasis.slidingmenu.NavDrawerListAdapter;
import fr.bde_eseo.eseoasis.utils.ImageUtils;
import fr.bde_eseo.eseoasis.utils.Utilities;
import fr.bde_eseo.eseoasis.version.AsyncCheckVersion;

/**
 * Main Activity for ESEOmega app
 * Too much things to describe here, check code please ...
 */
public class MainActivity extends AppCompatActivity implements OnUserProfileChange, OnItemAddToCart {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;



    // Others constant values
    public static final int MAX_PROFILE_SIZE = 256; // seems good

    // used to store app title
    private CharSequence mTitle;

    // Profile item
    UserProfile profile = new UserProfile();

    // Preferences
    private SharedPreferences prefs_Read;
    private SharedPreferences.Editor prefs_Write;
    private SharedPreferences prefsUser;

    // Latch to remember position of fragment
    private int fragPosition = 0;

    // GCM
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    // slide menu items
    private String[] navMenuTitles, navMenuOptions;
    private TypedArray navMenuIcons;
    private ListView mDrawerList;
    private NavDrawerListAdapter navAdapter;
    private ArrayList<NavDrawerItem> navDrawerItems;

    // Material Toolbar
    private Toolbar toolbar;

    // Inside update
    private LinearLayout llUpdate;
    private TextView tvUpdate;
    private ProgressBar progressUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Global UI View
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setPadding(0, Utilities.getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        mTitle = getTitle();
        llUpdate = (LinearLayout) findViewById(R.id.llMainUpdate);
        tvUpdate = (TextView) findViewById(R.id.tvMainUpdate);
        progressUpdate = (ProgressBar) findViewById(R.id.progressMainUpdate);
        progressUpdate.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_white_1000), PorterDuff.Mode.SRC_IN);
        llUpdate.setVisibility(View.GONE);

        // Check app auth
        boolean installPlayStore = Utilities.verifyInstaller(this);
        boolean installSigned = Utilities.checkAppSignature(this);

        if (!BuildConfig.DEBUG && (!installPlayStore || !installSigned)) {
            new MaterialDialog.Builder(this)
                    .title(R.string.bad_signature_title)
                    .content(R.string.bad_signature_content)
                    .negativeText(R.string.bad_signature_button)
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            MainActivity.this.finish();
                        }
                    }).show();
        }

        // GCM Receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                ConnectProfileFragment mFragment = (ConnectProfileFragment) getSupportFragmentManager().findFragmentByTag("frag0");
                if (mFragment != null) {
                    mFragment.setPushRegistration(sentToken);
                } else {
                    progressUpdate.setVisibility(View.GONE);
                    if (sentToken) {
                        tvUpdate.setText(R.string.finished);
                    } else {
                        tvUpdate.setText(R.string.error_network2);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            llUpdate.animate().translationY(llUpdate.getHeight());
                        }
                    }, 1500);
                }
            }
        };

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // load slide menu options
        navMenuOptions = getResources().getStringArray(R.array.nav_drawer_options);

        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<>();

        // add profile view item
        profile.readProfilePromPrefs(this);
        navDrawerItems.add(profile.getDrawerProfile());

        // adding nav drawer items to array
        for (int it = 1; it < navMenuTitles.length; it++) {
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[it], navMenuIcons.getResourceId(it, -1)));
        }

        // add divider
        navDrawerItems.add(new NavDrawerItem());
        // ↑ Yes it'll be better to implement an algorithm to detect end of first list and put a divider
        //   But LA FLEMME

        // adding nav drawer options to array
        for (String navMenuOption : navMenuOptions) navDrawerItems.add(new NavDrawerItem(navMenuOption));

        // Recycle the typed array
        navMenuIcons.recycle();

        // Listen events
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        navAdapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        // set picture
        // WARNING ! Bitmap could be null if picture is removed from storage !
        // EDIT : It's ok, getResizedBitmap has been modified to survive to that kind of mistake
        // TODO : correct photo orientation
        // @see http://stackoverflow.com/questions/7286714/android-get-orientation-of-a-camera-bitmap-and-rotate-back-90-degrees
        File fp = new File(profile.getPicturePath());
        if (fp.exists())
            navAdapter.setBitmap(ImageUtils.getResizedBitmap(BitmapFactory.decodeFile(profile.getPicturePath()), MAX_PROFILE_SIZE));

        // set data adapter to our listview
        mDrawerList.setAdapter(navAdapter);

        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Initialize preference objects
        prefs_Read = getSharedPreferences(Constants.PREFS_APP_WELCOME, 0);
        prefs_Write = prefs_Read.edit();
        prefsUser = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // Receive Intent from notification
        Bundle extras = getIntent().getExtras();
        String message, title;

        // Set the user's preferred theme for menu
        // Keep clear theme for now
        //mDrawerList.setBackgroundColor(getResources().getColor(R.color.drawer_background_dark));

        // Get the user's preferred homescreen
        int intendID = Integer.parseInt(prefsUser.getString(Constants.PREFS_GENERAL_HOMESCREEN, "1")); // default if news

        boolean passInstance = false;

        // If we've just received intent from push notification event, we handle it
        if (extras != null) {
            title = extras.getString(Constants.KEY_MAIN_TITLE);
            message = extras.getString(Constants.KEY_MAIN_MESSAGE);
            intendID = extras.getInt(Constants.KEY_MAIN_INTENT);
            passInstance = true;
            if (intendID == Constants.NOTIF_CONNECT) {
                intendID = 0;
            } else if (intendID == Constants.NOTIF_GENERAL) {
                intendID++;
                if (title != null && title.length() > 0 && message != null && message.length() > 0) {
                    new MaterialDialog.Builder(this)
                            .title(title)
                            .content(message)
                            .negativeText(R.string.dialog_close)
                            .show();
                }
            }
        }

        if (savedInstanceState == null || passInstance) {
            // on first time display view for first nav item
            displayView(intendID); // Note : 0 is profile
        }

        // If needed, show a welcome message
        // Why did I used to save version here ?...
        //prefs_Write.putString(Constants.PREFS_APP_VERSION, BuildConfig.VERSION_NAME);
        //prefs_Write.apply();

        // 1st start ever : just show drawer for connection
        if (prefs_Read.getBoolean(Constants.PREFS_APP_WELCOME_DATA, true)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
            }, 800);
            prefs_Write.putBoolean(Constants.PREFS_APP_WELCOME_DATA, false);
            prefs_Write.apply();

        } else if (prefsUser.getBoolean(Constants.PREFS_GENERAL_UPDATE, false)) {
            // Else check update if the user asks for it
            AsyncCheckVersion asyncCheckVersion = new AsyncCheckVersion(MainActivity.this);
            asyncCheckVersion.execute();
        }

        // Mise à jour automatique du push :
        // - check user est enregistré
        // - check pushToken pas bon / enregistré
        // - check Services Google présents
        // → alors on démarre la classe Registration
        if (profile.isCreated() && !profile.isPushRegistered() && Utilities.checkPlayServices(this)) {

            llUpdate.setVisibility(View.VISIBLE);
            //llUpdate.animate().translationY(-llUpdate.getHeight());
            progressUpdate.setVisibility(View.VISIBLE);
            tvUpdate.setText(R.string.preparing);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvUpdate.setText(R.string.updating);

                    // Start IntentService to register this application with GCM.
                    Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
                    MainActivity.this.startService(intent);
                }
            }, 1500);
        }
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch(fragPosition){
            case 2: //Event
                getMenuInflater().inflate(R.menu.menu_event, menu); // with Event buy option
                break;
            case 4: //Genealogie
                //getMenuInflater().inflate(R.menu.menu_families, menu);
                break;
            case 7: //Plan
                //getMenuInflater().inflate(R.menu.menu_rooms, menu);
                break;
            default:
                getMenuInflater().inflate(R.menu.main_less, menu);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {

            // Info : "A Propos" de l'application
            case R.id.action_info:
                new MaterialDialog.Builder(this)
                        .title(R.string.about_title)
                        .content(R.string.about_content)
                        .positiveText(R.string.dialog_contact)
                        .negativeText(R.string.dialog_close)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", Constants.MAIL_DIALOG, null));
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.mail_dev_title);
                                emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_dev_txt) + BuildConfig.VERSION_NAME + "\n\n" + "...");
                                startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_dev)));
                            }
                        })
                        .show();
                return true;

            // Event : on passe à la vue de l'historique d'achat des place events
            //TODO tickets
            /*case R.id.action_ticketevent:
                Intent intentEvent = new Intent(MainActivity.this, TicketHistoryActivity.class);
                startActivity(intentEvent);
                return true;
            */
            // Action par défaut, aucune
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        // boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList); // not used
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Displaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0: // Edit profile
                if (!profile.isCreated()) fragment = new ConnectProfileFragment();
                else fragment = new ViewProfileFragment();
                break;
            case 1: // News
                fragment = new NewsListFragment();
                break;
            case 2: // Events
                fragment = new EventsListFragment();
                break;
            case 3: // Clubs & Vie Asso
                fragment = new ClubsListFragment();
                break;
            case 4: // Généalogie
                fragment = new StudentSearchFragment();
                break;
            case 5: // Commande Cafet
                fragment = new OrderHistoryFragment();
                break;
            case 6: // Bons plans
                fragment = new SponsoFragment();
                break;
            case 7: // Plan des salles
                fragment = new RoomsListFragment();
                break;
            case 9: // Réglages
                fragment = new SettingsFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment, "frag" + position).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerList.setItemsCanFocus(true);

            // Invalidate Menu to redraw it : Ingenews only visible from fragment n°1 (news)
            invalidateOptionsMenu();
            fragPosition = position;

            if (position < navMenuTitles.length) {
                setTitle(navMenuTitles[position]);
            } else {
                setTitle(navMenuOptions[position - navMenuTitles.length - 1]);
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
            }, 100);
        }
    }

    /**
     * Sets the title of the current app window
     */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * INTERFACE : Synchronises the new profile with the old one in drawer
     */
    @Override
    public void OnUserProfileChange(UserProfile profile) {
        if (profile != null && navDrawerItems != null && navAdapter != null) {
            this.profile = profile;
            navDrawerItems.set(0, profile.getDrawerProfile());
            navAdapter.notifyDataSetChanged();
            mDrawerLayout.openDrawer(mDrawerList);
            navAdapter.setBitmap(profile.getPicturePath().length() == 0 ? null :
                    ImageUtils.getResizedBitmap(BitmapFactory.decodeFile(profile.getPicturePath()), MAX_PROFILE_SIZE));
        }
    }

    /**
     * INTERFACE : On item added to cart, refresh the content and title of order tab's title
     */
    @Override
    public void OnItemAddToCart() {
        OrderTabsFragment mFragment = (OrderTabsFragment) getSupportFragmentManager().findFragmentByTag(Constants.TAG_FRAGMENT_ORDER_TABS);
        mFragment.refreshCart();
    }

    /**
     * On back pressed : asks user if he really want to loose the cart's content (if viewing OrderTabsFragment)
     */
    @Override
    public void onBackPressed() {
        OrderTabsFragment fragment = (OrderTabsFragment) getSupportFragmentManager().findFragmentByTag(Constants.TAG_FRAGMENT_ORDER_TABS);
        if (fragment != null && fragment.isVisible() && DataManager.getInstance().getNbCartItems() > 0) {
            new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.order_cancel_title)
                    .content(R.string.order_cancel_message)
                    .positiveText(R.string.dialog_yes)
                    .negativeText(R.string.dialog_no)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            MainActivity.super.onBackPressed();
                        }
                    })
                    .show();
        } else { // Another fragment, we don't care
            MainActivity.super.onBackPressed();
        }
    }

    /**
     * onStop : we don't care
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * onResume : Register listener for BroadcastReceiver
     */
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    /**
     * onPause : Unregister listener for BroadcastReceiver
     */
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}
