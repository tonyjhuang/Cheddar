package com.tonyjhuang.cheddar.background.notif;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.util.Pair;
import android.view.View;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatRoom;
import com.tonyjhuang.cheddar.background.UnreadMessagesCounter;
import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView_;
import com.tonyjhuang.cheddar.utils.Scheduler;
import com.tonyjhuang.cheddar.utils.StringUtils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;
import org.androidannotations.annotations.res.DimensionRes;

import rx.Observable;
import timber.log.Timber;

/**
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
    @DimensionRes(R.dimen.notif_author_text_size)
    float authorTextSize;

    @Bean
    CheddarApi api;
    @Bean
    UnreadMessagesCounter unreadMessagesCounter;

    public void createOrUpdateChatEventNotification(Context context, ChatEvent chatEvent) {
        String chatRoomId = chatEvent.alias().chatRoomId();
        String contentText = chatEvent.displayBody();

        Observable.zip(api.getAliasForChatRoom(chatRoomId), api.getChatRoom(chatRoomId), Pair::new)
                .compose(Scheduler.defaultSchedulers())
                .subscribe(aliasAndChatRoom -> {
                    Alias alias = aliasAndChatRoom.first;
                    ChatRoom chatRoom = aliasAndChatRoom.second;
                    if (alias.equals(chatEvent.alias())) return;

                    NotificationCompat.Builder builder = getBuilder(context, alias.objectId())
                            .setContentTitle(chatRoom.name())
                            .setLargeIcon(getAuthorBitmap(context, chatEvent.alias()))
                            .setContentText(StringUtils.boldSubstring(contentText, chatEvent.alias().displayName()))
                            .setTicker(contentText)
                            .setNumber(unreadMessagesCounter.get(chatRoomId));

                    notificationManager.notify(chatRoomId.hashCode(), builder.build());
                }, error -> Timber.e(error, "couldn't fetch Alias for chatRoom " + chatRoomId));


    }

    private Bitmap getAuthorBitmap(Context context, Alias alias) {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), "Effra-Medium.ttf");
        }

        // Initialize AliasDisplayView
        AliasDisplayView aliasDisplayView = AliasDisplayView_.build(context);
        aliasDisplayView.setAlias(alias, false);
        aliasDisplayView.setTextSize(authorTextSize);
        // Set our own typeface since we're in a non-application context and
        // Calligraphy won't work here.
        aliasDisplayView.setTypeface(typeface);
        aliasDisplayView.showUnreadMessageIndicator(false);

        // Measure view so height and width are not 0
        aliasDisplayView.measure(View.MeasureSpec.makeMeasureSpec(largeIconDimen, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(largeIconDimen, View.MeasureSpec.EXACTLY));
        aliasDisplayView.layout(0, 0, aliasDisplayView.getMeasuredWidth(), aliasDisplayView.getMeasuredHeight());
        aliasDisplayView.resize();

        return getBitmapFromView(aliasDisplayView);
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        view.draw(canvas);
        return returnedBitmap;
    }

    private PendingIntent getContentPendingIntent(Context context, String aliasId) {
        Intent chatIntent = ChatActivity_.intent(context).aliasId(aliasId).get();
        return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(chatIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Creates a Notification Builder. Takes the CURRENT USER's Alias id.
     */
    private NotificationCompat.Builder getBuilder(Context context, String aliasId) {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notif_icon)
                .setColor(smallIconColor)
                .setContentIntent(getContentPendingIntent(context, aliasId))
                .setVibrate(vibratePattern)
                .setSound(sound)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
    }

    public void removeNotification(String chatRoomId) {
        notificationManager.cancel(chatRoomId.hashCode());
    }
}
