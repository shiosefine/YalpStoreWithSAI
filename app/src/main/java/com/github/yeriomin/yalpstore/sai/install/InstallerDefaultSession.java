package com.github.yeriomin.yalpstore.sai.install;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.github.yeriomin.yalpstore.sai.BuildConfig;
import com.github.yeriomin.yalpstore.sai.PackageSpecificReceiver;
import com.github.yeriomin.yalpstore.sai.Paths;
import com.github.yeriomin.yalpstore.sai.YalpStoreApplication;
import com.github.yeriomin.yalpstore.sai.Util;
import com.github.yeriomin.yalpstore.sai.model.App;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class InstallerDefaultSession extends InstallerAbstract {
    private static final String BROADCAST_ACTION_INSTALL = BuildConfig.APPLICATION_ID + ".ACTION_INSTALL_COMMIT";
    private final InstallationResultReceiver broadcastReceiver;
    private final PackageInstaller mPackageInstaller;

    private int sessionId;
    private App app;

    public InstallerDefaultSession(Context context) {
        super(context);
        broadcastReceiver = new InstallationResultReceiver(this);
        mPackageInstaller = context.getPackageManager().getPackageInstaller();
    }

    @Override
    protected boolean verify(App app) {
        if (background) {
            Log.e(getClass().getSimpleName(), "Background installation is not supported by default installer");
            return false;
        }
        return super.verify(app);
    }

    protected void install(App app) {
        registerReceiver();
        this.app = app;
        PackageInstaller.Session session = null;
        try {
            PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            sessionParams.setInstallLocation(PackageInfo.INSTALL_LOCATION_AUTO);
            sessionId = mPackageInstaller.createSession(sessionParams);
            session = mPackageInstaller.openSession(sessionId);
            for (File file: Paths.getApkAndSplits(context, this.app.getPackageName(), this.app.getVersionCode())) {
                writeFileToSession(file, session);
            }
            session.commit(getIntentSender(sessionId));
        } catch (IOException e) {
            fail(e, this.app.getPackageName());
        } finally {
            Util.closeSilently(session);
        }
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION_INSTALL);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void writeFileToSession(File file, PackageInstaller.Session session) throws IOException {
        InputStream in = context.getContentResolver().openInputStream(FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                file
        ));
        OutputStream out = session.openWrite(file.getName(), 0, file.length());
        try {
            int c;
            byte[] buffer = new byte[65536];
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            session.fsync(out);
        } finally {
            Util.closeSilently(in);
            Util.closeSilently(out);
        }
    }

    private IntentSender getIntentSender(int sessionId) {
        return PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(BROADCAST_ACTION_INSTALL),
                0
        ).getIntentSender();
    }

    private void processResult(Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999);
        String packageName = app.getPackageName();
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                startInstallerActivity(confirmationIntent);
                break;
            case PackageInstaller.STATUS_SUCCESS:
                cleanSession();
                InstallationState.setSuccess(packageName);
                break;
            default:
                cleanSession();
                sendFailureBroadcast(packageName);
                InstallationState.setFailure(packageName);
                break;
        }
    }

    private void fail(Exception e, String packageName) {
        Log.e(getClass().getSimpleName(), "Could not start installation: " + e.getClass().getName() + " " + e.getMessage());
        ((YalpStoreApplication) context.getApplicationContext()).removePendingUpdate(packageName);
        InstallationState.setFailure(packageName);
        sendFailureBroadcast(packageName);
    }

    private void startInstallerActivity(Intent installerActivity) {
        installerActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(installerActivity);
        } catch (Exception e) {
            Log.i(getClass().getSimpleName(), "This ROM is incompatible with SplitApkInstaller. Change Installer to InstallerDefault");
            cleanSession();

            // Change Installer to InstallerDefault
            InstallerDefault installerDefault = new InstallerDefault(context);
            installerDefault.verifyAndInstall(app);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            cleanSession();
        } catch (Exception e) {
            // Not registered, apparently
        }
        super.finalize();
    }

    private void cleanSession() {
        context.unregisterReceiver(broadcastReceiver);
        try {
            mPackageInstaller.abandonSession(sessionId);
        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), "Unable to abandon session", e);
        }
    }

    private static class InstallationResultReceiver extends PackageSpecificReceiver {
        private final InstallerDefaultSession installerDefaultSession;

        private InstallationResultReceiver(InstallerDefaultSession installerDefaultSession) {
            this.installerDefaultSession = installerDefaultSession;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            installerDefaultSession.processResult(intent);
        }
    }
}
