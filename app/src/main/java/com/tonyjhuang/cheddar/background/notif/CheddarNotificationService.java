package com.tonyjhuang.cheddar.background.notif;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.background.UnreadMessagesCounter;
import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView_;
import com.tonyjhuang.cheddar.utils.StringUtils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;

/**
 * Created by tonyjhuang on 3/15/16.
 * Handles creating and launching system notifications for api objects.
 */
@EBean
public class CheddarNotificationService {

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


    public void createOrUpdateChatEventNotification(Context context, ChatEvent chatEvent) {
        String chatRoomId = chatEvent.alias().chatRoomId();
        String contentText = chatEvent.displayBody();

        NotificationCompat.Builder builder = getBuilder(context, chatEvent.alias().objectId())
                .setLargeIcon(getAuthorBitmap(context, chatEvent.alias()))
                .setContentText(StringUtils.boldSubstring(contentText, chatEvent.alias().displayName()))
                .setTicker(contentText)
                .setNumber(unreadMessagesCounter.get(chatRoomId));

        notificationManager.notify(chatRoomId.hashCode(), builder.build());
    }

    private Bitmap getAuthorBitmap(Context context, Alias alias) {
        AliasDisplayView_ aliasDisplayView = (AliasDisplayView_)
                View.inflate(context, R.layout.stub_notif_author_view, null);
        aliasDisplayView.setAliasName(alias.name());
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

    private PendingIntent getContentPendingIntent(Context context, String aliasId) {
        return PendingIntent.getActivity(
                context,
                0,
                ChatActivity_.intent(context).aliasId(aliasId).get(),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private NotificationCompat.Builder getBuilder(Context context, String aliasId) {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notif_icon)
                .setColor(smallIconColor)
                .setContentIntent(getContentPendingIntent(context, aliasId))
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