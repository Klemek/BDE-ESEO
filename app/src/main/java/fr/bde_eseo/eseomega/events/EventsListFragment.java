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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.tickets.model.TicketStore;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.JSONUtils;
import fr.bde_eseo.eseomega.utils.Utils;

/**
 * Created by François L. on 14/08/2015.
 * Displays Events
 */
public class EventsListFragment extends Fragment {

    // Constants
    private final static int LATENCY_REFRESH = 8; // 8 sec min between 2 refreshs
    // UI
    private ProgressBar progCircle;
    private ImageView img;
    private TextView tv1, tv2;
    private RecyclerView recList;
    private MyEventsAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long timestamp;
    private AsyncJSON asyncJSON;
    private RecyclerView.OnItemTouchListener disabler;
    // Model
    private ArrayList<EventItem> eventItems;
    private String cachePath;
    private File cacheFileEseo;

    public EventsListFragment() {
    }

    public static void openEvent(final Activity act, final EventItem ei){
        if (!ei.isHeader()) {
            boolean hasUrl = ei.getUrl() != null && ei.getUrl().length() != 0;
            boolean hasPlace = ei.getLieu() != null && ei.getLieu().length() != 0;
            boolean hasImg = ei.getImgUrl() != null && ei.getImgUrl().length() != 0;
            MaterialDialog.Builder mdb = new MaterialDialog.Builder(act)
                    .customView(R.layout.dialog_event, false);


            MaterialDialog md = mdb.show();

            View mdView = md.getView();



            ImageView iv = (ImageView)mdView.findViewById(R.id.eventImg);
            if (hasImg){
                Picasso.with(iv.getContext()).load(ei.getImgUrl()).into(iv);
                iv.setVisibility(View.VISIBLE);
            }else{
                iv.setVisibility(View.GONE);
            }

            ((TextView) mdView.findViewById(R.id.tvEventName)).setText(ei.getName());
            (mdView.findViewById(R.id.rlBackDialogEvent)).setBackgroundColor(ei.getColor());
            if (ei.getDetails() != null && ei.getDetails().length() > 0) {
                ((TextView) mdView.findViewById(R.id.tvEventDetails)).setText(JSONUtils.fromHtml(ei.getDetails()));
            } else {
                ((TextView) mdView.findViewById(R.id.tvEventDetails)).setText(R.string.event_no_details);
            }
            ((TextView) mdView.findViewById(R.id.tvEventDate)).setText(act.getText(R.string.event_date)+" : " + ei.getDateString(act.getBaseContext()));
            ((TextView) mdView.findViewById(R.id.tvEventEndDate)).setText(act.getText(R.string.event_end_date)+" : "  + ei.getDateFinString(act.getBaseContext()));
            if (ei.getLieu() != null && ei.getLieu().length() > 0) {
                ((TextView) mdView.findViewById(R.id.tvEventPlace)).setText(act.getText(R.string.event_place)+" : "  + ei.getLieu());
                mdView.findViewById(R.id.tvEventPlace).setVisibility(View.VISIBLE);
            } else {
                mdView.findViewById(R.id.tvEventPlace).setVisibility(View.GONE);
            }

            mdView.findViewById(R.id.eventAddCalendar).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(ei.toCalendarIntent());
                }
            });

            ImageButton signup = (ImageButton)mdView.findViewById(R.id.eventSignup);
            if(ei.isSignup() && !ei.isPassed()) {
                signup.setVisibility(View.VISIBLE);
                if(ei.isSignedUp()){
                    signup.setClickable(false);
                    signup.setImageResource(R.drawable.ic_done);
                }else{
                    signup.setClickable(true);
                    signup.setImageResource(R.drawable.ic_signup);
                    signup.findViewById(R.id.eventSignup).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UserProfile up = new UserProfile();
                            up.readProfilePromPrefs(act);
                            if (up.isCreated()) {
                                if(up.getPhoneNumber().equals("")){
                                    Toast.makeText(v.getContext(), R.string.toast_no_phone,Toast.LENGTH_LONG).show();
                                }else {
                                    (new AsyncSignup(ei, (ImageButton) v, up, false)).execute(Constants.URL_API_EVENT_SIGNUP + ei.getId());
                                }
                            } else {
                                Toast.makeText(v.getContext(), R.string.toast_no_user_event, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }else{
                mdView.findViewById(R.id.eventSignup).setVisibility(View.GONE);
            }

            ImageButton urlButton = (ImageButton)mdView.findViewById(R.id.eventUrl);
            if (hasUrl){
                urlButton.setVisibility(View.VISIBLE);
                urlButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = ei.getUrl();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        v.getContext().startActivity(i);
                    }
                });
            }else{
                urlButton.setVisibility(View.GONE);
            }

            ImageButton mapButton = (ImageButton)mdView.findViewById(R.id.eventMap);
            if (hasPlace){
                mapButton.setVisibility(View.VISIBLE);
                mapButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(ei.getLieu()));
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            v.getContext().startActivity(mapIntent);
                        } catch (ActivityNotFoundException ex) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
                            v.getContext().startActivity(intent);
                        }
                    }
                });
            }else{
                mapButton.setVisibility(View.GONE);
            }
        }
    }

    public static void openEvent(Activity act, int eventid){
        new AsyncJSONSingleEvent(act).execute(eventid);
    }

    @Override
    public View onCreateView(LayoutInflater rootInfl, ViewGroup container, Bundle savedInstanceState) {

        // UI
        View rootView = rootInfl.inflate(R.layout.fragment_event_list, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.events_refresh);
        swipeRefreshLayout.setColorSchemeColors(Utils.resolveColorFromTheme(getContext(), R.attr.colorPrimaryDark));
        progCircle = (ProgressBar) rootView.findViewById(R.id.progressEvent);
        tv1 = (TextView) rootView.findViewById(R.id.tvListNothing);
        tv2 = (TextView) rootView.findViewById(R.id.tvListNothing2);
        img = (ImageView) rootView.findViewById(R.id.imgNoEvent);
        progCircle.setVisibility(View.GONE);
        progCircle.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_grey_500), PorterDuff.Mode.SRC_IN);
        tv1.setVisibility(View.GONE);
        tv2.setVisibility(View.GONE);
        img.setVisibility(View.GONE);
        disabler = new RecyclerViewDisabler();

        // I/O cache data
        cachePath = getActivity().getCacheDir() + "/";
        cacheFileEseo = new File(cachePath + "events.json");

        // Init static model
        TicketStore.getInstance().reset();

        // Model / objects
        eventItems = TicketStore.getInstance().getEventItems();
        mAdapter = new MyEventsAdapter(eventItems, getActivity());
        recList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        mAdapter.setEventItems(eventItems);
        mAdapter.notifyDataSetChanged();

        // Start download of data
        asyncJSON = new AsyncJSON(true); // circle needed for first call
        asyncJSON.execute(Constants.URL_JSON_EVENTS);


        // Swipe-to-refresh implementations
        timestamp = 0;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Toast.makeText(getActivity(), "Refreshing ...", Toast.LENGTH_SHORT).show();
                long t = System.currentTimeMillis() / 1000;
                if (t - timestamp > LATENCY_REFRESH) { // timestamp in seconds)
                    timestamp = t;
                    asyncJSON = new AsyncJSON(false); // no circle here (already in SwipeLayout)
                    asyncJSON.execute(Constants.URL_JSON_EVENTS);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (asyncJSON != null) asyncJSON.cancel(true);
    }

    public static class AsyncSignup extends AsyncTask<String, String, String> {

        private ImageButton button;
        private UserProfile up;
        private Context ctx;
        private EventItem ei;
        private boolean bg;

        public AsyncSignup(EventItem ei, ImageButton button, UserProfile up, boolean bg) {
            this.button = button;
            this.up = up;
            this.ei = ei;
            this.ctx = button.getContext();
            this.bg = bg;
        }

        @Override
        protected void onPreExecute() {
            if(bg)((GradientDrawable)button.getBackground()).setColor(ctx.getResources().getColor(R.color.circle_preparing));
            button.setImageResource(R.drawable.ic_working);
            button.setClickable(false);
        }

        @Override
        protected void onPostExecute(String result) {
            boolean done = false;
            try{
                JSONObject servresult = new JSONObject(result);
                if(servresult.has("signedup"))
                    done = servresult.getBoolean("signedup");
                else if(servresult.has("placeholder_error") && servresult.has("info")){
                    Toast.makeText(ctx, ctx.getText(R.string.error)+" "+servresult.getInt("placeholder_error")+" : "+servresult.getString("info"), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ctx, R.string.error_server_unknown, Toast.LENGTH_SHORT).show();
                }
            }catch(JSONException e){
                e.printStackTrace();
                Toast.makeText(ctx, R.string.error_network, Toast.LENGTH_SHORT).show();
            }

            if(done){
                if(bg)((GradientDrawable)button.getBackground()).setColor(ctx.getResources().getColor(R.color.circle_ready));
                button.setImageResource(R.drawable.ic_done);
                ei.setSignedUp(true);
            }else{
                if(bg)((GradientDrawable)button.getBackground()).setColor(ctx.getResources().getColor(R.color.circle_error));
                button.setImageResource(R.drawable.ic_signup);
                button.setClickable(true);
            }
        }

        @Override
        protected String doInBackground(String... args) {
            HashMap<String, String> params = new HashMap<>();
            params.put(Constants.POST_EVENT_SIGNUP_NAME,up.getName());
            params.put(Constants.POST_EVENT_SIGNUP_MAIL,up.getEmail());
            params.put(Constants.POST_EVENT_SIGNUP_TEL, up.getPhoneNumber());
            return ConnexionUtils.postServerData(args[0], params, ctx);
        }
    }

    public static class AsyncJSONSingleEvent extends AsyncTask<Integer ,Void, JSONObject> {

        private Activity act;

        public AsyncJSONSingleEvent(Activity act) {
            this.act = act;
        }

        @Override
        protected void onPostExecute(JSONObject obj) {
            try{
                openEvent(act, new EventItem(act.getBaseContext(), obj));
            }catch(JSONException e){
                e.printStackTrace();
                Toast.makeText(act.getBaseContext(), R.string.toast_cant_read_event,Toast.LENGTH_SHORT).show();
            }


        }

        @Override
        protected JSONObject doInBackground(Integer... params) {

            JSONObject event = JSONUtils.getJSONFromUrl(Constants.URL_JSON_EVENTS+params[0]);

            return event;
        }
    }

    // Scroll listener to prevent issue 77846
    public class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            return true;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    /**
     * Download JSON data
     */
    public class AsyncJSON extends AsyncTask<String, String, JSONArray> {

        boolean displayCircle;

        public AsyncJSON(boolean displayCircle) {
            this.displayCircle = displayCircle;
        }

        @Override
        protected void onPreExecute() {
            recList.addOnItemTouchListener(disabler);
            if (eventItems != null) {
                eventItems.clear();
            }
            img.setVisibility(View.GONE);
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
            if (displayCircle) progCircle.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onPostExecute(JSONArray array) {

            if (array != null) {
                try {
                    String lastHeader = "---"; // undefined for the first iteration (no event before)
                    //int scrollTo = -1, counter = 0;

                    for (int i = 0; i < array.length(); i++) {

                        EventItem ei = new EventItem(getContext(), array.getJSONObject(i));

                        /*if (ei.getDate().after(new Date()) && scrollTo == -1) {
                            scrollTo++;
                        }*/

                        if (ei.getMonthHeader().equals(lastHeader)) { // same month, no header
                            eventItems.add(0, ei);
                        } else if (lastHeader.equals("---")) { // another month : add header then event
                            eventItems.add(ei);
                            lastHeader = ei.getMonthHeader();
                        } else {
                            eventItems.add(0, new EventItem(eventItems.get(0).getMonthHeader()));
                            eventItems.add(0, ei);
                            lastHeader = ei.getMonthHeader();
                        }
                    }

                    eventItems.add(0, new EventItem(eventItems.get(0).getMonthHeader()));

                    if (displayCircle) progCircle.setVisibility(View.GONE);
                    mAdapter.setEventItems(eventItems);
                    mAdapter.notifyDataSetChanged();
                    /*if (scrollTo >= 2 && recList.getAdapter().getItemViewType(scrollTo-2) == MyEventsAdapter.TYPE_HEADER) {
                        scrollTo-=2;

                    }
                    if (scrollTo != -1) {
                        recList.scrollToPosition(scrollTo);
                    }*/

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if (displayCircle) progCircle.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
                img.setVisibility(View.VISIBLE);
                tv1.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.VISIBLE);
            }
            swipeRefreshLayout.setRefreshing(false);
            recList.removeOnItemTouchListener(disabler);
        }

        @Override
        protected JSONArray doInBackground(String... params) {

            JSONArray obj = JSONUtils.getJSONArrayFromUrl(params[0]);

            if (obj == null) {
                if (cacheFileEseo.exists()) {
                    try {
                        obj = new JSONArray(Utils.getStringFromFile(cacheFileEseo));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Utils.writeStringToFile(cacheFileEseo, obj.toString());
            }
            return obj;
        }
    }
}

