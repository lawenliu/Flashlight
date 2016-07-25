package com.care.flashlight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.care.core.Constants;
import com.care.core.Utilities;
import com.flurry.android.FlurryAgent;
import com.wandoujia.ads.sdk.Ads;

import java.util.List;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements View.OnClickListener {

    public final static int OPEN_LIGHT = 1011;
    public final static int CLOSE_LIGHT = 1012;

    public ImageView mSwitchImageView = null;
    public ImageView mMenuImageView = null;
    public RelativeLayout mBgImageView = null;

    private Camera mCamera = null;
    private PowerManager.WakeLock mWakeLock;
    private MediaPlayer mMediaPlayer = null;
    private PopupWindow mPopupMenu = null;
    private TextView mColorScreenTextView = null;
    private TextView mToolbarTextView = null;
    private TextView mRateUsTextView = null;
    private RelativeLayout mAdBannerLayout = null;
    private boolean mIsFlashLightOpened;
    private FlightThread mFlashThread = null;
    private boolean mIsFlashRunning = false;

    private Context mAppContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mAppContext = getApplicationContext();

        mBgImageView = (RelativeLayout) findViewById(R.id.bg_image);
        mMenuImageView = (ImageView) findViewById(R.id.btn_menu);
        mSwitchImageView = (ImageView) findViewById(R.id.btn_image);
        mSwitchImageView.setOnClickListener(this);
        mAdBannerLayout = (RelativeLayout)this.findViewById(R.id.id_ad_banner);

        mMediaPlayer = MediaPlayer.create(this, R.raw.sound);
        mMediaPlayer.setLooping(false);

        initialize();
        initPopupMenu();
        initListener();
        InitializeAds();
    }

    public void initialize() {
        mHandler.sendEmptyMessage(OPEN_LIGHT);
    }

    private void InitializeAds() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    Ads.init(MainActivity.this, Constants.WANDOUJIA_APP_ID, Constants.WANDOUJIA_SECRET_KEY);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Ads.preLoad(Constants.WANDOUJIA_BANNER_ID, Ads.AdFormat.banner);
                    View bannerView = Ads.createBannerView(MainActivity.this, Constants.WANDOUJIA_BANNER_ID);
                    mAdBannerLayout.addView(bannerView, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));

                }
            }
        }.execute();
    }

    private void initPopupMenu() {
        View menuContent = View.inflate(this, R.layout.menu_popup, null);
        mColorScreenTextView = (TextView)menuContent.findViewById(R.id.menu_color_screen);
        mToolbarTextView = (TextView)menuContent.findViewById(R.id.menu_tool_bar);
        mRateUsTextView = (TextView)menuContent.findViewById(R.id.menu_rate_us);

        mPopupMenu = new PopupWindow(menuContent, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.setBackgroundDrawable(new ColorDrawable());
        mPopupMenu.update();
    }

    private void initListener() {
        mMenuImageView.setOnClickListener(this);
        mColorScreenTextView.setOnClickListener(this);
        mToolbarTextView.setOnClickListener(this);
        mRateUsTextView.setOnClickListener(this);
    }

    private void showAndDismissPopupMenu() {
        if(mPopupMenu.isShowing()) {
            mPopupMenu.dismiss();
        } else {
            mPopupMenu.showAsDropDown(mMenuImageView);
        }
    }

    @Override
    protected void onDestroy() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, getString(R.string.app_name));
        mWakeLock.acquire();
    }

    @Override
    protected void onPause() {
        if (mWakeLock != null) {
            mWakeLock.release();
        }

        super.onPause();
    }

    public void onStart() {
        super.onStart();

        FlurryAgent.onStartSession(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        FlurryAgent.onEndSession(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_image:
                if (mIsFlashLightOpened) {
                    mHandler.sendEmptyMessage(CLOSE_LIGHT);
                    mMediaPlayer.start();
                } else {
                    mHandler.sendEmptyMessage(OPEN_LIGHT);
                    mMediaPlayer.start();
                }
                break;
            case R.id.btn_menu:
                showAndDismissPopupMenu();
                break;
            case R.id.menu_color_screen:
                mHandler.sendEmptyMessage(CLOSE_LIGHT);
                showAndDismissPopupMenu();
                Intent intent = new Intent(this, ColorActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_tool_bar:
                showAndDismissPopupMenu();
                if(mFlashThread == null) {
                    mToolbarTextView.setText(R.string.action_stop_sos);
                    mFlashThread = new FlightThread();
                    mIsFlashRunning = true;
                    mFlashThread.start();
                } else {
                    mToolbarTextView.setText(R.string.action_start_sos);
                    mIsFlashRunning = false;
                    mFlashThread.interrupt();
                    mFlashThread = null;
                }
                break;
            case R.id.menu_rate_us:
                showAndDismissPopupMenu();
                Utilities.launchAppStoreDetail(this);
                break;
            default:
                break;
        }
    }

    public boolean openCamera() {
        try {
            mCamera = Camera.open();

            return true;
        } catch(Exception ex) {
            Toast.makeText(mAppContext, getString(R.string.open_failed),
                    Toast.LENGTH_SHORT).show();

            return false;
        }
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case OPEN_LIGHT:
                    try {
                        if(mCamera == null && !openCamera()) {
                            return;
                        }

                        Camera.Parameters openParams = mCamera.getParameters();
                        List<String> list = openParams.getSupportedFlashModes();
                        if (list.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                            openParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        } else {
                            Toast.makeText(mAppContext, getString(R.string.not_support),
                                    Toast.LENGTH_SHORT).show();
                        }

                        mCamera.setParameters(openParams);
                        mCamera.startPreview();

                        mBgImageView.setBackgroundResource(R.drawable.bg_flashlight_on);
                        mSwitchImageView.setImageResource(R.drawable.switch_on);
                        mIsFlashLightOpened = true;
                    } catch(Exception ex){
                        Toast.makeText(mAppContext, getString(R.string.open_failed),
                                Toast.LENGTH_SHORT).show();
                    }

                    break;
                case CLOSE_LIGHT:
                    try {
                        if(mCamera == null && !openCamera()) {
                            return;
                        }

                        Camera.Parameters closeParams = mCamera.getParameters();
                        closeParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

                        mCamera.setParameters(closeParams);
                        mCamera.stopPreview();

                        mBgImageView.setBackgroundResource(R.drawable.bg_flashlight_off);
                        mSwitchImageView.setImageResource(R.drawable.switch_off);
                        mIsFlashLightOpened = false;
                    } catch (Exception ex) {
                        Toast.makeText(mAppContext, getString(R.string.open_failed),
                                Toast.LENGTH_SHORT).show();
                    }

                    break;
                default:
                    break;
            }
        }
    };

    class FlightThread extends Thread {
        @Override
        public void run() {
            boolean bOpenFlag = false;

            while(mIsFlashRunning) {
                if(bOpenFlag) {
                    mHandler.sendEmptyMessage(CLOSE_LIGHT);
                    bOpenFlag = false;
                } else {
                    mHandler.sendEmptyMessage(OPEN_LIGHT);
                    bOpenFlag = true;
                }

                try {
                    Thread.sleep(800);
                } catch (InterruptedException ex)
                {}
            }
        }
    }
}
