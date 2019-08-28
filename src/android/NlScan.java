package com.nlscan.cordova.plugin.newlandscanner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *@date 20190822
 *@author Alan
 *@company nlscan
 *@describe Cordova plugin to interface with NLSCAN barcode scanners.
 **/
public class NlScan extends CordovaPlugin {

    // Debugging
    private static final String TAG = "NlScan";
    private static final boolean D = false;

    private BCRBroadcastReceiver mReceiver;
    private static final String SCANNER_RESULT = "nlscan.action.SCANNER_RESULT";
    private static final String SCANNER_TRIG = "nlscan.action.SCANNER_TRIG";
    private static final String ACTION_BAR_SCANCFG = "ACTION_BAR_SCANCFG";
    private static final String Tag = "NlScanTag";
    private static boolean registeredTag = false;
    private Activity activity;

    // BCR states
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;         // we're doing nothing
    public static final int STATE_READING = 1;      // reading BCR reader
    public static final int STATE_READ = 2;         // read received BCR reader
    public static final int STATE_ERROR = 3;        // Error
    public static final int STATE_DESTROYED = 4;    // BCR reader destroyed
    public static final int STATE_READY = 5;        // BCR reader ready

    private int mState;

    // BCR actions
    private static final String ACTION_INIT = "init";
    private static final String ACTION_TRIGGER = "scan";
    private static final String ACTION_DESTROY = "destroy";
    private static final String ACTION_READ = "read";
    private static final String ACTION_GETSTATE = "getState";
    private static final String ACTION_SCANSETTING = "scanSetting";

    // BCR result fields
    private String szComResult;

    /**
     * Create a BCR reader
     */
    public NlScan() {
        this.setState(STATE_NONE);
    }

    /**
      *@date
      *@author Alan, fappels
      *@company nlscan, Z-Application
      *@describe plugin init method
     **/
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.activity = cordova.getActivity();
        // init BCR receiver
        mReceiver = new BCRBroadcastReceiver();
        IntentFilter intFilter = new IntentFilter(SCANNER_RESULT);
        this.activity.registerReceiver(mReceiver, intFilter);
        registeredTag = true;
    }

    /**
     *@date 20190822
     *@author Alan
     *@company nlscan
     *@describe set scanner as power on,normal trig,sound on
     **/
    private void initSetting(){
        Intent intent = new Intent ("ACTION_BAR_SCANCFG");
        intent.putExtra("EXTRA_SCAN_MODE", 3);
        intent.putExtra("EXTRA_SCAN_POWER", 1);
        intent.putExtra("EXTRA_TRIG_MODE", 0);
        intent.putExtra("EXTRA_SCAN_NOTY_SND", 1);
        this.activity.sendBroadcast(intent);
        this.setState(STATE_READY);
    }

    /**
      *@date 20190822
      *@author Alan, fappels
      *@company nlscan, Z-Application
      *@describe plugin execute method
     **/
    @Override
    public boolean execute(String action, JSONArray para, CallbackContext callbackContext) throws JSONException {

        if (D) 
            Log.d(TAG, "Action: " + action);

        if (ACTION_INIT.equals(action)) {
            initSetting();
            callbackContext.success();
        } else if (ACTION_DESTROY.equals(action)) {
            if ((mReceiver != null)) {
                this.activity.unregisterReceiver(mReceiver);
                registeredTag = false;
            }
            callbackContext.success();
            this.onDestroy();
        } else if (ACTION_TRIGGER.equals(action)) {
            Intent intent = new Intent(SCANNER_TRIG);
            this.activity.sendBroadcast(intent);
            callbackContext.success();
        } else if (ACTION_SCANSETTING.equals(action)){
            if (D) 
                Log.d(Tag,"Parameter:"+para.getString(0)+" Value:"+para.getString(1));

            Intent intent = new Intent (ACTION_BAR_SCANCFG);
            intent.putExtra(para.getString(0), Integer.parseInt(para.getString(1)));
            this.activity.sendBroadcast(intent);
            callbackContext.success();
        } else if(ACTION_GETSTATE.equals(action)) {
            JSONObject stateJSON = new JSONObject();
            try {
                stateJSON.put("state", mState);
                callbackContext.success(stateJSON);
            } catch (JSONException e) {
                Log.e(TAG,  e.getMessage() );
                this.setState(STATE_ERROR);
                callbackContext.error(e.getMessage());
            }
        } else if (ACTION_READ.equals(action) && (mState != STATE_READING)) {
            this.setState(STATE_READING);

            if (D)
                Log.d(TAG, "Reading...");

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    while (true) {
                        if (mState == STATE_READ) {
                            try {
                                PluginResult result = new PluginResult(PluginResult.Status.OK, szComResult);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                                mState = STATE_READING;
                                if (D)
                                    Log.d(TAG, "Read result = " + szComResult);
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                mState = STATE_ERROR;
                                callbackContext.error(e.getMessage());
                                break;
                            }
                        } else if ((mState == STATE_DESTROYED) || (mState == STATE_ERROR)) {
                            callbackContext.error("Not Read");
                            break;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            mState = STATE_ERROR;
                            callbackContext.error(e.getMessage());
                            break;
                        }
                    }
                }
            });
        } else {
            callbackContext.error("Action '" + action + "' not supported (now) state = " + mState);
        }
        return true;
    }


    /**
     *@date 20190822
     *@author Alan, fappels
     *@company nlscan, Z-application
     *@describe regist the scan receiver.receive the scanning result and return it to js source.
     **/
    private class BCRBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SCANNER_RESULT.equals(action)) {
                final String scanResult_1 = intent.getStringExtra("SCAN_BARCODE1");
                final String scanStatus = intent.getStringExtra("SCAN_STATE");
                Log.d(Tag, "scanResult_1：" + scanResult_1);
                if ("ok".equals(scanStatus)) {
                    if (!TextUtils.isEmpty(scanResult_1)) {
                        szComResult = scanResult_1;
                        mState = STATE_READ;
                    }
                }
            }
        }
    }

    /**
     *@date 20190822
     *@author Alan, fappels
     *@company nlscan, Z-Application
     *@describe unregist scan receiver when destroy
     **/
    @Override
    public void onDestroy() {
        this.setState(STATE_DESTROYED);
        if ((mReceiver != null) && registeredTag) {
            this.activity.unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }

    /**
     * set current scanning state
     *
     * @param state
     */
    private void setState(int state) {
        this.mState = state;
    }
}
