package com.forwardthinking.conversionmasterfree;

import android.app.Activity;
import android.content.DialogInterface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


class ScientificKeyboard implements android.content.DialogInterface.OnClickListener {

    /** A link to the KeyboardView that is used to render this CustomKeyboard. */
    private KeyboardView keyboardView;
    /** A link to the activity that hosts the {@link #keyboardView}. */
    private Activity     hostActivity;
    private boolean hapticFeedback;
    
    /** The key (code) handler. */
    private OnKeyboardActionListener keyboardActionListener = new OnKeyboardActionListener() {

	// add your own special keys here:
        public final static int CodeDelete   = Keyboard.KEYCODE_DELETE; // Keyboard.KEYCODE_DELETE
        public final static int CodeDone   = Keyboard.KEYCODE_DONE; // Keyboard.KEYCODE_CANCEL
        public final static int CodeSelectAll = 55000;

        @Override public void onKey(int primaryCode, int[] keyCodes) {
            // NOTE We can say '<Key android:codes="49,50" ... >' in the xml file; all codes come in keyCodes, the first in this list in primaryCode
            // Get the EditText and its Editable
            View focusCurrent = hostActivity.getCurrentFocus();
            if( focusCurrent==null || focusCurrent.getClass()!=EditText.class ) return;
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            String text = editable.toString();
            
            int start = edittext.getSelectionStart();
            int end = edittext.getSelectionEnd();
            // delete the selection, if chars are selected: 
            if(end>start){
            	editable.delete(start, end);
            }
            
            // Apply the key to the edittext
            if (primaryCode==CodeDone ) {
            	edittext.setText(text);
                hideKeyboard();
            } 
            // Apply the key to the edittext
            else if (primaryCode==CodeDelete ) {
                if (editable!=null && start>0 ) editable.delete(start - 1, start);
            } 
            else if (primaryCode==CodeSelectAll) {
            	edittext.selectAll();
            }
            else if (Character.toString((char) primaryCode).equals("e")) {
            	if ( !editable.toString().contains("e") && editable.length() > 0) {
            		// numbers can only have one "e" and it can't be first char
            		editable.insert(start, Character.toString((char) primaryCode));
            	}
            }
            else if (Character.toString((char) primaryCode).equals(".")) {
            	if ( !editable.toString().contains(".")) {
            		// numbers can only have one of these characters
            		editable.insert(start, Character.toString((char) primaryCode));
            	}
            }
            else if (Character.toString((char) primaryCode).equals("-")) {
            	if ( editable.toString().length() == 0 || editable.toString().endsWith("e")) {
            		// negative sign can only be first character or follow an e
            		editable.insert(start, Character.toString((char) primaryCode));
            	}
            }
            else { // insert character
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }

        @Override public void onPress(int arg0) {
        	// vibrate if haptic feedback is enabled:
        	if(hapticFeedback && arg0!=0)
        		keyboardView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }

        @Override public void onRelease(int primaryCode) {
        }

        @Override public void onText(CharSequence text) {
        }

        @Override public void swipeDown() {
        }

        @Override public void swipeLeft() {
        }

        @Override public void swipeRight() {
        }

        @Override public void swipeUp() {
        }
    };

    /**
     * Create a custom keyboard, that uses the KeyboardView (with resource id <var>viewid</var>) of the <var>host</var> activity,
     * and load the keyboard layout from xml file <var>layoutid</var> (see {@link Keyboard} for description).
     * Note that the <var>host</var> activity must have a <var>KeyboardView</var> in its layout (typically aligned with the bottom of the activity).
     * Note that the keyboard layout xml file may include key codes for navigation; see the constants in this class for their values.
     * Note that to enable EditText's to use this custom keyboard, call the {@link #registerEditText(int)}.
     *
     * @param host The hosting activity.
     * @param viewid The id of the KeyboardView.
     * @param layoutid The id of the xml file containing the keyboard layout.
     */
    public ScientificKeyboard(Activity host, int viewid, int layoutid) {
        hostActivity= host;
        keyboardView= (KeyboardView)hostActivity.findViewById(viewid);
        keyboardView.setKeyboard(new Keyboard(hostActivity, layoutid));
        keyboardView.setPreviewEnabled(false); // NOTE Do not show the preview balloons
        keyboardView.setOnKeyboardActionListener(keyboardActionListener);
        // Hide the standard keyboard initially
        hostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /** Returns whether the CustomKeyboard is visible. */
    public boolean isKeyboardVisible() {
        return keyboardView.getVisibility() == View.VISIBLE;
    }

    /** Make the CustomKeyboard visible, and hide the system keyboard for view v. */
    public void showKeyboard( View v ) {
        keyboardView.setVisibility(View.VISIBLE);
        keyboardView.setEnabled(true);
        if( v!=null ) ((InputMethodManager)hostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /** Make the CustomKeyboard invisible. */
    public void hideKeyboard() {
        keyboardView.setVisibility(View.GONE);
        keyboardView.setEnabled(false);
    }

    /**
     * Register <var>EditText<var> with resource id <var>resid</var> (on the hosting activity) for using this custom keyboard.
     *
     * @param resid The resource id of the EditText that registers to the custom keyboard.
     */
    public void registerEditText(int resid) {
        // Find the EditText 'resid'
        final EditText edittext= (EditText)hostActivity.findViewById(resid);
        // Make the custom keyboard appear
        edittext.setOnFocusChangeListener(new OnFocusChangeListener() {
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            @Override 
            public void onFocusChange(View v, boolean hasFocus) {
                if( hasFocus ) {
                	showKeyboard(v);
                }
                else {
                	hideKeyboard();
                }
            }
        });
        edittext.setOnClickListener(new OnClickListener() {
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            @Override 
            public void onClick(View v) {
                showKeyboard(v);
            }
        });
        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
        edittext.setOnTouchListener(new OnTouchListener() {
            @Override 
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);               // Call native handler
                edittext.setInputType(inType);              // Restore input type
                edittext.setCursorVisible(true);
                return true; // Consume touch event
            }
        });
        // Disable spell check (hex strings look like words to Android)
        edittext.setInputType(edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        /**
         * Try to show cursor the complicated way:
         * @source http://androidpadanam.wordpress.com/2013/05/29/customkeyboard-example/
         * fixes the cursor not movable bug
         */
        OnTouchListener otl = new OnTouchListener() {
        	 
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!(isKeyboardVisible())) {
                    showKeyboard(v);
                }
                v.requestFocus();	// SETH HACK to insure edit text gets focus!! in multipane view
                
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Layout layout = ((EditText) v).getLayout();
                        float x = event.getX() + edittext.getScrollX();
                        int offset = layout.getOffsetForHorizontal(0, x);
                        if (offset > 0)
                            if (x > layout.getLineMax(0))
                                edittext.setSelection(offset);     // touch was at the end of the text
                            else
                                edittext.setSelection(offset - 1);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        layout = ((EditText) v).getLayout();
                        x = event.getX() + edittext.getScrollX();
                        offset = layout.getOffsetForHorizontal(0, x);
                        if (offset > 0)
                            if (x > layout.getLineMax(0))
                                edittext.setSelection(offset);     // Touchpoint was at the end of the text
                            else
                                edittext.setSelection(offset - 1);
                        break;
 
                }
                return true;
            }
        };
        edittext.setOnTouchListener(otl); 
    }
	
/*
 * Added methods: (may need additional resource files)
 */    
	/**
	 * Enables or disables the Haptic feedback on keyboard touches
	 * @param goEnabled true if you want haptic feedback, falso otherwise
	 */
	public void enableHapticFeedback(boolean goEnabled){
		keyboardView.setHapticFeedbackEnabled(goEnabled);
		hapticFeedback = goEnabled;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO empty stubb
	}
}
