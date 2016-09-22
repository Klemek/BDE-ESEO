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

package fr.bde_eseo.eseomega.clubs;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.EventsListFragment;
import fr.bde_eseo.eseomega.news.ImageViewActivity;
import fr.bde_eseo.eseomega.news.NewsListFragment;
import fr.bde_eseo.eseomega.utils.Blur;
import fr.bde_eseo.eseomega.utils.JSONUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by François L. on 31/08/2015.
 */
public class ClubViewActivity extends AppCompatActivity {

    public static final String COM_SNAPCHAT_ANDROID = "com.snapchat.android";
    private Toolbar toolbar;
    private ClubItem clubItem;
    private TextView tvDesc, tvNoMember;
    private ImageView imgClub, iWeb, iFb, iTw, iSnap, iMail, iLinked, iPhone, iYou, iInsta;
    private ArrayList<MixedItem> items;
    private DetailsAdapter mAdapter;
    private RecyclerView recList;
    private RelativeLayout rlClubPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_view);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setPadding(0, Utilities.getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00263238")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#550000ff")));

        // Get parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Toast.makeText(ClubViewActivity.this, R.string.toast_error, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                clubItem = ClubDataHolder.getInstance().getClubs().get(extras.getInt(Constants.KEY_CLUB_VIEW));
                getSupportActionBar().setTitle(clubItem.getName());
            }
        } else {
            clubItem = ClubDataHolder.getInstance().getClubs().get((int) savedInstanceState.getSerializable(Constants.KEY_CLUB_VIEW));
            getSupportActionBar().setTitle(clubItem.getName());
        }

        tvDesc = (TextView) findViewById(R.id.tvDescClub);
        tvNoMember = (TextView) findViewById(R.id.tvNoDetails);
        imgClub = (ImageView) findViewById(R.id.imgClub);
        rlClubPicture = (RelativeLayout) findViewById(R.id.rlClubPicture);

        iWeb = (ImageView) findViewById(R.id.icoWeb);
        iFb = (ImageView) findViewById(R.id.icoFb);
        iTw = (ImageView) findViewById(R.id.icoTwitter);
        iSnap = (ImageView) findViewById(R.id.icoSnap);
        iMail = (ImageView) findViewById(R.id.icoMail);
        iLinked = (ImageView) findViewById(R.id.icoLinkedIn);
        iYou = (ImageView) findViewById(R.id.icoYoutube);
        iPhone = (ImageView) findViewById(R.id.icoPhone);
        iInsta = (ImageView) findViewById(R.id.icoInsta);

        tvDesc.setText(JSONUtils.fromHtml(clubItem.getDesc()));

        // Load image, decode it to Bitmap and return Bitmap to callback
        Picasso.with(this).load(clubItem.getImg()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap loadedImage, Picasso.LoadedFrom from) {
                imgClub.setImageBitmap(Blur.fastblur(ClubViewActivity.this, loadedImage, 12)); // seems ok
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });


        // Image visibility
        if (clubItem.hasContact(Constants.JSON_CLUB_CONTACTS_WEB)){
            iWeb.setVisibility(View.VISIBLE);
            // Web (browser)
            iWeb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intentToBrowser(clubItem.getContact(Constants.JSON_CLUB_CONTACTS_WEB));
                }
            });
        }
        else iWeb.setVisibility(View.GONE);

        if (clubItem.hasContact(Constants.JSON_CLUB_CONTACTS_FB)){
            iFb.setVisibility(View.VISIBLE);
            // Facebook (browser or app)
            iFb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intentToBrowser(clubItem.getContact(Constants.JSON_CLUB_CONTACTS_FB));
                }
            });
        }
        else iFb.setVisibility(View.GONE);

        if (clubItem.hasContact(Constants.JSON_CLUB_CONTACTS_TWIT)){
            iTw.setVisibility(View.VISIBLE);
            // Twitter (browser or app)
            iTw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + clubItem.getContact(Constants.JSON_CLUB_CONTACTS_TWIT)));
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);
                    else
                        intentToBrowser("https://twitter.com/" + clubItem.getContact(Constants.JSON_CLUB_CONTACTS_TWIT));
                }
            });
        }
        else iTw.setVisibility(View.GONE);

        if (clubItem.hasContact(Constants.JSON_CLUB_CONTACTS_SNAP)){
            iSnap.setVisibility(View.VISIBLE);
            // Snapchat (dialog or app)
            iSnap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MaterialDialog.Builder mdb = new MaterialDialog.Builder(ClubViewActivity.this)
                            .title(clubItem.getContact(Constants.JSON_CLUB_CONTACTS_SNAP))
                            .content(R.string.contact_snapchat)
                            .negativeText(R.string.dialog_close)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }

                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    openApp(ClubViewActivity.this, COM_SNAPCHAT_ANDROID);
                                }
                            });

                    if (Utilities.isPackageExisted(ClubViewActivity.this, COM_SNAPCHAT_ANDROID))
                        mdb.positiveText(R.string.open_snapchat);

                    mdb.show();
                }
            });
        }
        else iSnap.setVisibility(View.GONE);

        if (clubItem.hasContact(Constants.JSON_CLUB_CONTACTS_INST)){
            iInsta.setVisibility(View.VISIBLE);
            // Instagram (browser or app)
            iInsta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("instagram://user?username=" + clubItem.getContact(Constants.JSON_CLUB_CONTACTS_INST)));
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);
                    else
                        intentToBrowser("https://instagram.com/" + clubItem.getContact(Constants.JSON_CLUB_CONTACTS_INST) + "/");
                }
            });
        }
        else iInsta.setVisibility(View.GONE);

        if (clubItem.hasContact(Constants.JSON_CLUB_CONTACTS_MAIL)){
            iMail.setVisibility(View.VISIBLE);
            // Mail (app)
            iMail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", clubItem.getContact(Constants.JSON_CLUB_CONTACTS_MAIL), null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.contact_mail_subject));
                    emailIntent.putExtra(Intent.EXTRA_TEXT, getText(R.string.contact_mail_text));
                    startActivity(Intent.createChooser(emailIntent, getText(R.string.contact_mail_name) +" " + clubItem.getName() + " ..."));
                }
            });
        }
        else iMail.setVisibility(View.GONE);

        if (clubItem.hasContact(Constants.JSON_CLUB_CONTACTS_LI)){
            iLinked.setVisibility(View.VISIBLE);
            // LinkedIn (website or app)
            iLinked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intentToBrowser(clubItem.getContact(Constants.JSON_CLUB_CONTACTS_LI));
                }
            });
        }
        else iLinked.setVisibility(View.GONE);

        if (clubItem.hasContact(Constants.JSON_CLUB_CONTACTS_YOU)){
            iYou.setVisibility(View.VISIBLE);
            // Youtube (browser or app)
            iYou.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intentToBrowser(clubItem.getContact(Constants.JSON_CLUB_CONTACTS_YOU));
                }
            });
        }
        else iYou.setVisibility(View.GONE);

        if (clubItem.hasContact(Constants.JSON_CLUB_CONTACTS_TEL)){
            iPhone.setVisibility(View.VISIBLE);
            // Phone call (app)
            iPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clubItem.getContact(Constants.JSON_CLUB_CONTACTS_TEL)));
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);
                    else
                        Toast.makeText(ClubViewActivity.this, R.string.no_app, Toast.LENGTH_SHORT).show();
                }
            });
        }
        else iPhone.setVisibility(View.GONE);

        // Create data array
        items = new ArrayList<>();

        // Club picture clic : open it wide
        rlClubPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(ClubViewActivity.this, ImageViewActivity.class);
                myIntent.putExtra(Constants.KEY_IMG, clubItem.getImg());
                startActivity(myIntent);
            }
        });

        if(clubItem.isUpdated())
            updateDetails();
        new AsyncJSONClubDetail(getBaseContext()).execute(clubItem.getId());






    }

    public void updateDetails(){
        // Adapter & recycler view
        mAdapter = new DetailsAdapter();
        recList = (RecyclerView) findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        // Fill data array with Data Holder's objects
        for (ClubItem.SubList l: clubItem.getDetails()){
            if(l.getSize() > 0) {
                items.add(new MixedItem(l.getName()));
                for (ClubItem.ListItem li : l.getItems()) {
                    items.add(new MixedItem(li.getTitle(), li.getDetail(), li.getImg(), li.getId()));
                }
            }
        }

        if (items.size() > 0)
            tvNoMember.setVisibility(View.GONE);
        else
            tvNoMember.setVisibility(View.VISIBLE);

        mAdapter.notifyDataSetChanged();
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

    /**
     * Custom class for Member + Module
     */
    private class MixedItem {
        private boolean isModule;
        private String title, desc, imgLink;
        private int id;

        public MixedItem(String title) {
            this.title = title;
            isModule = true;
        }

        public MixedItem(String title, String desc, String imgLink, int id) {
            this.title = title;
            this.desc = desc;
            this.imgLink = imgLink;
            this.id = id;
            isModule = false;
        }

        public boolean isModule() {
            return isModule;
        }

        public int getId(){
            return id;
        }

        public String getImgLink() {
            return imgLink;
        }

        public String getDesc() {
            return desc;
        }

        public String getName() {
            return title;
        }
    }

    /**
     * Custom adapter for members + module header
     */
    public class DetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int TYPE_TITLE = 0;
        private final static int TYPE_ITEM = 1;

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final MixedItem mi = items.get(position);

            if (mi.isModule()) {
                TitleViewHolder mvh = (TitleViewHolder) holder;
                mvh.name.setText(mi.getName());
            } else {
                ItemViewHolder mvh = (ItemViewHolder) holder;
                mvh.name.setText(mi.getName());
                //String s = mi.getDesc();
                //mvh.desc.setText(s!=null&&s.length()>0?s:"Membre");
                mvh.desc.setText(JSONUtils.fromHtml(mi.getDesc()));
                if(mi.getImgLink().equals(ClubItem.EVENTTAG)){
                    Picasso.with(ClubViewActivity.this).load(R.drawable.ic_events2).into(mvh.img);
                    mvh.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EventsListFragment.openEvent(ClubViewActivity.this, mi.getId());
                        }
                    });
                }else if(mi.getImgLink().equals(ClubItem.NEWSTAG)){
                    Picasso.with(ClubViewActivity.this).load(R.drawable.ic_news2).into(mvh.img);
                    mvh.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NewsListFragment.openArticle(ClubViewActivity.this, mi.getId());
                        }
                    });
                }else{
                    if (mi.getImgLink().length() > 0){
                        Picasso.with(ClubViewActivity.this).load(mi.getImgLink()).placeholder(R.drawable.ic_unknown2).error(R.drawable.ic_unknown2).into(mvh.img);
                    }else{
                        Picasso.with(ClubViewActivity.this).load(R.drawable.ic_unknown2).into(mvh.img);
                    }
                }

            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_TITLE)
                return new TitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_club_header, parent, false));
            else
                return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_member, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).isModule() ? TYPE_TITLE : TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // Classic View Holder for Module (header)
        public class TitleViewHolder extends RecyclerView.ViewHolder {

            protected TextView name;

            public TitleViewHolder(View v) {
                super(v);
                name = (TextView) v.findViewById(R.id.tvModuleHeader);
            }
        }

        // Classic View Holder for Member
        public class ItemViewHolder extends RecyclerView.ViewHolder {

            protected TextView name, desc;
            protected CircleImageView img;
            protected View view;

            public ItemViewHolder(View v) {
                super(v);
                view = v;
                name = (TextView) v.findViewById(R.id.tvTitle);
                desc = (TextView) v.findViewById(R.id.tvDesc);
                img = (CircleImageView) v.findViewById(R.id.circleMember);
            }
        }
    }

    // Intent to browser
    public void intentToBrowser(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    /**
     * Open another app.
     *
     * @param context     current Context, like Activity, App, or Service
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();

        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
            //throw new PackageManager.NameNotFoundException();
        }
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(i);
        return true;
    }

    public class AsyncJSONClubDetail extends AsyncTask<Integer ,Void, JSONObject> {

        private Context ctx;

        public AsyncJSONClubDetail(Context ctx){
            this.ctx = ctx;
        }

        @Override
        protected void onPostExecute(JSONObject obj) {
            if(obj != null){
                try {
                    JSONArray bureau = obj.getJSONArray(Constants.JSON_CLUB_BUREAU);
                    JSONArray related = obj.getJSONArray(Constants.JSON_CLUB_RELATED);
                    JSONArray events = obj.getJSONArray(Constants.JSON_CLUB_EVENTS);
                    clubItem.update(ctx, bureau,related,events);
                    updateDetails();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected JSONObject doInBackground(Integer... params) {
            JSONObject details = JSONUtils.getJSONFromUrl(Constants.URL_JSON_CLUBS+params[0]);
            return details;
        }
    }

}
