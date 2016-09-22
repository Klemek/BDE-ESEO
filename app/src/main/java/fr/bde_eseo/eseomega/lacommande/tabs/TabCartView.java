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

package fr.bde_eseo.eseomega.lacommande.tabs;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.lacommande.DataManager;
import fr.bde_eseo.eseomega.lacommande.MyCartAdapter;
import fr.bde_eseo.eseomega.lacommande.OrderTabsFragment;
import fr.bde_eseo.eseomega.lacommande.model.LacmdMenu;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.lydia.LydiaActivity;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.Utilities;


public class TabCartView extends Fragment {

    private FloatingActionButton floatingShop;
    private TextView tvPrice;
    private ImageView img;
    private TextView tv1, tv2;
    private RecyclerView recList;
    private MyCartAdapter mAdapter;
    private EditText etInstr;
    private View viewOrder;
    private ProgressBar progressBarOrder;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_cartview_2,container,false);

        // UI objets
        floatingShop = (FloatingActionButton) v.findViewById(R.id.fabCommand);
        tvPrice = (TextView) v.findViewById(R.id.tvCartPrice);
        tv1 = (TextView) v.findViewById(R.id.tvListNothing);
        tv2 = (TextView) v.findViewById(R.id.tvListNothing2);
        img = (ImageView) v.findViewById(R.id.imgNoCart);
        tv1.setVisibility(View.VISIBLE);
        tv2.setVisibility(View.VISIBLE);
        img.setVisibility(View.VISIBLE);
        viewOrder = v.findViewById(R.id.viewCircle);
        viewOrder.setVisibility(View.INVISIBLE);
        progressBarOrder = (ProgressBar) v.findViewById(R.id.progressLoading);
        progressBarOrder.setVisibility(View.INVISIBLE);
        progressBarOrder.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_white_1000), PorterDuff.Mode.SRC_IN);

        // Database model and view
        mAdapter = new MyCartAdapter(getActivity());
        recList = (RecyclerView) v.findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        // event bus
        //EventBus.getDefault().register(this);
        // remove on clic ?
        recList.addOnItemTouchListener(
            new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, final int position) {
                    new MaterialDialog.Builder(getActivity())
                            .title(DataManager.getInstance().getCartArray().get(position).getName())
                            .content(R.string.cafet_cart_action_title)
                            .positiveText(R.string.dialog_delete)
                            .negativeText(R.string.dialog_cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }

                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    DataManager.getInstance().getCartArray().remove(position);
                                    refreshCart();
                                    OrderTabsFragment mFragment = (OrderTabsFragment) getActivity().getSupportFragmentManager().findFragmentByTag(Constants.TAG_FRAGMENT_ORDER_TABS);
                                    mFragment.refreshCart();
                                    Toast.makeText(getActivity(),R.string.toast_cafet_removed, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                }
            })
        );

        // GO GO GO Post the order data to server !
        floatingShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int size = DataManager.getInstance().getCartArray().size();

                int nbMenu = 0;
                for (int i=0;i<size;i++)
                    if (DataManager.getInstance().getCartArray().get(i).getObjectType().equals(LacmdMenu.ID_CAT_MENU))
                        nbMenu++;

                if (size > 0 && size <= 10 && nbMenu <= 2) {

                    MaterialDialog md = new MaterialDialog.Builder(getActivity())
                            .customView(R.layout.dialog_add_instructions, false)
                            .title(R.string.cafet_comment)
                            .positiveText(R.string.cafet_dialog_confirm1)
                            .negativeText(R.string.dialog_cancel)
                            .cancelable(false)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    DataManager.getInstance().setInstructions(

                                            // Convert InputText into formatted string without Emojis / Unicode characters
                                            etInstr
                                                    .getText()
                                                    .toString()
                                                    .trim());

                                    new MaterialDialog.Builder(getActivity())
                                            .title(R.string.cafet_confirm_title)
                                            .content(R.string.cafet_confirm_txt)
                                            .positiveText(R.string.cafet_dialog_confirm2)
                                            .negativeText(R.string.dialog_cancel)
                                            .callback(new MaterialDialog.ButtonCallback() {
                                                @Override
                                                public void onPositive(MaterialDialog dialog) {
                                                    super.onPositive(dialog);
                                                    AsyncPostCart asyncPostCart = new AsyncPostCart();
                                                    asyncPostCart.execute();
                                                }

                                                @Override
                                                public void onNegative(MaterialDialog dialog) {
                                                    super.onNegative(dialog);
                                                }
                                            })
                                            .show();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }
                            })
                            .build();

                    etInstr = ((EditText)(md.getView().findViewById(R.id.etInstructions)));
                    md.show();

                } else if (size == 0){
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.cafet_empty_cart_title)
                            .content(R.string.cafet_empty_cart_txt)
                            .negativeText(R.string.dialog_indeed)
                            .show();
                } else if (size == 10){
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.cafet_full_cart_title)
                            .content(R.string.cafet_full_cart_txt)
                            .negativeText(R.string.cafet_dialog_remove)
                            .show();
                } else if (nbMenu > 2) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.cafet_too_much_title)
                            .content(R.string.cafet_too_much_txt)
                            .negativeText(R.string.dialog_shame)
                            .show();
                }
            }
        });

        return v;
    }

    public void refreshCart () {

        // Cart empty, or not ?
        if (DataManager.getInstance().getNbCartItems() == 0) {
            tv1.setVisibility(View.VISIBLE);
            tv2.setVisibility(View.VISIBLE);
            img.setVisibility(View.VISIBLE);
        } else {
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
            img.setVisibility(View.GONE);
        }

        // Total price
        tvPrice.setText(new DecimalFormat("0.00").format(DataManager.getInstance().getCartPrice()) + "€");

        // Notify the adapter
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Async task to post token & JSON data (in base64 format) to server
     */
    private class AsyncPostCart extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {

            String JSONstr = DataManager.getInstance().outputJSON();
            String instr = DataManager.getInstance().getInstructions();
            String resp = null;

            try {
                HashMap<String, String> pairs = new HashMap<>();
                pairs.put(getActivity().getResources().getString(R.string.token), DataManager.getInstance().getToken());
                pairs.put(getActivity().getResources().getString(R.string.data), Base64.encodeToString(JSONstr.getBytes("UTF-8"), Base64.NO_WRAP));
                pairs.put(getActivity().getResources().getString(R.string.instructions), Base64.encodeToString(instr.getBytes("UTF-8"), Base64.NO_WRAP));
                resp = ConnexionUtils.postServerData(Constants.URL_API_ORDER_SEND, pairs, getActivity());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return resp;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBarOrder.setVisibility(View.VISIBLE);
            floatingShop.setVisibility(View.INVISIBLE);
            viewOrder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            progressBarOrder.setVisibility(View.INVISIBLE);
            floatingShop.setVisibility(View.VISIBLE);
            viewOrder.setVisibility(View.INVISIBLE);

            if (Utilities.isNetworkDataValid(data)) {

                try {
                    JSONObject obj = new JSONObject(data);
                    if (obj.getInt("status") == 1) {

                        JSONObject objData = obj.getJSONObject("data");
                        final int idstr = objData.getInt("idcmd");
                        final double price = objData.getDouble("price");
                        final boolean lydia_enabled = objData.getBoolean("lydia_enabled");

                        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Create Calendar-Object and get hour

                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.cafet_cmd_validated_title)
                                .content(getString(R.string.cafet_cmd_validated_txt1)+" "
                                        + (hour < 12 ? getString(R.string.cafet_cmd_validated_txt2)
                                        : getString(R.string.cafet_cmd_validated_txt3)) + " " +
                                        getString(R.string.cafet_cmd_validated_txt4))
                                .positiveText(R.string.cafet_cmd_pay_lydia)
                                .positiveColor(
                                        (DataManager.getInstance().getCartPrice() >= 0.5 && lydia_enabled) ?
                                                getActivity().getResources().getColor(R.color.md_blue_700):
                                                getActivity().getResources().getColor(R.color.md_grey_500)
                                )
                                .negativeText(R.string.cafet_cmd_pay)
                                .cancelable(false)
                                .autoDismiss(false)
                                .callback(new MaterialDialog.ButtonCallback() {

                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);


                                        // Paiement uniquement si somme > 50c€ et si lydia actif
                                        if (DataManager.getInstance().getCartPrice() >= 0.5 && lydia_enabled) {

                                            dialog.hide();

                                            Intent i = new Intent(getActivity(), LydiaActivity.class);
                                            i.putExtra(Constants.KEY_LYDIA_ORDER_ID, idstr);
                                            i.putExtra(Constants.KEY_LYDIA_ORDER_TYPE, Constants.TYPE_LYDIA_CAFET);
                                            i.putExtra(Constants.KEY_LYDIA_ORDER_ASKED, false);
                                            getActivity().startActivity(i);

                                            getFragmentManager().popBackStackImmediate();


                                        } else if (!lydia_enabled) {
                                            Toast.makeText(getActivity(), "Le paiement par Lydia n'est actuellement pas disponible", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "Le paiement par Lydia ne peut se faire qu'avec une somme d'au moins de 0,50€", Toast.LENGTH_SHORT).show();
                                        }

                                        //Toast.makeText(getActivity(), R.string.toast_pay_lydia_temp, Toast.LENGTH_SHORT).show();

                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        super.onNegative(dialog);
                                        dialog.hide();
                                        getFragmentManager().popBackStackImmediate();
                                    }
                                })
                                .show();
                    } else {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.log_error_title)
                                .content(getString(R.string.error_server)+ "\n\n("+getString(R.string.reason)+" : " + obj.getString("cause") + ")")
                                .negativeText(R.string.dialog_close)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        super.onNegative(dialog);
                                    }
                                })
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.error_network_short)
                        .content(R.string.error_network)
                        .negativeText(R.string.dialog_close)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .show();
            }
        }
    }
}