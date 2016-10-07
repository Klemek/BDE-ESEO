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

package fr.bde_eseo.eseomega.lacommande;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashMap;
import java.util.TimeZone;

import fr.bde_eseo.eseomega.BuildConfig;
import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.lacommande.model.HistoryItem;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;
import fr.bde_eseo.eseomega.version.AsyncCheckVersion;

/**
 * Created by François on 13/04/2015.
 * Permet d'afficher l'historique des commandes passées (synchronisée avec le serveur ESEOmega)
 * Implémente un FloatingActionButton (Lollipop Style) afin de permettre l'ajout d'une nouvelle commande.
 *
 * Si en ligne -> refresh des données depuis le serveur et enregistrement du JSON
 * Si hors ligne -> chargement des données depuis le JSON enregistré
 */
public class OrderHistoryFragment extends Fragment {

    public OrderHistoryFragment() {}

    private RecyclerView recList;
    private ProgressBar progressBar, progressBarToken;
    private View viewToken;
    private FloatingActionButton fab;
    private MyHistoryAdapter mAdapter;
    private ArrayList<HistoryItem> historyList;
    private UserProfile userProfile;
    private String userLogin, userPass;
    private static Handler mHandler;
    private static final int RUN_UPDATE = 8000;
    private static final int RUN_START = 100;
    private static boolean run, backgrounded = false;
    private static boolean firstDisplay = true;
    private File cacheHistoryJSON;
    private HashMap<String, String> params;
    private long lastUpdate = 0;
    private Context context;
    private AsyncInfoService asyncInfoService;
    private SyncHistory syncHistory;
    private SyncTimeToken syncTimeToken;
    private AsyncCheckVersion asyncCheckVersion;


    private TextView tvNothing, tvNothing2, tvServiceInfo;
    private ImageView imgNothing;

    @Override
    public void onResume() {
        super.onResume();
        firstDisplay = true;
        // Delay to update data
        run = true;

        if (progressBarToken != null) progressBarToken.setVisibility(View.INVISIBLE);
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
    public void onDetach(){
        super.onDetach();
        if(asyncInfoService != null)asyncInfoService.cancel(true);
        if(syncTimeToken != null)syncTimeToken.cancel(true);
        if(asyncCheckVersion != null)asyncCheckVersion.cancel(true);
        if(syncHistory != null)syncHistory.cancel(true);
    }

    @Override
    public void onPause() {
        if( mHandler != null) {
            mHandler.removeCallbacks(updateTimerThread);
        }
        run = false;
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Find elements and attach listView / floating button
        View rootView = inflater.inflate(R.layout.fragment_cafet_history, container, false);

        // Get current fragment's context
        context = getActivity();

        // Get user's data
        userProfile = new UserProfile();
        userProfile.readProfilePromPrefs(getActivity());
        userLogin = userProfile.getId();
        userPass = userProfile.getPassword();

        // Search for the listView, then set its adapter
        mAdapter = new MyHistoryAdapter(getActivity());
        recList = (RecyclerView) rootView.findViewById(R.id.cardHistory);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressHistoryList);
        progressBarToken = (ProgressBar) rootView.findViewById(R.id.progressLoading);
        viewToken = rootView.findViewById(R.id.viewCircle);
        tvNothing = (TextView) rootView.findViewById(R.id.tvListNothing);
        tvNothing2 = (TextView) rootView.findViewById(R.id.tvListNothing2);
        tvServiceInfo = (TextView) rootView.findViewById(R.id.tvServiceInfo);
        imgNothing = (ImageView) rootView.findViewById(R.id.imgNoCommand);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mAdapter);
        recList.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_grey_500), PorterDuff.Mode.SRC_IN);
        progressBarToken.setVisibility(View.INVISIBLE);
        progressBarToken.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_white_1000), PorterDuff.Mode.SRC_IN);
        viewToken.setVisibility(View.INVISIBLE);
        tvNothing.setVisibility(View.GONE);
        tvNothing2.setVisibility(View.GONE);
        imgNothing.setVisibility(View.GONE);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.attachToRecyclerView(recList);

        // Change message
        if (userProfile.isCreated()) {
            tvNothing.setText(getActivity().getResources().getString(R.string.empty_header_history));
            tvNothing2.setText(getActivity().getResources().getString(R.string.empty_desc_history));
        } else {
            tvNothing.setText(getActivity().getResources().getString(R.string.empty_header_noconnect));
            tvNothing2.setText(getActivity().getResources().getString(R.string.empty_desc_noconnect));
            tvNothing.setVisibility(View.VISIBLE);
            tvNothing2.setVisibility(View.VISIBLE);
            imgNothing.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        }

        // Get file from cache directory
        String cachePath = getActivity().getCacheDir() + "/";
        cacheHistoryJSON = new File(cachePath + "history.json");

        // Create array and check online history
        historyList = new ArrayList<>();

        // Delay to update data
        run = true;

        if (mHandler == null) {
            mHandler = new android.os.Handler();
            mHandler.postDelayed(updateTimerThread, RUN_START);
        } else {
            mHandler.removeCallbacks(updateTimerThread);
            mHandler.postDelayed(updateTimerThread, RUN_START);
        }
        mAdapter.setHistoryArray(historyList);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimeZone tz = Calendar.getInstance().getTimeZone();
                String tzStr = tz.getID();

                if (!userProfile.isCreated()) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.user_not_connected)
                            .content(R.string.user_not_connected_cafet)
                            .negativeText(R.string.dialog_ok)
                            .cancelable(false)
                            .show();
                } else if (false && !tzStr.equalsIgnoreCase(Constants.TZ_ID_PARIS)) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.error)
                            .content(R.string.cafet_bad_country)
                            .negativeText(R.string.dialog_ok)
                            .cancelable(false)
                            .show();
                } else {

                    String versionName = BuildConfig.VERSION_NAME;

                    /** Prepare data **/
                    long timestamp = System.currentTimeMillis() / 1000; // timestamp in seconds
                    params = new HashMap<>();
                    params.put(getActivity().getResources().getString(R.string.client), userLogin);
                    params.put(getActivity().getResources().getString(R.string.password), userPass);
                    params.put(getActivity().getResources().getString(R.string.tstp), "" + timestamp);
                    params.put(getActivity().getResources().getString(R.string.os), "" + Constants.APP_ID);
                    //params.put(getActivity().getResources().getString(R.string.version), "" + versionName);
                    params.put(getActivity().getResources().getString(R.string.version), "" + "1.0.2" /*getString(R.string.app_version_name)*/);
                    params.put(getActivity().getResources().getString(R.string.hash), EncryptUtils.sha256(getActivity().getResources().getString(R.string.MESSAGE_GET_TOKEN) + userLogin + userPass + timestamp + Constants.APP_ID));

                    /** Call async task **/
                    syncTimeToken = new SyncTimeToken(getActivity());
                    //syncTimeToken.execute(Constants.URL_API_ORDER_PREPARE);
                    asyncCheckVersion = new AsyncCheckVersion(getContext(), syncTimeToken, "");
                    asyncCheckVersion.execute();
                }

            }
        });

        recList.addOnItemTouchListener(
            new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent i = new Intent(getActivity(), OrderDetailsActivity.class);
                    i.putExtra(Constants.KEY_ORDER_ID, historyList.get(position).getCommandNumber());
                    getActivity().startActivity(i);
                }
            }
        ));

        // Who's cooking ?
        asyncInfoService = new AsyncInfoService(getActivity());
        asyncInfoService.execute();

        return rootView;
    }


    /**
     * Asynctask to sync time and get token from server
     */
    class SyncTimeToken extends AsyncTask<String, Void, String> {

        String content;
        Context context;

        public SyncTimeToken (Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBarToken.setVisibility(View.VISIBLE);
            fab.setVisibility(View.INVISIBLE);
            viewToken.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String...args) {
            return ConnexionUtils.postServerData(Constants.URL_API_ORDER_PREPARE, params, context);
        }

        @Override
        protected void onPostExecute(String data) {

            String err = "";
            int retCode = 0;
            String jsonToken = "";

            /** Check if response is token, or an placeholder_error **/
            if (Utilities.isNetworkDataValid(data)) { // 64 : nb chars for a SHA256 value
                try {
                    JSONObject obj = new JSONObject(data);
                    retCode = obj.getInt("status");
                    err = obj.getString("cause");

                    if (retCode == 1) {
                        jsonToken = obj.getJSONObject("data").getString("token");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Check service answer
            if (retCode == 1) {
                // Success !
                run = false;
                DataManager.getInstance().reset(); // reset data before writing in it
                DataManager.getInstance().setToken(jsonToken); // Sets the Token

                OrderTabsFragment fragment = new OrderTabsFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.slide_in, R.anim.slide_out)
                        .replace(R.id.frame_container, fragment, Constants.TAG_FRAGMENT_ORDER_TABS)
                        .addToBackStack("BACK")
                        .commit();
            } else {

                retCode = -retCode; // -5 => placeholder_error n°5
                String errorStr;

                switch (retCode) {
                    case Constants.ERROR_TIMESTAMP:
                        errorStr = getString(R.string.error_timestamp);
                        break;
                    case Constants.ERROR_SERVICE_OUT:
                        errorStr = getString(R.string.error_service_out);
                        break;
                    case Constants.ERROR_USERREGISTER:
                        errorStr = getString(R.string.error_user_register);
                        break;
                    case Constants.ERROR_UNPAID:
                        errorStr = getString(R.string.error_unpaid);
                        break;
                    case Constants.ERROR_APP_PB:
                        errorStr = getString(R.string.error_app_pb);
                        break;
                    case Constants.ERROR_USER_BAN:
                        errorStr = getString(R.string.error_user_ban) + err + ")";
                        break;
                    case Constants.ERROR_BAD_VERSION:
                        errorStr = getString(R.string.error_bad_version);
                        break;
                    case Constants.ERROR_NETWORK:
                        errorStr = getString(R.string.error_network);
                        break;
                    default:
                        errorStr = getString(R.string.error_network_unknown) + " :\n" + data;
                        break;
                }

                progressBarToken.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.VISIBLE);
                viewToken.setVisibility(View.INVISIBLE);

                new MaterialDialog.Builder(context)
                        .title(R.string.error)
                        .content(errorStr)
                        .cancelable(false)
                        .negativeText(R.string.dialog_close)
                        .show();
            }
        }
    }

    /**
     * Background task to fetch data periodically from server
     */
    private Runnable updateTimerThread = new Runnable() {
        public void run() {

            try {
                if (run && userProfile.isCreated()) {// && System.currentTimeMillis() - lastUpdate >= RUN_UPDATE) {
                    run = false;
                    syncHistory = new SyncHistory();
                    syncHistory.execute();
                }
            } catch (NullPointerException e) { // Stop handler if fragment disappears
                mHandler.removeCallbacks(updateTimerThread);
                run = false;
            }
        }
    };

    /**
     * Sync history, fetch data from server
     */
    // Async Task Class
    class SyncHistory extends AsyncTask<String, String, String> {

        private HashMap<String, String> syncParam;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            syncParam = new HashMap<>();
            run = false;
            if (firstDisplay) {
                progressBar.setVisibility(View.VISIBLE);
                recList.setVisibility(View.INVISIBLE);
                firstDisplay = false;
            }

            // Prepare param array
            syncParam.clear(); // in case of ...
            syncParam.put(context.getResources().getString(R.string.client), userLogin);
            syncParam.put(context.getResources().getString(R.string.password), userPass);
            syncParam.put(context.getResources().getString(R.string.hash), EncryptUtils.sha256(context.getResources().getString(R.string.MESSAGE_HISTORY_USER) + userLogin + userPass));

        }

        @Override
        protected String doInBackground(String... args) {

            // Prepare JSON String
            String jsonStr;

            // Try to fetch data from server
            jsonStr = ConnexionUtils.postServerData(Constants.URL_API_ORDER_LIST, syncParam, context);

            return jsonStr;
        }

        // Once File is downloaded
        @Override
        protected void onPostExecute(String jsonStr) {

            // If data is empty
            if (!Utilities.isNetworkDataValid(jsonStr)) {

                // Fetch data from cache history
                if (cacheHistoryJSON.exists()) {
                    jsonStr = Utilities.getStringFromFile(cacheHistoryJSON);
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
                if (historyList == null)
                    historyList = new ArrayList<>();
                else
                    historyList.clear();

                // 1 if ok, -2 if not

                try {

                    // Temporary's array
                    ArrayList<HistoryItem> tempErrorArray = new ArrayList<>();
                    ArrayList<HistoryItem> tempReadyArray = new ArrayList<>();
                    ArrayList<HistoryItem> tempPreparingArray = new ArrayList<>();
                    ArrayList<HistoryItem> tempDoneArray = new ArrayList<>();

                    // parse JSON
                    JSONObject json = new JSONObject(jsonStr);

                    // Get all history items
                    JSONArray jsData = new JSONArray(json.getString("history"));
                    for (int i = 0; i < jsData.length(); i++) {
                        JSONObject obj = jsData.getJSONObject(i);
                        String sDate = obj.getString("datetime");
                        String parsed = obj.getString("resume");
                        parsed = parsed.replaceAll("<br>", ", ");
                        HistoryItem hi = new HistoryItem(getContext(), parsed, obj.getInt("status"),
                                obj.getDouble("price"), sDate, obj.getInt("idcmd"), obj.getInt("modcmd"), obj.getString("strcmd"), false);

                        switch (obj.getInt("status")) {
                            case HistoryItem.STATUS_NOPAID:
                                tempErrorArray.add(hi);
                                break;
                            case HistoryItem.STATUS_READY:
                                tempReadyArray.add(hi);
                                break;
                            case HistoryItem.STATUS_PREPARING:
                                tempPreparingArray.add(hi);
                                break;
                            case HistoryItem.STATUS_DONE:
                                tempDoneArray.add(hi);
                                break;
                        }
                    }

                    // Add all data with headers if needed
                    int aSize = tempErrorArray.size();
                    if (aSize > 0) {
                        historyList.add(new HistoryItem(getString(R.string.cmd_status_unpaid)+ (aSize > 1 ? getString(R.string.plural).toUpperCase() : ""), false));
                    }
                    historyList.addAll(tempErrorArray);
                    if (aSize > 0) {
                        historyList.add(new HistoryItem(getString(R.string.cmd_status_error_txt1) + (aSize > 1 ? getString(R.string.plural) : getString(R.string.this_fr)) + " "+ getString(R.string.cmd_status_error_txt2) + (aSize > 1 ? getString(R.string.plural) : "") + " "+getString(R.string.cmd_status_error_txt3), true));
                    }

                    aSize = tempReadyArray.size();
                    if (aSize > 0) {
                        historyList.add(new HistoryItem(getString(R.string.cmd_status_ready) + (aSize > 1 ? getString(R.string.plural).toUpperCase() : ""), false));
                    }
                    historyList.addAll(tempReadyArray);
                    if (aSize > 0) {
                        historyList.add(new HistoryItem(getString(R.string.cmd_status_ready_txt), true));
                    }

                    aSize = tempPreparingArray.size();
                    if (aSize > 0) {
                        historyList.add(new HistoryItem(getString(R.string.cmd_status_preparing), false));
                    }
                    historyList.addAll(tempPreparingArray);

                    aSize = tempDoneArray.size();
                    if (aSize > 0) {
                        historyList.add(new HistoryItem(getString(R.string.cmd_status_finished) + (aSize > 1 ? getString(R.string.plural).toUpperCase() : ""), false));
                    }
                    historyList.addAll(tempDoneArray);

                    // save data
                    Utilities.writeStringToFile(cacheHistoryJSON, jsonStr);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            progressBar.setVisibility(View.GONE);
            if (jsonStr != null) {

                mAdapter.notifyDataSetChanged();

                // display "no command" or not
                if (historyList.size() == 0) {
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
     * Custom class to get information about cafeteria (current club ...)
     */
    private class AsyncInfoService extends AsyncTask <String, String, String> {

        private Activity a;

        public AsyncInfoService(Activity a){
            this.a = a;
        }

        @Override
        protected String doInBackground(String... params) {
            return ConnexionUtils.postServerData(Constants.URL_API_INFO_SERVICE, null, a);
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            if (Utilities.isNetworkDataValid(data)) {
                try {
                    JSONObject jsResp = new JSONObject(data);
                    JSONObject jsData = jsResp.getJSONObject("data");
                    String service = jsData.getString("service");

                    if (service.length() > 0) {
                        service = service.replace("\\n", "\n");
                        tvServiceInfo.setText(service);
                        tvServiceInfo.setVisibility(View.VISIBLE);
                    } else {
                        tvServiceInfo.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                tvServiceInfo.setVisibility(View.GONE);
            }
        }
    }
}
