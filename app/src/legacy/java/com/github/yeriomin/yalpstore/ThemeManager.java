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

package com.github.yeriomin.yalpstore.sai;

import android.os.Build;

public class ThemeManager extends ThemeManagerAbstract {

    protected int getThemeLight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return android.R.style.Theme_Material_Light;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return android.R.style.Theme_Holo_Light;
        } else {
            return android.R.style.Theme_Light;
        }
    }

    protected int getThemeDark() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return android.R.style.Theme_Material;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return android.R.style.Theme_Holo;
        } else {
            return android.R.style.Theme;
        }
    }

    @Override
    protected int getThemeBlack() {
        return getThemeDark();
    }

    @Override
    protected int getDialogThemeLight() {
        return 0;
    }

    @Override
    protected int getDialogThemeDark() {
        return 0;
    }
}
