/*
 * Kiwix Android
 * Copyright (c) 2024 Kiwix <android.kiwix.org>
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

package org.kiwix.kiwixmobile.core.downloader.downloadManager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.tonyodev.fetch2.DefaultFetchNotificationManager
import com.tonyodev.fetch2.DownloadNotification
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.R.drawable
import com.tonyodev.fetch2.R.string
import com.tonyodev.fetch2.util.DEFAULT_NOTIFICATION_TIMEOUT_AFTER_RESET
import org.kiwix.kiwixmobile.core.CoreApp
import org.kiwix.kiwixmobile.core.Intents
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.dao.DownloadRoomDao
import org.kiwix.kiwixmobile.core.main.CoreMainActivity
import javax.inject.Inject

const val DOWNLOAD_NOTIFICATION_TITLE = "OPEN_ZIM_FILE"

class FetchDownloadNotificationManager @Inject constructor(
  context: Context,
  private val downloadRoomDao: DownloadRoomDao
) : DefaultFetchNotificationManager(context) {
  override fun getFetchInstanceForNamespace(namespace: String): Fetch = Fetch.getDefaultInstance()

  override fun registerBroadcastReceiver() {
    val context = CoreApp.instance.applicationContext
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.registerReceiver(
        broadcastReceiver,
        IntentFilter(notificationManagerAction),
        Context.RECEIVER_EXPORTED
      )
    } else {
      context.registerReceiver(
        broadcastReceiver,
        IntentFilter(notificationManagerAction)
      )
    }
  }

  override fun createNotificationChannels(
    context: Context,
    notificationManager: NotificationManager
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channelId =
        context.getString(string.fetch_notification_default_channel_id)
      if (notificationManager.getNotificationChannel(channelId) == null) {
        notificationManager.createNotificationChannel(createChannel(channelId, context))
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createChannel(channelId: String, context: Context) =
    NotificationChannel(
      channelId,
      context.getString(string.fetch_notification_default_channel_name),
      NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
      setSound(null, null)
      enableVibration(false)
    }

  override fun updateNotification(
    notificationBuilder: NotificationCompat.Builder,
    downloadNotification: DownloadNotification,
    context: Context
  ) {
    val smallIcon = if (downloadNotification.isDownloading) {
      android.R.drawable.stat_sys_download
    } else {
      android.R.drawable.stat_sys_download_done
    }
    val notificationTitle =
      downloadRoomDao.getEntityForFileName(downloadNotification.title)?.title
        ?: downloadNotification.title
    notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setSmallIcon(smallIcon)
      .setContentTitle(notificationTitle)
      .setContentText(getSubtitleText(context, downloadNotification))
      .setOngoing(downloadNotification.isOnGoingNotification)
      .setGroup(downloadNotification.groupId.toString())
      .setGroupSummary(false)
    if (downloadNotification.isFailed || downloadNotification.isCompleted) {
      notificationBuilder.setProgress(ZERO, ZERO, false)
    } else {
      val progressIndeterminate = downloadNotification.progressIndeterminate
      val maxProgress = if (downloadNotification.progressIndeterminate) ZERO else HUNDERED
      val progress =
        if (downloadNotification.progress < ZERO) ZERO else downloadNotification.progress
      notificationBuilder.setProgress(maxProgress, progress, progressIndeterminate)
    }
    when {
      downloadNotification.isDownloading ->
        notificationBuilder.setTimeoutAfter(getNotificationTimeOutMillis())
          .addAction(
            drawable.fetch_notification_cancel,
            context.getString(R.string.cancel),
            getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.DELETE)
          ).addAction(
            drawable.fetch_notification_pause,
            context.getString(R.string.notification_pause_button_text),
            getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.PAUSE)
          )

      downloadNotification.isPaused ->
        notificationBuilder.setTimeoutAfter(getNotificationTimeOutMillis())
          .addAction(
            drawable.fetch_notification_resume,
            context.getString(R.string.notification_resume_button_text),
            getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.RESUME)
          )
          .addAction(
            drawable.fetch_notification_cancel,
            context.getString(R.string.cancel),
            getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.DELETE)
          )

      downloadNotification.isQueued ->
        notificationBuilder.setTimeoutAfter(getNotificationTimeOutMillis())

      else -> notificationBuilder.setTimeoutAfter(DEFAULT_NOTIFICATION_TIMEOUT_AFTER_RESET)
    }
    notificationCustomisation(downloadNotification, notificationBuilder, context)
  }

  @SuppressLint("UnspecifiedImmutableFlag")
  private fun notificationCustomisation(
    downloadNotification: DownloadNotification,
    notificationBuilder: NotificationCompat.Builder,
    context: Context
  ) {
    if (downloadNotification.isCompleted) {
      val internal = Intents.internal(CoreMainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(DOWNLOAD_NOTIFICATION_TITLE, downloadNotification.title)
      }
      val pendingIntent =
        getActivity(
          context,
          downloadNotification.notificationId,
          internal,
          FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
      notificationBuilder.setContentIntent(pendingIntent)
      notificationBuilder.setAutoCancel(true)
    }
  }
}
