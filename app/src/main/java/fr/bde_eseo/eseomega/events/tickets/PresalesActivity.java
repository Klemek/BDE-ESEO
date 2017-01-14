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

package fr.bde_eseo.eseomega.events.tickets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.EventItem;
import fr.bde_eseo.eseomega.events.tickets.adapters.MyPresalesAdapter;
import fr.bde_eseo.eseomega.events.tickets.model.SubEventItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketPictItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketStore;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.lydia.LydiaActivity;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.Utils;

/**
 * Created by François L. on 11/01/2016.
 * Liste les évenements que l'utilisateur peut acheter
 * <p/>
 * Événements (liste) → type de place (dialogue) → Activité de choix de la navette (si il y a navette) → Paiement Lydia
 * <p/>
 * Événements possibles :
 * - date < date_debut
 * - au moins un type de place de dispo
 * - prix >= 0.5
 */
public class PresalesActivity extends AppCompatActivity {

    private static final long MAX_DELAY_ORDER = 582 * 1000;
    // Model
    private ArrayList<TicketPictItem> ticketPictItems;
    // Android objects
    private Context context;
    private boolean isVisible, messageNotShown;
    // User profile
    private UserProfile userProfile;
    // Data
    private int idcmd = -1;
    private String eventName, eventDate, eventID, ticketName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set view / call parent
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presales);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;

        // Get user profile
        userProfile = new UserProfile();
        userProfile.readProfilePromPrefs(context);

        // Get layout
        TextView tvNo1 = (TextView) findViewById(R.id.tvListNothing);
        TextView tvNo2 = (TextView) findViewById(R.id.tvListNothing2);
        ImageView imgNo = (ImageView) findViewById(R.id.imgNoPresale);

        // Get current events / tickets
        fillArray();

        // Init views
        if (ticketPictItems.size() == 0) {
            tvNo1.setVisibility(View.VISIBLE);
            tvNo2.setVisibility(View.VISIBLE);
            imgNo.setVisibility(View.VISIBLE);
        } else {
            tvNo1.setVisibility(View.GONE);
            tvNo2.setVisibility(View.GONE);
            imgNo.setVisibility(View.GONE);
        }

        // Init adapter / recycler view
        MyPresalesAdapter mAdapter = new MyPresalesAdapter(context);
        RecyclerView recList = (RecyclerView) findViewById(R.id.recyList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mAdapter);

        recList.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ArrayList<SubEventItem> subTickets = ticketPictItems.get(position).getExternalEventItem().getSubEventItems();
                final ArrayList<SubEventItem> subTicketPrintable = new ArrayList<>();
                for (int i=0;i<subTickets.size();i++) {
                    SubEventItem sei = subTickets.get(i);
                    if (sei.isAvailable()) {
                        subTicketPrintable.add(sei);
                    }
                }
                eventName = ticketPictItems.get(position).getExternalEventItem().getName();
                eventDate = ticketPictItems.get(position).getExternalEventItem().getDayAsString(ticketPictItems.get(position).getExternalEventItem().getDate());

                CharSequence items[] = new CharSequence[subTicketPrintable.size()];
                for (int i = 0; i < subTicketPrintable.size(); i++)
                    items[i] = subTicketPrintable.get(i).getTitre() + " • " + subTicketPrintable.get(i).getEventPriceAsString();

                // Material dialog to show list of items
                new MaterialDialog.Builder(context)
                        .items(items)
                        .title(R.string.presales_available)
                        .cancelable(true) // faster for user
                        .positiveText(R.string.dialog_choose)
                        .negativeText(R.string.dialog_cancel)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                // Conservation des paramètres
                                eventID = subTicketPrintable.get(which).getId();
                                ticketName = "" + text;

                                /**
                                 * Si évenement activé, on propose les choix, sinon, message d'erreur
                                 */

                                /**
                                 * Check si navette dispo pour ce ticket :
                                 * - si navette : choix navette
                                 * - sinon : payer
                                 */
                                if (subTicketPrintable.get(which).hasShuttles()) {
                                    Intent i = new Intent(context, ShuttleActivity.class);
                                    TicketStore.getInstance().setSelectedTicket(subTicketPrintable.get(which));
                                    startActivityForResult(i, Constants.RESULT_SHUTTLES_KEY);
                                } else {
                                    // Payer directement
                                    new MaterialDialog.Builder(context)
                                            .title(R.string.presales_confirm)
                                            .content(eventName + "\n" + ticketName + "\n" + eventDate + "\n\n" +
                                                    getString(R.string.presales_confirm_txt))
                                            .positiveText(R.string.dialog_pay)
                                            .negativeText(R.string.dialog_cancel)
                                            .callback(new MaterialDialog.ButtonCallback() {
                                                @Override
                                                public void onPositive(MaterialDialog dialog) {
                                                    super.onPositive(dialog);
                                                    AsyncSendTicket asyncSendTicket = new AsyncSendTicket(context);
                                                    asyncSendTicket.execute(eventID);
                                                }
                                            })
                                            .show();
                                }

                                return true;
                            }
                        })
                        .show();

            }
        }));

        // Set data
        mAdapter.setPictItems(ticketPictItems);

        // Timeout de 10 minutes max de commande (token périmé après)
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (context != null && isVisible) {
                    showPeremptedToken();
                } else {
                    messageNotShown = true;
                }
            }
        }, MAX_DELAY_ORDER);

    }

    /**
     * Permet d'ajouter les événements visibles dans un dataset utilisable par la RecyclerView
     */
    private void fillArray() {

        if (ticketPictItems == null)
            ticketPictItems = new ArrayList<>();
        ticketPictItems.clear();

        ArrayList<EventItem> eventItems = TicketStore.getInstance().getEventItems();

        for (int i = 0; i < eventItems.size(); i++) {
            EventItem ei = eventItems.get(i);
            if (!ei.isHeader() && !ei.isPassed() && ei.hasSubEventChildEnabled() && ei.hasSubEventChildPriced()) {
                ticketPictItems.add(new TicketPictItem(ei));
            }
        }
    }

    /**
     * Called when called Activities (child) finished
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // From Lydia-Activity
        if (requestCode == Constants.RESULT_LYDIA_KEY) {

            if (LydiaActivity.LAST_STATUS() == 2) {
                new MaterialDialog.Builder(context)
                        .title(R.string.congrats)
                        .content(R.string.presales_reservation_done_txt)
                        .cancelable(false)
                        .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                        .input("sterling@archer.fr", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                AsyncEventEmail asyncEmail = new AsyncEventEmail(context, "" + input, PresalesActivity.this, userProfile, idcmd); // convert charSequence into String object
                                asyncEmail.execute();
                            }
                        }).show();
            } else {
                new MaterialDialog.Builder(context)
                        .title(R.string.presales_reservation_fail)
                        .content(R.string.presales_reservation_fail_txt)
                        .cancelable(false)
                        .negativeText(R.string.dialog_close)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                PresalesActivity.this.finish();
                            }
                        })
                        .show();
            }

            // From Shuttle-Activity
        } else if (requestCode == Constants.RESULT_SHUTTLES_KEY && resultCode == Activity.RESULT_OK) {

            // Get shuttle ID
            final String shuttleID = String.valueOf(data.getExtras().getInt(Constants.RESULT_SHUTTLES_VALUE));
            final String shuttleName = data.getExtras().getString(Constants.RESULT_SHUTTLES_NAME);

            // Ask for user confirmation
            new MaterialDialog.Builder(context)
                    .title(R.string.presales_confirm)
                    .content(eventName + "\n" + ticketName + "\n" + shuttleName + "\n\n" +
                            getString(R.string.presales_confirm_txt))
                    .positiveText(R.string.dialog_pay)
                    .negativeText(R.string.dialog_cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);

                            // Send network request
                            AsyncSendTicket asyncSendTicket = new AsyncSendTicket(context);
                            asyncSendTicket.execute(eventID, shuttleID);
                        }
                    })
                    .show();
        }
    }

    /**
     * Menu : back button + arrow in toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_empty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
        if (messageNotShown) {
            showPeremptedToken();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    private void showPeremptedToken () {
        new MaterialDialog.Builder(context)
                .title(R.string.presales_reservation_expired)
                .content(R.string.presales_reservation_expired_txt)
                .cancelable(false)
                .negativeText(R.string.dialog_close)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        finish();
                    }
                })
                .show();
    }

    /**
     * Permet d'envoyer la commande sur les serveurs
     */
    private class AsyncSendTicket extends AsyncTask<String, String, String> {

        private final Context context;
        private MaterialDialog md;

        public AsyncSendTicket(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            md = new MaterialDialog.Builder(context)
                    .title(R.string.presales_reserving)
                    .content(R.string.wait)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            md.hide();
            int err = 0;
            String errMsg = getString(R.string.error_network_short);

            if (Utils.isNetworkDataValid(data)) {
                try {
                    JSONObject obj = new JSONObject(data);
                    err = obj.getInt("status");
                    errMsg = obj.getString("cause");
                    JSONObject objData = obj.getJSONObject("data");
                    idcmd = objData.getInt("idcmd");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (err == 1) {
                // Ok ! Send order to Lydia
                Intent i = new Intent(context, LydiaActivity.class);
                i.putExtra(Constants.KEY_LYDIA_ORDER_ID, idcmd);
                i.putExtra(Constants.KEY_LYDIA_ORDER_TYPE, Constants.TYPE_LYDIA_EVENT);
                i.putExtra(Constants.KEY_LYDIA_ORDER_ASKED, false);
                startActivityForResult(i, Constants.RESULT_LYDIA_KEY);

            } else {
                // Error, show message
                new MaterialDialog.Builder(context)
                        .title(R.string.error)
                        .content(errMsg + (err == 0 ? "" : " (code : " + err + ")"))
                        .negativeText(R.string.dialog_close)
                        .show();
            }
        }

        @Override
        protected String doInBackground(String... sData) {

            try {
                HashMap<String, String> params = new HashMap<>();
                params.put(context.getResources().getString(R.string.token), TicketStore.getInstance().getToken());
                params.put(context.getResources().getString(R.string.idevent), Base64.encodeToString(sData[0].getBytes("UTF-8"), Base64.NO_WRAP));
                if (sData.length > 1 && sData[1] != null && sData[1].length() > 0) {
                    params.put(context.getResources().getString(R.string.nav), Base64.encodeToString(sData[1].getBytes("UTF-8"), Base64.NO_WRAP));
                }
                return ConnexionUtils.postServerData(Constants.URL_API_EVENT_SEND, params, context);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
