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

package fr.bde_eseo.eseomega.rooms;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.listeners.RecyclerViewDisabler;
import fr.bde_eseo.eseomega.utils.DateUtils;
import fr.bde_eseo.eseomega.utils.JSONUtils;
import fr.bde_eseo.eseomega.utils.Utils;

/**
 * Created by François L. on 22/12/2015.
 * Donne la liste des salles de l'ESEO Angers + un plan accessible
 */
public class RoomsListFragment extends Fragment {

    // Constants
    private final static String URI_IMG_PLANS = "assets://plan.jpg";
    private final static int LATENCY_REFRESH = 8; // 8 sec min between 2 refreshs
    private static final int SORT_NAME = 0;
    private static final int SORT_BAT = 1;
    private static final int SORT_FLOOR = 2;
    // UI
    private ProgressBar progCircle;
    private ImageView imgA;
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
    // Cache managing
    private String cachePath;
    private File cacheFileEseo;
    private int sortType;
    private AsyncJSON asyncJSON;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_simple_list, container, false);

        setHasOptionsMenu(true);

        sortType = 0;

        // UI
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        swipeRefreshLayout.setColorSchemeColors(Utils.resolveColorFromTheme(getContext(), R.attr.colorPrimaryDark));
        progCircle = (ProgressBar) rootView.findViewById(R.id.progress);
        imgA = (ImageView) rootView.findViewById(R.id.imgA);
        imgA.setImageResource(R.drawable.img_nothing);
        tv1 = (TextView) rootView.findViewById(R.id.tvListNothing);
        tv1.setText(R.string.empty_header_plans);
        tv2 = (TextView) rootView.findViewById(R.id.tvListNothing2);
        tv2.setText(R.string.empty_desc_plans);
        progCircle.setVisibility(View.GONE);
        progCircle.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_grey_500), PorterDuff.Mode.SRC_IN);
        tv1.setVisibility(View.GONE);
        tv2.setVisibility(View.GONE);
        disabler = new RecyclerViewDisabler();

        // I/O cache data
        cachePath = getActivity().getCacheDir() + "/";
        cacheFileEseo = new File(cachePath + "salles.json");

        // Model / objects
        roomItems = new ArrayList<>();
        roomItemsDisplay = new ArrayList<>();
        mAdapter = new MyRoomAdapter(getActivity(), roomItemsDisplay);
        recList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(true);
        recList.setVisibility(View.VISIBLE);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        //recList.setLayoutManager(llm);
        mAdapter.notifyDataSetChanged();

        // Start download of data
        asyncJSON = new AsyncJSON(true); // circle needed for first call
        asyncJSON.execute(Constants.URL_JSON_PLANS);

        // Swipe-to-refresh implementations
        timestamp = 0;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        /*swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Toast.makeText(getActivity(), "Refreshing ...", Toast.LENGTH_SHORT).show();
                long t = System.currentTimeMillis() / 1000;
                if (t - timestamp > LATENCY_REFRESH) { // timestamp in seconds)
                    timestamp = t;
                    AsyncJSONFamily asyncJSON = new AsyncJSONFamily(false); // no circle here (already in SwipeLayout)
                    asyncJSON.execute(Constants.URL_JSON_PLANS);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });*/
        return rootView;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        if(asyncJSON != null)asyncJSON.cancel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rooms, menu);
        super.onCreateOptionsMenu(menu, inflater);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;

            case R.id.action_plans_details:
                Intent i = new Intent(getActivity(), BigPictureActivity.class);
                startActivity(i);
                return true;

            case R.id.action_sort:
                sortType++;
                if(sortType>SORT_FLOOR)
                    sortType=SORT_NAME;
                sortRoomArray();
                fillItems();
                addHeaders();
                mAdapter.notifyDataSetChanged();
                String msg = "";
                switch(sortType){
                    case SORT_BAT:
                        msg = getString(R.string.toast_sort_bat);
                        break;
                    case SORT_FLOOR:
                        msg = getString(R.string.toast_sort_floor);
                        break;
                    case SORT_NAME:
                        msg = getString(R.string.toast_sort_names);
                        break;
                }
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sort rooms by names
     */
    public void sortRoomArray() {
        Collections.sort(roomItems, new Comparator<RoomItem>() {
            @Override
            public int compare(RoomItem lhs, RoomItem rhs) {
                switch (sortType) {
                    case SORT_BAT:
                        if (lhs.getBatiment() != rhs.getBatiment())
                            return lhs.getBatiment().compareToIgnoreCase(rhs.getBatiment());
                        return lhs.getNumber().compareToIgnoreCase(rhs.getNumber());
                    case SORT_FLOOR:
                        if (lhs.getFloor() != rhs.getFloor())
                            return lhs.getFloor() - rhs.getFloor();
                        return lhs.getName().compareToIgnoreCase(rhs.getName());
                    default:
                        return lhs.getName().compareToIgnoreCase(rhs.getName());
                }

            }
        });
    }

    /**
     * Restrict data in array (search mode)
     */
    private void searchInArray(String search) {
        String sLow = search.toLowerCase(DateUtils.getLocale());
        roomItemsDisplay.clear();
        int size = roomItems.size();
        for (int i = 0; i < size; i++) {
            RoomItem ci = roomItems.get(i);
            if (
                // Recherche sur le nom
                    ci.getName().toLowerCase(DateUtils.getLocale()).contains(sLow)
                            ||
                            // Recherche sur la salle
                            ci.getNumber().toLowerCase(DateUtils.getLocale()).contains(sLow)
                            ||
                            // Recherche sur les infos
                            ci.getInfo().toLowerCase(DateUtils.getLocale()).contains(sLow)

                    ) {
                roomItemsDisplay.add(ci);
            }
        }
    }

    /**
     * Add headers to items
     */
    private void addHeaders() {
        // Add headers into list
        String actual;
        int size = roomItemsDisplay.size();
        if (size > 0) {
            switch (sortType) {
                case SORT_NAME:
                    actual = "" + roomItemsDisplay.get(0).getName().charAt(0);
                    for (int i = 0; i < size; i++) {
                        RoomItem ri = roomItemsDisplay.get(i);

                        if (i == 0 || !ri.getName().startsWith(actual)) {
                            actual = ri.getName().charAt(0) + "";
                            roomItemsDisplay.add(i, new RoomItem(actual));
                            i++;
                        }
                    }
                    break;
                case SORT_BAT:
                    actual = getString(R.string.room_bat) + " " + roomItemsDisplay.get(0).getBatiment();
                    for (int i = 0; i < size; i++) {
                        RoomItem ri = roomItemsDisplay.get(i);
                        if (i == 0 || !(getString(R.string.room_bat) + " " + ri.getBatiment()).equals(actual)) {
                            actual = getString(R.string.room_bat) + " " + ri.getBatiment();
                            roomItemsDisplay.add(i, new RoomItem(actual));
                            i++;
                        }
                    }
                    break;
                case SORT_FLOOR:
                    actual = getString(R.string.room_floor) + " " + roomItemsDisplay.get(0).getFloor();
                    for (int i = 0; i < size; i++) {
                        RoomItem ri = roomItemsDisplay.get(i);
                        if (i == 0 || !(getString(R.string.room_floor) + " " + ri.getFloor()).equals(actual)) {
                            actual = getString(R.string.room_floor) + " " + ri.getFloor();
                            roomItemsDisplay.add(i, new RoomItem(actual));
                            i++;
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Add headers to list
     */
    private void fillItems() {
        roomItemsDisplay.clear();
        int size = roomItems.size();
        for (int i = 0; i < size; i++) {
            RoomItem ri = roomItems.get(i);
            roomItemsDisplay.add(ri);
        }
    }

    /**
     * Download JSON data
     */
    public class AsyncJSON extends AsyncTask<String, String, JSONArray> {

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
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
            if (displayCircle) progCircle.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONArray array) {

            if (array != null) {
                try {

                    // Get / add data
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        roomItems.add(new RoomItem(getContext(), obj));
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
        protected JSONArray doInBackground(String... params) {

            JSONArray array = JSONUtils.getJSONArrayFromUrl(params[0]);

            if (array == null) {
                if (cacheFileEseo.exists()) {
                    try {
                        array = new JSONArray(Utils.getStringFromFile(cacheFileEseo));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Utils.writeStringToFile(cacheFileEseo, array.toString());
            }
            return array;
        }
    }
}
