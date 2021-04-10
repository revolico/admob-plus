package admob.plugin;

import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import admob.plugin.Generated.Actions;
import admob.plugin.ads.AdBase;
import admob.plugin.ads.Banner;
import admob.plugin.ads.IAdIsLoaded;
import admob.plugin.ads.IAdShow;
import admob.plugin.ads.Interstitial;
import admob.plugin.ads.Rewarded;
import admob.plugin.ads.RewardedInterstitial;

import static admob.plugin.ExecuteContext.ads;

public class AdMob extends CordovaPlugin {
    private static final String TAG = "AdMobPlus";
    private final ArrayList<PluginResult> eventQueue = new ArrayList<PluginResult>();
    private CallbackContext readyCallbackContext = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.i(TAG, "Initialize plugin");

        ExecuteContext.plugin = this;
    }

    @Override
    public boolean execute(String actionKey, JSONArray args, CallbackContext callbackContext) {
        Log.d(TAG, String.format("Execute %s", actionKey));
        ExecuteContext ctx = new ExecuteContext(actionKey, args, callbackContext);

        switch (actionKey) {
            case Actions.READY:
                return executeReady(callbackContext);
            case Actions.START:
                MobileAds.initialize(cordova.getActivity(), status -> callbackContext.success(new JSONObject(new HashMap<String, Object>() {{
                    put("version", MobileAds.getVersionString());
                }})));
                break;
            case Actions.CONFIG_REQUEST:
                MobileAds.setRequestConfiguration(ctx.optRequestConfiguration());
                callbackContext.success();
                break;
            case Actions.BANNER_LOAD:
                return executeBannerLoad(ctx);
            case Actions.BANNER_SHOW:
                return executeBannerShow(ctx);
            case Actions.BANNER_HIDE:
                return executeBannerHide(ctx);
            case Actions.INTERSTITIAL_LOAD:
                return executeInterstitialLoad(ctx);
            case Actions.REWARDED_LOAD:
                return executeRewardedLoad(ctx);
            case Actions.REWARDED_INTERSTITIAL_LOAD:
                return executeRewardedInterstitialLoad(ctx);
            case Actions.SET_APP_MUTED: {
                boolean value = args.optBoolean(0);
                MobileAds.setAppMuted(value);
                callbackContext.success();
                break;
            }
            case Actions.SET_APP_VOLUME: {
                float value = BigDecimal.valueOf(args.optDouble(0)).floatValue();
                MobileAds.setAppVolume(value);
                callbackContext.success();
                break;
            }
            case Actions.INTERSTITIAL_IS_LOADED:
            case Actions.REWARDED_IS_LOADED:
            case Actions.REWARDED_INTERSTITIAL_IS_LOADED:
                return executeAdIsLoaded(ctx);
            case Actions.INTERSTITIAL_SHOW:
            case Actions.REWARDED_SHOW:
            case Actions.REWARDED_INTERSTITIAL_SHOW:
                return executeAdShow(ctx);
            default:
                return false;
        }

        return true;
    }

    private boolean executeReady(CallbackContext callbackContext) {
        if (readyCallbackContext == null) {
            for (PluginResult result : eventQueue) {
                callbackContext.sendPluginResult(result);
            }
            eventQueue.clear();
        } else {
            Log.e(TAG, "Ready action should only be called once.");
        }
        readyCallbackContext = callbackContext;
        emit(Generated.Events.READY, new HashMap<String, Object>() {{
            put("isRunningInTestLab", isRunningInTestLab());
        }});
        return true;
    }

    private boolean executeBannerLoad(ExecuteContext ctx) {
        cordova.getActivity().runOnUiThread(() -> {
            Banner banner = ctx.optAdOrCreate(Banner.class);
            if (banner != null) {
                banner.load(ctx);
            }
        });
        return true;
    }

    private boolean executeBannerShow(ExecuteContext ctx) {
        cordova.getActivity().runOnUiThread(() -> {
            Banner banner = (Banner) ctx.optAdOrError();
            if (banner != null) {
                banner.show(ctx);
            }
        });
        return true;
    }

    private boolean executeBannerHide(ExecuteContext ctx) {
        cordova.getActivity().runOnUiThread(() -> {
            Banner banner = (Banner) ctx.optAdOrError();
            if (banner != null) {
                banner.hide(ctx);
            }
        });
        return true;
    }

    private boolean executeAdIsLoaded(ExecuteContext ctx) {
        cordova.getActivity().runOnUiThread(() -> {
            IAdIsLoaded ad = (IAdIsLoaded) ctx.optAdOrError();
            if (ad != null) {
                ctx.success(ad.isLoaded());
            }
        });
        return true;
    }

    private boolean executeInterstitialLoad(ExecuteContext ctx) {
        cordova.getActivity().runOnUiThread(() -> {
            Interstitial ad = ctx.optAdOrCreate(Interstitial.class);
            if (ad != null) {
                ad.load(ctx);
            }
        });
        return true;
    }

    private boolean executeAdShow(ExecuteContext ctx) {
        cordova.getActivity().runOnUiThread(() -> {
            IAdShow ad = (IAdShow) ctx.optAdOrError();
            if (ad != null) {
                ad.show(ctx);
            }
        });
        return true;
    }

    private boolean executeRewardedLoad(ExecuteContext ctx) {
        cordova.getActivity().runOnUiThread(() -> {
            Rewarded ad = ctx.optAdOrCreate(Rewarded.class);
            if (ad != null) {
                ad.load(ctx);
            }
        });
        return true;
    }

    private boolean executeRewardedInterstitialLoad(ExecuteContext ctx) {
        cordova.getActivity().runOnUiThread(() -> {
            RewardedInterstitial ad = ctx.optAdOrCreate(RewardedInterstitial.class);
            if (ad != null) {
                ad.load(ctx);
            }
        });
        return true;
    }

    @Override
    public void onPause(boolean multitasking) {
        for(int i = 0; i < ads.size(); i++) {
            AdBase ad = ads.valueAt(i);
            ad.onPause(multitasking);
        }
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        for(int i = 0; i < ads.size(); i++) {
            AdBase ad = ads.valueAt(i);
            ad.onResume(multitasking);
        }
    }

    @Override
    public void onDestroy() {
        readyCallbackContext = null;

        for(int i = 0; i < ads.size(); i++) {
            AdBase ad = ads.valueAt(i);
            ad.onDestroy();
        }

        super.onDestroy();
    }

    public void emit(String eventName, Map<String, Object> data) {
        JSONObject event = new JSONObject(new HashMap<String, Object>() {{
            put("type", eventName);
            put("data", data);
        }});

        PluginResult result = new PluginResult(PluginResult.Status.OK, event);
        result.setKeepCallback(true);
        if (readyCallbackContext == null) {
            eventQueue.add(result);
        } else {
            readyCallbackContext.sendPluginResult(result);
        }
    }

    private boolean isRunningInTestLab() {
        String testLabSetting = Settings.System.getString(cordova.getActivity().getContentResolver(),
                "firebase.test.lab");
        return "true".equals(testLabSetting);
    }
}
