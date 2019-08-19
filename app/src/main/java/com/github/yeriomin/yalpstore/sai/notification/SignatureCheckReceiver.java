package com.github.yeriomin.yalpstore.sai.notification;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.github.yeriomin.yalpstore.sai.PackageSpecificReceiver;
import com.github.yeriomin.yalpstore.sai.download.DownloadManager;
import com.github.yeriomin.yalpstore.sai.install.InstallerAbstract;
import com.github.yeriomin.yalpstore.sai.install.InstallerFactory;
import com.github.yeriomin.yalpstore.sai.task.InstallTask;

public class SignatureCheckReceiver extends PackageSpecificReceiver {

    static public final String ACTION_CHECK_APK = "ACTION_CHECK_APK";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        Log.i(getClass().getSimpleName(), "Launching installer for " + packageName);
        InstallerAbstract installerDefault = InstallerFactory.getDefaultInstaller(context);
        installerDefault.setBackground(false);
        new InstallTask(installerDefault, DownloadManager.getApp(packageName)).execute();
    }
}
