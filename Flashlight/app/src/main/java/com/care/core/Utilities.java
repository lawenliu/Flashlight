package com.care.core;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;



import java.util.Locale;

import com.care.flashlight.R;

/**
 * Created by laliu on 2015/8/11.
 */
public class Utilities {

    public static void launchAppStoreDetail(Context context) {
        if (context != null) {
            try {
                String link = String.format(Locale.getDefault(), Constants.LinkMarketPackageFormat,
                        context.getPackageName());
                Intent browserIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(context, context.getString(R.string.message_no_store), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
