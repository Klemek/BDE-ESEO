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
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.tickets.model.CheckShuttleItem;

/**
 * Created by François L. on 11/01/2016.
 * Adapter pour l'historique des commandes évènements
 */
public class MyShuttlesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_TICKET_ITEM = 0;
    public static final int TYPE_TICKET_HEADER = 1;

    private ArrayList<CheckShuttleItem> checkShuttleItems;
    private Context context;

    public MyShuttlesAdapter(Context context) {
        this.checkShuttleItems = new ArrayList<>();
        this.context = context;
    }

    public void setShuttleItems(ArrayList<CheckShuttleItem> checkShuttleItems) {
        if (checkShuttleItems != null) {
            this.checkShuttleItems = checkShuttleItems;
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_TICKET_ITEM)
            return new ShuttleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_shuttle, parent, false));
        else
            return new ShuttleHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_shuttle_header, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return checkShuttleItems.get(position).isHeader() ? TYPE_TICKET_HEADER : TYPE_TICKET_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CheckShuttleItem csi = checkShuttleItems.get(position);

        if (!csi.isHeader()) {
            final ShuttleViewHolder svh = (ShuttleViewHolder) holder;
            //svh.vPlace.setText(csi.getShuttleItem().getDepartPlace());
            svh.vDeparture.setText(csi.getShuttleItem().getDepartureStr());
            svh.vSeats.setText(csi.getShuttleItem().getRemainingSeats() + "/" + csi.getShuttleItem().getTotalSeats() + " " + context.getString(R.string.shuttle_place));
            svh.checkShuttle.setChecked(csi.isCheck());
            svh.checkShuttle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uncheckAll();
                    csi.setIsCheck(svh.checkShuttle.isChecked());
                    notifyDataSetChanged();
                }
            });

            svh.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uncheckAll();
                    svh.checkShuttle.setChecked(!svh.checkShuttle.isChecked());
                    csi.setIsCheck(svh.checkShuttle.isChecked());
                    notifyDataSetChanged();
                }
            });
        } else {
            ShuttleHeaderViewHolder shvh = (ShuttleHeaderViewHolder) holder;
            shvh.vName.setText(csi.getName());
        }
    }

    @Override
    public int getItemCount() {
        return checkShuttleItems == null ? 0 : checkShuttleItems.size();
    }

    /**
     * Uncheck all items
     */
    void uncheckAll() {
        for (int i = 0; i < checkShuttleItems.size(); i++) {
            checkShuttleItems.get(i).setIsCheck(false);
        }
    }

    // Classic View Holder for Shuttle
    public static class ShuttleViewHolder extends RecyclerView.ViewHolder {

        protected TextView vPlace, vDeparture, vSeats;
        protected CheckBox checkShuttle;
        protected CardView cardView;

        public ShuttleViewHolder(View v) {
            super(v);
            vPlace =  (TextView) v.findViewById(R.id.shuttlePlace);
            vDeparture = (TextView)  v.findViewById(R.id.shuttleDeparture);
            vSeats = (TextView) v.findViewById(R.id.shuttleSeats);
            checkShuttle = (CheckBox) v.findViewById(R.id.checkShuttle);
            cardView = (CardView) v.findViewById(R.id.card_view);
        }
    }

    // Classic View Holder for Ticket Header
    public static class ShuttleHeaderViewHolder extends RecyclerView.ViewHolder {

        protected TextView vName;

        public ShuttleHeaderViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.shuttleHeader);
        }
    }
}
