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

package fr.bde_eseo.eseoasis.lacommande;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import fr.bde_eseo.eseoasis.R;
import fr.bde_eseo.eseoasis.lacommande.tabs.TabCartView;
import fr.bde_eseo.eseoasis.slidingtab.SlidingTabLayout;
import fr.bde_eseo.eseoasis.slidingtab.ViewPagerAdapter;

/**
 * Created by François L. on 21/07/2015.
 */
public class OrderTabsFragment extends Fragment {

    public static final long MAX_DELAY_ORDER = 582*1000;
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private ViewPagerAdapter mAdapter;
    private CharSequence titles[];
    private int nbTabs = 2;
    private Handler mHandler;
    private int count = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);

        mPager = (ViewPager) rootView.findViewById(R.id.home_fragment_pager);
        mAdapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager(), titles, nbTabs);
        mPager.setAdapter(mAdapter);
        titles = new CharSequence[]{getString(R.string.cafet_menu2), getString(R.string.cafet_cart)+" (0)"};
        mTabs = (SlidingTabLayout) rootView.findViewById(R.id.home_fragment_tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setViewPager(mPager);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                Context ctx = getActivity();
                if (ctx != null) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.cafet_cart_expired)
                            .content(R.string.cafet_cart_expired_text)
                            .cancelable(false)
                            .negativeText(R.string.dialog_ok)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                    getFragmentManager().popBackStackImmediate();
                                }
                            })
                            .show();
                }

            }
        }, MAX_DELAY_ORDER);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // Used to refresh cart's item numbers
    public void refreshCart () {

        // Get number
        int countItem = DataManager.getInstance().getNbCartItems();

        // Update tabs
        mAdapter.setCartTitle(getString(R.string.cafet_cart)+" (" + countItem + ")");
        mAdapter.notifyDataSetChanged();
        mTabs.setViewPager(mPager); // RESOLVES EVERYTHING YEAH :D

        // Update cart fragment
        ((TabCartView)mAdapter.getItem(1)).refreshCart();
    }
}
