package my.mimos.mitujusdk.search;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;

import my.mimos.mitujusdk.AndroidUtil;
import my.mimos.mitujusdk.R;

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class SearchView extends FrameLayout {
    private final static int CLEAR_BTN_WIDTH_DP                  = 48;
    private final static int LEFT_MENU_WIDTH_AND_MARGIN_START_DP = 52;
    private final static long CLEAR_BTN_FADE_ANIM_DURATION       = 500;
    private final static int MENU_ICON_ANIM_DURATION             = 250;
    private final static float MENU_BUTTON_PROGRESS_HAMBURGER    = 0.0f;


    private Activity host_activity;

    private View main_layout;
    private CardView query_section;
    private ImageView img_left_action;
    private ImageView img_clear_btn;
    private View search_input_parent;
    private ProgressBar search_progress;
    private SearchInputView search_input;
    private MenuView menu_view;

    private String old_query               = "";
    private boolean flag_focused           = false;
    private Drawable icon_back_arrow;
    private Drawable icon_search;
    private Drawable icon_clear;

    public ISearchView callback;

    public interface ISearchView {
        void onSearchMenuItemSelected(MenuItem item);
        void onSearchTextChanged(String old_query, String new_query);
        void onSearchAction(String query);
        void onSearchFocused(boolean focused);
    }

    public SearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        host_activity                 = AndroidUtil.getHostActivity(context);

        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        main_layout                   = inflate(getContext(), R.layout.searchview_layout, this);

        query_section                 = (CardView) findViewById(R.id.search_query_section);
        search_progress               = (ProgressBar) findViewById(R.id.search_bar_search_progress);
        img_left_action               = (ImageView) findViewById(R.id.search_left_action);
        img_clear_btn                 = (ImageView) findViewById(R.id.search_clear_btn);
        search_input_parent           = findViewById(R.id.search_input_parent);
        search_input                  = (SearchInputView) findViewById(R.id.search_bar_text);
        menu_view                     = (MenuView) findViewById(R.id.menu_view);

        icon_back_arrow               = AndroidUtil.getWrappedDrawable(getContext(), R.drawable.ic_arrow_back);
        icon_search                   = AndroidUtil.getWrappedDrawable(getContext(), R.drawable.ic_search);
        icon_clear                    = AndroidUtil.getWrappedDrawable(getContext(), R.drawable.ic_clear);

        img_clear_btn.setImageDrawable(icon_clear);

        setupQueryBar();
    }

    /**
     * Shows a circular progress on top of the
     * menu action button.
     * <p/>
     * Call hidProgress()
     * to change back to normal and make the menu
     * action visible.
     */
    public void showProgress() {
        img_left_action.setVisibility(View.GONE);
        search_progress.setAlpha(0.0f);
        search_progress.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(search_progress, "alpha", 0.0f, 1.0f).start();
    }

    /**
     * Hides the progress bar after
     * a prior call to showProgress()
     */
    public void hideProgress() {
        search_progress.setVisibility(View.GONE);
        img_left_action.setAlpha(0.0f);
        img_left_action.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(img_left_action, "alpha", 0.0f, 1.0f).start();
    }

    private int actionMenuAvailWidth() {
        if (isInEditMode())
            return query_section.getMeasuredWidth() / 2;

        return query_section.getWidth() / 2;
    }

    public boolean isFocused() {
        return flag_focused;
    }

    public void setQueryText(CharSequence text) {
        search_input.setText(text);
        //move cursor to end of text
        search_input.setSelection(search_input.getText().length());
    }

    private void setupQueryBar() {
        if (!isInEditMode() && host_activity != null) {
            host_activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        ViewTreeObserver vto = query_section.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AndroidUtil.removeGlobalLayoutObserver(query_section, this);
            }
        });

        menu_view.setMenuCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                if (callback != null)
                    callback.onSearchMenuItemSelected(item);
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {
            }

        });

        menu_view.setOnVisibleWidthChanged(new MenuView.OnVisibleWidthChangedListener() {
            @Override
            public void onItemsMenuVisibleWidthChanged(int newVisibleWidth) {
                handleOnVisibleMenuItemsWidthChanged(newVisibleWidth);
            }
        });

        img_clear_btn.setVisibility(View.INVISIBLE);
        img_clear_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                search_input.setText("");
//                if (mOnClearSearchActionListener != null) {
//                    mOnClearSearchActionListener.onClearSearchClicked();
//                }
            }
        });

        search_input.addTextChangedListener(new TextWatcherAdapter() {

            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                //todo investigate why this is called twice when pressing back on the keyboard

                if (search_input.getText().toString().length() != 0 &&
                        img_clear_btn.getVisibility() == View.INVISIBLE) {
                    img_clear_btn.setAlpha(0.0f);
                    img_clear_btn.setVisibility(View.VISIBLE);
                    ViewCompat.animate(img_clear_btn).alpha(1.0f).setDuration(CLEAR_BTN_FADE_ANIM_DURATION).start();
                } else if (search_input.getText().toString().length() == 0) {
                    img_clear_btn.setVisibility(View.INVISIBLE);
                }

                if (callback != null && flag_focused && !old_query.equals(search_input.getText().toString()))
                    callback.onSearchTextChanged(old_query, search_input.getText().toString());
                old_query = search_input.getText().toString();
            }

        });

        search_input.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus != flag_focused) {
                    setSearchFocusedInternal(hasFocus);
                }
            }
        });

        search_input.setOnKeyboardDismissedListener(new SearchInputView.OnKeyboardDismissedListener() {
            @Override
            public void onKeyboardDismissed() {
//                if (mCloseSearchOnSofteKeyboardDismiss) {
//                    setSearchFocusedInternal(false);
//                }
            }
        });

        search_input.setOnSearchKeyListener(new SearchInputView.OnKeyboardSearchKeyClickListener() {
            @Override
            public void onSearchKeyClicked() {
                if (callback != null)
                    callback.onSearchAction(old_query);

                setQueryText(old_query);
                setSearchFocusedInternal(false);
            }
        });

        img_left_action.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag_focused) {
                    if (callback != null)
                        callback.onSearchFocused(false);
                    setQueryText("");
                    setSearchFocusedInternal(false);
                }
            }
        });

        // not showing hamburger
        int leftActionWidthAndMarginLeft = AndroidUtil.dpToPx(LEFT_MENU_WIDTH_AND_MARGIN_START_DP);
        img_left_action.setVisibility(View.INVISIBLE);
        search_input_parent.setTranslationX(-leftActionWidthAndMarginLeft);
    }

    public void setSearchFocusedInternal(final boolean focused) {
        this.flag_focused = focused;
        if (callback != null && focused)
            callback.onSearchFocused(true);

        if (focused) {
            search_input.requestFocus();
            handleOnVisibleMenuItemsWidthChanged(0);//this must be called before  mMenuView.hideIfRoomItems(...)
            menu_view.hideIfRoomItems(true);
            transitionInLeftSection(true);
            AndroidUtil.showSoftKeyboard(getContext(), search_input);

//            if (mIsTitleSet) {
//                mSkipTextChangeEvent = true;
//                search_input.setText("");
//            } else {
                search_input.setSelection(search_input.getText().length());
//            }
            search_input.setLongClickable(true);
            img_clear_btn.setVisibility((search_input.getText().toString().length() == 0) ?
                    View.INVISIBLE : View.VISIBLE);
//            if (mFocusChangeListener != null) {
//                mFocusChangeListener.onFocus();
//            }
        } else {
            main_layout.requestFocus();
            handleOnVisibleMenuItemsWidthChanged(0);//this must be called before  mMenuView.hideIfRoomItems(...)
            menu_view.showIfRoomItems(true);
            transitionOutLeftSection(true);
            img_clear_btn.setVisibility(View.GONE);

            if (host_activity != null)
                AndroidUtil.closeSoftKeyboard(host_activity);

//            if (mIsTitleSet) {
//                mSkipTextChangeEvent = true;
//                search_input.setText(mTitleText);
//            }
            search_input.setLongClickable(false);
        }
    }


    //ensures that the end margin of the search input is according to Material specs
    private void handleOnVisibleMenuItemsWidthChanged(int menuItemsWidth) {
        if (menuItemsWidth == 0) {
            img_clear_btn.setTranslationX(-AndroidUtil.dpToPx(4));
            int paddingRight = AndroidUtil.dpToPx(4);
            if (flag_focused) {
                paddingRight += AndroidUtil.dpToPx(CLEAR_BTN_WIDTH_DP);
            } else {
                paddingRight += AndroidUtil.dpToPx(14);
            }
            search_input.setPadding(0, 0, paddingRight, 0);
        } else {
            img_clear_btn.setTranslationX(-menuItemsWidth);
            int paddingRight = menuItemsWidth;
            if (flag_focused) {
                paddingRight += AndroidUtil.dpToPx(CLEAR_BTN_WIDTH_DP);
            }
            search_input.setPadding(0, 0, paddingRight, 0);
        }
    }

    private void transitionInLeftSection(boolean withAnim) {

        if (search_progress.getVisibility() != View.VISIBLE) {
            img_left_action.setVisibility(View.VISIBLE);
        } else {
            img_left_action.setVisibility(View.INVISIBLE);
        }

        // not showing hamburger
        img_left_action.setImageDrawable(icon_back_arrow);
        if (withAnim) {
            ObjectAnimator searchInputTransXAnim = ViewPropertyObjectAnimator
                    .animate(search_input_parent).translationX(0).get();

            img_left_action.setScaleX(0.5f);
            img_left_action.setScaleY(0.5f);
            img_left_action.setAlpha(0.0f);
            img_left_action.setTranslationX(AndroidUtil.dpToPx(8));
            ObjectAnimator transXArrowAnim = ViewPropertyObjectAnimator.animate(img_left_action).translationX(1.0f).get();
            ObjectAnimator scaleXArrowAnim = ViewPropertyObjectAnimator.animate(img_left_action).scaleX(1.0f).get();
            ObjectAnimator scaleYArrowAnim = ViewPropertyObjectAnimator.animate(img_left_action).scaleY(1.0f).get();
            ObjectAnimator fadeArrowAnim = ViewPropertyObjectAnimator.animate(img_left_action).alpha(1.0f).get();
            transXArrowAnim.setStartDelay(150);
            scaleXArrowAnim.setStartDelay(150);
            scaleYArrowAnim.setStartDelay(150);
            fadeArrowAnim.setStartDelay(150);

            AnimatorSet animSet = new AnimatorSet();
            animSet.setDuration(500);
            animSet.playTogether(searchInputTransXAnim, transXArrowAnim, scaleXArrowAnim, scaleYArrowAnim, fadeArrowAnim);
            animSet.start();
        } else
            search_input_parent.setTranslationX(0);
    }

    private void transitionOutLeftSection(boolean withAnim) {
        // not showing hamburger
        img_left_action.setImageDrawable(icon_back_arrow);
        if (withAnim) {
            ObjectAnimator searchInputTransXAnim = ViewPropertyObjectAnimator.animate(search_input_parent)
                    .translationX(-AndroidUtil.dpToPx(LEFT_MENU_WIDTH_AND_MARGIN_START_DP)).get();

            ObjectAnimator scaleXArrowAnim = ViewPropertyObjectAnimator.animate(img_left_action).scaleX(0.5f).get();
            ObjectAnimator scaleYArrowAnim = ViewPropertyObjectAnimator.animate(img_left_action).scaleY(0.5f).get();
            ObjectAnimator fadeArrowAnim = ViewPropertyObjectAnimator.animate(img_left_action).alpha(0.5f).get();
            scaleXArrowAnim.setDuration(300);
            scaleYArrowAnim.setDuration(300);
            fadeArrowAnim.setDuration(300);
            scaleXArrowAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

                    //restore normal state
                    img_left_action.setScaleX(1.0f);
                    img_left_action.setScaleY(1.0f);
                    img_left_action.setAlpha(1.0f);
                    img_left_action.setVisibility(View.INVISIBLE);
                }
            });

            AnimatorSet animSet = new AnimatorSet();
            animSet.setDuration(350);
            animSet.playTogether(scaleXArrowAnim, scaleYArrowAnim, fadeArrowAnim, searchInputTransXAnim);
            animSet.start();
        } else
            img_left_action.setVisibility(View.INVISIBLE);
    }
}
