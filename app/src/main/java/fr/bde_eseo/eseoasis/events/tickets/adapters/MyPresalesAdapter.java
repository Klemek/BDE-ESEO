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

package fr.bde_eseo.eseoasis.events.tickets.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import fr.bde_eseo.eseoasis.R;
import fr.bde_eseo.eseoasis.events.tickets.model.TicketPictItem;

/**
 * Created by François L. on 11/01/2016.
 * Adapter pour l'historique des commandes évènements
 */
public class MyPresalesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<TicketPictItem> ticketPictItems;
    private Context context;

    public MyPresalesAdapter(Context context) {
        this.ticketPictItems = new ArrayList<>();
        this.context = context;
    }

    public void setPictItems(ArrayList<TicketPictItem> ticketPictItems) {
        if (ticketPictItems != null) {
            this.ticketPictItems = ticketPictItems;
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PresalesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_ticket_pict, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TicketPictItem tpi = ticketPictItems.get(position);
        PresalesViewHolder pvh = (PresalesViewHolder) holder;
        pvh.vTitle.setText(tpi.getTitle());
        pvh.vDesc.setText(tpi.getDescription());
        pvh.vPrice.setText(context.getString(R.string.menu_price)+" " + tpi.getLowPrice());

        Picasso.with(context).load(tpi.getImgUrl()).into(pvh.vImg);
    }

    @Override
    public int getItemCount() {
        return ticketPictItems == null ? 0 : ticketPictItems.size();
    }

    // Classic View Holder for Ticket
    public static class PresalesViewHolder extends RecyclerView.ViewHolder {

        protected TextView vTitle;
        protected TextView vDesc;
        protected TextView vPrice;
        protected ImageView vImg;

        public PresalesViewHolder(View v) {
            super(v);
            vTitle =  (TextView) v.findViewById(R.id.tvTitleTicket);
            vDesc = (TextView)  v.findViewById(R.id.tvDescTicket);
            vPrice = (TextView)  v.findViewById(R.id.tvPriceTicket);
            vImg = (ImageView) v.findViewById(R.id.imgTicket);
        }
    }
}
