package com.mediatek.incallui.volte;

import android.content.Context;
import android.util.AttributeSet;
import com.android.ex.chips.MTKRecipientEditTextView;

public class AddMemberEditView extends MTKRecipientEditTextView {

    private static final int AUTO_SEARCH_THRESHOLD_LENGTH = 1;

    public AddMemberEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setValidator(new NumberValidator());
        //set search address threshold length as 1
        setThreshold(AUTO_SEARCH_THRESHOLD_LENGTH);
    }

    /** A noop validator that does not munge invalid texts and claims any number is valid */
    private class NumberValidator implements Validator {
        public CharSequence fixText(CharSequence invalidText) {
            return invalidText;
        }

        public boolean isValid(CharSequence text) {
            return true;
        }
    }
}
