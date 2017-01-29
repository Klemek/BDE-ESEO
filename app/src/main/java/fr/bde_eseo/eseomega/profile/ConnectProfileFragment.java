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

package fr.bde_eseo.eseomega.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.gcmpush.RegistrationIntentService;
import fr.bde_eseo.eseomega.interfaces.OnUserProfileChange;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.DateUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utils;

/**
 * Created by François on 13/04/2015.
 */
public class ConnectProfileFragment extends Fragment {

    private MaterialEditText etUserID, etUserPassword;
    private MaterialDialog mdProgress;
    private String userID, userName, userPassword, userLogin;
    private OnUserProfileChange mOnUserProfileChange;
    private String[] bullshitHint;
    private Random rand;
    private AsyncLogin asyncLogin;
    private UserProfile profile;

    public ConnectProfileFragment() {
    }

    // Function to set the dialog visibity from activity
    public void setPushRegistration (boolean isSent) {
        if (isSent) {
            if (mdProgress != null) mdProgress.hide();
            //Toast.makeText(getActivity(), "Enregistrement notifs ok !", Toast.LENGTH_SHORT).show();

            new MaterialDialog.Builder(getActivity())
                    .title(getText(R.string.user_welcome) +" " + profile.getFirstName() + " !")
                    .negativeText(R.string.dialog_close)
                    .content(R.string.user_welcome_desc)
                    .iconRes(R.drawable.ic_checked_user)
                    .show();

        } else {
            if (mdProgress != null) mdProgress.hide();

            new MaterialDialog.Builder(getActivity())
                    .title(R.string.log_error_title)
                    .content(R.string.log_error_desc)
                    .negativeText(R.string.dialog_close)
                    .iconRes(R.drawable.ic_error_user)
                    .show();
        }

        mOnUserProfileChange.OnUserProfileChange(profile);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.frame_container, new ViewProfileFragment(), "FRAG_VIEW_PROFILE")
                .commit();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mOnUserProfileChange = (OnUserProfileChange) getActivity();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        if(asyncLogin != null)asyncLogin.cancel(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Find layout elements
        View rootView = inflater.inflate(R.layout.fragment_connect_profile, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Button btValid = (Button) rootView.findViewById(R.id.button_disconnect);
        etUserID = (MaterialEditText) rootView.findViewById(R.id.etUserID);
        etUserID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int l = charSequence.length();
                if (l > 0 && charSequence.charAt(l - 1) == '@') {
                    etUserID.setText(charSequence.subSequence(0, l - 1) + UserProfile.RESEAU_ESEO_FR);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etUserPassword = (MaterialEditText) rootView.findViewById(R.id.etUserPassword);
        //Utils.hideSoftKeyboard(getActivity()); // UI's better with that

        rand = new Random();

        // Bullshit
        bullshitHint = getActivity().getResources().getStringArray(R.array.bullshitHintUser);
        etUserID.setHint(bullshitHint[rand.nextInt(bullshitHint.length)]);

        // Listener on validation button
        btValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etUserID.getText().toString().length() < 1) {
                    etUserID.setError(getText(R.string.login_no_id));
                } else if (etUserPassword.getText().toString().length() < 1) {
                    etUserPassword.setError(getText(R.string.login_no_pass));
                } else {
                    asyncLogin = new AsyncLogin(getActivity());
                    asyncLogin.execute();
                }
                etUserID.setHint(bullshitHint[rand.nextInt(bullshitHint.length)]);

            }
        });




        return rootView;
    }

    /**
     * Async task to login client
     */
    class AsyncLogin extends AsyncTask<String, String, String> {

        private final Context ctx;
        private String enPass;

        public AsyncLogin (Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            userID = etUserID.getText().toString().toLowerCase(DateUtils.getLocale()).trim();
            etUserID.setText(userID);
            userPassword = etUserPassword.getText().toString();
            enPass = EncryptUtils.passBase64(userPassword);

            mdProgress = new MaterialDialog.Builder(ctx)
                    .title(R.string.wait)
                    .content(R.string.connecting)
                    .negativeText(R.string.dialog_cancel)
                    .cancelable(false)
                    .progress(true, 4, false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            AsyncLogin.this.cancel(true);
                        }
                    })
                    .show();
            /*
                mdProgress = new MaterialDialog.Builder(ctx)
                        .title("Oups ...")
                        .content("Impossible d'accéder au réseau. Veuillez vérifier votre connexion, puis réessayer.")
                        .negativeText("Fermer")
                        .cancelable(false)
                        .iconRes(R.drawable.ic_facepalm)
                        .limitIconToDefaultSize()
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                AsyncLogin.this.cancel(true);
                            }
                        })
                        .show();
            }*/
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            //Log.d("PROGRESS", "Called");
            //mdProgress.incrementProgress(1);
        }

        @Override
        protected String doInBackground(String... urls) {

            HashMap<String, String> pairs = new HashMap<>();
            pairs.put(getActivity().getResources().getString(R.string.mail), userID);
            pairs.put(getActivity().getResources().getString(R.string.pass), enPass);
            pairs.put(getActivity().getResources().getString(R.string.hash), EncryptUtils.sha256(userID + enPass + getActivity().getResources().getString(R.string.MEMORY_SYNC_USER)));

            if (Utils.isOnline(getActivity())) {
                return ConnexionUtils.postServerData(Constants.URL_API_CLIENT_CONNECT, pairs, getActivity());
            } else {
                return null;
            }
        }

        // Once connexion is done
        @Override
        protected void onPostExecute(String result) {

            String res;
            int status = 0;
            // Vérification des données reçues
            if (Utils.isNetworkDataValid(result)) {
                try {
                    JSONObject obj = new JSONObject(result);
                    status = obj.getInt("status");

                    if (status == 1) {
                        JSONObject data = obj.getJSONObject("data");
                        userName = data.getString("username");
                        userLogin = data.getString("login");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Pas d'erreur ? Mdp ok ?
            if (status == 1) {

                // On crée le profil
                // Le nom / prénom de l'utilisateur est stocké dans le champ "username" du JSON retourné
                profile = new UserProfile(ctx, userName, userLogin, userPassword);
                profile.setEmail(userID);
                //profile.guessEmailAddress();
                profile.registerProfileInPrefs(getActivity());

                // On check les Services Google, puis on démarre la classe Registration
                if (Utils.checkPlayServices(getActivity())) {
                    mdProgress.setContent(getText(R.string.registration));
                    // Start IntentService to register this application with GCM.
                    Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
                    getActivity().startService(intent);
                }
            } else {

                // Erreur : -2 →
                mdProgress.dismiss();
                res = (status==-2) ?
                        getString(R.string.error_password) + "\n" :
                        getString(R.string.error_network_unknown);

                mdProgress = new MaterialDialog.Builder(ctx)
                        .title(R.string.log_error_title)
                        .content(res)
                        .negativeText(R.string.dialog_close)
                        .iconRes(R.drawable.ic_quit_user)
                        .show();

            }
        }
    }
}
