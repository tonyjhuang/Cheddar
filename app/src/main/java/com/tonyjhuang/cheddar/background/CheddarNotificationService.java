package com.tonyjhuang.cheddar.background;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import com.tonyjhuang.cheddar.AppRouter_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.api.models.ChatEvent;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView;
import com.tonyjhuang.cheddar.utils.StringUtils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Created by tonyjhuang on 3/15/16.
 * Handles creating and launching system notifications for api objects.
 */
@EBean
public class CheddarNotificationService {

    private static final String TAG = CheddarNotificationService.class.getSimpleName();
    private static final Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private static final long[] vibratePattern = new long[]{0, 250, 250, 500};
    private static Typeface typeface;

    @SystemService
    NotificationManager notificationManager;
    @ColorRes(R.color.colorAccent)
    int smallIconColor;
    @ColorRes(R.color.chat_author_text_incoming)
    int incomingAuthorTextColor;
    @ColorRes(R.color.chat_author_background_incoming)
    int incomingAuthorBackgroundColor;
    @DimensionPixelSizeRes(R.dimen.notif_large_icon_dimen)
    int largeIconDimen;

    @Bean
    CheddarApi api;
    @Bean
    UnreadMessagesCounter unreadMessagesCounter;

    public void createOrUpdatePresenceNotification(Context context, ChatEvent presence) {
        String authorName = WordUtils.capitalizeFully(presence.getAlias().getName());
        String chatRoomId = presence.getAlias().getChatRoomId();

        NotificationCompat.Builder builder = getBuilder(context)
                .setLargeIcon(getAuthorBitmap(context, presence.getAlias()))
                .setContentText(StringUtils.boldSubstring(presence.getBody(), authorName))
                .setTicker(presence.getBody())
                .setNumber(unreadMessagesCounter.get(chatRoomId));

        notificationManager.notify(chatRoomId.hashCode(), builder.build());
    }

    public void createOrUpdateMessageNotification(Context context, ChatEvent message) {
        String authorName = WordUtils.capitalizeFully(message.getAlias().getName());
        String contextText = authorName + ": " + message.getBody();

        String chatRoomId = message.getAlias().getChatRoomId();

        NotificationCompat.Builder builder = getBuilder(context)
                .setLargeIcon(getAuthorBitmap(context, message.getAlias()))
                .setContentText(StringUtils.boldSubstring(contextText, authorName))
                .setTicker(contextText)
                .setNumber(unreadMessagesCounter.get(message.getAlias().getChatRoomId()));

        notificationManager.notify(chatRoomId.hashCode(), builder.build());
    }

    private Bitmap getAuthorBitmap(Context context, Alias alias) {
        AliasDisplayView aliasDisplayView = (AliasDisplayView)
                View.inflate(context, R.layout.stub_notif_author_view, null);
        aliasDisplayView.setAliasName(alias.getName());
        aliasDisplayView.setTextColor(incomingAuthorTextColor);
        ((GradientDrawable) aliasDisplayView.getBackground()).setColor(incomingAuthorBackgroundColor);

        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), "Effra-Medium.ttf");
        }
        aliasDisplayView.setTypeface(typeface);

        // Measure view so height and width are not 0
        aliasDisplayView.measure(View.MeasureSpec.makeMeasureSpec(largeIconDimen, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(largeIconDimen, View.MeasureSpec.EXACTLY));
        aliasDisplayView.layout(0, 0, aliasDisplayView.getMeasuredWidth(), aliasDisplayView.getMeasuredHeight());

        return getBitmapFromView(aliasDisplayView);
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        bgDrawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }

    private PendingIntent getContentPendingIntent(Context context) {
        Intent resultIntent = new Intent(context, AppRouter_.class);
        return PendingIntent.getActivity(
                context,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private NotificationCompat.Builder getBuilder(Context context) {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notif_icon)
                .setColor(smallIconColor)
                .setContentIntent(getContentPendingIntent(context))
                .setVibrate(vibratePattern)
                .setSound(sound)
                .setAutoCancel(true)
                .setContentTitle(context.getString(R.string.notif_title))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
    }

    public void removeNotification(String chatRoomId) {
        notificationManager.cancel(chatRoomId.hashCode());
    }
}
