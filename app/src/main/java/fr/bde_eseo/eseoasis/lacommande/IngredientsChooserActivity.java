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

package fr.bde_eseo.eseoasis.lacommande;

import android.os.Bundle;
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

import fr.bde_eseo.eseoasis.Constants;
import fr.bde_eseo.eseoasis.R;
import fr.bde_eseo.eseoasis.lacommande.model.LacmdElement;
import fr.bde_eseo.eseoasis.lacommande.model.LacmdIngredient;
import fr.bde_eseo.eseoasis.lacommande.model.LacmdRoot;

/**
 * Created by François L. on 18/08/2015.
 * Displays a list of items associated with bundled data
 * Used to display checkboxes with ingredients inside
 */
public class IngredientsChooserActivity extends AppCompatActivity {

    private RecyclerView recList;
    private CheckboxListAdapter mAdapter;
    private TextView tvIngredients, tvAdd, tvStackMorePrice, tvStackMoreText;
    private ArrayList<CheckboxItem> checkboxItems;
    private Toolbar toolbar;
    private LacmdElement element;
    private String elementID;
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

        // Add ingredients to list's dataset
        for (int i=0;i<DataManager.getInstance().getIngredients().size();i++) {
            checkboxItems.add(
                new CheckboxItem(
                    DataManager.getInstance().getIngredients().get(i).getName(),
                    DataManager.getInstance().getIngredients().get(i).getIdstr(),
                    DataManager.getInstance().getIngredients().get(i).getPrice()
                )
            );
        }

        supplMore = 0;
        mAdapter.notifyDataSetChanged();
        tvStackMoreText.setVisibility(View.INVISIBLE);
        tvStackMorePrice.setVisibility(View.INVISIBLE);

        // Get parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Toast.makeText(IngredientsChooserActivity.this, R.string.toast_error, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                elementID = extras.getString(Constants.KEY_ELEMENT_ID);
                elemPos = extras.getInt(Constants.KEY_ELEMENT_POSITION);
                element = new LacmdElement(DataManager.getInstance().getElementFromID(elementID));
                getSupportActionBar().setTitle(getString(R.string.ingr_title_base) + " " + element.getName());
            }
        } else {
            elementID = (String) savedInstanceState.getSerializable(Constants.KEY_ELEMENT_ID);
            elemPos = (int) savedInstanceState.getSerializable(Constants.KEY_ELEMENT_POSITION);
            element = new LacmdElement(DataManager.getInstance().getElementFromID(elementID));
            getSupportActionBar().setTitle(getString(R.string.ingr_title_base) + " " + element.getName());
        }

        maxElements = element.hasIngredients();
        currentElements = 0;
        tvIngredients.setText(getString(R.string.ingr_text1) + " " + maxElements + " " +
                getString(R.string.ingr_text2) + (maxElements>0?"s":"") + " " +
                getString(R.string.ingr_text3));

        // Validation
        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAdd.setBackgroundColor(0x2fffffff);
                // Add all checked items
                ArrayList<LacmdRoot> items = new ArrayList<LacmdRoot>();
                for (int i=0;i<checkboxItems.size();i++) {
                    if (checkboxItems.get(i).isChecked())
                        items.add(checkboxItems.get(i));
                }
                element.setItems(items);

                if (elemPos == -1) {
                    Toast.makeText(IngredientsChooserActivity.this, "\"" + element.getName() + "\" " + getString(R.string.cafet_added), Toast.LENGTH_SHORT).show();
                    DataManager.getInstance().addCartItem(element);
                } else {
                    DataManager.getInstance().getMenu().getItems().set(elemPos, element);
                }
                IngredientsChooserActivity.this.finish();
            }
        });
    }

    /**
     * Custom adapter to handle checkbox events
     */
    private class CheckboxListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CheckBoxHolder cbh = new CheckBoxHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check, parent, false));

            return cbh;
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
                        checkboxItems.get(position).setChecked(cb.isChecked());
                        currentElements += cb.isChecked()?1:-1;
                        if (currentElements > maxElements) {
                            supplMore = checkboxItems.get(position).getPrice()*(currentElements - maxElements);
                            tvStackMorePrice.setVisibility(View.VISIBLE);
                            tvStackMorePrice.setText(new DecimalFormat("0.00").format(supplMore) + "€");
                            tvStackMoreText.setVisibility(View.VISIBLE);
                        } else {
                            tvStackMorePrice.setVisibility(View.INVISIBLE);
                            tvStackMoreText.setVisibility(View.INVISIBLE);
                        }
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

    /**
     * Custom class to handle on checkbox
     */
    private class CheckboxItem extends LacmdRoot {
        private boolean checked;
        private String more;
        private boolean visible;

        public CheckboxItem (String name, String id, double price) {
            super(name, id, 0, 0, price, LacmdIngredient.ID_CAT_INGREDIENT);
            checked = false;
            this.visible = false;
            this.more = this.price == 0.0?"":"+" + new DecimalFormat("0.00").format(this.price) + "€";
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
