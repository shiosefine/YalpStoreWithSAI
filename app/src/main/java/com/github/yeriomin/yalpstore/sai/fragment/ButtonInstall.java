/*
 * Yalp Store
 * Copyright (C) 2018 Sergey Yeriomin <yeriomin@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.yeriomin.yalpstore.sai.fragment;

import android.view.View;

import com.github.yeriomin.yalpstore.sai.DetailsActivity;
import com.github.yeriomin.yalpstore.sai.Paths;
import com.github.yeriomin.yalpstore.sai.R;
import com.github.yeriomin.yalpstore.sai.download.DownloadManager;
import com.github.yeriomin.yalpstore.sai.install.InstallationState;
import com.github.yeriomin.yalpstore.sai.model.App;
import com.github.yeriomin.yalpstore.sai.task.InstallTask;

public class ButtonInstall extends Button {

    public ButtonInstall(DetailsActivity activity, App app) {
        super(activity, app);
    }

    @Override
    public void draw() {
        super.draw();
        if (button instanceof android.widget.Button) {
            ((android.widget.Button) button).setText(R.string.details_install);
        }
        if (InstallationState.isInstalling(app.getPackageName())) {
            disable(R.string.details_installing);
        }
    }

    @Override
    protected View getButton() {
        return activity.findViewById(R.id.install);
    }

    @Override
    protected boolean shouldBeVisible() {
        return DownloadManager.isSuccessful(app.getPackageName())
            && Paths.getApkPath(activity, app.getPackageName(), app.getVersionCode()).exists()
        ;
    }

    @Override
    protected void onButtonClick(View v) {
        disable(R.string.details_installing);
        new InstallTask(activity, app).execute();
    }
}
