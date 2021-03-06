package com.tonyjhuang.cheddar.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ocpsoft.pretty.time.PrettyTime;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.ui.customviews.AliasDisplayView;

import java.util.List;

import timber.log.Timber;

/**
 * Created by tonyjhuang on 3/28/16.
 */
public class AliasListDialog extends AlertDialog {

    private List<Alias> aliases;
    private String currentUserId;

    protected AliasListDialog(Context context) {
        super(context);
    }

    protected AliasListDialog(Context context, int theme) {
        super(context, theme);
    }

    protected AliasListDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    private AliasListDialog(Context context, List<Alias> aliases, String currentUserId) {
        this(context, true, null);
        this.aliases = aliases;
        this.currentUserId = currentUserId;
    }

    public static AliasListDialog show(Context context, List<Alias> aliases, String currentUserId) {
        AliasListDialog aliasListDialog = new AliasListDialog(context, aliases, currentUserId);
        aliasListDialog.show();
        return aliasListDialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_alias_list);
        LinearLayout container = (LinearLayout) findViewById(R.id.alias_container);
        assert container != null;

        PrettyTime prettyTime = new PrettyTime();

        for (Alias alias : aliases) {
            View aliasView = View.inflate(getContext(), R.layout.row_alias_alias, null);
            AliasDisplayView aliasDisplay = (AliasDisplayView) aliasView.findViewById(R.id.alias_display);
            TextView aliasName = (TextView) aliasView.findViewById(R.id.alias_name);
            TextView joinedAt = (TextView) aliasView.findViewById(R.id.joined_at);

            aliasDisplay.setAlias(alias, alias.userId().equals(currentUserId));
            aliasName.setText(alias.name());
            joinedAt.setText(getContext().getString(R.string.chat_alias_timestamp,
                    prettyTime.format(alias.createdAt())));

            container.addView(aliasView);
        }
    }
}
