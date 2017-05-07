package selecttooltip.zj.com.selecttooltip.selectHelper;

import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import selecttooltip.zj.com.selecttooltip.R;

/**
 * Created by zhangjun on 17/3/20.
 */

public class SelectTextHelper {
    private final static int DEFAULT_SELECTION_LENGTH = 1;
    private static final int DEFAULT_SHOW_DURATION = 100;

    private boolean isHideWhenScroll;
    private boolean isHide = true;

    public SelectionInfo mSelectionInfo;
    public TextView mTextView;
    public ISelectTextOprateWindow mSelectTextOprateWindow;
    private Spannable mSpannable;
    private BackgroundColorSpan mSpan;

    private CursorHandle mStartHandle;
    private CursorHandle mEndHandle;

    private int mTouchX;
    private int mTouchY;

    private ViewGroup rootView;

    private ViewTreeObserver.OnPreDrawListener mOnPreDrawListener;
    ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;


    private List<TextView> mTextViewList;

    public SelectTextHelper(ViewGroup root){
        this.rootView = root;
        mTextViewList = new ArrayList<>();
        mSelectionInfo = new SelectionInfo();
    }

    public void registerTextView(TextView textView,String title,String author){
        mTextViewList.add(textView);
        textView.setTag(R.id.selecthelper_title, title);
        textView.setTag(R.id.selecthelper_author,author);
        textView.setText(textView.getText(), TextView.BufferType.SPANNABLE);

        textView.setOnTouchListener(touchListener);
        textView.setOnLongClickListener(longClickListener);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSelectionInfo();
                hideSelectView();
            }
        });
    }


    public void registerTextView(TextView textView){
        mTextViewList.add(textView);
        textView.setText(textView.getText(), TextView.BufferType.SPANNABLE);

        textView.setOnTouchListener(touchListener);
        textView.setOnLongClickListener(longClickListener);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSelectionInfo();
                hideSelectView();
            }
        });
    }

    public void registerTextView(List<TextView> textViewList){
        for(TextView textView:textViewList){
            mTextViewList.add(textView);
            textView.setText(textView.getText(), TextView.BufferType.SPANNABLE);

            textView.setOnTouchListener(touchListener);
            textView.setOnLongClickListener(longClickListener);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetSelectionInfo();
                    hideSelectView();
                }
            });
        }
    }
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mTouchX = (int) event.getX();
            mTouchY = (int) event.getY();
            return false;
        }
    };

    private View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if(null != mTextView && v != mTextView){
                Log.e("zj test","LongClick case 1");
                //清空选中状态
                mSelectionInfo = new SelectionInfo();

                mTextView = (TextView) v;
                init();
                showSelectView(mTouchX, mTouchY);
            }else if (null == mTextView){
                Log.e("zj test","LongClick case 2");

                //首次选中
                mTextView = (TextView) v;
                init();
                showSelectView(mTouchX, mTouchY);
            }else if(null !=mTextView && v == mTextView){
                Log.e("zj test","LongClick case 3");
                //操作同一个TextView
                showSelectView(mTouchX,mTouchY);
            }
            return true;
        }
    };

    private void init(){
        if(null != mSelectTextOprateWindow)
            mSelectTextOprateWindow.dismiss();

        mSelectTextOprateWindow = new TooltipSelectTextOprateWindow(mTextView.getContext(),
                SelectTextHelper.this,rootView);

        mTextView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                destroy();
            }
        });

        mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (isHideWhenScroll) {
                    isHideWhenScroll = false;
                    postShowSelectView(DEFAULT_SHOW_DURATION);
                }
                return true;
            }
        };
        mTextView.getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);

        mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (!isHideWhenScroll && !isHide) {
                    isHideWhenScroll = true;
                    if(mSelectTextOprateWindow != null){
                        mSelectTextOprateWindow.dismiss();
                    }
                    if (mStartHandle != null) {
                        mStartHandle.dismiss();
                    }
                    if (mEndHandle != null) {
                        mEndHandle.dismiss();
                    }
                }
            }
        };
        mTextView.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener);
    }

    private void postShowSelectView(int duration) {
        mTextView.removeCallbacks(mShowSelectViewRunnable);
        if (duration <= 0) {
            mShowSelectViewRunnable.run();
        } else {
            mTextView.postDelayed(mShowSelectViewRunnable, duration);
        }
    }


    private final Runnable mShowSelectViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (isHide) return;
            if (mStartHandle != null) {
                showCursorHandle(mStartHandle);
            }
            if (mEndHandle != null) {
                showCursorHandle(mEndHandle);
            }
            if(mSelectTextOprateWindow != null){
                while (!mStartHandle.getmPopupWindow().isShowing()){

                }
                mSelectTextOprateWindow.show();
            }
        }
    };

    private void showSelectView(int x, int y) {
        hideSelectView();
        resetSelectionInfo();
        isHide = false;
        if (mStartHandle == null) mStartHandle = new CursorHandle(mTextView.getContext(),
                TextLayoutUtil.dp2px(mTextView.getContext(), 30),
                Color.parseColor("#e46042"),
                true,SelectTextHelper.this);
        if (mEndHandle == null) mEndHandle = new CursorHandle(mTextView.getContext(),
                TextLayoutUtil.dp2px(mTextView.getContext(), 30),
                Color.parseColor("#e46042"),
                false,SelectTextHelper.this);

        int startOffset = TextLayoutUtil.getPreciseOffset(mTextView, x, y);
        int endOffset = startOffset + DEFAULT_SELECTION_LENGTH;
        if (mTextView.getText() instanceof Spannable) {
            mSpannable = (Spannable) mTextView.getText();
        }
        if (mSpannable == null || startOffset >= mTextView.getText().length()) {
            return;
        }
        selectText(startOffset, endOffset);
        showCursorHandle(mStartHandle);
        showCursorHandle(mEndHandle);

        while (!mStartHandle.getmPopupWindow().isShowing()){
            ;
        }
        mSelectTextOprateWindow.show();
    }

    private void showCursorHandle(CursorHandle cursorHandle) {
        Layout layout = mTextView.getLayout();
        int offset = cursorHandle.isLeft ? mSelectionInfo.mStart : mSelectionInfo.mEnd;
        cursorHandle.show((int) layout.getPrimaryHorizontal(offset),
                cursorHandle.isLeft ? layout.getLineTop(layout.getLineForOffset(offset))
                        : layout.getLineBottom(layout.getLineForOffset(offset)));
    }

    public void destroy() {
        mTextView.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
        mTextView.getViewTreeObserver().removeOnPreDrawListener(mOnPreDrawListener);
        resetSelectionInfo();
        hideSelectView();
        mStartHandle = null;
        mEndHandle = null;
        mSelectTextOprateWindow = null;
    }

    public void clearSelectText(){
        resetSelectionInfo();
        hideSelectView();
    }


    public void resetSelectionInfo() {
        mSelectionInfo.mSelectionContent = null;
        if (mSpannable != null && mSpan != null) {
            mSpannable.removeSpan(mSpan);
            mSpan = null;
        }
    }

    public void selectText(int startPos, int endPos) {
        if (startPos != -1) {
            mSelectionInfo.mStart = startPos;
        }
        if (endPos != -1) {
            mSelectionInfo.mEnd = endPos;
        }
        if (mSelectionInfo.mStart > mSelectionInfo.mEnd) {
            int temp = mSelectionInfo.mStart;
            mSelectionInfo.mStart = mSelectionInfo.mEnd;
            mSelectionInfo.mEnd = temp;
        }

        if (mSpannable != null) {
            if (mSpan == null) {
                mSpan = new BackgroundColorSpan(Color.parseColor("#FFE8E4"));
            }
            mSelectionInfo.mSelectionContent = mSpannable.subSequence(mSelectionInfo.mStart, mSelectionInfo.mEnd).toString();
            mSpannable.setSpan(mSpan, mSelectionInfo.mStart, mSelectionInfo.mEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            /*if (mSelectListener != null) {
                mSelectListener.onTextSelected(mSelectionInfo.mSelectionContent);
            }*/
        }
    }

    public void hideSelectView(){
        isHide = true;
        if (mStartHandle != null) {
            mStartHandle.dismiss();
        }
        if (mEndHandle != null) {
            mEndHandle.dismiss();
        }
        if(mSelectTextOprateWindow != null){
            mSelectTextOprateWindow.dismiss();
        }
    }

    public CursorHandle getCursorHandle(boolean isLeft) {
        if (mStartHandle.isLeft == isLeft) {
            return mStartHandle;
        } else {
            return mEndHandle;
        }
    }

    public class SelectionInfo {
        public int mStart;
        public int mEnd;
        public String mSelectionContent;
    }
}