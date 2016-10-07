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

package fr.bde_eseo.eseomega.family;

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
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.listeners.RecyclerViewDisabler;
import fr.bde_eseo.eseomega.utils.JSONUtils;

public class StudentSearchFragment extends Fragment {

    // UI
    private ProgressBar progCircle;
    private ImageView imgA;
    private TextView tv1, tv2;
    private RecyclerView recList;
    private MyStudentAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long timestamp;
    private RecyclerView.OnItemTouchListener disabler;

    // Toolbar
    private MenuItem mSearchAction;
    private EditText etSearch;
    private ImageView imgClear;
    private TextView tvSearchTitle;
    private AsyncJSONSearch asyncJSONSearch;
    private boolean isSearchOpened = false;

    // Model
    private ArrayList<StudentItem> studentItems;

    // Constants
    private final static int LATENCY_REFRESH = 8; // 8 sec min between 2 refreshs

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_simple_list, container, false);

        setHasOptionsMenu(true);

        // UI
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        swipeRefreshLayout.setColorSchemeColors(R.color.color_primary_dark);
        progCircle = (ProgressBar) rootView.findViewById(R.id.progress);
        imgA = (ImageView) rootView.findViewById(R.id.imgA);
        imgA.setImageResource(R.drawable.img_family_search);
        tv1 = (TextView) rootView.findViewById(R.id.tvListNothing);
        tv2 = (TextView) rootView.findViewById(R.id.tvListNothing2);
        progCircle.setVisibility(View.GONE);
        progCircle.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_grey_500), PorterDuff.Mode.SRC_IN);
        tv1.setVisibility(View.GONE);
        tv2.setText(R.string.empty_desc_family);
        disabler = new RecyclerViewDisabler();

        // Model / objects
        studentItems = new ArrayList<>();
        mAdapter = new MyStudentAdapter(studentItems);
        recList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(true);
        recList.setVisibility(View.VISIBLE);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        /*
        Intent intent = new Intent(, FamilyTreeActivity.class);
                intent.putExtra("id",ri.getId());
                v.getContext().startActivity(intent);
         */
        recList.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // Start activity
                        Intent intent = new Intent(getActivity(), FamilyTreeActivity.class);
                        intent.putExtra("id",studentItems.get(position).getId());
                        startActivity(intent);
                    }
                })
        );

        recList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d("debug","scrolled "+dy);
            }
        });

        //recList.setLayoutManager(llm);
        mAdapter.notifyDataSetChanged();

        /*// Start download of data
        AsyncJSONSearch asyncJSONSearch = new AsyncJSONSearch(true); // circle needed for first call
        asyncJSONSearch.execute(Constants.URL_JSON_PLANS);*/

        // Swipe-to-refresh implementations
        timestamp = 0;
        return rootView;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(asyncJSONSearch != null)asyncJSONSearch.cancel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_families, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {return false;}

            @Override
            public boolean onQueryTextChange(String newText) {
                if (studentItems != null) {
                    studentItems.clear();
                }else{
                    studentItems = new ArrayList<>();
                }
                imgA.setVisibility(View.GONE);
                tv2.setVisibility(View.GONE);
                progCircle.setVisibility(View.GONE);
                if(asyncJSONSearch != null && asyncJSONSearch.getStatus() != AsyncTask.Status.FINISHED){
                    asyncJSONSearch.cancel(true);
                }
                asyncJSONSearch = new AsyncJSONSearch(studentItems.isEmpty());

                if (newText.length() > 2) {
                    asyncJSONSearch.execute(Constants.URL_JSON_STUDENT_SEARCH + newText);
                } else {
                    imgA.setVisibility(View.VISIBLE);
                    tv2.setVisibility(View.VISIBLE);
                    imgA.setImageResource(R.drawable.img_family_search);
                    tv2.setText(R.string.empty_desc_family);
                    mAdapter.notifyDataSetChanged();
                }
                //mAdapter.notifyDataSetChanged();
                if (studentItems.size() > 0) recList.scrollToPosition(0);
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
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Download JSON data
     */
    public class AsyncJSONSearch extends AsyncTask<String, String, JSONArray> {

        boolean displayCircle;

        public AsyncJSONSearch(boolean displayCircle) {
            this.displayCircle = displayCircle;
        }

        @Override
        protected void onPreExecute() {
            recList.addOnItemTouchListener(disabler);
            if (displayCircle) progCircle.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONArray array) {
            if (array != null && array.length() > 0) {
                try {
                    // Get / add data
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        studentItems.add(new StudentItem(obj));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                imgA.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.VISIBLE);
                imgA.setImageResource(R.drawable.img_nothing);
                tv2.setText(R.string.empty_desc_nothing_family);
            }
            progCircle.setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
            recList.removeOnItemTouchListener(disabler);
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected JSONArray doInBackground(String... params) {

            JSONArray array = JSONUtils.getJSONArrayFromUrl(params[0]);
            return array;
        }

        @Override
        protected void onCancelled() {
            recList.removeOnItemTouchListener(disabler);
        }
    }
}
