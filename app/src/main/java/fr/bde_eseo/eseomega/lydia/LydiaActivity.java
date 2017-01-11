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

package fr.bde_eseo.eseomega.lydia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utils;

/**
 * Created by François L. on 05/01/2016.
 * Activité permettant de vérifier si, lors du retour de l'app Lydia, la commande a bien été passée
 * + également passer une demande de paiement + redir Lydia
 * V1.2
 * Sous la forme d'un dialogue ? tests
 */
public class LydiaActivity extends AppCompatActivity {

    // Returns
    private static int LAST_STATUS = 0;
    // Android lifecycle objects
    private Context context;
    // Our dialog
    private MaterialDialog md;
    private MaterialDialog.Builder mdb;
    // Our dialog's elements
    private EditText etPhone;
    private CheckBox checkRememberPhone;
    private View mdView;
    private TextView tvStatus;
    private ProgressBar progressStatus;
    // Divers
    private UserProfile userProfile;
    private String clientPhone;
    private boolean hasBeenPaused = false;
    private SharedPreferences prefsUser;
    // Intent-from
    private String orderType = "";
    private int orderID = -1;
    private String MOBILE_URL;
    //private String LYDIA_PACKAGE;
    private String LYDIA_INTENT;

    // Getteur
    public static int LAST_STATUS() {
        return LAST_STATUS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set view / call parent
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lydia);
        context = this;

        // Init intent flag
        INTENT_REQUEST intent_request = INTENT_REQUEST.ERROR;

        // Get intent parameters
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            String action = intent.getAction();

            if (action != null) {

                // Check if intent's action is correct (obviously yes, but prevents Manifest modifications)
                if (action.equals(Intent.ACTION_VIEW)) {

                    // Intent depuis Lydia
                    intent_request = INTENT_REQUEST.FROM_LYDIA;

                    // Get all parameters
                    Uri qUri = intent.getData();

                    // Order ID
                    String sOrderID = qUri.getQueryParameter("id");

                    if (sOrderID != null) {
                        orderID = Integer.parseInt(sOrderID);
                    }

                    orderType = qUri.getQueryParameter("cat");
                }

            } else if (extras != null) {

                // Intent interne
                intent_request = INTENT_REQUEST.FROM_APP;
                orderID = extras.getInt(Constants.KEY_LYDIA_ORDER_ID);
                orderType = extras.getString(Constants.KEY_LYDIA_ORDER_TYPE);

                // Demande de check ? → donc ask déjà effectué
                if (extras.getBoolean(Constants.KEY_LYDIA_ORDER_ASKED)) {
                    intent_request = INTENT_REQUEST.FROM_LYDIA;
                }
            }
        }

        // No intent received
        if (intent_request == INTENT_REQUEST.ERROR || orderID == -1) {
            Toast.makeText(context, R.string.toast_error, Toast.LENGTH_SHORT).show();
            close();
        }

        // Get objects
        userProfile = new UserProfile();
        userProfile.readProfilePromPrefs(context);
        prefsUser = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // Init dialog
        dialogInit();

        // Get intent type
        if (intent_request == INTENT_REQUEST.FROM_APP) {

            /**
             * FROM_APP : On doit demander le numéro de téléphone du client, puis lorsqu'il clique sur "Payer", effectuer une demande de paiement auprès de Lydia.
             * Il faut ensuite lancer l'Intent vers Lydia avec le bon numéro de requête.
             *
             * A afficher :
             * - InputText téléphone
             * - Checkbox mémorisation numéro
             * - Texte "légal" sur le numéro de téléphone
             * - Boutons Annuler / Valider → AsyncTask ask.php
             */
            dialogFromApp();

        } else {

            /**
             * FROM_LYDIA : L'activité vient d'être ouverte après que le client ait cliqué sur "Accepter" depuis l'app Lydia.
             * Dans 100% des cas, le retour à notre activité se fait si il a eu un paiement validé.
             * Cependant, on vérifiera le statut de la commande auprès de notre serveur malgré tout.
             *
             * A afficher :
             * - Texte "État de votre commande" → titre dialogue
             * - Texte status : actualisé toutes les 3 secondes → asyncTask
             * - Bouton Fermer si status différent de "en cours"
             */
            dialogFromLydia();
        }
    }

    /**
     * Initialise un dialogue avec une entrée texte téléphone
     * Les boutons Annuler / Payer permettent de fermer l'activité et d'effectuer une demande de paiement auprès de Lydia
     * → passage direct à l'AsyncTask si téléphone déjà renseigné → ok done
     */
    void dialogFromApp() {

        // If phone number already present, skip renseignements
        if (userProfile.verifyPhoneNumber(userProfile.getPhoneNumber())) {

            // Send request to server
            AsyncRequestLydia asyncRequestLydia = new AsyncRequestLydia();
            asyncRequestLydia.execute();

        } else {

            // Current title
            mdb.title(R.string.lydia_payment);

            // Current content
            mdb.customView(R.layout.dialog_lydia, false);

            // Customize
            mdb.positiveText(R.string.dialog_pay);
            mdb.negativeText(R.string.dialog_cancel);
            mdb.cancelable(false);
            mdb.autoDismiss(false);

            // Events
            mdb.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);

                    clientPhone = etPhone.getText().toString();

                    // Check if phone is correct
                    if (userProfile.verifyPhoneNumber(clientPhone)) {

                        userProfile.setPhoneNumber(clientPhone); // bug correction 3.+

                        // Save it to preferences if checkbox checked
                        if (checkRememberPhone.isChecked()) {
                            userProfile.registerProfileInPrefs(context);
                        }

                        // Send request to server
                        AsyncRequestLydia asyncRequestLydia = new AsyncRequestLydia();
                        asyncRequestLydia.execute();

                    } else {
                        // Phone syntax is bad
                        new MaterialDialog.Builder(context)
                                .title(R.string.lydia_phone_error_title)
                                .content(R.string.lydia_phone_error_text)
                                .negativeText(R.string.dialog_close)
                                .show();
                    }
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                    close();
                }
            });

            // Dialog view
            md = mdb.build();
            mdView = md.getCustomView();

            // Dialog's elements
            etPhone = (EditText) mdView.findViewById(R.id.etLydiaPhone);
            checkRememberPhone = (CheckBox) mdView.findViewById(R.id.checkRememberPhone);

            // Set values
            etPhone.setText(userProfile.getPhoneNumber());

            // Show dialog
            md.show();
        }
    }

    /**
     * Initialise un dialogue avec un texte affichant le statut de la commande
     */
    void dialogFromLydia() {

        // Current title
        mdb.title(R.string.lydia_payment_status);

        // Current content
        mdb.customView(R.layout.dialog_lydia_check, false);

        // Customize
        mdb.negativeText(R.string.dialog_close);
        mdb.cancelable(false);

        // Events
        mdb.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);

                if (LAST_STATUS == 1) {
                    // Retry (cause payment is pending)
                    intentToLydia();
                } else {
                    // Close only (success / fail)
                    close();
                }
            }
        });

        // Dialog view
        md = mdb.build();
        mdView = md.getCustomView();

        // Dialog's elements
        tvStatus = (TextView) mdView.findViewById(R.id.tvStatusLydia);
        progressStatus = (ProgressBar) mdView.findViewById(R.id.progressCheckLydia);
        progressStatus.getIndeterminateDrawable().setColorFilter(Utils.resolveColorFromTheme(context, R.attr.colorPrimaryDark), PorterDuff.Mode.SRC_IN);

        // Set values
        progressStatus.setVisibility(View.GONE);

        // Start asyncTask
        AsyncStatusLydia asyncStatusLydia = new AsyncStatusLydia();
        asyncStatusLydia.execute();

        // Show dialog
        md.show();

    }

    /**
     * Closes the dialog / activity
     */
    void close() {
        Intent data = new Intent();
        data.putExtra(Constants.RESULT_LYDIA_VALUE, 2);
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, data);
        } else {
            getParent().setResult(Activity.RESULT_OK, data);
        }
        LydiaActivity.this.finish();
    }

    /**
     * Make an Intent to Lydia App / Web navigator if app is not found
     */
    void intentToLydia() {

        // Configure and make Lydia Intent
        //String intentUri;
        boolean closeAfter = false;

        // Package Lydia exists ?
        //intentUri = LYDIA_INTENT; // suppose yes

        /*if (Utils.isPackageExisted(context, LYDIA_PACKAGE)) {
            intentUri = LYDIA_INTENT;
        } else {
            intentUri = MOBILE_URL; // Package doesn't exists : open URL
            closeAfter = true; // @see comment below
            Toast.makeText(context, "Le navigateur va être ouvert.", Toast.LENGTH_SHORT).show();
        }*/

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(LYDIA_INTENT));

        PackageManager packageManager = context.getPackageManager();
        if (i.resolveActivity(packageManager) != null) {
            startActivity(i);
        } else { // Package doesn't exists : open URL
            Intent iweb = new Intent();
            closeAfter = true; // @see comment below
            Toast.makeText(context, R.string.toast_lydia_payment_web, Toast.LENGTH_SHORT).show();
            iweb.setData(Uri.parse(MOBILE_URL));
            startActivity(iweb);
        }

        if (closeAfter) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    md.dismiss();
                    close(); // prevent app-resume with null orderID
                    Toast.makeText(context, "Closing LydiaActivity ...", Toast.LENGTH_SHORT).show();
                }
            }, 1000);
        }
    }

    /**
     * If the user come back to the Activity without Intent :
     * It means that the payment has failed / was cancelled
     * We have to show a Dialog which checks order status from server and then show status to user
     */
    @Override
    public void onResume() {
        super.onResume();

        if (hasBeenPaused) {
            hasBeenPaused = false;

            dialogInit();
            dialogFromLydia();
        }
    }

    /**
     * On pause : just save the app has been paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        hasBeenPaused = true;
    }

    /**
     * Set TextView status text and color
     */
    void updateTextStatus(String text, int status, boolean isLoading) {

        tvStatus.setText(text);
        if (isLoading) {
            tvStatus.setTextColor(Utils.resolveColorFromTheme(context, R.attr.colorPrimaryDark));
        } else {
            if (status == 0) {
                tvStatus.setTextColor(context.getResources().getColor(R.color.md_prim_dark_yellow));
            } else if (status == 1) {
                tvStatus.setTextColor(Utils.resolveColorFromTheme(context, R.attr.colorPrimaryDark));
            } else if (status == 2) {
                tvStatus.setTextColor(context.getResources().getColor(R.color.circle_ready));
            } else {
                tvStatus.setTextColor(context.getResources().getColor(R.color.circle_error));
            }
        }
    }

    /**
     * Init the dialog for the current session
     */
    void dialogInit() {
        mdb = new MaterialDialog.Builder(context);
        mdb.theme(Theme.LIGHT);
        mdb.titleColor(Utils.resolveColorFromTheme(context, R.attr.colorPrimaryDark));
    }

    // Types de requêtes qui ont lieu lors du onCreate (première ouverture de l'app)
    private enum INTENT_REQUEST {
        FROM_LYDIA, // Via URL Scheme
        FROM_APP,   // Via Intent interne
        ERROR       // Aucun des deux précédents
    }

    /**
     * Class to create request to server (Lydia - ESEOmega)
     */
    private class AsyncRequestLydia extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String resp = null;
            try {
                String b64phone = Base64.encodeToString(userProfile.getPhoneNumber().getBytes("UTF-8"), Base64.NO_WRAP);
                String strOrder = String.valueOf(orderID);
                HashMap<String, String> pairs = new HashMap<>();
                pairs.put("username", userProfile.getId());
                pairs.put("password", userProfile.getPassword());
                pairs.put("phone", b64phone);
                pairs.put("idcmd", strOrder);
                pairs.put("cat_order", orderType);
                pairs.put("os", Constants.APP_ID); // Pour recevoir un Intent dédié à Android, sinon c'est iOS
                pairs.put("hash", EncryptUtils.sha256(userProfile.getId() + userProfile.getPassword() + b64phone + strOrder + orderType + getString(R.string.lydia_payment_done)));
                resp = ConnexionUtils.postServerData(Constants.URL_API_LYDIA_ASK, pairs, context);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return resp;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (md != null) {
                md.dismiss();
            }
            md = new MaterialDialog.Builder(context)
                    .title(R.string.lydia_payment_ask)
                    .content(R.string.wait)
                    .cancelable(false)
                    .progressIndeterminateStyle(false)
                    .progress(true, 0, false)
                    .show();
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            md.hide();
            int result = 0;
            String msg = getString(R.string.error_network_unknown);

            // Check data
            if (Utils.isNetworkDataValid(data)) {

                try {
                    // Get object
                    JSONObject obj = new JSONObject(data);
                    result = obj.getInt("status");
                    msg = obj.getString("cause");

                    // Get placeholder_error-status
                    if (result == 1) {

                        // Get shared data
                        JSONObject sharedData = obj.getJSONObject("data");

                        // Get Lydia pay values
                        MOBILE_URL = sharedData.getString("lydia_url"); // IMPORTANT : Utiliser si app Lydia non installée !
                        //LYDIA_PACKAGE = sharedData.getString("lydia_package");
                        LYDIA_INTENT = sharedData.getString("lydia_intent");

                        // Configure and make Lydia Intent
                        intentToLydia();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (result != 1) {
                md = new MaterialDialog.Builder(context)
                        .title(R.string.error)
                        .content(getString(R.string.reason) + " : " + msg + "\n(code : " + result + ")")
                        .negativeText(R.string.dialog_close)
                        .show();
            }
        }
    }

    /**
     * AsyncTask inside check sub dialog (Lydia callback intent)
     */
    private class AsyncStatusLydia extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String strOrder = String.valueOf(orderID);
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("username", userProfile.getId());
            pairs.put("password", userProfile.getPassword());
            pairs.put("idcmd", strOrder);
            pairs.put("cat_order", orderType);
            pairs.put("hash", EncryptUtils.sha256(userProfile.getId() + userProfile.getPassword() + strOrder + orderType + getString(R.string.lydia_payment_refused)));
            return ConnexionUtils.postServerData(Constants.URL_API_LYDIA_CHECK, pairs, context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressStatus.setVisibility(View.VISIBLE);
            updateTextStatus(getString(R.string.lydia_payment_check), 0, true);
        }

        @Override
        protected void onPostExecute(final String data) {
            super.onPostExecute(data);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    progressStatus.setVisibility(View.GONE);

                    int result = 0;
                    String msg = getString(R.string.error_network_unknown);

                    // Check data
                    if (Utils.isNetworkDataValid(data)) {

                        try {
                            // Get object
                            JSONObject obj = new JSONObject(data);
                            result = obj.getInt("status");
                            msg = obj.getString("cause");

                            // Get placeholder_error-status
                            if (result == 1) {

                                // Get shared data
                                JSONObject sharedData = obj.getJSONObject("data");

                                // Get Lydia order status
                                int status = sharedData.getInt("status");
                                String info = sharedData.getString("info");

                                // Ok ?
                                updateTextStatus(info, status, false);

                                // Save result
                                LAST_STATUS = status;

                                // If status is 1 (pending payment)
                                if (status == 1) {

                                    // Then set close button value : Fermer → Réessayer
                                    md.setActionButton(DialogAction.NEGATIVE, R.string.dialog_retry);

                                } else {

                                    // Then set close button value : Fermer
                                    md.setActionButton(DialogAction.NEGATIVE, R.string.dialog_close);

                                }

                            } else {
                                LAST_STATUS = result;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (result != 1) {
                        updateTextStatus(getString(R.string.error)+" : " + msg, -1, false);
                    }
                }
            }, 2000);
        }
    }
}
