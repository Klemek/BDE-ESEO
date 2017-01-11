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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.lacommande.model.HistoryItem;

/**
 * Created by François on 24/04/2015.
 */

public class MyHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HISTORY_ITEM = 0;
    public static final int TYPE_HISTORY_HEADER = 1;
    public static final int TYPE_HISTORY_FOOTER = 2;
    private static final int TYPE_NUMBER = TYPE_HISTORY_ITEM + TYPE_HISTORY_HEADER + TYPE_HISTORY_FOOTER;
    private ArrayList<HistoryItem> historyList;
    private Context context;

    public MyHistoryAdapter(Context context) {
        this.historyList = new ArrayList<>();
        this.context = context;
    }

    public void setHistoryArray(ArrayList<HistoryItem> historyList) {
        if (historyList != null) {
            this.historyList = historyList;
            notifyDataSetChanged();
        }
    }

    public HistoryItem getItem(int position) {
        return historyList.get(position);
    }

    @Override
    public int getItemCount() {
        return historyList == null ? 0 : historyList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        HistoryItem hi = historyList.get(position);
        int type = getItemViewType(position);

        if (type == TYPE_HISTORY_ITEM) { // History Item
            HistoryViewHolder hvh = (HistoryViewHolder) viewHolder;

            switch (hi.getCommandStatus()) {
                case HistoryItem.STATUS_PREPARING:
                    hvh.vImg.setImageResource(R.drawable.ic_prepare);
                    hvh.vCircle.setBackgroundResource(R.drawable.circle_preparing);
                    hvh.vPrice.setTextColor(context.getResources().getColor(R.color.circle_preparing));
                    break;
                case HistoryItem.STATUS_READY:
                    hvh.vImg.setImageResource(R.drawable.ic_ready);
                    hvh.vCircle.setBackgroundResource(R.drawable.circle_ready);
                    hvh.vPrice.setTextColor(context.getResources().getColor(R.color.circle_ready));
                    break;
                case HistoryItem.STATUS_DONE:
                    hvh.vImg.setImageResource(R.drawable.ic_done);
                    hvh.vCircle.setBackgroundResource(R.drawable.circle_done);
                    hvh.vPrice.setTextColor(context.getResources().getColor(R.color.circle_done));
                    break;
                case HistoryItem.STATUS_NOPAID:
                    hvh.vImg.setImageResource(R.drawable.ic_nopaid);
                    hvh.vCircle.setBackgroundResource(R.drawable.circle_error);
                    hvh.vPrice.setTextColor(context.getResources().getColor(R.color.circle_error));
                    break;
            }

            hvh.vName.setText(hi.getCommandName());
            hvh.vDate.setText(hi.getCommandDate());
            hvh.vPrice.setText(hi.getCommandPriceAsString());
            hvh.vNumero.setText(hi.getCommandNumberAsString());
        } else if (type == TYPE_HISTORY_HEADER || type == TYPE_HISTORY_FOOTER) {
            HistoryHeaderViewHolder hhvh = (HistoryHeaderViewHolder) viewHolder;
            hhvh.vName.setText(hi.getCommandName());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return historyList.get(position).isHeader() ? historyList.get(position).isFooter() ? TYPE_HISTORY_FOOTER : TYPE_HISTORY_HEADER : TYPE_HISTORY_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        switch (viewType) {
            case TYPE_HISTORY_ITEM:
                return new HistoryViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_history, viewGroup, false));

            case TYPE_HISTORY_HEADER:
                return new HistoryHeaderViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_history_header, viewGroup, false));

            case TYPE_HISTORY_FOOTER:
                return new HistoryHeaderViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_history_footer, viewGroup, false));
        }

        return null;
    }

    // Classic View Holder for News
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        protected TextView vName;
        protected TextView vNumero;
        protected ImageView vImg;
        protected TextView vPrice;
        protected TextView vDate;
        protected View vCircle;

        public HistoryViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.commandName);
            vNumero = (TextView)  v.findViewById(R.id.commandNumber);
            vImg = (ImageView) v.findViewById(R.id.imgDone);
            vPrice = (TextView) v.findViewById(R.id.commandPrice);
            vDate = (TextView) v.findViewById(R.id.commandDate);
            vCircle = v.findViewById(R.id.circleView);
        }
    }

    // Classic View Holder for Header
    public static class HistoryHeaderViewHolder extends RecyclerView.ViewHolder {

        protected TextView vName;

        public HistoryHeaderViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.historyHeader);
        }
    }
}