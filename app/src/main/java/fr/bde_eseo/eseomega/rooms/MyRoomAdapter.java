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

package fr.bde_eseo.eseomega.rooms;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;

/**
 * Created by François L. on 23/03/2016.
 */
public class MyRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int TYPE_HEADER = 0;
    private int TYPE_ITEM = 1;

    private ArrayList<RoomItem> roomItems;
    private Context ctx;

    public ArrayList<RoomItem> getRoomItems() {
        return roomItems;
    }

    public void setRoomItems(ArrayList<RoomItem> roomItems) {
        this.roomItems = roomItems;
    }

    public MyRoomAdapter(Context ctx, ArrayList<RoomItem> roomItems) {
        this.roomItems = roomItems;
        this.ctx = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER)
            return new RoomHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_simple_header, parent, false));
        else
            return new RoomItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_simple, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return roomItems.get(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        RoomItem ri = roomItems.get(position);

        if (!ri.isHeader()) {
            RoomItemViewHolder rivh = (RoomItemViewHolder) holder;
            rivh.name.setText(ri.getName());
            rivh.details.setText(ri.getDetails());
        } else {
            RoomHeaderViewHolder rhvh = (RoomHeaderViewHolder) holder;
            rhvh.name.setText(ri.getName());
        }
    }

    @Override
    public int getItemCount() {
        return roomItems.size();
    }

    // Classic View Holder for Room item
    public class RoomItemViewHolder extends RecyclerView.ViewHolder {

        protected TextView name, details;

        public RoomItemViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tvName);
            details = (TextView) v.findViewById(R.id.tvDesc);
        }
    }

    // Classic View Holder for Room header
    public class RoomHeaderViewHolder extends RecyclerView.ViewHolder {

        protected TextView name, details;

        public RoomHeaderViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tvHeader);
        }
    }
}
