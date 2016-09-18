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

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import fr.bde_eseo.eseoasis.R;
import fr.bde_eseo.eseoasis.lacommande.model.LacmdCategory;

/**
 * Created by François on 24/04/2015.
 */

public class MyFoodListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<LacmdCategory> foodCategoryArrayList;
    private Context context;

    public static final int TYPE_FOOD_ITEM = 0;
    private static final int TYPE_NUMBER = TYPE_FOOD_ITEM + 1;

    public MyFoodListAdapter(Context context) {
        this.foodCategoryArrayList = new ArrayList<>();
        this.context = context;
    }

    public void setFoodListArray(ArrayList<LacmdCategory> foodCategoryArrayList) {
        if (foodCategoryArrayList != null) {
            this.foodCategoryArrayList = foodCategoryArrayList;
            notifyDataSetChanged();
        }
    }

    public LacmdCategory getItem(int position) {
        return foodCategoryArrayList.get(position);
    }

    @Override
    public int getItemCount() {
        return foodCategoryArrayList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        LacmdCategory fc = foodCategoryArrayList.get(position);

        if (getItemViewType(position) == TYPE_FOOD_ITEM) { // Food Item
            FoodListViewHolder fvh = (FoodListViewHolder) viewHolder;

            fvh.vName.setText(fc.getName());
            fvh.vPrice.setText(fc.getBeginPriceAsStr());
            fvh.vSmallText.setText(fc.getSmallText());

            Picasso.with(context).load(fc.getImgUrl()).placeholder(R.drawable.placeholder).error(R.drawable.placeholder_error).into(fvh.vImg);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                fvh.cardView.setPreventCornerOverlap(false);
            } else {
                fvh.cardView.setPreventCornerOverlap(true); // Only supported if Android Version is >= Lollipop
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_FOOD_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        switch (viewType) {
            case TYPE_FOOD_ITEM:
                return new FoodListViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_food_image, viewGroup, false));
        }

        return null;
    }

    // Classic View Holder for Food
    public static class FoodListViewHolder extends RecyclerView.ViewHolder {

        protected TextView vName;
        protected TextView vSmallText;
        protected ImageView vImg;
        protected TextView vPrice;
        protected CardView cardView;

        public FoodListViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.foodTitle);
            vSmallText = (TextView)  v.findViewById(R.id.foodSmall);
            vImg = (ImageView) v.findViewById(R.id.foodPicture);
            vPrice = (TextView) v.findViewById(R.id.foodMore);
            cardView = (CardView) v.findViewById(R.id.card_food_list);
        }
    }
}