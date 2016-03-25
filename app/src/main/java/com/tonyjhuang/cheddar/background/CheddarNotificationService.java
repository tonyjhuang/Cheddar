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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseObject;
import com.tonyjhuang.cheddar.AppRouter_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.Presence;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;
import org.apache.commons.lang3.text.WordUtils;

import rx.Observable;

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

    private Spannable boldSubstring(String source, String target) {
        int targetIndex = source.indexOf(target);
        Spannable sb = new SpannableString(source);
        if(targetIndex != -1) {
            sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    targetIndex, targetIndex + target.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return sb;
    }

    public void createOrUpdatePresenceNotification(Context context, Presence presence) {
        String authorName = WordUtils.capitalizeFully(presence.getAlias().getName());
        String presenceText = authorName;
        presenceText += presence.getAction() == Presence.Action.JOIN ? " joined the room." :
                " left the room.";

        NotificationCompat.Builder builder = getBuilder(context)
                .setLargeIcon(getAuthorBitmap(context, presence.getAlias()))
                .setContentText(boldSubstring(presenceText, authorName))
                .setTicker(presenceText)
                .setNumber(unreadMessagesCounter.get(presence.getAlias().getChatRoomId()));

        api.getCurrentUser().map(ParseObject::getObjectId)
                .flatMap(currentUserId -> getNotificationTitle(currentUserId,
                        presence.getAlias().getChatRoomId()))
                .subscribe(title -> {
                    builder.setContentTitle(title);
                    notificationManager.notify(0, builder.build());
                }, error -> Log.e(TAG, "failed to get notification title: " + error.toString()));
    }

    public void createOrUpdateMessageNotification(Context context, Message message) {
        String authorName = WordUtils.capitalizeFully(message.getAlias().getName());
        String contextText = authorName + ": " + message.getBody();
        NotificationCompat.Builder builder = getBuilder(context)
                .setLargeIcon(getAuthorBitmap(context, message.getAlias()))
                .setContentText(boldSubstring(contextText, authorName))
                .setTicker(contextText)
                .setNumber(unreadMessagesCounter.get(message.getAlias().getChatRoomId()));


        api.getCurrentUser().map(ParseObject::getObjectId)
                .flatMap(currentUserId -> getNotificationTitle(currentUserId,
                        message.getAlias().getChatRoomId()))
                .subscribe(title -> {
                    builder.setContentTitle(title);
                    notificationManager.notify(0, builder.build());
                }, error -> Log.e(TAG, "failed to get notification title: " + error.toString()));
    }


    private Observable<String> getNotificationTitle(String currentUserId, String chatRoomId) {
        return Observable.just("Cheddar - Beta");
        /*
        return api.getUsersInChatRoom(chatRoomId)
                .flatMap(Observable::from)
                .filter(alias -> !alias.getUserId().equals(currentUserId))
                .map(Alias::getName)
                .map(name -> name.split(" "))
                // Turn Vindictive Fireant into V. Fireant
                .map(nameParts -> nameParts[0].substring(0, 1) + ". " + nameParts[1])
                .compose(o -> StringObservable.join(o, ", "));
                */
    }

    private Bitmap getAuthorBitmap(Context context, Alias alias) {
        TextView textView = (TextView) View.inflate(context, R.layout.stub_notif_author_view, null);
        textView.setText(getAliasDisplayName(alias));
        textView.setTextColor(incomingAuthorTextColor);
        ((GradientDrawable) textView.getBackground()).setColor(incomingAuthorBackgroundColor);

        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), "Effra-Medium.ttf");
        }
        textView.setTypeface(typeface);

        // Measure view so height and width are not 0
        textView.measure(View.MeasureSpec.makeMeasureSpec(largeIconDimen, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(largeIconDimen, View.MeasureSpec.EXACTLY));
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());

        return getBitmapFromView(textView);
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        bgDrawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }

    private String getAliasDisplayName(Alias alias) {
        String display = "";
        for (String namePart : alias.getName().split(" ")) {
            display += namePart.substring(0, 1).toUpperCase();
        }
        return display;
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
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
    }
}
