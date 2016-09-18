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

package fr.bde_eseo.eseoasis.rooms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import fr.bde_eseo.eseoasis.R;

/**
 * Created by François L. on 23/03/16.
 */
public class BigPictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_image);

        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.bigImg);
        imageView.setImage(ImageSource.asset("plan.jpg"));
    }
}
