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

package fr.bde_eseo.eseomega.plans;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.listeners.RecyclerViewDisabler;
import fr.bde_eseo.eseomega.utils.JSONUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by François L. on 22/12/2015.
 * Donne la liste des salles de l'ESEO Angers + un plan accessible
 */
public class PlansActivity extends AppCompatActivity {

    // UI
    private ProgressBar progCircle;
    private ImageView imgA, imgB;
    private TextView tv1, tv2;
    private RecyclerView recList;
    private MyRoomAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long timestamp;
    private RecyclerView.OnItemTouchListener disabler;

    // Toolbar
    private MenuItem mSearchAction;
    private EditText etSearch;
    private ImageView imgClear;
    private TextView tvSearchTitle;
    private boolean isSearchOpened = false;

    // Model
    private ArrayList<RoomItem> roomItems;
    private ArrayList<RoomItem> roomItemsDisplay;

    // Constants
    private final static String JSON_KEY_ARRAY = "salles";
    private final static String URI_IMG_PLANS = "assets://plan.jpg";
    private final static int LATENCY_REFRESH = 8; // 8 sec min between 2 refreshs

    // Cache managing
    private String cachePath;
    private File cacheFileEseo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // UI
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.plans_refresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.md_blue_800);
        progCircle = (ProgressBar) findViewById(R.id.progressPlans);
        tv1 = (TextView) findViewById(R.id.tvListNothing);
        tv2 = (TextView) findViewById(R.id.tvListNothing2);
        imgA = (ImageView) findViewById(R.id.imgNoPlans_A);
        imgB = (ImageView) findViewById(R.id.imgNoPlans_B);
        progCircle.setVisibility(View.GONE);
        progCircle.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_grey_500), PorterDuff.Mode.SRC_IN);
        tv1.setVisibility(View.GONE);
        tv2.setVisibility(View.GONE);
        imgA.setVisibility(View.GONE);
        imgB.setVisibility(View.GONE);
        disabler = new RecyclerViewDisabler();

        // I/O cache data
        cachePath = getCacheDir() + "/";
        cacheFileEseo = new File(cachePath + "salles.json");

        // Model / objects
        roomItems = new ArrayList<>();
        roomItemsDisplay = new ArrayList<>();
        mAdapter = new MyRoomAdapter(this, roomItemsDisplay);
        recList = (RecyclerView) findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(true);
        recList.setVisibility(View.VISIBLE);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        //recList.setLayoutManager(llm);
        mAdapter.notifyDataSetChanged();

        // Start download of data
        AsyncJSON asyncJSON = new AsyncJSON(true); // circle needed for first call
        asyncJSON.execute(Constants.URL_JSON_PLANS);

        // Hidden trick
        imgA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgA.setVisibility(View.INVISIBLE);
                imgB.setVisibility(View.VISIBLE);
            }
        });

        imgB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgB.setVisibility(View.INVISIBLE);
                imgA.setVisibility(View.VISIBLE);
            }
        });

        // Swipe-to-refresh implementations
        timestamp = 0;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Toast.makeText(getActivity(), "Refreshing ...", Toast.LENGTH_SHORT).show();
                long t = System.currentTimeMillis() / 1000;
                if (t - timestamp > LATENCY_REFRESH) { // timestamp in seconds)
                    timestamp = t;
                    AsyncJSON asyncJSON = new AsyncJSON(false); // no circle here (already in SwipeLayout)
                    asyncJSON.execute(Constants.URL_JSON_PLANS);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.length() > 0) {
                    searchInArray(newText);
                    addHeaders();
                } else {
                    fillItems();
                    addHeaders();
                }
                mAdapter.notifyDataSetChanged();
                if (roomItemsDisplay.size() > 0) recList.scrollToPosition(0);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                this.onBackPressed();
                return true;

            case R.id.action_plans_details:
                Intent i = new Intent(PlansActivity.this, BigPictureActivity.class);
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Download JSON data
     */
    public class AsyncJSON extends AsyncTask<String, String, JSONObject> {

        boolean displayCircle;

        public AsyncJSON (boolean displayCircle) {
            this.displayCircle = displayCircle;
        }

        @Override
        protected void onPreExecute() {
            recList.addOnItemTouchListener(disabler);
            if (roomItems != null) {
                roomItems.clear();
            }
            imgA.setVisibility(View.GONE);
            imgB.setVisibility(View.GONE);
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
            if (displayCircle) progCircle.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            if (jsonObject != null) {
                JSONArray array;
                try {

                    // Get / add data
                    array = jsonObject.getJSONArray(JSON_KEY_ARRAY);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        roomItems.add(new RoomItem(obj));
                    }

                    // Sort data
                    sortRoomArray();

                    // Add data
                    fillItems();

                    // Add categories
                    addHeaders();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (displayCircle) progCircle.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            } else {
                if (displayCircle) progCircle.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
                imgA.setVisibility(View.VISIBLE);
                tv1.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.VISIBLE);
            }
            swipeRefreshLayout.setRefreshing(false);
            recList.removeOnItemTouchListener(disabler);
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject obj = JSONUtils.getJSONFromUrl(params[0], PlansActivity.this);

            if (obj == null) {
                if (cacheFileEseo.exists()) {
                    try {
                        obj = new JSONObject(Utilities.getStringFromFile(cacheFileEseo));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Utilities.writeStringToFile(cacheFileEseo, obj.toString());
            }
            return obj;
        }
    }

    /**
     * Sort rooms by names
     */
    public void sortRoomArray () {
        Collections.sort(roomItems, new Comparator<RoomItem>() {
            @Override
            public int compare(RoomItem lhs, RoomItem rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });
    }

    /**
     * Restrict data in array (search mode)
     */
    private void searchInArray (String search) {
        String sLow = search.toLowerCase(Locale.FRANCE);
        roomItemsDisplay.clear();
        int size = roomItems.size();
        for (int i=0;i<size;i++) {
            RoomItem ci = roomItems.get(i);
            if (
                    // Recherche sur le nom
                    ci.getName().toLowerCase(Locale.FRANCE).contains(sLow)
                    ||
                    // Recherche sur la salle
                    ci.getNumber().toLowerCase(Locale.FRANCE).contains(sLow)
                    ||
                    // Recherche sur les infos
                    ci.getInfo().toLowerCase(Locale.FRANCE).contains(sLow)

            ) {
                roomItemsDisplay.add(ci);
            }
        }
    }

    /**
     * Add headers to items
     */
    private void addHeaders () {
        String sLetter = "A";
        boolean bLetter = true;
        int cnt = 0;

        // Add headers into list
        int size = roomItemsDisplay.size();
        for (int i=0;i<size;i++) {
            RoomItem ri = roomItemsDisplay.get(i);

            if (bLetter || !ri.getName().startsWith(sLetter)) {
                sLetter = ri.getName().charAt(0) + "";
                roomItemsDisplay.add(i, new RoomItem(sLetter));
                i++;
                cnt++;
                bLetter = false;
            }

            cnt++;
        }
    }

    /**
     * Add headers to list
     */
    private void fillItems () {
        roomItemsDisplay.clear();
        int size = roomItems.size();
        for (int i=0;i<size;i++) {
            RoomItem ri = roomItems.get(i);
            roomItemsDisplay.add(ri);
        }
    }
}
