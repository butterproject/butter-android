/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.tv.service.recommendation;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.util.concurrent.ExecutionException;

import butter.droid.base.utils.VersionUtils;
import butter.droid.tv.R;

/*
 * This class builds recommendations as notifications with videos as inputs.
 */
public class RecommendationBuilder {

    private static final String TAG = "RecommendationBuilder";

    private Context context;
    private NotificationManager notificationManager;

    private int id;
    private int priority;
    private int smallIcon;
    private String title;
    private String description;
    private String imageUri;
    private String backgroundUri;
    private PendingIntent intent;

    public RecommendationBuilder() {
    }

    public RecommendationBuilder setContext(Context context) {
        this.context = context;
        return this;
    }

    public RecommendationBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public RecommendationBuilder setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public RecommendationBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public RecommendationBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public RecommendationBuilder setImage(String uri) {
        imageUri = uri;
        return this;
    }

    public RecommendationBuilder setBackgroundContentUri(String uri) {
        backgroundUri = uri;
        return this;
    }

    public RecommendationBuilder setIntent(PendingIntent intent) {
        this.intent = intent;
        return this;
    }

    public RecommendationBuilder setSmallIcon(int resourceId) {
        smallIcon = resourceId;
        return this;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Notification build() {
        if (VersionUtils.isLollipop()) {

            Log.d(TAG, "Building notification - " + this.toString());

            if (notificationManager == null) {
                notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
            }

            Bundle extras = new Bundle();
            if (backgroundUri != null) {
                extras.putString(Notification.EXTRA_BACKGROUND_IMAGE_URI, backgroundUri);
            }

            Bitmap image;
            try {
                image = Glide.with(context)
                        .asBitmap()
                        .load(imageUri)
                        .submit((int) context.getResources().getDimension(R.dimen.card_width),
                                (int) context.getResources().getDimension(R.dimen.card_height))
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                image = null;
            }

            Notification notification = new NotificationCompat.BigPictureStyle(
                    new NotificationCompat.Builder(context)
                            .setContentTitle(title)
                            .setContentText(description)
                            .setPriority(priority)
                            .setLocalOnly(true)
                            .setOngoing(true)
                            .setColor(context.getResources().getColor(R.color.primary))
                            .setCategory(Notification.CATEGORY_RECOMMENDATION)
                            .setLargeIcon(image)
                            .setSmallIcon(smallIcon)
                            .setContentIntent(intent)
                            .setExtras(extras))
                    .build();

            notificationManager.notify(id, notification);
            notificationManager = null;
            return notification;
        }

        return null;
    }

    @Override
    public String toString() {
        return "RecommendationBuilder{"
                + ", id=" + id
                + ", priority=" + priority
                + ", smallIcon=" + smallIcon
                + ", title='" + title + '\''
                + ", description='" + description + '\''
                + ", imageUri='" + imageUri + '\''
                + ", backgroundUri='" + backgroundUri + '\''
                + ", intent=" + intent
                + '}';
    }
}
