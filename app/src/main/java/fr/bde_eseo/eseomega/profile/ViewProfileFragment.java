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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.MainActivity;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.interfaces.OnUserProfileChange;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.ImageUtils;
import fr.bde_eseo.eseomega.utils.Utils;

/**
 * Created by François L. on 29/07/2015.
 */
public class ViewProfileFragment extends Fragment {

    private static final int INTENT_GALLERY_ID = 0x42; // quarantdeuuux t'as vu
    private static final int RESULT_OK = -1;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private UserProfile profile;
    private TextView tvUserName, tvDisconnect;
    private String userName;
    private CircleImageView imageView;
    private OnUserProfileChange mOnUserProfileChange;
    private String userFirst;
    private MaterialDialog materialDialog;
    private AsyncDisconnect asyncDisconnect;

    public ViewProfileFragment () {}

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mOnUserProfileChange = (OnUserProfileChange) getActivity();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        if(asyncDisconnect != null)asyncDisconnect.cancel(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Find layout elements
        View rootView = inflater.inflate(R.layout.fragment_view_profile, container, false);
        tvUserName = (TextView) rootView.findViewById(R.id.tvUserName);
        tvDisconnect = (TextView) rootView.findViewById(R.id.tvDisconnect);
        imageView = (CircleImageView) rootView.findViewById(R.id.circleView);

        // Get current profile
        profile = new UserProfile();
        profile.readProfilePromPrefs(getActivity());
        //Log.d("PROFILE", profile.getId() + ", " + profile.getPushToken());
        userName = profile.getName();
        tvUserName.setText(userName);
        userFirst = profile.getFirstName();
        setImageView();

        // If user want to change its profile picture, call Intent to gallery
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyStoragePermissions(getActivity());
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, INTENT_GALLERY_ID);
            }
        });

        // If disconnects, reset profile and says bye-bye
        tvDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            tvDisconnect.setBackgroundColor(0x2fffffff);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvDisconnect.setBackgroundColor(0x00ffffff);
                }
            }, 500);

            MaterialDialog mdConfirm = new MaterialDialog.Builder(getActivity())
                .title(R.string.deconnect_title)
                .content(getString(R.string.deconnect_txt1)+" " + userFirst + getString(R.string.deconnect_txt2))
                .positiveText(R.string.deconnect_yes)
                .negativeText(R.string.deconnect_no)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        asyncDisconnect = new AsyncDisconnect(getActivity(), profile);
                        asyncDisconnect.execute(profile);
/*
                        materialDialog = new MaterialDialog.Builder(getActivity())
                                .title("Au revoir, " + userFirst + ".")
                                .content("Votre profil a été déconnecté de nos services.")
                                .negativeText("Fermer")
                                .cancelable(false)
                                .iconRes(R.drawable.ic_oppress)
                                .limitIconToDefaultSize()
                                .show();*/
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        Toast.makeText(getActivity(), R.string.toast_good_choice, Toast.LENGTH_SHORT).show();
                    }
                })
                .iconRes(R.drawable.ic_quit_user)
                .limitIconToDefaultSize()
                .show();

            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_GALLERY_ID && resultCode == RESULT_OK && data != null) {

            Uri profPicture = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(profPicture, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                if (profile != null) { // cas impossible, mais au cas où ...
                    profile.setPicturePath(picturePath);
                    profile.registerProfileInPrefs(getActivity());
                    setImageView();
                    mOnUserProfileChange.OnUserProfileChange(profile);
                }
            } else {
                Toast.makeText(getActivity(), R.string.toast_error_img, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setImageView() {
        File fp = new File(profile.getPicturePath());
        if (fp.exists()) {
            Bitmap bmp = ImageUtils.getResizedBitmap(BitmapFactory.decodeFile(profile.getPicturePath()), MainActivity.MAX_PROFILE_SIZE);
            if (bmp != null)
                imageView.setImageBitmap(bmp);
        }
    }

    // Class to disconnect profile from server
    private class AsyncDisconnect extends AsyncTask<UserProfile, String, String> {

        private Context ctx;
        private UserProfile profile;

        public AsyncDisconnect (Context ctx, UserProfile profile) {
            this.ctx = ctx;
            this.profile = profile;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            materialDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.deconnect_title)
                    .content(R.string.wait)
                    .negativeText(R.string.dialog_cancel)
                    .cancelable(false)
                    .progress(true, 4, false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            AsyncDisconnect.this.cancel(true);
                        }
                    })
                    .show();
        }

        @Override
        protected String doInBackground(UserProfile ... params) {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put(getResources().getString(R.string.client), this.profile.getId());
            pairs.put(getResources().getString(R.string.password), this.profile.getPassword());
            pairs.put(getResources().getString(R.string.os), Constants.APP_ID);
            pairs.put(getResources().getString(R.string.token), this.profile.getPushToken());
            pairs.put(getResources().getString(R.string.hash), EncryptUtils.sha256(
                    getResources().getString(R.string.MESSAGE_DESYNC_PUSH) +
                            this.profile.getId() +
                            this.profile.getPassword() +
                            Constants.APP_ID +
                            this.profile.getPushToken()));

            return ConnexionUtils.postServerData(Constants.URL_API_PUSH_UNREGISTER, pairs, getActivity());
        }

        @Override
        protected void onPostExecute(String data) {

            int retCode = -1;
            String err = "";

            // If disconnexion is successfull (no network problem)
            if (Utils.isNetworkDataValid(data)) {

                try {
                    JSONObject obj = new JSONObject(data);
                    retCode = obj.getInt("status");
                    err = obj.getString("cause");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Check server's answer
            if (retCode == 1) {

                // Success ! Remove profile from preferences
                materialDialog.dismiss();
                this.profile.removeProfileFromPrefs(ctx);
                this.profile.readProfilePromPrefs(ctx);
                mOnUserProfileChange.OnUserProfileChange(this.profile);

                // Delete cache file
                String cachePath = getActivity().getCacheDir() + "/";
                File cacheHistoryJSON = new File(cachePath + "history.json");
                if (cacheHistoryJSON.exists()) {
                    cacheHistoryJSON.delete();
                }

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.frame_container, new ConnectProfileFragment(), "frag0")
                        .commit();
            } else {

                materialDialog.dismiss();
                materialDialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.deconnect_error_title)
                        .content(R.string.deconnect_error_txt)
                        .negativeText(R.string.dialog_close)
                        .cancelable(false)
                        .show();
            }
        }
    }
}
