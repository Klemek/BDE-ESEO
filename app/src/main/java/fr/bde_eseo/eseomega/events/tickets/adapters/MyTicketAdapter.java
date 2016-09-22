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

package fr.bde_eseo.eseomega.events.tickets.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.tickets.model.EventTicketItem;

/**
 * Created by François L. on 11/01/2016.
 * Adapter pour l'historique des commandes évènements
 */
public class MyTicketAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<EventTicketItem> eventTicketItems;
    private Context context;

    public static final int TYPE_TICKET_ITEM = 0;
    public static final int TYPE_TICKET_HEADER = 1;

    public MyTicketAdapter(Context context) {
        this.eventTicketItems = new ArrayList<>();
        this.context = context;
    }

    public void setTicketsItems(ArrayList<EventTicketItem> eventTicketItems) {
        if (eventTicketItems != null) {
            this.eventTicketItems = eventTicketItems;
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_TICKET_ITEM)
            return new TicketViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_tickets, parent, false));
        else
            return new TicketHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_events_header, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return eventTicketItems.get(position).isHeader() ? TYPE_TICKET_HEADER : TYPE_TICKET_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        EventTicketItem eti = eventTicketItems.get(position);

        if (eti.isHeader()) {
            TicketHeaderViewHolder thvh = (TicketHeaderViewHolder) holder;
            thvh.vName.setText(eti.getName());
        } else {
            TicketViewHolder tvh = (TicketViewHolder) holder;
            tvh.vName.setText(eti.getName());
            tvh.vNumero.setText(String.valueOf(eti.getTicketNumberAsString()));
            tvh.vPrice.setText(String.valueOf(eti.getTicketPriceAsString()));
            tvh.vDate.setText(eti.getFrenchDate());

            if (eti.isPassed()) {
                tvh.vImg.setImageResource(R.drawable.ic_date_passed);
                tvh.vCircle.setBackgroundResource(R.drawable.circle_done);
                tvh.vPrice.setTextColor(context.getResources().getColor(R.color.circle_done));
            } else {
                tvh.vImg.setImageResource(R.drawable.ic_date_current);
                tvh.vCircle.setBackgroundResource(R.drawable.circle_next);
                tvh.vPrice.setTextColor(context.getResources().getColor(R.color.md_red_500));
            }
        }
    }

    @Override
    public int getItemCount() {
        return eventTicketItems == null ? 0 : eventTicketItems.size();
    }

    // Classic View Holder for Ticket
    public static class TicketViewHolder extends RecyclerView.ViewHolder {

        protected TextView vName;
        protected TextView vNumero;
        protected TextView vPrice;
        protected TextView vDate;
        protected ImageView vImg;
        protected View vCircle;

        public TicketViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.ticketName);
            vNumero = (TextView)  v.findViewById(R.id.ticketNumber);
            vPrice = (TextView) v.findViewById(R.id.ticketPrice);
            vDate = (TextView) v.findViewById(R.id.ticketDate);
            vImg = (ImageView) v.findViewById(R.id.imgDone);
            vCircle = v.findViewById(R.id.circleView);
        }
    }

    // Classic View Holder for Ticket Header
    public static class TicketHeaderViewHolder extends RecyclerView.ViewHolder {

        protected TextView vName;

        public TicketHeaderViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.eventsHeader);
        }
    }
}
