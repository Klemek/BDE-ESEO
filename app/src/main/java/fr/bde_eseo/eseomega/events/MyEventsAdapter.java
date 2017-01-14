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

package fr.bde_eseo.eseomega.events;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ThemeUtils;

/**
 * Created by François L. on 11/08/2015.
 */
class MyEventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_HEADER = 0;
    private final static int TYPE_ITEM = 1;
    private final Activity parentActivity;
    private ArrayList<EventItem> eventItems;

    public MyEventsAdapter (ArrayList<EventItem> eventItems, Activity parentActivity) {
        this.eventItems = eventItems;
        this.parentActivity = parentActivity;
    }

    public void setEventItems(ArrayList<EventItem> EventItems) {
        this.eventItems = EventItems;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER)
            return new EventHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_events_header, parent, false));
        else
            return new EventItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_event, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return eventItems.get(position).isHeader()?TYPE_HEADER:TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,final int position) {
        final EventItem ei = eventItems.get(position);
        int type = getItemViewType(position);

        if (type == TYPE_HEADER) {
            EventHeaderViewHolder ehvh = (EventHeaderViewHolder) holder;
            ehvh.name.setText(ei.getName());
        } else {
            EventItemViewHolder eivh = (EventItemViewHolder) holder;

            eivh.name.setText(ei.getName());

            if (ei.isPassed()) {
                eivh.name.setTextColor(parentActivity.getResources().getColor(R.color.md_grey_600));
            } else {
                eivh.name.setTextColor(parentActivity.getResources().getColor(R.color.md_grey_800));
            }

            // For RelativeLayout : get background drawable, then change color
            //LayerDrawable layerDrawable = (LayerDrawable) eivh.rlColor.getBackground();
            //eivh.rlColor.setBackgroundColor(ei.getColor());
            if (ei.isPassed()) {
                ((GradientDrawable) eivh.rlColor.getBackground()).setColor(parentActivity.getResources().getColor(R.color.md_grey_600));
            } else {
                //((GradientDrawable)eivh.rlColor.getBackground()).setColor(eivh.rlColor.getContext().getResources().getColor(ThemeUtils.resolveColorFromTheme(eivh.rlColor.getContext(), R.attr.colorAccent)));
                ((GradientDrawable) eivh.rlColor.getBackground()).setColor(parentActivity.getResources().getColor(ThemeUtils.resolveColorFromTheme2(parentActivity.getApplicationContext(), R.attr.colorAccent, false)));
            }


            if (ei.getShortedDetails().length() > 1) {
                eivh.details.setVisibility(View.VISIBLE);
                eivh.details.setText(ei.getShortedDetails());
            } else
                eivh.details.setVisibility(View.GONE);

            eivh.dayNum.setText(ei.getDayNumero());
            eivh.dayName.setText(ei.getDayName());
            if(ei.isSignup() && !ei.isPassed()){
                eivh.button.setVisibility(View.VISIBLE);
                if(ei.isSignedUp()){
                    ((GradientDrawable)eivh.button.getBackground()).setColor(eivh.button.getContext().getResources().getColor(R.color.circle_ready));
                    eivh.button.setImageResource(R.drawable.ic_done);
                    eivh.button.setClickable(false);
                }else{
                    ((GradientDrawable)eivh.button.getBackground()).setColor(eivh.button.getContext().getResources().getColor(R.color.circle_preparing));
                    eivh.button.setImageResource(R.drawable.ic_signup);
                    eivh.button.setClickable(true);
                    eivh.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UserProfile up = new UserProfile();
                            up.readProfilePromPrefs(parentActivity);
                            if(up.isCreated()){
                                if(up.getPhoneNumber().equals("")){
                                    Toast.makeText(v.getContext(), R.string.toast_no_phone,Toast.LENGTH_LONG).show();
                                }else{
                                    (new EventsListFragment.AsyncSignup(ei, (ImageButton)v, up, true)).execute(Constants.URL_API_EVENT_SIGNUP+ei.getId());
                                }
                            }else{
                                Toast.makeText(v.getContext(), R.string.toast_no_user_event,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }else{
                eivh.button.setVisibility(View.GONE);
            }

            eivh.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventsListFragment.openEvent(parentActivity, ei);
                }
            });

        }
    }




    @Override
    public int getItemCount() {
        return eventItems.size();
    }

    // Classic View Holder for Event item
    public class EventItemViewHolder extends RecyclerView.ViewHolder{

        final View view;
        final TextView name;
        final TextView details;
        final TextView dayNum;
        final TextView dayName;
        final RelativeLayout rlColor;
        final ImageButton button;

        public EventItemViewHolder(View v) {
            super(v);
            view = v;
            name = (TextView) v.findViewById(R.id.tvNameEvent);
            details = (TextView) v.findViewById(R.id.tvDetailsEvent);
            dayName = (TextView) v.findViewById(R.id.tvDayEvent);
            dayNum = (TextView) v.findViewById(R.id.tvDateEvent);
            rlColor = (RelativeLayout) v.findViewById(R.id.rlEventColor);
            button = (ImageButton) v.findViewById(R.id.eventButton);
        }
    }

    // Classic View Holder for Event header
    public class EventHeaderViewHolder extends RecyclerView.ViewHolder {

        final TextView name;

        public EventHeaderViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.eventsHeader);
        }
    }


}
