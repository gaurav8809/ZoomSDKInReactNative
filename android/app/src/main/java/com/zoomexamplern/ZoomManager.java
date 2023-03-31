package com.zoomexamplern;

import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingError;
import us.zoom.sdk.MeetingParameter;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;

public class ZoomManager extends ReactContextBaseJavaModule implements ZoomSDKInitializeListener, MeetingServiceListener {

    private final ReactApplicationContext reactContext;

    private Promise initPromise;

    private Promise meetingPromise;

    ZoomManager(ReactApplicationContext context){
        super(context);
        this.reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "ZoomManager";
    }

    @ReactMethod
    public void initZoom(String publicKey, String privateKey, String domain, Promise promise) {
        Log.d(this.getName(), "Init zoom: " + publicKey
                + " and privateKey: " + privateKey + " domain: " + domain);

        try {
            initPromise = promise;
            this.getReactApplicationContext().getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ZoomSDK zoomSDK = ZoomSDK.getInstance();
                    ZoomSDKInitParams initParams = new ZoomSDKInitParams();
                    initParams.appKey = publicKey;
                    initParams.appSecret = privateKey;
                    initParams.enableGenerateDump = true;
                    initParams.enableLog = true;
                    initParams.domain = domain;
                    zoomSDK.initialize(reactContext.getCurrentActivity(),ZoomManager.this, initParams);
                }
            });
        } catch (Exception e) {
            Log.e("ERR_UNEXPECTED_EXCEPTIO", e.getMessage());
            promise.reject("ERR_UNEXPECTED_EXCEPTIO", e);
        }

    }

    @ReactMethod
    public void joinMeeting(String displayName, String meetingNumber, String password, Promise promise) {
        Log.d("ou shit", "Join meeting called : displayName " + displayName
                + " and meetingNumber: " + meetingNumber + " password" + password);

        try {
            this.getReactApplicationContext().getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ZoomSDK zoomSDK = ZoomSDK.getInstance();

                    if (!zoomSDK.isInitialized()) {
                        promise.reject("ERR_ZOOM_START", "ZoomSDK has not been initialized successfully");
                        return;
                    }

                    MeetingService meetingService = zoomSDK.getMeetingService();
                    if (meetingService == null) {
                        promise.reject("ERR_ZOOM_START", "Zoom MeetingService has not been initialized successfully");
                        return;
                    }

                    JoinMeetingOptions opts = new JoinMeetingOptions();

                    JoinMeetingParams params = new JoinMeetingParams();

                    params.displayName = displayName;
                    params.meetingNo = meetingNumber;
                    params.password = password;
                    try {
                        int joinMeetingResult = meetingService.joinMeetingWithParams(ZoomManager.this.reactContext, params, opts);
                        Log.i(ZoomManager.this.getName(), "joinMeeting, joinMeetingResult=" + joinMeetingResult);
                        if (joinMeetingResult != MeetingError.MEETING_ERROR_SUCCESS) {
                            promise.reject("ERR_ZOOM_JOIN", "joinMeeting, errorCode=" + joinMeetingResult);
                        }
                        meetingPromise = promise;

                    } catch (Exception e) {
                        promise.reject("JoinMeetingException", e);
                    }
                }
            });
        } catch (Exception e) {
            promise.reject("JoinMeetingException", e);
        }
    }


    @Override
    public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
        Log.d(this.getName(), "Init Zoom Result with : errorCode " + errorCode
                + " and internalErrorCode: " + internalErrorCode);

        if(errorCode == ZoomError.ZOOM_ERROR_SUCCESS) {
            Log.d(this.getName(), "Initializing meeting service SUCCESSFUL");
            ZoomSDK zoomSDK = ZoomSDK.getInstance();
            MeetingService meetingService = zoomSDK.getMeetingService();
            if(meetingService != null) {
                Log.d(this.getName(), "Adding listener for meeting service ");
                meetingService.addListener(this);
            }
            //here we should notify JS
            initPromise.resolve("Zoom initialized");
        }
    }

    @Override
    public void onZoomAuthIdentityExpired() {

    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
        Log.d(this.getName(), "Meeting Status Changed  meetingStatus : " + meetingStatus
                + "errorCode: "  + errorCode + " and internalErrorCode: " + internalErrorCode);


        switch(meetingStatus) {
            case MEETING_STATUS_FAILED:

            case MEETING_STATUS_DISCONNECTING:
                this.reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("EventReminder", "DISCONNECTED");
                break;

            case MEETING_STATUS_INMEETING:
                this.reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("EventReminder", "CONNECTED");
                break;

            case MEETING_STATUS_IN_WAITING_ROOM: {
                break;
            }
        }
    }

    @Override
    public void onMeetingParameterNotification(MeetingParameter meetingParameter) {

    }
}
