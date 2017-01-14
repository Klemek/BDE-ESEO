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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.lacommande.model.DetailedItem;
import fr.bde_eseo.eseomega.lacommande.model.HistoryItem;
import fr.bde_eseo.eseomega.lydia.LydiaActivity;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.Blur;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.DateUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.ThemeUtils;
import fr.bde_eseo.eseomega.utils.Utils;

/**
 * Created by François L. on 10/01/2016.
 * Affiche les détails d'une commande / permet de payer. Style clear material design.
 */
public class OrderDetailsActivity extends AppCompatActivity {

    private static final int RUN_UPDATE = 8000;
    private static final int RUN_START = 100;
    private static Handler mHandler;
    private static boolean run;
    /**
     * Background task to fetch data periodically from server
     */
    private final Runnable updateTimerThread = new Runnable() {
        public void run() {
            try {
                if (run) {
                    AsyncDetails async = new AsyncDetails();
                    async.execute();
                    run = false;
                }
            } catch (NullPointerException e) { // Stop handler if fragment disappears
                mHandler.removeCallbacks(updateTimerThread);
                run = false;
            }
        }
    };
    private TextView tvOrderDetails, tvOrderPrice, tvOrderDate, tvOrderNumero, tvDesc, tvInstruction, tvInstrHeader;
    private ImageView imgCategory;
    private ProgressBar progressBar;
    private RelativeLayout rl1, rl2;
    // Android
    private Context context;
    // Others
    private float oldScreenBrightness;
    private int idcmd;
    private String oldData = "";
    private UserProfile profile;
    private DetailedItem detailedItem = null;
    // Couleurs des commandes
    private int circle_preparing, blue_light, circle_done, gray_light, circle_ready, green_light, circle_error, orange_light;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setTheme(ThemeUtils.preferredTheme(getApplicationContext()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setPadding(0, Utils.getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00263238")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#550000ff")));

        // Android setup
        context = OrderDetailsActivity.this;

        // Intent recuperation
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Toast.makeText(context, R.string.toast_error, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                idcmd = extras.getInt(Constants.KEY_ORDER_ID);
            }
        }

        // Layout
        tvOrderDate = (TextView) findViewById(R.id.tvCommandDate);
        tvOrderPrice = (TextView) findViewById(R.id.tvCommandPrice);
        tvOrderDetails = (TextView) findViewById(R.id.tvOrderDetail);
        tvOrderNumero = (TextView) findViewById(R.id.tvCommandNumero);
        tvInstruction = (TextView) findViewById(R.id.tvOrderInstructions);
        tvInstrHeader = (TextView) findViewById(R.id.tvHeaderInstructions);
        progressBar = (ProgressBar) findViewById(R.id.progressDetails);
        tvDesc = (TextView) findViewById(R.id.textView3);
        imgCategory = (ImageView) findViewById(R.id.imgOrder);
        rl1 = (RelativeLayout) findViewById(R.id.relativeLayout3);
        rl2 = (RelativeLayout) findViewById(R.id.relativeLayout5);

        progressBar.setVisibility(View.VISIBLE);
        tvOrderDate.setVisibility(View.INVISIBLE);
        tvOrderPrice.setVisibility(View.INVISIBLE);
        tvOrderDetails.setVisibility(View.INVISIBLE);
        tvOrderNumero.setVisibility(View.INVISIBLE);
        tvDesc.setVisibility(View.INVISIBLE);
        imgCategory.setVisibility(View.INVISIBLE);
        rl1.setVisibility(View.INVISIBLE);
        rl2.setVisibility(View.INVISIBLE);

        // profile
        profile = new UserProfile();
        profile.readProfilePromPrefs(context);

        // Save old brightness level and set it now to 100%
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        oldScreenBrightness = layout.screenBrightness;
        layout.screenBrightness = 1F;
        getWindow().setAttributes(layout);

        // Couleurs
        circle_preparing = context.getResources().getColor(R.color.circle_preparing);
        blue_light = context.getResources().getColor(R.color.blue_light);
        circle_done = context.getResources().getColor(R.color.circle_done);
        gray_light = context.getResources().getColor(R.color.gray_light);
        circle_ready = context.getResources().getColor(R.color.circle_ready);
        green_light = context.getResources().getColor(R.color.green_light);
        circle_error = context.getResources().getColor(R.color.circle_error);
        orange_light = context.getResources().getColor(R.color.orange_light);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_pay_lydia:

                /**
                 * Le mec peut payer :
                 * - commande en préparation
                 * - commande prête
                 * - commande impayée
                 *
                 * Si :
                 * - idlyida = -1 : pas de requête de fait, on demande le paiement (ask) → si Lydiaenabled
                 * - idlydia != -1 : requete effectuée, on vérifie le paiement (check) → si Lydiaenabled
                 * - status = 2 : La commande est terminée, impossible de payer, on affiche un Toast. Idem si paidbefore = 1
                 */

                if (detailedItem != null) {
                    if (detailedItem.getCommandStatus() == 2 || detailedItem.isPaidbefore()) {
                        Toast.makeText(context, R.string.toast_cmd_already_paid, Toast.LENGTH_SHORT).show();
                    } else {
                        Intent iPay = new Intent(OrderDetailsActivity.this, LydiaActivity.class);
                        iPay.putExtra(Constants.KEY_LYDIA_ORDER_ID, idcmd);
                        iPay.putExtra(Constants.KEY_LYDIA_ORDER_TYPE, Constants.TYPE_LYDIA_CAFET);
                        iPay.putExtra(Constants.KEY_LYDIA_ORDER_ASKED, detailedItem.getIdlydia() != -1);
                        startActivity(iPay);
                    }
                }

                return true;

            case android.R.id.home:
                this.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Delay to update data
        run = true;

        if (mHandler == null) {
            mHandler = new android.os.Handler();
            mHandler.postDelayed(updateTimerThread, RUN_START);
        } else {
            mHandler.removeCallbacks(updateTimerThread);
            mHandler.postDelayed(updateTimerThread, RUN_START);
        }
    }

    @Override
    public void onPause() {
        if (mHandler != null) {
            mHandler.removeCallbacks(updateTimerThread);
        }
        run = false;
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        run = false;
        mHandler.removeCallbacks(updateTimerThread);

        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = oldScreenBrightness;
        getWindow().setAttributes(layout);
    }

    public Date getParsedDate(String strDate) {
        return DateUtils.oldfromString(strDate);
    }

    /**
     * Async task to download order details
     */
    private class AsyncDetails extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            run = false;
        }

        @Override
        protected String doInBackground(String... params) {

            if (context != null) {

                HashMap<String, String> pairs = new HashMap<>();
                pairs.put(context.getResources().getString(R.string.idcmd), String.valueOf(idcmd));
                pairs.put(context.getResources().getString(R.string.username), profile.getId());
                pairs.put(context.getResources().getString(R.string.password), profile.getPassword());
                pairs.put(context.getResources().getString(R.string.hash), EncryptUtils.sha256(context.getResources().getString(R.string.MACRO_SYNC_SINGLE) + String.valueOf(idcmd) + profile.getId() + profile.getPassword()));

                return ConnexionUtils.postServerData(Constants.URL_API_ORDER_RESUME, pairs, context);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);

            if (Utils.isNetworkDataValid(str)) {

                if (!str.equals(oldData)) {
                    oldData = str;
                    try {
                        JSONObject obj = new JSONObject(str);

                        if (obj.getInt("status") == 1) {

                            detailedItem = new DetailedItem(getBaseContext(), obj.getJSONObject("data"), idcmd);

                            tvOrderDate.setText(detailedItem.getCommandDate());
                            tvOrderNumero.setText(detailedItem.getCommandNumberAsString());
                            if (detailedItem.getInstructions().length() > 0) {
                                tvInstruction.setText(detailedItem.getInstructions());
                                tvInstrHeader.setVisibility(View.VISIBLE);
                                tvInstruction.setVisibility(View.VISIBLE);
                            } else {
                                tvInstrHeader.setVisibility(View.GONE);
                                tvInstruction.setVisibility(View.GONE);
                            }

                            String txtDesc = detailedItem.getCommandName();
                            txtDesc = " - " + txtDesc.replaceAll("<br>", "\n - ");
                            tvOrderDetails.setText(txtDesc);
                            tvOrderPrice.setText(detailedItem.getCommandPriceAsString());

                            // Load image, decode it to Bitmap and return Bitmap to callback

                            Picasso.with(context).load(detailedItem.getImgUrl()).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap loadedImage, Picasso.LoadedFrom from) {
                                    imgCategory.setImageBitmap(Blur.fastblur(context, loadedImage, 12)); // seems ok
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });

                            int color = 0;
                            switch (detailedItem.getCommandStatus()) {
                                case HistoryItem.STATUS_PREPARING:
                                    color = circle_preparing;
                                    rl2.setBackgroundColor(blue_light);
                                    break;
                                case HistoryItem.STATUS_DONE:
                                    color = circle_done;
                                    rl2.setBackgroundColor(gray_light);
                                    break;
                                case HistoryItem.STATUS_READY:
                                    color = circle_ready;
                                    rl2.setBackgroundColor(green_light);
                                    break;
                                case HistoryItem.STATUS_NOPAID:
                                    color = circle_error;
                                    rl2.setBackgroundColor(orange_light);
                                    break;
                            }

                            tvOrderDate.setVisibility(View.VISIBLE);
                            tvOrderPrice.setVisibility(View.VISIBLE);
                            tvOrderDetails.setVisibility(View.VISIBLE);
                            tvOrderNumero.setVisibility(View.VISIBLE);
                            tvDesc.setVisibility(View.VISIBLE);
                            imgCategory.setVisibility(View.VISIBLE);
                            rl1.setVisibility(View.VISIBLE);
                            rl2.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);

                            // Assignation des couleurs
                            rl1.setBackgroundColor(color);
                            tvDesc.setTextColor(color);

                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            tvOrderDate.setVisibility(View.INVISIBLE);
                            tvOrderPrice.setVisibility(View.INVISIBLE);
                            tvOrderDetails.setVisibility(View.INVISIBLE);
                            tvOrderNumero.setVisibility(View.INVISIBLE);
                            tvDesc.setVisibility(View.INVISIBLE);
                            imgCategory.setVisibility(View.INVISIBLE);
                            rl1.setVisibility(View.INVISIBLE);
                            rl2.setVisibility(View.INVISIBLE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (context != null) {

                    Toast.makeText(context, R.string.error_network_unknown, Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    }, 500);
                }
            }

            mHandler.postDelayed(updateTimerThread, RUN_UPDATE);
            run = true;
        }
    }
}