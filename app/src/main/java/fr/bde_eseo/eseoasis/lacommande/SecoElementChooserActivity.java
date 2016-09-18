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

package fr.bde_eseo.eseoasis.lacommande;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

import fr.bde_eseo.eseoasis.R;
import fr.bde_eseo.eseoasis.lacommande.model.LacmdElement;
import fr.bde_eseo.eseoasis.lacommande.model.LacmdIngredient;
import fr.bde_eseo.eseoasis.lacommande.model.LacmdMenu;
import fr.bde_eseo.eseoasis.lacommande.model.LacmdRoot;

/**
 * Created by François L. on 24/08/2015.
 * Displays a list of items associated with bundled data
 * Used to display checkboxes with elements inside
 */
public class SecoElementChooserActivity extends AppCompatActivity {

    private RecyclerView recList;
    private CheckboxListAdapter mAdapter;
    private TextView tvIngredients, tvAdd, tvStackMorePrice, tvStackMoreText;
    private ArrayList<CheckboxItem> checkboxItems;
    private Toolbar toolbar;
    private LacmdMenu menu;
    private String menuID;
    private int maxElements, currentElements, elemPos;
    private double supplMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        setSupportActionBar(toolbar);

        // Objets & UI
        tvAdd = (TextView) findViewById(R.id.tvValid);
        tvIngredients = (TextView) findViewById(R.id.tv_act_ingr_desc);
        tvStackMorePrice = (TextView) findViewById(R.id.tvStackMorePrice);
        tvStackMoreText = (TextView) findViewById(R.id.tvStackMoreText);
        checkboxItems = new ArrayList<>();
        mAdapter = new CheckboxListAdapter();
        recList = (RecyclerView) findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        currentElements = 0;

        // Add elemnts without ingredients to list's dataset
        for (int i = 0; i < DataManager.getInstance().getElements().size(); i++) {
            if (DataManager.getInstance().getElements().get(i).hasIngredients() == 0 &&
                    DataManager.getInstance().getElements().get(i).getOutofmenu() == 0)
                checkboxItems.add(
                        new CheckboxItem(
                                DataManager.getInstance().getElements().get(i).getName(),
                                DataManager.getInstance().getElements().get(i).getIdstr(),
                                DataManager.getInstance().getElements().get(i).getPrice()
                        )
                );
        }

        for (int i = 0; i < DataManager.getInstance().getMenu().getItems().size(); i++) {
            LacmdRoot element = DataManager.getInstance().getMenu().getItems().get(i);
            if (element.hasIngredients() == 0) {
                if (element.getName().length() > 0) {
                    setArrayCheck(element.getIdstr());
                    currentElements++;
                }
            }
        }

        supplMore = 0;
        mAdapter.notifyDataSetChanged();
        tvStackMoreText.setVisibility(View.INVISIBLE);
        tvStackMorePrice.setVisibility(View.INVISIBLE);

        getSupportActionBar().setTitle(R.string.cafet_choose3);
        menu = DataManager.getInstance().getMenu();
        maxElements = menu.getMaxSecoElem();
        tvIngredients.setText(getString(R.string.cafet_choose4) +" " + maxElements +
                " " +getString(R.string.cafet_elem) + (maxElements > 0 ? "s" : "") +
                " "+getString(R.string.cafet_choose5));

        // Validation
        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tvAdd.setBackgroundColor(0x2fffffff);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvAdd.setBackgroundColor(0x00ffffff);
                    }
                }, 500);

                if (currentElements == maxElements) {

                    // Remove old items
                    for (int i = 0; i < DataManager.getInstance().getMenu().getItems().size(); i++) {
                        LacmdRoot element = DataManager.getInstance().getMenu().getItems().get(i);
                        if (element.hasIngredients() == 0) {
                            DataManager.getInstance().getMenu().getItems().remove(i);
                            i--;
                        }
                    }

                    // Add all checked items
                    for (int i = 0; i < checkboxItems.size(); i++) {
                        if (checkboxItems.get(i).isChecked()) {
                            DataManager.getInstance().getMenu().getItems().add(
                                    new LacmdElement(DataManager.getInstance().getElementFromID(checkboxItems.get(i).getIdstr())));
                        }
                    }

                    SecoElementChooserActivity.this.finish();
                } else {
                    Toast.makeText(SecoElementChooserActivity.this, R.string.toast_cafet_select, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private class CheckboxListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new CheckBoxHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (checkboxItems != null) {
                final CheckBoxHolder cbh = (CheckBoxHolder) holder;
                cbh.checkBox.setChecked(checkboxItems.get(position).isChecked());
                cbh.checkBox.setText(checkboxItems.get(position).getName());
                cbh.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        currentElements += cb.isChecked() ? 1 : -1;
                        if (currentElements > maxElements) {
                            cb.setChecked(false);
                            currentElements--;
                            Toast.makeText(SecoElementChooserActivity.this, R.string.toast_cafet_max, Toast.LENGTH_SHORT).show();
                        }
                        checkboxItems.get(position).setChecked(cb.isChecked());
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return checkboxItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        // Holder for checkbox item
        public class CheckBoxHolder extends RecyclerView.ViewHolder {

            protected CheckBox checkBox;
            protected TextView tvMore;

            public CheckBoxHolder(View itemView) {
                super(itemView);
                checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
                tvMore = (TextView) itemView.findViewById(R.id.tvSuppl);
            }
        }
    }

    private void setArrayCheck(String idstr) {
        for (int i = 0; i < checkboxItems.size(); i++) {
            if (checkboxItems.get(i).getIdstr().equals(idstr)) {
                checkboxItems.get(i).setChecked(true);
            }
        }
    }

    private class CheckboxItem extends LacmdRoot {
        private boolean checked;
        private String more;
        private boolean visible;

        public CheckboxItem(String name, String id, double price) {
            super(name, id, 0, 0, price, LacmdIngredient.ID_CAT_INGREDIENT);
            checked = false;
            this.visible = false;
            this.more = this.price == 0.0 ? "" : "+" + new DecimalFormat("0.00").format(this.price) + "€";
        }

        public boolean isVisible() {
            return visible;
        }

        public String getMore() {
            return more;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isChecked() {

            return checked;
        }
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
