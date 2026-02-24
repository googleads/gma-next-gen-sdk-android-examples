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

package com.example.nextgenexample

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.nextgenexample.databinding.ActivityMainBinding
import com.google.android.libraries.ads.mobile.sdk.MobileAds

/** Main Activity. Inflates main activity xml and child fragments. */
class MainActivity : AppCompatActivity() {

  private lateinit var appBarConfiguration: AppBarConfiguration
  private lateinit var binding: ActivityMainBinding
  private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbar)

    val navController = findNavController(R.id.nav_host_fragment_content_main)
    appBarConfiguration = AppBarConfiguration(navController.graph)
    setupActionBarWithNavController(navController, appBarConfiguration)

    googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(applicationContext)
    // [START add_privacy_options]
    if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
      // Regenerate the options menu to include a privacy setting.
      invalidateOptionsMenu()
    }
    // [END add_privacy_options]
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_action, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId != R.id.action_more) return super.onOptionsItemSelected(item)

    val activity = this
    findViewById<View>(item.itemId)?.let { anchor ->
      PopupMenu(activity, anchor).apply {
        menuInflater.inflate(R.menu.menu_main, menu)

        menu.findItem(R.id.privacy_settings).isVisible =
          googleMobileAdsConsentManager.isPrivacyOptionsRequired

        setOnMenuItemClickListener { menuItem ->
          when (menuItem.itemId) {
            R.id.privacy_settings -> {
              // Handle changes to user consent.
              googleMobileAdsConsentManager.showPrivacyOptionsForm(activity) { error ->
                error?.let { Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show() }
              }
              true
            }
            R.id.ad_inspector -> {
              MobileAds.openAdInspector { error ->
                error?.let {
                  // Error will be non-null if ad inspector closed due to an error.
                  runOnUiThread { Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show() }
                }
              }
              true
            }
            else -> false
          }
        }
        show()
      }
    }

    return true
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment_content_main)
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
  }
}
