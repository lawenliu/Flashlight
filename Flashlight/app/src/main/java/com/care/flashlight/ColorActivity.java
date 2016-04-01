package com.care.flashlight;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.care.core.SharedDataManager;
import com.flurry.android.FlurryAgent;

public class ColorActivity extends Activity {
    private RelativeLayout mColorPageRelativeLayout;
    private ImageView mColorPickerImageView;
    private ImageView mCloseImageView;

    private ColorPickerDialog mColorPickerDialog;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_color);
        mContext = this;

        initialize();
    }

    public void initialize() {
        mColorPageRelativeLayout = (RelativeLayout)findViewById(R.id.view_color_page);
        mColorPageRelativeLayout.setBackgroundColor(SharedDataManager.getInstance().getColorScreenColor());
        mColorPickerImageView = (ImageView)findViewById(R.id.btn_color_picker);
        mColorPickerImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mColorPickerDialog = new ColorPickerDialog(mContext, SharedDataManager.getInstance().getColorScreenColor(),
                        getString(R.string.color_picker_title),
                        new ColorPickerDialog.OnColorChangedListener() {

                            @Override
                            public void colorChanged(int color) {
                                mColorPageRelativeLayout.setBackgroundColor(color);
                                SharedDataManager.getInstance().setColorScreenColor(color);
                            }
                        });

                mColorPickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mColorPickerDialog.show();
            }
        });

        mCloseImageView = (ImageView) findViewById(R.id.btn_close);
        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
