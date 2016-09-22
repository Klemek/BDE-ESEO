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

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.lacommande.model.LacmdRoot;

/**
 * Created by François L. on 19/08/2015.
 * Simple adapter for cart's items easy view
 */
public class MyCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private Context ctx;
    public MyCartAdapter (Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CartItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_cart, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final LacmdRoot root = DataManager.getInstance().getCartArray().get(position);

        CartItemHolder cih = (CartItemHolder) holder;
        cih.tvName.setText(root.getName());
        String desc = root.getFriendlyString(false);
        if (desc.length() == 0) cih.tvDetails.setVisibility(View.GONE);
        else {
            cih.tvDetails.setVisibility(View.VISIBLE);
            cih.tvDetails.setText(desc);
        }
        cih.tvPrice.setText(root.getFormattedPrice());

    }

    @Override
    public int getItemCount() {
        return DataManager.getInstance().getNbCartItems();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    // Holder for cart item
    public class CartItemHolder extends RecyclerView.ViewHolder {

        protected TextView tvPrice, tvName, tvDetails;
        protected CardView cardView;

        public CartItemHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvNameCartItem);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPriceCartItem);
            tvDetails = (TextView) itemView.findViewById(R.id.tvDetailsCartItem);
            cardView = (CardView) itemView.findViewById(R.id.cardCart);
        }
    }
}
