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

package fr.bde_eseo.eseoasis.news;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import fr.bde_eseo.eseoasis.Constants;
import fr.bde_eseo.eseoasis.R;

/**
 * Created by François L. on 30/08/2015.
 */
public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        String imgUrl = "http://";

        // Get parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Toast.makeText(ImageViewActivity.this, R.string.toast_error, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                imgUrl = extras.getString(Constants.KEY_IMG);
            }
        }

        TouchImageView touchImageView = (TouchImageView) findViewById(R.id.touchImg);
        Picasso.with(this).load(imgUrl).placeholder(R.drawable.placeholder).error(R.drawable.placeholder_error).into(touchImageView);

    }

}
