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

package fr.bde_eseo.eseomega.news;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.utils.Blur;
import fr.bde_eseo.eseomega.utils.ImageUtils;
import fr.bde_eseo.eseomega.utils.Utils;

/**
 * Created by François L. on 30/08/2015.
 */
public class ViewNewsActivityMaterial extends AppCompatActivity {

    private Toolbar toolbar;
    private NewsItem newsItem;
    //private MyImageAdapter mAdapter;
    //private RecyclerView recList;
    private ImageView imageView;
    private TextView tvName, tvAuthor;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setTheme(Utils.getPreferredTheme(getApplicationContext()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_material);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setPadding(0, Utils.getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00263238")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#550000ff")));

        // Get parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Toast.makeText(ViewNewsActivityMaterial.this, R.string.toast_error, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                newsItem = new NewsItem(
                        extras.getString(Constants.KEY_NEWS_TITLE),
                        extras.getString(Constants.KEY_NEWS_HTML),
                        extras.getString(Constants.KEY_NEWS_DATE),
                        extras.getStringArrayList(Constants.KEY_NEWS_IMGARRAY));

                getSupportActionBar().setTitle(R.string.news_reading);
            }
        }

        if (newsItem != null) {
            imageView = (ImageView) findViewById(R.id.imgHeaderNews);
            // Load image, decode it to Bitmap and return Bitmap to callback
            ImageUtils.loadImage(this, newsItem.getImgLinks().get(0), R.drawable.placeholder, R.drawable.placeholder_error, new Target() {
                @Override
                public void onBitmapLoaded(Bitmap loadedImage, Picasso.LoadedFrom from) {
                    imageView.setImageBitmap(Blur.fastblur(ViewNewsActivityMaterial.this, loadedImage, 12)); // seems ok
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
            tvName = (TextView) findViewById(R.id.tvTitle);
            tvName.setText(newsItem.getName());
            tvAuthor = (TextView) findViewById(R.id.tvAuthor);
            tvAuthor.setText(newsItem.getFrenchStr());
            /*
            tvHtml = (TextView) findViewById(R.id.tvHtml);
            tvHtml.setText(Html.fromHtml(newsItem.getData()));
            tvHtml.setMovementMethod(LinkMovementMethod.getInstance());*/
            webView = (WebView) findViewById(R.id.webview);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setHapticFeedbackEnabled(false);
            webView.getSettings().setDefaultFontSize(15);
            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
            webView.setLongClickable(false);
            //webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadData("" +
                    "<body style=\"margin:20px 10px 20px 10px\"><style>img {max-width:98%;}</style>" + newsItem.getData() + "</body>", "text/html; charset=UTF-8", null);
            //mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_share:
                //TODO link to share
                /*
                Intent intent2 = new Intent(); intent2.setAction(Intent.ACTION_SEND);
                intent2.setType("text/plain");
                intent2.putExtra(Intent.EXTRA_TEXT, "\"" + newsItem.getName() + "\"\n" + newsItem.getLink());
                startActivity(Intent.createChooser(intent2, "Partager ..."));*/
                //NavUtils.navigateUpFromSameTask(this);
                return true;

            case android.R.id.home:
                this.onBackPressed();
                return true;

            case R.id.action_browser:
                /*
                String adr = newsItem.getLink();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(adr));

                if (i.resolveActivity(getPackageManager()) != null)
                    startActivity(i);
                else
                    Toast.makeText(ViewNewsActivityMaterial.this, "Pas d'application installée pour ça", Toast.LENGTH_SHORT).show();
                return true;
                */
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
