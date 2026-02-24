/*
 * Copyright 2024 Google LLC
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

package com.example.nextgenexample;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.nextgenexample.databinding.ActivityMainBinding;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;

/** An activity showing a list of ad formats. */
public class MainActivity extends AppCompatActivity {

  private AppBarConfiguration appBarConfiguration;
  private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setSupportActionBar(binding.toolbar);

    NavController navController =
        Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    googleMobileAdsConsentManager =
        GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
    // [START add_privacy_options]
    if (googleMobileAdsConsentManager.isPrivacyOptionsRequired()) {
      // Regenerate the options menu to include a privacy setting.
      invalidateOptionsMenu();
    }
    // [END add_privacy_options]
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_action, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() != R.id.action_more) {
      return super.onOptionsItemSelected(item);
    }

    View anchor = findViewById(item.getItemId());
    if (anchor == null) {
      return true;
    }

    PopupMenu popup = new PopupMenu(this, anchor);
    popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());

    boolean isPrivacyRequired = googleMobileAdsConsentManager.isPrivacyOptionsRequired();
    popup.getMenu().findItem(R.id.privacy_settings).setVisible(isPrivacyRequired);

    popup.setOnMenuItemClickListener(
        menuItem -> {
          int id = menuItem.getItemId();

          if (id == R.id.privacy_settings) {
            // Handle changes to user consent.
            googleMobileAdsConsentManager.showPrivacyOptionsForm(
                this,
                formError -> {
                  if (formError != null) {
                    Toast.makeText(this, formError.getMessage(), Toast.LENGTH_SHORT).show();
                  }
                });
            return true;
          }

          if (id == R.id.ad_inspector) {
            MobileAds.openAdInspector(
                error -> {
                  if (error != null) {
                    // Error will be non-null if ad inspector closed due to an error.
                    runOnUiThread(
                        () -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show());
                  }
                });
            return true;
          }

          return false;
        });

    popup.show();
    return true;
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController =
        Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    return NavigationUI.navigateUp(navController, appBarConfiguration)
        || super.onSupportNavigateUp();
  }
}
