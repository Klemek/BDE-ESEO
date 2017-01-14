/**
 * Copyright (C) 2016 - François LEPAROUX
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.bde_eseo.eseomega.lacommande;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.lacommande.model.LacmdElement;
import fr.bde_eseo.eseomega.lacommande.model.LacmdMenu;
import fr.bde_eseo.eseomega.lacommande.model.LacmdRoot;
import fr.bde_eseo.eseomega.utils.ThemeUtils;

/**
 * Created by François L. on 24/08/2015.
 */
public class ElementChooserActivity extends AppCompatActivity {

    private MenuListAdapter mAdapter;
    private TextView tvAdd;
    private TextView tvStackMorePrice;
    private TextView tvStackMoreText;
    private LacmdMenu menuTemp, menu;
    private int nbSandw, nbElems;
    private boolean activityStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeUtils.preferredTheme(getApplicationContext()));
        setContentView(R.layout.activity_ingredients);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        setSupportActionBar(toolbar);

        // Objets & UI
        tvAdd = (TextView) findViewById(R.id.tvValid);
        TextView tvIngredients = (TextView) findViewById(R.id.tv_act_ingr_desc);
        tvStackMorePrice = (TextView) findViewById(R.id.tvStackMorePrice);
        tvStackMoreText = (TextView) findViewById(R.id.tvStackMoreText);
        mAdapter = new MenuListAdapter();
        RecyclerView recList = (RecyclerView) findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        // Get parameters
        String menuID;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Toast.makeText(ElementChooserActivity.this, R.string.toast_error, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                menuID = extras.getString(Constants.KEY_MENU_ID);
                menuTemp = DataManager.getInstance().getMenuFromID(menuID);
                getSupportActionBar().setTitle(getString(R.string.cafet_menu)+" " + menuTemp.getName());
            }
            menuTemp.setItems(new ArrayList<LacmdRoot>());
            DataManager.getInstance().setMenu(menuTemp);
        } else {
            menuID = (String) savedInstanceState.getSerializable(Constants.KEY_MENU_ID);
            menuTemp = DataManager.getInstance().getMenuFromID(menuID);
            getSupportActionBar().setTitle(getString(R.string.cafet_menu)+" " + menuTemp.getName());
            DataManager.getInstance().setMenu(menuTemp);
        }

        // copy menu
        menu = DataManager.getInstance().getMenu();

        nbSandw = menu.getMaxMainElem();
        nbElems = menu.getMaxSecoElem();
        menu.setItems(new ArrayList<LacmdRoot>());
        for (int i = 0; i < nbSandw; i++)
            menu.getItems().add(new LacmdElement("", "", 0.0, 0.0, 0, 0, 1));
        if (nbElems > 0)
            menu.getItems().add(new LacmdElement("", "", 0.0, 0.0, 0, 0, 0));

        double supplMore = 0;
        mAdapter.notifyDataSetChanged();
        tvStackMoreText.setVisibility(View.INVISIBLE);
        tvStackMorePrice.setVisibility(View.INVISIBLE);

        tvIngredients.setVisibility(View.GONE);

        // Validation
        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAdd.setBackgroundColor(0x2fffffff);

                // Everything is checked ?
                boolean ok = true;
                for (int i = 0; i < menu.getItems().size(); i++) {
                    if (menu.getItems().get(i).getFriendlyString(true).length() == 0) {
                        ok = false;
                    }
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvAdd.setBackgroundColor(0x00ffffff);
                    }
                }, 500);

                if (ok) {
                    Toast.makeText(ElementChooserActivity.this, getString(R.string.cafet_menu)+" \"" + menu.getName() +" " + getString(R.string.cafet_added), Toast.LENGTH_SHORT).show();
                    DataManager.getInstance().addCartItem(menu);
                    ElementChooserActivity.this.finish();
                } else {
                    new MaterialDialog.Builder(ElementChooserActivity.this)
                            .title(R.string.hey)
                            .content(R.string.cafet_cant_send)
                            .cancelable(false)
                            .negativeText(R.string.dialog_back)
                            .show();
                }
            }
        });
    }

    // A method to find height of the status bar
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (activityStarted && mAdapter != null) {
            activityStarted = false;
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activityStarted && mAdapter != null && menu != null) {
            activityStarted = false;

            mAdapter.adapterObjects.clear();
            String str = "";
            for (int i = 0; i < menu.getItems().size(); i++) {

                if (menu.getItems().get(i).hasIngredients() > 0) {
                    mAdapter.adapterObjects.add(new CustomObjItem(menu.getItems().get(i).getName(),
                            menu.getItems().get(i).getIdstr(),
                            menu.getItems().get(i).getPrice(),
                            true));
                } else {
                    if (str.length() == 0)
                        str += "\n";
                    str += menu.getItems().get(i).getName();
                }
            }
            mAdapter.adapterObjects.add(new CustomObjItem(str, "", 0.0, false));

            mAdapter.notifyDataSetChanged();
            double total = 0.0;
            for (int i = 0; i < menu.getItems().size(); i++) {
                total += menu.getItems().get(i).calcRealPrice(true);
            }

            if (total > 0) {
                tvStackMoreText.setVisibility(View.VISIBLE);
                tvStackMorePrice.setVisibility(View.VISIBLE);
                tvStackMorePrice.setText(new DecimalFormat("0.00").format(total) + "€");
            } else {
                tvStackMoreText.setVisibility(View.INVISIBLE);
                tvStackMorePrice.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Custom adapter
     */
    public class MenuListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public final ArrayList<CustomObjItem> adapterObjects;

        public MenuListAdapter() {
            adapterObjects = new ArrayList<>();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new MenuItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_menu, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            if (menu.getItems() != null) {
                final MenuItemHolder mih = (MenuItemHolder) holder;

                final LacmdElement elem = (LacmdElement) menu.getItems().get(position);

                if (elem.hasIngredients() > 0) {
                    mih.tvHeader.setText(getString(R.string.cafet_choose) +" " + (position + 1) + (position == 0 ? getString(R.string.st) : (position == 1 ? getString(R.string.nd) : (position == 2 ? getString(R.string.rd) : getString(R.string.th))))+" "+ getString(R.string.cafet_main));
                    if (elem.getFriendlyString(true).length() == 0) {
                        mih.tvName.setText(getString(R.string.touch));
                    } else {
                        mih.tvName.setText(elem.getName());
                    }

                } else {

                    if (position - nbSandw == 0) {
                        mih.tvHeader.setText(getString(R.string.cafet_choose2) +" "+ nbElems +" "+ getString(R.string.cafet_second));
                        mih.tvHeader.setVisibility(View.VISIBLE);
                    } else {
                        mih.tvHeader.setVisibility(View.GONE);
                    }
                    mih.tvName.setText(elem.getName().length() == 0 ? getString(R.string.touch) : elem.getName());
                }


                double price = elem.calcRealPrice(true);
                if (price > 0)
                    mih.tvMore.setText("+" + new DecimalFormat("0.00").format(elem.calcRealPrice(true)) + "€");
                else
                    mih.tvMore.setText("");

                mih.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // If is sandwich : call IngredientChooser
                        if (elem.hasIngredients() > 0) {
                            String elems = menu.getMainElemStr(); // sandw|panini etc
                            StringTokenizer st = new StringTokenizer(elems, "|");

                            final int nb = st.countTokens();
                            CharSequence items[] = new CharSequence[nb];
                            final ArrayList<String> strings = new ArrayList<>();

                            for (int i = 0; i < nb; i++) {
                                String s = st.nextToken();
                                strings.add(s);
                                items[i] = DataManager.getInstance().getElementFromID(s).getName();
                            }

                            new MaterialDialog.Builder(ElementChooserActivity.this)
                                    .title(R.string.cafet_main2)
                                    .items(items)
                                    .cancelable(true) // faster for user
                                    .positiveText(R.string.personnalize)
                                    .negativeText(R.string.dialog_cancel)
                                    .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                        @Override
                                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                            Intent myIntent = new Intent(ElementChooserActivity.this, IngredientsChooserActivity.class);
                                            myIntent.putExtra(Constants.KEY_ELEMENT_ID, strings.get(which));
                                            myIntent.putExtra(Constants.KEY_ELEMENT_POSITION, position);
                                            startActivity(myIntent);
                                            activityStarted = true;

                                            return true;
                                        }
                                    })
                                    .show();
                        } else { // call seco element chooser
                            Intent myIntent = new Intent(ElementChooserActivity.this, SecoElementChooserActivity.class);
                            startActivity(myIntent);
                            activityStarted = true;
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return menu.getItems().size();
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        // Holder for menu item
        public class MenuItemHolder extends RecyclerView.ViewHolder {

            final TextView tvName;
            final TextView tvHeader;
            final TextView tvMore;
            final CardView cardView;

            public MenuItemHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tvNameCartItem);
                tvMore = (TextView) itemView.findViewById(R.id.tvPriceCartItem);
                tvHeader = (TextView) itemView.findViewById(R.id.menuHeader);
                cardView = (CardView) itemView.findViewById(R.id.card);
            }
        }
    }

    /**
     * Mixed
     */
    private class CustomObjItem {
        private final String idstr;
        private final double priceMore;
        private final boolean isMain;
        private String name;

        public CustomObjItem(String name, String idstr, double priceMore, boolean isMain) {
            this.name = name;
            this.idstr = idstr;
            this.priceMore = priceMore;
            this.isMain = isMain;
        }

        public String getName() {
            if (name == null || name.length() == 0)
                return "-- Touchez ici --";
            else
                return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
