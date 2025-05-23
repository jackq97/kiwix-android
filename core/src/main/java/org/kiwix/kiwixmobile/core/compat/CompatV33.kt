/*
 * Kiwix Android
 * Copyright (c) 2023 Kiwix <android.kiwix.org>
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

package org.kiwix.kiwixmobile.core.compat

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.content.pm.ResolveInfo
import android.net.ConnectivityManager
import android.os.Build.VERSION_CODES.TIRAMISU
import androidx.annotation.RequiresApi
import java.util.Locale

@RequiresApi(TIRAMISU)
open class CompatV33 : Compat {
  private val compatV25 = CompatV25()
  override fun queryIntentActivities(
    packageManager: PackageManager,
    intent: Intent,
    flags: ResolveInfoFlagsCompat
  ): List<ResolveInfo> = packageManager.queryIntentActivities(
    intent,
    PackageManager.ResolveInfoFlags.of(flags.value)
  )

  override fun getPackageInformation(
    packageName: String,
    packageManager: PackageManager,
    flag: Int
  ): PackageInfo =
    packageManager.getPackageInfo(packageName, PackageInfoFlags.of(flag.toLong()))

  override fun isNetworkAvailable(connectivity: ConnectivityManager): Boolean =
    compatV25.isNetworkAvailable(connectivity)

  override fun isWifi(connectivity: ConnectivityManager): Boolean =
    compatV25.isWifi(connectivity)

  override fun convertToLocal(language: String): Locale = compatV25.convertToLocal(language)
}
