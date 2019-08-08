package com.alinz.parkerdan.shareextension;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

import android.graphics.Bitmap;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.InputStream;


public class ShareModule extends ReactContextBaseJavaModule {


    public ShareModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ReactNativeShareExtension";
    }

    @ReactMethod
    public void close() {
        getCurrentActivity().finish();
    }

    @ReactMethod
    public void data(Promise promise) {
        promise.resolve(processIntent());
    }

    public WritableMap processIntent() {
        WritableMap map = Arguments.createMap();

        String value = "";
        String type = "";
        String action = "";

        Activity currentActivity = getCurrentActivity();

        if (currentActivity != null) {
            String filepath = Environment.getExternalStorageDirectory().getPath();
            System.out.println(filepath);
            Intent intent = currentActivity.getIntent();
            action = intent.getAction();
            type = intent.getType();
            if (type == null) {
                type = "";
            }
            if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
                value = intent.getStringExtra(Intent.EXTRA_TEXT);
            } else if (Intent.ACTION_SEND.equals(action) && ("image/*".equals(type) || "image/jpeg".equals(type) || "image/png".equals(type) || "image/jpg".equals(type))) {
                Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if ("image/*".equals(type)) {
                    String specifiedType = getMimeType(uri);
                    if (specifiedType != null && !specifiedType.isEmpty()) {
                        type = specifiedType;
                    }
                }
                value = "file://" + RealPathUtil.getRealPathFromURI(currentActivity, uri);

            } else if (Intent.ACTION_SEND.equals(action)) {
                Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (type == "") {
                    String specifiedType = getMimeType(uri);
                    if(specifiedType!=null && !specifiedType.isEmpty()){
                        type=specifiedType;
                    }
                }
                value = "file://" + RealPathUtil.getRealPathFromURI(currentActivity, uri);
            }
        } else {
            value = "";
            type = "";
        }

        map.putString("type", type);
        map.putString("value", value);

        return map;
    }

    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getReactApplicationContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }
}
