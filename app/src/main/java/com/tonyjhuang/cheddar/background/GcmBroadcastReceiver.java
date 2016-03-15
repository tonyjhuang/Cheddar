package com.tonyjhuang.cheddar.background;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseObject;
import com.tonyjhuang.cheddar.AppRouter_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarParser;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.MessageEvent;
import com.tonyjhuang.cheddar.api.models.Presence;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;
import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;
import rx.observables.StringObservable;

/**
 * Handles Gcm payloads if no Activity wants to handle it.
 */
@EReceiver
public class GcmBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = GcmBroadcastReceiver.class.getSimpleName();
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

    String currentUserId;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive!");
        String payloadString = intent.getStringExtra("payload");
        try {
            MessageEvent messageEvent = CheddarParser.parseMessageEvent(new JSONObject(payloadString));
            switch (messageEvent.getType()) {
                case PRESENCE:
                    handlePresence(context, (Presence) messageEvent);
                    break;
                case MESSAGE:
                    handleMessage(context, (Message) messageEvent);
                    break;
                default:
                    Log.e(TAG, "Unrecognized MessageEvent: " + payloadString);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse payload into json: " + payloadString);
        } catch (CheddarParser.UnrecognizedParseException e) {
            Log.e(TAG, "Failed to parse json into MessageEvent: " + payloadString);
        }
    }

    private Observable<String> getNotificationTitle(String currentUserId, String chatRoomId) {
        return api.getUsersInChatRoom(chatRoomId)
                .flatMap(Observable::from)
                .filter(alias -> !alias.getUserId().equals(currentUserId))
                .map(Alias::getName)
                .map(name -> name.split(" "))
                // Turn Vindictive Fireant into V. Fireant
                .map(nameParts -> nameParts[0].substring(0, 1) + ". " + nameParts[1])
                .compose(o -> StringObservable.join(o, ", "));
    }

    private void handlePresence(Context context, Presence presence) {
        String presenceText = presence.getAlias().getName();
        presenceText += presence.getAction() == Presence.Action.JOIN ? " has joined the room." :
                " has left the room.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setLargeIcon(getAuthorBitmap(context, presence.getAlias()))
                .setContentText(presenceText);

        api.getCurrentUser().map(ParseObject::getObjectId)
                .flatMap(currentUserId -> getNotificationTitle(currentUserId,
                        presence.getAlias().getChatRoomId()))
                .subscribe(title -> {
                    builder.setContentTitle(title);
                    notificationManager.notify(0, builder.build());
                }, error -> Log.e(TAG, "failed to get notification title: " + error.toString()));
    }

    private void handleMessage(Context context, Message message) {
        String contextText = message.getAlias().getName() + ": " + message.getBody();
        NotificationCompat.Builder builder = getBuilder(context)
                .setLargeIcon(getAuthorBitmap(context, message.getAlias()))
                .setContentText(contextText);


        api.getCurrentUser().map(ParseObject::getObjectId)
                .flatMap(currentUserId -> getNotificationTitle(currentUserId,
                        message.getAlias().getChatRoomId()))
                .subscribe(title -> {
                    builder.setContentTitle(title);
                    notificationManager.notify(0, builder.build());
                }, error -> Log.e(TAG, "failed to get notification title: " + error.toString()));

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

    private PendingIntent getPendingIntent(Context context) {
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
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(smallIconColor)
                .setContentIntent(getPendingIntent(context))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
    }
}
