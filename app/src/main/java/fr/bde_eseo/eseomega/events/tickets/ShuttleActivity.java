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
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.tickets.adapters.MyShuttlesAdapter;
import fr.bde_eseo.eseomega.events.tickets.model.CheckShuttleItem;
import fr.bde_eseo.eseomega.events.tickets.model.ShuttleItem;
import fr.bde_eseo.eseomega.events.tickets.model.SubEventItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketStore;
import fr.bde_eseo.eseomega.utils.DateUtils;

/**
 * Created by François L. on 13/01/2016.
 * Affiche les navettes disponibles pour un ticket précisé
 */
public class ShuttleActivity extends AppCompatActivity {

    // Model
    private ArrayList<CheckShuttleItem> checkShuttleItems;

    // Android objects
    private Context context;

    // Adapter / recycler
    private MyShuttlesAdapter mAdapter;
    private RecyclerView recList;

    // Layout
    private TextView tvValid;

    // Ticket ID
    private SubEventItem subEventItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set view / call parent
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuttles);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;

        // Get layout
        tvValid = (TextView) findViewById(R.id.tvValid);

        // Valid listener
        tvValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvValid.setBackgroundColor(0x2fffffff);

                ShuttleItem selectShut = getSelected();

                if (selectShut == null) {

                    // Aucune navette sélectionnée
                    new MaterialDialog.Builder(context)
                            .title(R.string.log_error_title)
                            .content(R.string.shuttle_no_select)
                            .cancelable(false)
                            .negativeText(R.string.dialog_close)
                            .show();
                } else {

                    // Sauvegarde navette sélectionnée
                    // TicketStore ... setNavette
                    TicketStore.getInstance().setSelectedShuttle(selectShut);

                    // Retour des données
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(Constants.RESULT_SHUTTLES_VALUE, selectShut.getIdshuttle());
                    returnIntent.putExtra(Constants.RESULT_SHUTTLES_NAME, selectShut.getDepartPlace() + " (" + selectShut.getDepartureStr() + ")");
                    setResult(Activity.RESULT_OK, returnIntent);

                    // Retour UI précédente
                    finish();

                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvValid.setBackgroundColor(0x00ffffff);
                    }
                }, 500);
            }
        });

        // Get intent
        subEventItem = TicketStore.getInstance().getSelectedTicket();

        // Init model
        fillCheckables();

        // Init adapter / recycler view
        mAdapter = new MyShuttlesAdapter(context);
        recList = (RecyclerView) findViewById(R.id.recyList);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mAdapter);
        mAdapter.setShuttleItems(checkShuttleItems);
    }

    /**
     * Permet d'ajouter les navettes en tant qu'objets checkables
     * Définit aussi les headers
     */
    private void fillCheckables() {
        if (checkShuttleItems == null)
            checkShuttleItems = new ArrayList<>();
        checkShuttleItems.clear();

        String lastHeader = "";

        for (int i=0;i<subEventItem.getShuttleItems().size();i++) {

            ShuttleItem si = subEventItem.getShuttleItems().get(i);
            if (!si.getDepartPlace().equalsIgnoreCase(lastHeader)) {
                lastHeader = si.getDepartPlace();
                checkShuttleItems.add(new CheckShuttleItem(lastHeader.toUpperCase(DateUtils.getLocale())));
            }
            checkShuttleItems.add(new CheckShuttleItem(si));
        }
    }

    /**
     * Retourne la navette sélectionnée
     */
    private ShuttleItem getSelected () {
        ShuttleItem si = null;
        for (int i=0;i<checkShuttleItems.size() && si == null;i++) {
            if (checkShuttleItems.get(i).isCheck()) {
                si = checkShuttleItems.get(i).getShuttleItem();
            }
        }
        return si;
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
}
