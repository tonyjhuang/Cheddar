package com.tonyjhuang.cheddar.api.feedback;

/**
 * Created by tonyjhuang on 3/17/16.
 */
public class FeedbackRequest {
    private static final String USERNAME = "Feedback Bot";
    private static final String ICON_EMOJI = ":feelsgood:";

    final String text;

    final String username = USERNAME;
    final String icon_emoji = ICON_EMOJI;

    public FeedbackRequest(String text) {
        this.text = text;
    }
}
