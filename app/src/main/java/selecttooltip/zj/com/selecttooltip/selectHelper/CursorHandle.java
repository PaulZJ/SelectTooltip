package selecttooltip.zj.com.selecttooltip.selectHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.Layout;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import selecttooltip.zj.com.selecttooltip.R;

/**
 * Created by zhangjun on 17/3/20.
 */

public class CursorHandle extends View {
    private PopupWindow mPopupWindow;
    private Paint mPaint;
    private SelectTextHelper selectTextHelper;

    private int mCircleRadius ;
    private int mWidth;
    private int mHeight;
    private int mPadding = 25;
    public boolean isLeft;

    private Bitmap bitmap;
    private Bitmap bitmap2;
    public CursorHandle(Context context, int cursorHandleSize, int mCursorHandleColor, boolean isLeft,
                        SelectTextHelper helper) {
        super(context);
        mCircleRadius = cursorHandleSize / 2;
        this.isLeft = isLeft;
        this.selectTextHelper = helper;

        mWidth = mCircleRadius * 2;
        mHeight = mCircleRadius * 2;

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cursor_icon);
        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        float scale = 0;
        if(bitmap.getWidth()< bitmap.getHeight()){
            scale = ((float) cursorHandleSize)/bitmap.getHeight();
        }else {
            scale = ((float) cursorHandleSize)/bitmap.getWidth();
        }
        matrix.postScale(scale,scale);
        bitmap2 = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),
                matrix,true);
        matrix = new Matrix();
        matrix.postRotate(180);
        bitmap = Bitmap.createBitmap(bitmap2,0,0,bitmap2.getWidth(),bitmap2.getHeight(),matrix,true);

        mHeight = bitmap.getHeight();
        mWidth = bitmap.getWidth();
        mCircleRadius = mHeight/2;


        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mCursorHandleColor);
        mPopupWindow = new PopupWindow(this);
        mPopupWindow.setClippingEnabled(false);
        mPopupWindow.setWidth(mWidth + mPadding * 2);
        mPopupWindow.setHeight(mHeight + mPadding / 2);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(isLeft?bitmap2:bitmap,0,0,new Paint());
    }

    private int mAdjustX;
    private int mAdjustY;

    private int mBeforeDragStart;
    private int mBeforeDragEnd;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mBeforeDragStart = selectTextHelper.mSelectionInfo.mStart;
                mBeforeDragEnd = selectTextHelper.mSelectionInfo.mEnd;
                mAdjustX = (int) event.getX();
                mAdjustY = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                int rawX = (int) event.getRawX();
                int rawY = (int) event.getRawY();
                update(rawX + mAdjustX - mWidth, rawY + mAdjustY - mHeight);
                break;
        }
        return true;
    }

    /**游标 左右位置切换*/
    private void changeDirection() {
        isLeft = !isLeft;
        invalidate();
    }

    public PopupWindow getmPopupWindow(){
        return this.mPopupWindow;
    }

    public void dismiss() {
        mPopupWindow.dismiss();
    }

    private int[] mTempCoors = new int[2];

    /**
     * 位置更新
     * */
    public void update(int x, int y) {
        selectTextHelper.mTextView.getLocationInWindow(mTempCoors);
        int oldOffset;
        if (isLeft) {
            oldOffset = selectTextHelper.mSelectionInfo.mStart;
        } else {
            oldOffset = selectTextHelper.mSelectionInfo.mEnd;
        }

        y -= mTempCoors[1];

        int offset = TextLayoutUtil.getHysteresisOffset(selectTextHelper.mTextView, x, y, oldOffset);

        if (offset != oldOffset) {
            selectTextHelper.resetSelectionInfo();
            if (isLeft) {
                if (offset > mBeforeDragEnd) {
                    CursorHandle handle = selectTextHelper.getCursorHandle(false);
                    changeDirection();
                    handle.changeDirection();
                    mBeforeDragStart = mBeforeDragEnd;
                    selectTextHelper.selectText(mBeforeDragEnd, offset);
                    handle.updateCursorHandle();
                } else {
                    selectTextHelper.selectText(offset, -1);
                }
                updateCursorHandle();
                selectTextHelper.mSelectTextOprateWindow.update();
            } else {
                if (offset < mBeforeDragStart) {
                    CursorHandle handle = selectTextHelper.getCursorHandle(true);
                    changeDirection();
                    handle.changeDirection();
                    mBeforeDragEnd = mBeforeDragStart;
                    selectTextHelper.selectText(offset, mBeforeDragStart);
                    handle.updateCursorHandle();
                } else {
                    selectTextHelper.selectText(mBeforeDragStart, offset);
                }
                updateCursorHandle();
                selectTextHelper.mSelectTextOprateWindow.update();
            }
        }
    }

    /**
     * 游标 刷新
     * */
    private void updateCursorHandle() {
        selectTextHelper.mTextView.getLocationInWindow(mTempCoors);
        Layout layout = selectTextHelper.mTextView.getLayout();
        if (isLeft) {
            mPopupWindow.update((int) layout.getPrimaryHorizontal(selectTextHelper.mSelectionInfo.mStart)
                            - mWidth*0 + getExtraX(),
                    layout.getLineTop(layout.getLineForOffset(selectTextHelper.mSelectionInfo.mStart))
                            -2*mCircleRadius+ getExtraY(), -1, -1);
        } else {
            mPopupWindow.update((int) layout.getPrimaryHorizontal(selectTextHelper.mSelectionInfo.mEnd)
                            + getExtraX(),
                    layout.getLineBottom(layout.getLineForOffset(selectTextHelper.mSelectionInfo.mEnd))
                            + getExtraY(), -1, -1);
        }
    }

    public void show(int x, int y) {
        selectTextHelper.mTextView.getLocationInWindow(mTempCoors);
        int offset = isLeft ? mWidth*0 : 0;
        int offetY = isLeft?2*mCircleRadius:0;
        mPopupWindow.showAtLocation(selectTextHelper.mTextView, Gravity.NO_GRAVITY,
                x - offset + getExtraX(), y -offetY + getExtraY());
    }

    public int getExtraX() {
        return mTempCoors[0] - mPadding + selectTextHelper.mTextView.getPaddingLeft();
    }

    public int getExtraY() {
        return mTempCoors[1] + selectTextHelper.mTextView.getPaddingTop();
    }

}