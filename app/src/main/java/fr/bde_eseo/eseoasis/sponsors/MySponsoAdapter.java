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

package fr.bde_eseo.eseoasis.sponsors;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.bde_eseo.eseoasis.R;
import fr.bde_eseo.eseoasis.utils.JSONUtils;

/**
 * Created by François L. on 11/08/2015.
 */
public class MySponsoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<SponsorItem> sponsorItems;
    private Context ctx;
    private float px;
    private Drawable drawable;
    private MyGetter myGetter;


    public MySponsoAdapter(Context ctx, ArrayList<SponsorItem> sponsorItems) {
        this.sponsorItems = sponsorItems;
        this.ctx = ctx;

        // Dp to Pixels : 15 -> ?
        px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, ctx.getResources().getDisplayMetrics());

        drawable = ctx.getResources().getDrawable(R.drawable.ic_green_avantage);
        drawable.setBounds(0,0,(int)px,(int)px);
        myGetter = new MyGetter();
    }

    public void setSponsorItems(ArrayList<SponsorItem> sponsorItems) {
        this.sponsorItems = sponsorItems;
        notifyDataSetChanged();
    }

    @Override
    public SponsorTipsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SponsorTipsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_sponso, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final SponsorItem si = sponsorItems.get(position);

        SponsorTipsViewHolder svh = (SponsorTipsViewHolder) holder;
        svh.name.setText(si.getName());
        svh.details.setText(JSONUtils.fromHtml(si.getDetail()));
        if (si.hasUrl()){
            svh.adr.setVisibility(View.VISIBLE);
            svh.adr.setText(si.getUrl().replace("http://", ""));
            svh.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (si.getUrl() != null && si.getUrl().length() > 0) {
                        String url = si.getUrl();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        ctx.startActivity(i);
                    }
                }
            });
        } else if(si.hasAdr()){
            svh.adr.setVisibility(View.VISIBLE);
            svh.adr.setText(si.getAdr());
            svh.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(si.getAdr()));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        v.getContext().startActivity(mapIntent);
                    } catch (ActivityNotFoundException ex) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
                        v.getContext().startActivity(intent);
                    }
                }
            });
        } else{
            svh.adr.setVisibility(View.GONE);
        }

        if (si.getAvantages().size() == 0) {
            svh.offers.setVisibility(View.GONE);
        } else {
            svh.offers.setVisibility(View.VISIBLE);
            String tv = ctx.getString(R.string.sponso_perks)+" :<br/>";
            ArrayList<String> av = si.getAvantages();
            for (int i=0;i<av.size();i++) {
                tv += "<img src=\"check.png\" />&nbsp; " + av.get(i);
                if (i!=av.size()-1)
                    tv += "<br/>";
            }

            svh.offers.setText(Html.fromHtml(tv, myGetter, null));
        }

        Picasso.with(ctx).load(si.getImg()).into(svh.imageView);
    }

    private class MyGetter implements Html.ImageGetter {

        public MyGetter () {

        }

        @Override
        public Drawable getDrawable(String source) {
            if (source.equals("check.png"))
                return drawable;
            else
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return sponsorItems.size();
    }

    // Classic View Holder for Sponsor
    public class SponsorTipsViewHolder extends RecyclerView.ViewHolder {

        protected CircleImageView imageView;
        protected View view;
        protected TextView name, details, adr, offers;

        public SponsorTipsViewHolder(View v) {
            super(v);
            view = v;
            name = (TextView) v.findViewById(R.id.tvNameSponsor);
            details = (TextView) v.findViewById(R.id.tvDescSponsor);
            imageView = (CircleImageView) v.findViewById(R.id.circleSponsor);
            adr = (TextView) v.findViewById(R.id.tvAdr);
            offers = (TextView) v.findViewById(R.id.tvOffers);
        }
    }
}
