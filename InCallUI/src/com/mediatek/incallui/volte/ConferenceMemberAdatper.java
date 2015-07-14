package com.mediatek.incallui.volte;

import android.content.Context;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.incallui.R;

public class ConferenceMemberAdatper extends BaseRecipientAdapter {
    private static final int DEFAULT_PREFERRED_MAX_RESULT_COUNT = 10;

    public ConferenceMemberAdatper(Context context) {
        // The Chips UI is email-centric by default. By setting QUERY_TYPE_PHONE, the chips UI
        // will operate with phone numbers instead of emails.
        super(context, DEFAULT_PREFERRED_MAX_RESULT_COUNT, QUERY_TYPE_PHONE);
        setShowDuplicateResults(true);
    }

    /**
     * Returns a layout id for each item inside auto-complete list.
     *
     * Each View must contain two TextViews (for display name and destination) and one ImageView
     * (for photo). Ids for those should be available via {@link #getDisplayNameId()},
     * {@link #getDestinationId()}, and {@link #getPhotoId()}.
     */
    @Override
    protected int getItemLayout() {
        return R.layout.mtk_volte_add_conference_member_dropdown_item;
    }

    public void setShowEmailAddress(boolean showEmailAddress) {
        super.setShowPhoneAndEmail(showEmailAddress);
    }
}
