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

package fr.bde_eseo.eseomega.slidingmenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.utils.Utils;

public class NavDrawerListAdapter extends BaseAdapter {

    // Custom definitions
    public static final int TYPE_DRAWER_ITEM = 0;
    public static final int TYPE_DRAWER_PROFILE = 1;
    public static final int TYPE_DRAWER_DIVIDER = 2;
    public static final int TYPE_DRAWER_OPTION = 3;
    public static final int TYPE_MAX_COUNT = 4; // profile - itemlist - divider - option

    private Context context;
	private ArrayList<NavDrawerItem> navDrawerItems;
    private Bitmap bmp; // faster

    // Pour garder en mémoire la position du profile
    //private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();

    // Custom constructor
    /*public NavDrawerListAdapter(Context context, Bitmap bmp){
        this.context = context;
        this.navDrawerItems = new ArrayList<>();
        this.bmp = bmp;
    }*/

    // Custom adders
    /*
    public void addProfileItem(String title, String id) {
        navDrawerItems.add(new NavDrawerItem(title, id));

        // save separator position
        mSeparatorsSet.add(navDrawerItems.size() - 1);
        notifyDataSetChanged();
    }*/

    /**
     * Default constructor for adapter
     * @param context The app context
     * @param navDrawerItems The items to put in listView
     */
    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    // faster than in listadapter operation process
    public void setBitmap(Bitmap bmp) {
        this.bmp = bmp;
    }

    @Override
	public int getCount() {
		return navDrawerItems.size();
	}

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return navDrawerItems.get(position).isProfile() ?
                TYPE_DRAWER_PROFILE : (navDrawerItems.get(position).isDivider() ?
                TYPE_DRAWER_DIVIDER : (navDrawerItems.get(position).isOption() ?
                TYPE_DRAWER_OPTION : TYPE_DRAWER_ITEM));
    }

	@Override
	public NavDrawerItem getItem(int position) {
		return navDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        NavDrawerItem ndi = navDrawerItems.get(position);

		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            if (ndi.isProfile()) {
                convertView = mInflater.inflate(R.layout.drawer_profile, null);
                ImageView iv = (ImageView) convertView.findViewById(R.id.menu_material);
                context.setTheme(Utils.getPreferredTheme(context));
                iv.setColorFilter(Utils.resolveColorFromTheme(context, R.attr.colorPrimary), PorterDuff.Mode.MULTIPLY);
            }
            else if (ndi.isDivider())
                convertView = mInflater.inflate(R.layout.drawer_list_divider, null);
            else if (ndi.isOption())
                convertView = mInflater.inflate(R.layout.drawer_list_option, null);
            else
                convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        if (ndi.isProfile()) {
            TextView txtName = (TextView) convertView.findViewById(R.id.tvProfileName);
            TextView txtEmail = (TextView) convertView.findViewById(R.id.tvProfileEmail);
            ImageView imgProfile = (ImageView) convertView.findViewById(R.id.circleView);
            //TextView txtEdit = (TextView) convertView.findViewById(R.id.profile_view);

            if (ndi.getTitle().length() < 1) { // profile not connected
                txtName.setText(R.string.user_not_connected);
                txtEmail.setText(R.string.user_not_connected_text);
                imgProfile.setVisibility(View.GONE);
            } else {
                txtName.setText(ndi.getTitle());
                txtEmail.setText(ndi.getId());
                imgProfile.setVisibility(View.VISIBLE);
                if (bmp != null) {
                    //imgProfile.setImageBitmap(ImageUtils.getResizedBitmap(BitmapFactory.decodeFile(ndi.getMoreData()), MAX_PROFILE_SIZE));
                    imgProfile.setImageBitmap(bmp);
                } else {
                    imgProfile.setImageResource(R.drawable.ic_unknown);
                }
            }
        } else if (ndi.isDivider()) {
            // Nothing to do, it's just a divider
        } else if (ndi.isOption()) {
            TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
            txtTitle.setText(ndi.getTitle());
        } else {
            ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
            TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
            TextView txtCounter = (TextView) convertView.findViewById(R.id.tv_counter);

            imgIcon.setImageResource(ndi.getIcon());
            txtTitle.setText(ndi.getTitle());
            txtCounter.setText(ndi.getCount());
            txtCounter.setVisibility(ndi.getCounterVisibility()?View.VISIBLE:View.INVISIBLE);
        }

        return convertView;
	}

}
