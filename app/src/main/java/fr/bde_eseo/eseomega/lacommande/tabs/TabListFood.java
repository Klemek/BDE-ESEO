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

package fr.bde_eseo.eseomega.lacommande.tabs;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.interfaces.OnItemAddToCart;
import fr.bde_eseo.eseomega.lacommande.DataManager;
import fr.bde_eseo.eseomega.lacommande.ElementChooserActivity;
import fr.bde_eseo.eseomega.lacommande.IngredientsChooserActivity;
import fr.bde_eseo.eseomega.lacommande.MyFoodListAdapter;
import fr.bde_eseo.eseomega.lacommande.model.LacmdRoot;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.utils.JSONUtils;

/**
 * Created by François L.
 * Used to display categories to user : menus, drinks, sandwiches ...
 * Before display : Fill DataManager's database
 *
 * On item click -> new activity with element's list
 *
 * Get all content by parsing JSON over network
 */

public class TabListFood extends Fragment {

    private OnItemAddToCart mOnItemAddToCart;
    private MyFoodListAdapter mAdapter;
    private TextView tvNetStatus;
    private boolean activityStarted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_listfood,container,false);

        // User GUI
        tvNetStatus = (TextView) rootView.findViewById(R.id.tvNetStatusFoodList);

        // Flags
        activityStarted = false;

        // Search for the listView, then set its adapter
        mAdapter = new MyFoodListAdapter(getActivity());
        RecyclerView recList = (RecyclerView) rootView.findViewById(R.id.cardListFood);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mAdapter);

        // Try to download categories's data
        AsyncGetData asyncGetData = new AsyncGetData();
        asyncGetData.execute(Constants.URL_API_ORDER_ITEMS);

        recList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //Toast.makeText(getActivity(), "Link to JSON category : " + DataManager.getInstance().getCategories().get(position).toString(), Toast.LENGTH_LONG).show();

                final ArrayList<LacmdRoot> roots = DataManager.getInstance().arrayToCatArray(DataManager.getInstance().getCategories().get(position).getCatname());

                CharSequence items[] = new CharSequence[roots.size()];
                for (int i=0;i<roots.size();i++)
                    items[i] = roots.get(i).getName() + " ("+roots.get(i).getFormattedPrice()+")";

                // Material dialog to show list of items
                MaterialDialog md = new MaterialDialog.Builder(getActivity())
                        .items(items)
                        .title(getString(R.string.our) + " "+ DataManager.getInstance().getCategories().get(position).getName())
                        .cancelable(true) // faster for user
                        .positiveText(R.string.dialog_choose)
                        .negativeText(R.string.dialog_cancel)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                /**
                                 * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                                 * returning false here won't allow the newly selected radio button to actually be selected.
                                 **/

                                /*  ON ITEM SELECTION
                                    - If item has ingredients : launch the IngredientChooserActivity
                                    - If item has elements : launch the ElementChooserActivity
                                    - If item is just a simple item : add it to cart directly
                                 */
                                LacmdRoot root = roots.get(which);

                                if (root.hasElements() > 0) { // ElementChooserActivity
                                    Intent myIntent = new Intent(getActivity(), ElementChooserActivity.class);
                                    myIntent.putExtra(Constants.KEY_MENU_ID, root.getIdstr());
                                    getActivity().startActivity(myIntent);
                                    activityStarted = true;
                                } else if (root.hasIngredients() > 0) { // IngredientChooserActivity
                                    Intent myIntent = new Intent(getActivity(), IngredientsChooserActivity.class);
                                    myIntent.putExtra(Constants.KEY_ELEMENT_ID, root.getIdstr());
                                    myIntent.putExtra(Constants.KEY_ELEMENT_POSITION, -1); // not in a menu
                                    getActivity().startActivity(myIntent);
                                    activityStarted = true;
                                } else { // Add it to cart !
                                    DataManager.getInstance().addCartItem(root);
                                    Toast.makeText(getActivity(), "\"" + text + "\" "+getString(R.string.cafet_added), Toast.LENGTH_SHORT).show();
                                    mOnItemAddToCart.OnItemAddToCart();
                                }

                                return true;
                            }
                        })
                        .show();
            }
        }));

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOnItemAddToCart = (OnItemAddToCart) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (activityStarted && mOnItemAddToCart != null) {
            mOnItemAddToCart.OnItemAddToCart();
            activityStarted = false;
        }
    }

    /**
     * Asynctask to parse JSON data (categories, elements, sandwiches, all data)
     * We could display list of categories once everything is downloaded :)
     */
    private class AsyncGetData extends AsyncTask<String, String, JSONArray> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            tvNetStatus.setBackgroundColor(getActivity().getResources().getColor(R.color.md_prim_dark_yellow));
            tvNetStatus.setText(R.string.connecting);
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            return JSONUtils.getJSONArrayFromUrl2(params[0], getActivity());
        }

        @Override
        protected void onPostExecute(JSONArray array) {

            // If network is ok, fill cafet items
            if (array != null) {

                DataManager.getInstance().fillData(array);

                // Associate data with Adapter
                mAdapter.setFoodListArray(DataManager.getInstance().getCategories());
                mAdapter.notifyDataSetChanged();

                // Set GUI data
                tvNetStatus.setText(R.string.connected);
                tvNetStatus.setBackgroundColor(getActivity().getResources().getColor(R.color.md_prim_dark_blue));

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvNetStatus.setVisibility(View.GONE);
                    }
                }, 2000);

            } else {

                // Else set placeholder_error message to user
                tvNetStatus.setBackgroundColor(getActivity().getResources().getColor(R.color.md_prim_dark_red));
                tvNetStatus.setText(R.string.error_offline);
            }
        }
    }
}
