package com.github.yeriomin.yalpstore.sai.notification;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.github.yeriomin.yalpstore.sai.PackageSpecificReceiver;
import com.github.yeriomin.yalpstore.sai.download.DownloadManager;

public class CancelDownloadReceiver extends PackageSpecificReceiver {

    public static final String ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        new DownloadManager(context).cancel(packageName);
    }
}
