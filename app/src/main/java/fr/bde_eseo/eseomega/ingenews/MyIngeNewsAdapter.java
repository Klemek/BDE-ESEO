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

package fr.bde_eseo.eseomega.ingenews;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;

/**
 * Created by François L. on 11/08/2015.
 */
public class MyIngeNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<IngenewsItem> ingenewsItems;
    private Context ctx;

    public MyIngeNewsAdapter(Context ctx, ArrayList<IngenewsItem> ingenewsItems) {
        this.ingenewsItems = ingenewsItems;
        this.ctx = ctx;
    }

    public ArrayList<IngenewsItem> getIngenewsItems() {
        return ingenewsItems;
    }

    public void setIngenewsItems(ArrayList<IngenewsItem> ingenewsItems) {
        this.ingenewsItems = ingenewsItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IngeNewsItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_ingenews, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        IngenewsItem ii = ingenewsItems.get(position);
        IngeNewsItemViewHolder inivh = (IngeNewsItemViewHolder) holder;
        inivh.name.setText(ii.getName());
        inivh.details.setText(ii.getDetails());

        if (ii.getImgLink() != null && ii.getImgLink().length() > 0)
            Picasso.with(ctx).load(ii.getImgLink()).placeholder(R.drawable.solid_loading_background).error(R.drawable.solid_loading_background).into(inivh.imgThumb);
        else
            inivh.imgThumb.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_file));

        // On click listener → open PDF file (intent)
        //Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(ingenewsItems.get(position).getFile()));
        inivh.llGlobal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Old code ...
                //Intent i = new Intent(Intent.ACTION_VIEW);
                //i.setData(Uri.parse(ingenewsItems.get(position).getFile()));
                //ctx.startActivity(i);

                // Ask to view directly
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(ingenewsItems.get(position).getFile()));
                if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                    ctx.startActivity(intent);
                } else {
                    Toast.makeText(ctx, "Pas d'application installée pour ça !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // On long click listener → share link to a friend
        inivh.icShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain"); // texte brut (lien de la news en pdf ...)
                intent.putExtra(Intent.EXTRA_TEXT, "\"" + ingenewsItems.get(position).getName() + "\"\n" + ingenewsItems.get(position).getFile());
                ctx.startActivity(Intent.createChooser(intent, "Partager ..."));

            }
        });
    }

    @Override
    public int getItemCount() {
        return ingenewsItems.size();
    }

    // Classic View Holder for Ingénews item
    public class IngeNewsItemViewHolder extends RecyclerView.ViewHolder {

        protected TextView name, details;
        protected ImageView imgThumb;
        protected RelativeLayout icShare;
        protected LinearLayout llGlobal;

        public IngeNewsItemViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tvNameIngeNews);
            details = (TextView) v.findViewById(R.id.tvDetailsIngeNews);
            llGlobal = (LinearLayout) v.findViewById(R.id.llGlobal); // For local-adapter on click listener
            imgThumb = (ImageView) v.findViewById(R.id.imgThumbFile);
            icShare = (RelativeLayout) v.findViewById(R.id.icShare);
        }
    }
}
