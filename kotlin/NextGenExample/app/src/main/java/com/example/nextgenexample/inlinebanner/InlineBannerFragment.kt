package com.example.nextgenexample.inlinebanner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nextgenexample.AdFragment
import com.example.nextgenexample.Constant
import com.example.nextgenexample.R
import com.example.nextgenexample.databinding.FragmentInlineBannerBinding
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import org.json.JSONArray
import org.json.JSONException

/** An [AdFragment] subclass that loads inline banner ads. */
class InlineBannerFragment :
  AdFragment<FragmentInlineBannerBinding>(), ViewTreeObserver.OnGlobalLayoutListener {
  override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentInlineBannerBinding
    get() = FragmentInlineBannerBinding::inflate

  // List of banner ad items and menu items populated in the RecyclerView.
  private val recyclerViewItems: MutableList<Any> = ArrayList()
  private lateinit var adapter: RecyclerViewAdapter

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    addMenuItemsFromJson()
    view.viewTreeObserver.addOnGlobalLayoutListener(this)

    val recyclerView = binding.recyclerView
    recyclerView.layoutManager = LinearLayoutManager(requireContext())

    // Specify an adapter.
    adapter = RecyclerViewAdapter(requireActivity(), recyclerViewItems)
    recyclerView.adapter = adapter
  }

  // Configure the RecyclerView when the layout is finished.
  override fun onGlobalLayout() {
    loadBannerAds(adapter)

    view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
  }

  private fun loadBannerAds(adapter: RecyclerViewAdapter) {
    // Get the ad size based on the recyclerView width.
    val recyclerView = binding.recyclerView
    val recyclerViewWidthPixels = recyclerView.width
    val density = resources.displayMetrics.density
    check(density != 0f) { "Density cannot be zero." }
    val adWidth = (recyclerViewWidthPixels / density).toInt()

    // The Google Mobile Ads SDK uses dp to size the banner.
    val adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(requireContext(), adWidth)

    for (adIndex in ITEMS_PER_AD until recyclerViewItems.size step ITEMS_PER_AD + 1) {
      // Add the banner item.
      val bannerItem = BannerItem()
      recyclerViewItems.add(adIndex, bannerItem)
      adapter.notifyItemInserted(adIndex)

      // Implementation is built for a list with <100 items.
      BannerAd.load(
        BannerAdRequest.Builder(AD_UNIT_ID, adSize).build(),
        object : AdLoadCallback<BannerAd> {
          override fun onAdLoaded(bannerAd: BannerAd) {
            Log.d(Constant.TAG, "Banner ad loaded.")
            activity?.runOnUiThread {
              // Update the banner item with the loaded banner ad.
              recyclerViewItems[adIndex] = bannerItem
              bannerItem.bannerAd = bannerAd
              adapter.notifyItemChanged(adIndex)
            }
            bannerAd.adEventCallback =
              object : BannerAdEventCallback {
                override fun onAdImpression() {
                  Log.d(Constant.TAG, "Banner ad recorded an impression.")
                }

                override fun onAdClicked() {
                  Log.d(Constant.TAG, "Banner ad clicked.")
                }

                override fun onAdShowedFullScreenContent() {
                  Log.d(Constant.TAG, "Banner ad showed.")
                }

                override fun onAdDismissedFullScreenContent() {
                  Log.d(Constant.TAG, "Banner ad dismissed.")
                }

                override fun onAdFailedToShowFullScreenContent(error: FullScreenContentError) {
                  Log.w(Constant.TAG, "Banner ad failed to show: $error")
                }
              }
          }

          override fun onAdFailedToLoad(adError: LoadAdError) {
            Log.w(Constant.TAG, "Banner ad failed to load: $adError")
          }
        },
      )
    }
  }

  /** Adds [FoodMenuItem]'s from a JSON file. */
  private fun addMenuItemsFromJson() {
    try {
      val menuItemsJsonArray = JSONArray(readJsonDataFromFile())
      (0 until menuItemsJsonArray.length()).map { i ->
        val menuItemObject = menuItemsJsonArray.getJSONObject(i)
        recyclerViewItems.add(
          FoodMenuItem(
            menuItemObject.getString("name"),
            menuItemObject.getString("description"),
            menuItemObject.getString("price"),
            menuItemObject.getString("category"),
            menuItemObject.getString("photo"),
          )
        )
      }
    } catch (exception: IOException) {
      Log.e(Constant.TAG, "Unable to parse JSON file with IOException.", exception)
    } catch (exception: JSONException) {
      Log.e(Constant.TAG, "Unable to parse JSON file with JSONException.", exception)
    }
  }

  /**
   * Reads the JSON file and converts the JSON data to a [String].
   *
   * @return A [String] representation of the JSON data.
   * @throws IOException if unable to read the JSON file.
   */
  @Throws(IOException::class)
  private fun readJsonDataFromFile(): String {
    return resources.openRawResource(R.raw.menu_items).use { inputStream ->
      InputStreamReader(inputStream).use { reader ->
        BufferedReader(reader).use { bufferedReader -> bufferedReader.readText() }
      }
    }
  }

  private companion object {
    // A banner ad is placed after every 8 menu items in the RecyclerView.
    const val ITEMS_PER_AD = 8
    // Sample inline adaptive banner ad unit ID.
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
  }
}
