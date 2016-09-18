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

package fr.bde_eseo.eseoasis.events.tickets;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import fr.bde_eseo.eseoasis.BuildConfig;
import fr.bde_eseo.eseoasis.Constants;
import fr.bde_eseo.eseoasis.R;
import fr.bde_eseo.eseoasis.events.EventItem;
import fr.bde_eseo.eseoasis.events.tickets.adapters.MyTicketAdapter;
import fr.bde_eseo.eseoasis.events.tickets.model.EventTicketItem;
import fr.bde_eseo.eseoasis.events.tickets.model.ShuttleItem;
import fr.bde_eseo.eseoasis.events.tickets.model.TicketStore;
import fr.bde_eseo.eseoasis.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseoasis.profile.UserProfile;
import fr.bde_eseo.eseoasis.utils.ConnexionUtils;
import fr.bde_eseo.eseoasis.utils.EncryptUtils;
import fr.bde_eseo.eseoasis.utils.Utilities;

/**
 * Created by François L. on 11/01/2016.
 * Donne l'historique des achats de l'utilisateur
 */
public class TicketHistoryActivity extends AppCompatActivity {

    // Android objects
    private Context context;

    // Model
    private ArrayList<EventTicketItem> eventTicketItems;

    // User profile
    private UserProfile userProfile;

    // Adapter / recycler
    private MyTicketAdapter mAdapter;
    private RecyclerView recList;

    // Layout
    private ProgressBar progressLoad, progressToken;
    private TextView tvNothing, tvNothing2;
    private ImageView imgNothing;
    private View viewToken;
    private FloatingActionButton fab;

    // Autoupdate
    private static Handler mHandler;
    private static final int RUN_UPDATE = 8000;
    private static final int RUN_START = 100;
    private static boolean run, backgrounded = false;
    private static boolean firstDisplay = true;

    // Cache
    private File cacheTicketsJSON;

    // Keys
    private final static String JSON_KEY_SHUTTLES = "event-navettes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set view / call parent
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;

        // Get profile
        // Get user's data
        userProfile = new UserProfile();
        userProfile.readProfilePromPrefs(context);

        // Layout
        progressLoad = (ProgressBar) findViewById(R.id.progressTicketList);
        progressToken = (ProgressBar) findViewById(R.id.progressLoading);
        progressLoad.setVisibility(View.GONE);
        progressLoad.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_grey_500), PorterDuff.Mode.SRC_IN);
        progressToken.setVisibility(View.INVISIBLE);
        progressToken.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_white_1000), PorterDuff.Mode.SRC_IN);
        tvNothing = (TextView) findViewById(R.id.tvListNothing);
        tvNothing2 = (TextView) findViewById(R.id.tvListNothing2);
        imgNothing = (ImageView) findViewById(R.id.imgNoCommand);
        tvNothing.setVisibility(View.GONE);
        tvNothing2.setVisibility(View.GONE);
        imgNothing.setVisibility(View.GONE);
        viewToken = findViewById(R.id.viewCircle);
        viewToken.setVisibility(View.INVISIBLE);

        // Get file from cache directory
        String cachePath = getCacheDir() + "/";
        cacheTicketsJSON = new File(cachePath + "tickets.json");

        // Init model → get it from TicketStore
        eventTicketItems = TicketStore.getInstance().getEventTicketItems();

        // Init adapter / recycler view
        mAdapter = new MyTicketAdapter(context);
        recList = (RecyclerView) findViewById(R.id.cardTickets);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mAdapter);
        recList.setVisibility(View.GONE);

        // Attach floating action button
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(recList);

        // On click listener for token
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeZone tz = Calendar.getInstance().getTimeZone();
                String tzStr = tz.getID();

                if (!userProfile.isCreated()) {
                    new MaterialDialog.Builder(context)
                            .title(R.string.user_not_connected)
                            .content(R.string.user_not_connected_ticket)
                            .negativeText(R.string.dialog_ok)
                            .cancelable(false)
                            .show();
                } else if (false  && !tzStr.equalsIgnoreCase(Constants.TZ_ID_PARIS)) {
                    new MaterialDialog.Builder(context)
                            .title(R.string.error)
                            .content(R.string.ticket_bad_country)
                            .negativeText(R.string.dialog_ok)
                            .cancelable(false)
                            .show();
                } else {
                    SyncToken syncToken = new SyncToken();
                    syncToken.execute();
                }
            }
        });

        // Recycler listener
        recList.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                final int idcmd = eventTicketItems.get(position).getIdcmd();

                new MaterialDialog.Builder(context)
                        .title(R.string.ticket_resend_title)
                        .content(R.string.ticket_resend_txt)
                        .negativeText(R.string.dialog_cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                Utilities.hideKeyboardFromActivity(TicketHistoryActivity.this); // Doesn't works ...
                                super.onNegative(dialog);
                            }
                        })
                        .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                        .input("sterling@archer.fr", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                AsyncEventEmail asyncEmail = new AsyncEventEmail(context, "" + input, null, userProfile, idcmd); // convert charSequence into String object
                                asyncEmail.execute();
                            }
                        }).show();
            }
        }));

        // Change message
        if (userProfile.isCreated()) {
            tvNothing.setText(getResources().getString(R.string.empty_header_order));
            tvNothing2.setText(getResources().getString(R.string.empty_desc_order));
        } else {
            tvNothing.setText(getResources().getString(R.string.empty_header_noorder));
            tvNothing2.setText(getResources().getString(R.string.empty_desc_noorder));
            tvNothing.setVisibility(View.VISIBLE);
            tvNothing2.setVisibility(View.VISIBLE);
            imgNothing.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        }

        // Set data
        mAdapter.setTicketsItems(eventTicketItems);

        // Start update
        if (mHandler == null) {
            mHandler = new android.os.Handler();
            mHandler.postDelayed(updateTimerThread, RUN_START);
        } else {
            mHandler.removeCallbacks(updateTimerThread);
            mHandler.postDelayed(updateTimerThread, RUN_START);
        }

        // Delay to update data
        run = true;

    }


    /**
     * Menu : back button + arrow in toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_empty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        firstDisplay = true;
        // Delay to update data
        run = true;

        if (progressToken != null) progressToken.setVisibility(View.INVISIBLE);
        if (fab != null) fab.setVisibility(View.VISIBLE);
        if (viewToken != null) viewToken.setVisibility(View.INVISIBLE);

        if (mHandler == null) {
            mHandler = new android.os.Handler();
            mHandler.postDelayed(updateTimerThread, RUN_START);
        } else {
            mHandler.removeCallbacks(updateTimerThread);
            mHandler.postDelayed(updateTimerThread, RUN_START);
        }
    }


    @Override
    public void onPause() {
        if (mHandler != null) {
            mHandler.removeCallbacks(updateTimerThread);
        }
        run = false;
        super.onPause();
    }


    /**
     * Background task to fetch data periodically from server
     */
    private Runnable updateTimerThread = new Runnable() {
        public void run() {

            try {
                if (run && userProfile.isCreated()) {
                    run = false;
                    SyncTickets syncTickets = new SyncTickets();
                    syncTickets.execute();
                }
            } catch (NullPointerException e) { // Stop handler if activity disappears
                mHandler.removeCallbacks(updateTimerThread);
                run = false;
            }
        }
    };


    /**
     * Sync history, fetch data from server
     */
    // Async Task Class
    class SyncTickets extends AsyncTask<String, String, String> {

        private HashMap<String, String> syncParam;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            syncParam = new HashMap<>();
            run = false;
            if (firstDisplay) {
                progressLoad.setVisibility(View.VISIBLE);
                recList.setVisibility(View.INVISIBLE);
                firstDisplay = false;
            }

            // Prepare param array
            syncParam.clear(); // in case of ...
            syncParam.put(context.getResources().getString(R.string.client), userProfile.getId());
            syncParam.put(context.getResources().getString(R.string.password), userProfile.getPassword());
            syncParam.put(context.getResources().getString(R.string.hash), EncryptUtils.sha256(context.getResources().getString(R.string.MESSAGE_TICKETS_USER) + userProfile.getId() + userProfile.getPassword()));
        }

        @Override
        protected String doInBackground(String... args) {

            // Prepare JSON String
            String jsonStr;

            // Try to fetch data from server
            jsonStr = ConnexionUtils.postServerData(Constants.URL_API_EVENT_LIST, syncParam, context);

            // If data is empty
            if (!Utilities.isNetworkDataValid(jsonStr)) {

                // Fetch data from cache history
                if (cacheTicketsJSON.exists()) {
                    jsonStr = Utilities.getStringFromFile(cacheTicketsJSON);
                } else {
                    jsonStr = null; // force empty message
                }

            } else {

                // Else, there is a server response : phone is online
                try {
                    JSONObject servJson = new JSONObject(jsonStr);

                    // Check if there are no errors
                    if (servJson.getInt("status") == 1) {

                        jsonStr = servJson.getString("data");

                    } else {
                        // bad password (-2) : display nothing
                        jsonStr = null;
                    }

                } catch (JSONException e) {

                    e.printStackTrace();

                    // other placeholder_error : display nothing
                    jsonStr = null;
                }
            }

            if (jsonStr != null) {

                // Check / fflush data
                if (eventTicketItems == null)
                    eventTicketItems = new ArrayList<>();
                else
                    eventTicketItems.clear();

                // 1 if ok, -2 if not
                try {
                    // Temporary array
                    ArrayList<EventTicketItem> tempNextArray = new ArrayList<>();
                    ArrayList<EventTicketItem> tempDoneArray = new ArrayList<>();

                    // Parse JSON
                    JSONObject json = new JSONObject(jsonStr);

                    // Get all history items
                    JSONArray jsData = new JSONArray(json.getString("tickets"));
                    for (int i = 0; i < jsData.length(); i++) {
                        eventTicketItems.add(new EventTicketItem(jsData.getJSONObject(i)));
                    }

                    // Set names to ID → not saved into cache !
                    TicketStore.getInstance().autoTicketAttributes();

                    // Parse data / set dates
                    for (int i = 0; i < eventTicketItems.size(); i++) {
                        EventTicketItem eti = eventTicketItems.get(i);

                        if (eti.getLinkedDatefin() == null || eti.getLinkedDatefin().before(new Date())) {
                            eti.setPassed(true);
                            tempDoneArray.add(eti);
                        } else {
                            eti.setPassed(false);
                            tempNextArray.add(eti);
                        }
                    }

                    // Clear everything
                    eventTicketItems.clear();

                    // Add tickets to view
                    if (tempNextArray.size() > 0) {
                        eventTicketItems.add(new EventTicketItem(getString(R.string.ticket_future_events)));
                    }
                    eventTicketItems.addAll(tempNextArray);

                    if (tempDoneArray.size() > 0) {
                        eventTicketItems.add(new EventTicketItem(getString(R.string.ticket_terminated_events)));
                    }
                    eventTicketItems.addAll(tempDoneArray);

                    // save data
                    Utilities.writeStringToFile(cacheTicketsJSON, jsonStr);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return jsonStr;
        }

        // Once File is downloaded
        @Override
        protected void onPostExecute(String sJson) {

            progressLoad.setVisibility(View.GONE);
            if (sJson != null) {

                mAdapter.notifyDataSetChanged();

                // display "no ticket" or not
                if (eventTicketItems.size() == 0) {
                    tvNothing.setVisibility(View.VISIBLE);
                    tvNothing2.setVisibility(View.VISIBLE);
                    imgNothing.setVisibility(View.VISIBLE);
                    recList.setVisibility(View.GONE);
                } else {
                    tvNothing.setVisibility(View.GONE);
                    tvNothing2.setVisibility(View.GONE);
                    imgNothing.setVisibility(View.GONE);
                    recList.setVisibility(View.VISIBLE);
                }

            } else {
                tvNothing.setVisibility(View.VISIBLE);
                tvNothing2.setVisibility(View.VISIBLE);
                imgNothing.setVisibility(View.VISIBLE);
                recList.setVisibility(View.GONE);
            }

            mHandler.postDelayed(updateTimerThread, RUN_UPDATE);
            run = true;
        }
    }


    /**
     * Asynctask to get token from server
     */
    class SyncToken extends AsyncTask<String, String, String> {

        private String networkPrepare, networkItems;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressToken.setVisibility(View.VISIBLE);
            fab.setVisibility(View.INVISIBLE);
            viewToken.setVisibility(View.VISIBLE);

            // Init network response
            networkItems = null;
            networkPrepare = null;
        }

        @Override
        protected String doInBackground(String... sUrl) {

            HashMap<String, String> params = new HashMap<>();
            params.put(context.getResources().getString(R.string.client), userProfile.getId());
            params.put(context.getResources().getString(R.string.password), userProfile.getPassword());
            params.put(context.getResources().getString(R.string.os), Constants.APP_ID);
            params.put(context.getResources().getString(R.string.version), BuildConfig.VERSION_NAME);
            params.put(context.getResources().getString(R.string.hash), EncryptUtils.sha256(context.getResources().getString(R.string.MESSAGE_GET_TOKEN_EVENT) + userProfile.getId() + userProfile.getPassword() + Constants.APP_ID));

            networkPrepare = ConnexionUtils.postServerData(Constants.URL_API_EVENT_PREPARE, params, context); // token answer
            networkItems = ConnexionUtils.postServerData(Constants.URL_API_EVENT_ITEMS, null, context); // items answer

            return null; // no need to return anything here
        }

        @Override
        protected void onPostExecute(String noUsedData) {

            String err = getString(R.string.error_network);
            int retCode = 0;
            String jsonToken = "";

            /** Check if response is token, or an placeholder_error **/
            if (Utilities.isNetworkDataValid(networkPrepare) && Utilities.isNetworkDataValid(networkItems)) { // 64 : nb chars for a SHA256 value
                try {
                    JSONObject obj = new JSONObject(networkPrepare);
                    JSONArray arrayItems = new JSONArray(networkItems);

                    retCode = obj.getInt("status");
                    err = obj.getString("cause");

                    if (retCode == 1) {

                        // Reset data before writing in it
                        TicketStore.getInstance().resetOrder();

                        // Get token
                        jsonToken = obj.getJSONObject("data").getString("token");

                        // Parse items
                        for (int i = 0; i < arrayItems.length(); i++) {

                            JSONObject objItem = arrayItems.getJSONObject(i);
                            if (objItem.has(JSON_KEY_SHUTTLES)) {
                                JSONArray arrayShuttles = objItem.getJSONArray(JSON_KEY_SHUTTLES);

                                // Get shuttles
                                for (int s = 0; s < arrayShuttles.length(); s++) {
                                    TicketStore.getInstance().getShuttleItems().add(new ShuttleItem(getBaseContext(), arrayShuttles.getJSONObject(s)));
                                }

                                // Assign shuttles to events
                                for (int e = 0; e < TicketStore.getInstance().getEventItems().size(); e++) {
                                    EventItem ei = TicketStore.getInstance().getEventItems().get(e);
                                    if (!ei.isHeader() && !ei.isPassed()) {
                                        for (int ee = 0; ee < ei.getSubEventItems().size(); ee++) {

                                            // Remove old shuttles from events, before
                                            // Then search
                                            // Ok, done in method
                                            ei.getSubEventItems().get(ee).searchShuttles(TicketStore.getInstance().getShuttleItems());
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Check service answer
            if (retCode == 1) {
                // Success !
                run = false;
                TicketStore.getInstance().setToken(jsonToken); // Sets the Token
                Intent i = new Intent(context, PresalesActivity.class);
                startActivity(i); // Start the sale's activity
            } else {
                progressToken.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.VISIBLE);
                viewToken.setVisibility(View.INVISIBLE);

                new MaterialDialog.Builder(context)
                        .title(R.string.error)
                        .content(err)
                        .cancelable(false)
                        .negativeText(R.string.dialog_close)
                        .show();
            }
        }
    }
}
