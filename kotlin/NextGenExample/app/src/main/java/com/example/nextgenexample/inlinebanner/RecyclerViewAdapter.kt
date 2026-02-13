package com.example.nextgenexample.inlinebanner

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.nextgenexample.Constant
import com.example.nextgenexample.databinding.InlineBannerAdBinding
import com.example.nextgenexample.databinding.RecyclerViewMenuItemBinding

class RecyclerViewAdapter(
  private val activity: FragmentActivity,
  private val recyclerViewItems: List<Any>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private class MenuItemViewHolder(binding: RecyclerViewMenuItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    val menuItemName: TextView = binding.menuItemName
    val menuItemDescription: TextView = binding.menuItemDescription
    val menuItemPrice: TextView = binding.menuItemPrice
    val menuItemCategory: TextView = binding.menuItemCategory
    val menuItemImage: ImageView = binding.menuItemImage
  }

  private class BannerAdViewHolder(private val binding: InlineBannerAdBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun getBannerViewContainer(): CardView = binding.bannerViewContainer
  }

  override fun getItemCount(): Int = recyclerViewItems.size

  /** Determines the view type for the given position. */
  override fun getItemViewType(position: Int): Int {
    return if (recyclerViewItems[position] is BannerItem) {
      BANNER_AD_VIEW_TYPE
    } else {
      MENU_ITEM_VIEW_TYPE
    }
  }

  /**
   * Creates a new view for a menu item view or a banner ad view based on the viewType. This method
   * is invoked by the layout manager.
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return if (viewType == MENU_ITEM_VIEW_TYPE) {
      val binding = RecyclerViewMenuItemBinding.inflate(inflater, parent, false)
      MenuItemViewHolder(binding)
    } else {
      val binding = InlineBannerAdBinding.inflate(inflater, parent, false)
      BannerAdViewHolder(binding)
    }
  }

  /**
   * Replaces the content in the views that make up the menu item view and the banner ad view. This
   * method is invoked by the layout manager.
   */
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is MenuItemViewHolder -> {
        val menuItem = recyclerViewItems[position] as? FoodMenuItem
        if (menuItem == null) {
          Log.w(Constant.TAG, "FoodMenuItem not found at position $position.")
          return
        }

        // Get the menu item image resource ID.
        val imageName = menuItem.imageName
        val imageResID =
          activity.resources.getIdentifier(imageName, "drawable", activity.packageName)

        // Add the menu item details to the menu item view.
        holder.menuItemImage.setImageResource(imageResID)
        holder.menuItemName.text = menuItem.name
        holder.menuItemPrice.text = menuItem.price
        holder.menuItemCategory.text = menuItem.category
        holder.menuItemDescription.text = menuItem.description
      }
      is BannerAdViewHolder -> {
        val bannerItem = recyclerViewItems[position] as? BannerItem
        if (bannerItem == null) {
          Log.w(Constant.TAG, "BannerItem not found at position $position.")
          return
        }

        if (bannerItem.isBannerAdInitialized()) {
          // Get the CardView container from the BannerAdViewHolder.
          val bannerViewContainer = holder.getBannerViewContainer()

          val bannerAd = bannerItem.bannerAd
          // Add the banner ad view to the CardView container.
          bannerViewContainer.addView(bannerAd.getView(activity))
        }
      }
    }
  }

  override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
    super.onViewRecycled(holder)
    if (holder is BannerAdViewHolder) {
      // Detach the banner ad from the recycled view holder.
      val bannerViewContainer = holder.getBannerViewContainer()
      bannerViewContainer.removeAllViews()
    }
  }

  private companion object {
    // A menu item view type.
    const val MENU_ITEM_VIEW_TYPE = 0
    // The banner ad view type.
    const val BANNER_AD_VIEW_TYPE = 1
  }
}
