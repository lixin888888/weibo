/*
 * Copyright (C) 2012 http://lytsing.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lytsing.android.weibo.ui;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.format.Formatter;

import com.actionbarsherlock.app.SherlockActivity;
import com.androidquery.util.AQUtility;
import com.orhanobut.logger.Logger;

import org.lytsing.android.weibo.R;
import org.lytsing.android.weibo.util.AlertUtil;

import java.io.File;

/**
 * Settings Activity.
 * @author Liqing Huang
 */
public class SettingsActivity extends SherlockActivity {

    private static SettingsActivity sSettingsActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();

        sSettingsActivity = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);
        }

        @Override
        public void onResume() {
            super.onResume();

            PreferenceScreen preferenceScreen = getPreferenceScreen();
            configureAboutSection(preferenceScreen);
            new ComputingCacheTask().execute();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                final Preference preference) {

            if ("os-licenses".equals(preference.getKey())) {
                startActivity(WebViewDialog.getIntent(sSettingsActivity,
                        R.string.os_licenses_label,
                        "file:///android_asset/licenses.html"));
            } else if ("clear-cache".equals(preference.getKey())) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        imageClearDisk();
                        preference.setSummary("Cache size: 0.00B");
                    }
                };

                AlertUtil.showAlert(sSettingsActivity, R.string.attention,
                        R.string.clear_cache_summary,
                        getString(R.string.ok), listener,
                        getString(R.string.cancel), null);
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void imageClearDisk() {
            AQUtility.cleanCacheAsync(sSettingsActivity, 0, 0);
        }

        private void configureAboutSection(PreferenceScreen preferenceScreen) {
            Preference buildVersion = preferenceScreen.findPreference("build-version");

            String versionName = "";
            PackageManager pm = sSettingsActivity.getPackageManager();

            try {
                PackageInfo pi = pm.getPackageInfo(sSettingsActivity.getPackageName(), 0);
                versionName = pi.versionName;
            } catch (NameNotFoundException e) {
                Logger.e("Get Version Code error!", e);
            }

            buildVersion.setSummary(versionName);
        }

        private class ComputingCacheTask extends AsyncTask<Void, Void, Long> {

            @Override
            protected Long doInBackground(Void... params) {
                File cacheDir = AQUtility.getCacheDir(sSettingsActivity);

                long size = 0;

                File[] files = cacheDir.listFiles();
                for (File f : files) {
                    size = size + f.length();
                }
                return size;
            }

            protected void onPostExecute(Long result) {
                Preference clearCache = getPreferenceScreen().findPreference("clear-cache");
                String cacheSize = Formatter.formatFileSize(sSettingsActivity, result);
                clearCache.setSummary("Cache size: " + cacheSize);
            }

        }
    }
}

