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

package fr.bde_eseo.eseomega.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by François L. on 10/08/2015.
 */
public class ImageUtils {

    public static Bitmap getScaledBitmap(String picturePath, int width, int height) {
        BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
        sizeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, sizeOptions);

        int inSampleSize = calculateInSampleSize(sizeOptions, width, height);

        sizeOptions.inJustDecodeBounds = false;
        sizeOptions.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(picturePath, sizeOptions);
    }

    // Get a resized bitmap
    public static Bitmap getResizedBitmap(Bitmap bm, int newSize) {

        if (bm != null) {
            int width = bm.getWidth();
            int height = bm.getHeight();

            float scale;

            if (height > width) { // portrait bitmap
                scale = ((float) newSize) / height;
            } else { // landscape bitmap
                scale = ((float) newSize) / width;
            }

            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scale, scale);

            // "RECREATE" THE NEW BITMAP
            return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        } else
            return null; // prodive null data to the next step, don't stop here
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static void loadImage(Context ctx, String url, ImageView view) {
        if (url != null && !url.equals("")) {
            Picasso.with(ctx).load(url).into(view);
        }
    }

    public static void loadImage(Context ctx, String url, int placeholder, int error, ImageView view) {
        if (url != null && !url.equals("")) {
            Picasso.with(ctx).load(url).placeholder(placeholder).error(error).into(view);
        } else {
            Picasso.with(ctx).load(error).into(view);
        }
    }

    public static void loadImage(Context ctx, String url, Target target) {
        if (url != null && !url.equals("")) {
            Picasso.with(ctx).load(url).into(target);
        }
    }

    public static void loadImage(Context ctx, String url, int placeholder, int error, Target target) {
        if (url != null && !url.equals("")) {
            Picasso.with(ctx).load(url).placeholder(placeholder).error(error).into(target);
        } else {
            Picasso.with(ctx).load(error).into(target);
        }
    }

}
