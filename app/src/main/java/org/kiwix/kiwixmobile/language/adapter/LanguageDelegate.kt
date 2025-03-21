/*
 * Kiwix Android
 * Copyright (c) 2019 Kiwix <android.kiwix.org>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.kiwix.kiwixmobile.language.adapter

import android.view.ViewGroup
import org.kiwix.kiwixmobile.core.base.adapter.AbsDelegateAdapter
import org.kiwix.kiwixmobile.core.extensions.ViewGroupExtensions.viewBinding
import org.kiwix.kiwixmobile.databinding.HeaderDateBinding
import org.kiwix.kiwixmobile.databinding.ItemLanguageBinding
import org.kiwix.kiwixmobile.language.adapter.LanguageListItem.HeaderItem
import org.kiwix.kiwixmobile.language.adapter.LanguageListItem.LanguageItem
import org.kiwix.kiwixmobile.language.adapter.LanguageListViewHolder.HeaderViewHolder
import org.kiwix.kiwixmobile.language.adapter.LanguageListViewHolder.LanguageViewHolder

sealed class LanguageDelegate<I : LanguageListItem, out VH : LanguageListViewHolder<I>> :
  AbsDelegateAdapter<I, LanguageListItem, VH> {
  class HeaderDelegate : LanguageDelegate<HeaderItem, HeaderViewHolder>() {
    override val itemClass = HeaderItem::class.java

    override fun createViewHolder(parent: ViewGroup) =
      HeaderViewHolder(
        parent.viewBinding(HeaderDateBinding::inflate, false)
      )
  }

  class LanguageItemDelegate(private val clickAction: (LanguageItem) -> Unit) :
    LanguageDelegate<LanguageItem, LanguageViewHolder>() {
    override val itemClass = LanguageItem::class.java

    override fun createViewHolder(parent: ViewGroup) =
      LanguageViewHolder(
        parent.viewBinding(ItemLanguageBinding::inflate, false),
        clickAction
      )
  }
}
