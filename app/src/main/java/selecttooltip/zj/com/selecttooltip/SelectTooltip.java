package selecttooltip.zj.com.selecttooltip;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by zhangjun on 17/3/17.
 */

public class SelectTooltip extends FrameLayout {

    public static final int AlignAimCenter = 0;
    public static final int AlignScreenCenter = 1;

    private ViewGroup layout;
    private View topArrow;
    private View leftArrow;
    private ViewGroup frame;
    private View rightArrow;
    private View bottomArrow;

    private int arrowSize;
    private int arrowSizeHalf;
    private int sysBarHeight;
    private int cornerRadius;
    private int align = AlignAimCenter;
    private boolean dismissOnTouchOutSide;

    public SelectTooltip(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SelectTooltip(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public SelectTooltip(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_tooltip, this);
        arrowSize = context.getResources().getDimensionPixelSize(R.dimen.tooltip_arrow_size);
        arrowSizeHalf = context.getResources().getDimensionPixelSize(R.dimen.tooltip_arrow_size_half);
        cornerRadius = context.getResources().getDimensionPixelSize(R.dimen.tooltip_frame_corner_radius);

        setVisibility(GONE);
        layout = (ViewGroup) findViewById(R.id.tooltip_layout);
        topArrow = layout.findViewById(R.id.tooltip_top_arrow);
        leftArrow = layout.findViewById(R.id.tooltip_left_arrow);
        frame = (ViewGroup) layout.findViewById(R.id.tooltip_frame);
        rightArrow = layout.findViewById(R.id.tooltip_right_arrow);
        bottomArrow = layout.findViewById(R.id.tooltip_bottom_arrow);
    }

    public void setContentView(View contentView){
        frame.addView(contentView);
    }

    public void attachTo(ViewGroup root){
        ViewParent parent = getParent();
        if (parent!=null) ((ViewGroup)parent).removeView(this);
        root.addView(this, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setDismissOnTouchOutSide(boolean dismissOnTouchOutSide) {
        this.dismissOnTouchOutSide = dismissOnTouchOutSide;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!dismissOnTouchOutSide) return false;
        if (event.getAction()==MotionEvent.ACTION_UP
                && isShowing()) dismiss();
        return true;
    }

    public void setAlign(int align){
        this.align = align;
    }

    public boolean isShowing() {
        return getVisibility()==VISIBLE;
    }

    public void dismiss() {
        setVisibility(GONE);
    }

    /** 弹出垂直方向箭头的tooltp
     * @param aim 被标志对象
     */
    public void showAsHorizontalArrow(View aim) {
        int[] aimPoint = new int[2];
        aim.getLocationOnScreen(aimPoint);
        Rect aimScreenLocation = new Rect();
        aimScreenLocation.left = aimPoint[0];
        aimScreenLocation.top = aimPoint[1];
        aimScreenLocation.right = aimPoint[0]+aim.getWidth();
        aimScreenLocation.bottom = aimPoint[1]+aim.getHeight();
        showAsHorizontalArrow(aimScreenLocation);
    }

    /**
     * 弹出垂直方向箭头的tooltip
     * @param aimScreenLocation 被标志对象在屏幕的位置
     */
    public void showAsVerticalArrow(Rect aimScreenLocation) {
        //屏幕大小
        Rect displayFrame = new Rect();
        layout.getWindowVisibleDisplayFrame(displayFrame);
        sysBarHeight = displayFrame.top;
        int winWidth = displayFrame.right;
        int winHeight = displayFrame.bottom;
        int winCenterX = winWidth/2;

        int[] rootLocation = new int[2];
        getLocationOnScreen(rootLocation);

        //计算内容宽高，与tooltip宽高
        frame.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int frameWidth = frame.getMeasuredWidth();
        int frameHeight = frame.getMeasuredHeight();
        int layoutWidth = frameWidth;
        int layoutHeight = frameHeight+arrowSizeHalf;

        //根据屏幕上下空间大小，计算是展示上箭头还是下箭头
        int topSpace = aimScreenLocation.top;
        int bottomSpace = winHeight-aimScreenLocation.bottom;
        boolean isTopArrow = topSpace<bottomSpace;
        if (isTopArrow) {
            topArrow.setVisibility(View.VISIBLE);
            bottomArrow.setVisibility(View.GONE);
        } else {
            topArrow.setVisibility(View.GONE);
            bottomArrow.setVisibility(View.VISIBLE);
        }
        leftArrow.setVisibility(GONE);
        rightArrow.setVisibility(GONE);

        //计算tooltip上下坐标位置
        Rect layoutLocation = new Rect();
        layoutLocation.top = isTopArrow?aimScreenLocation.bottom:(aimScreenLocation.top-layoutHeight);
        layoutLocation.bottom = layoutLocation.top+layoutHeight;
        if (layoutLocation.bottom<sysBarHeight){
            layoutLocation.top = sysBarHeight;
            layoutLocation.bottom = layoutLocation.top+layoutHeight;
        } else if (layoutLocation.top>winHeight){
            layoutLocation.bottom = winHeight;
            layoutLocation.top = layoutLocation.bottom-layoutHeight;
        }

        //计算箭头位置,tooltip左坐标位置
        int aimCenterX = aimScreenLocation.left+aimScreenLocation.width()/2;
        switch (align){
            case AlignAimCenter: //与目标中心对齐
                layoutLocation.left = aimCenterX-layoutWidth/2;
                if (aimCenterX<=winCenterX && layoutLocation.left<0) //目标在左半屏并且气泡左部被遮住，气泡右移
                    layoutLocation.left = 0;
                else if (aimCenterX>winCenterX && layoutLocation.left>winWidth-layoutWidth) //目标在右半屏并且气泡右部被遮住，气泡左移
                    layoutLocation.left = winWidth-layoutWidth;
                break;
            case AlignScreenCenter: //与屏幕中心对齐
                layoutLocation.left = (winWidth-layoutWidth)/2;
                int arrowAbsoluteLeft = aimCenterX - arrowSize/2;
                if (aimCenterX<=winCenterX){ //目标在左半屏
                    int minArrowAbsoluteLeft = cornerRadius;
                    if (arrowAbsoluteLeft<minArrowAbsoluteLeft) arrowAbsoluteLeft = minArrowAbsoluteLeft; //箭头左部被遮住，箭头右移
                    int maxLayoutLeft = arrowAbsoluteLeft-cornerRadius;
                    if (layoutLocation.left>maxLayoutLeft) layoutLocation.left = maxLayoutLeft;
                    if (layoutLocation.left<0) layoutLocation.left = 0;
                } else { //目标在右半屏
                    int maxArrowAbsoluteLeft = winWidth-cornerRadius-arrowSize;
                    if (arrowAbsoluteLeft>maxArrowAbsoluteLeft) arrowAbsoluteLeft = maxArrowAbsoluteLeft; //箭头右部被遮住，箭头左移
                    int minLayoutLeft = arrowAbsoluteLeft+arrowSize+cornerRadius-layoutWidth;
                    if (layoutLocation.left<minLayoutLeft) layoutLocation.left = minLayoutLeft;
                    if (layoutLocation.left>winWidth-layoutWidth) layoutLocation.left = winWidth-layoutWidth;
                }
                break;
        }
        int arrowRelateLeft = aimCenterX-layoutLocation.left-arrowSize/2;
        int minArrowRelateLeft = cornerRadius;
        int maxArrowRelateRight = layoutWidth-arrowSize-cornerRadius;
        if (arrowRelateLeft>maxArrowRelateRight) arrowRelateLeft = maxArrowRelateRight;
        if (arrowRelateLeft<minArrowRelateLeft) arrowRelateLeft = minArrowRelateLeft;
        View arrow = isTopArrow?topArrow:bottomArrow;
        LinearLayout.LayoutParams arrowLp = (LinearLayout.LayoutParams) arrow.getLayoutParams();
        arrowLp.leftMargin = arrowRelateLeft;
        arrow.setLayoutParams(arrowLp);

        //布局
        FrameLayout.LayoutParams layoutLp = (FrameLayout.LayoutParams) layout.getLayoutParams();
        layoutLp.leftMargin = layoutLocation.left;
        layoutLp.topMargin = layoutLocation.top-rootLocation[1];
        layout.setLayoutParams(layoutLp);

        setVisibility(VISIBLE);
    }


    /**
     * 弹出垂直方向箭头的tooltp
     * @param aim 被标志对象
     */
    public void showAsVerticalArrow(View aim) {
        int[] aimPoint = new int[2];
        aim.getLocationOnScreen(aimPoint);
        Rect aimScreenLocation = new Rect();
        aimScreenLocation.left = aimPoint[0];
        aimScreenLocation.top = aimPoint[1];
        aimScreenLocation.right = aimPoint[0]+aim.getWidth();
        aimScreenLocation.bottom = aimPoint[1]+aim.getHeight();
        showAsVerticalArrow(aimScreenLocation);
    }

    /**
     * 弹出水平方向箭头的tooltip
     * @param aimScreenLocation 被标志对象在屏幕的位置
     */
    public void showAsHorizontalArrow(Rect aimScreenLocation) {
        //屏幕大小
        Rect displayFrame = new Rect();
        layout.getWindowVisibleDisplayFrame(displayFrame);
        sysBarHeight = displayFrame.top;
        int winWidth = displayFrame.right;
        int winHeight = displayFrame.bottom;
        int winCenterY = (winHeight-sysBarHeight)/2+sysBarHeight;

        //计算内容宽高，与tooltip宽高
        frame.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int frameWidth = frame.getMeasuredWidth();
        int frameHeight = frame.getMeasuredHeight();
        int layoutWidth = frameWidth+arrowSizeHalf;
        int layoutHeight = frameHeight;

        //根据屏幕左右空间大小，计算是展示左箭头还是右箭头
        int leftSpace = aimScreenLocation.left;
        int rightSpace = winWidth-aimScreenLocation.right;
        boolean isLeftArrow = leftSpace<rightSpace;
        if (isLeftArrow) {
            leftArrow.setVisibility(View.VISIBLE);
            rightArrow.setVisibility(View.GONE);
        } else {
            leftArrow.setVisibility(View.GONE);
            rightArrow.setVisibility(View.VISIBLE);
        }
        topArrow.setVisibility(GONE);
        bottomArrow.setVisibility(GONE);

        //计算tooltip左右坐标位置
        Rect layoutLocation = new Rect();
        layoutLocation.left = isLeftArrow?aimScreenLocation.right:(aimScreenLocation.left-layoutWidth);
        layoutLocation.right = layoutLocation.top+layoutWidth;
        if (layoutLocation.left<0){
            layoutLocation.left = 0;
            layoutLocation.right = layoutLocation.left+layoutWidth;
        } else if (layoutLocation.left>winWidth){
            layoutLocation.right = winWidth;
            layoutLocation.left = layoutLocation.right-layoutWidth;
        }

        //计算箭头位置,tooltip上下坐标位置
        int aimCenterY = aimScreenLocation.top+aimScreenLocation.height()/2;
        switch (align){
            case AlignAimCenter: //与目标中心对齐
                layoutLocation.top = aimCenterY-layoutHeight/2;
                if (aimCenterY<=winCenterY && layoutLocation.top<sysBarHeight) //目标在上半屏并且气泡顶部被遮住，气泡下移
                    layoutLocation.top = sysBarHeight;
                else if (aimCenterY>winCenterY && layoutLocation.top>winHeight-layoutHeight) //目标在下半屏并且气泡底部被遮住，气泡上移
                    layoutLocation.top = winHeight-layoutHeight;
                break;
            case AlignScreenCenter: //与屏幕中心对齐
                layoutLocation.top = (winHeight-sysBarHeight-layoutHeight)/2+sysBarHeight;
                int arrowAbsoluteTop = aimCenterY - arrowSize/2;
                if (aimCenterY<=winCenterY) { //目标在上半屏
                    int minArrowAbsoluteTop = sysBarHeight+cornerRadius;
                    if (arrowAbsoluteTop<minArrowAbsoluteTop) arrowAbsoluteTop = minArrowAbsoluteTop; //箭头顶部被遮住，箭头下移
                    int maxLayoutTop = arrowAbsoluteTop-cornerRadius;
                    if (layoutLocation.top>maxLayoutTop) layoutLocation.top = maxLayoutTop;
                    if (layoutLocation.top<0) layoutLocation.top = 0;
                } else if (aimCenterY>winCenterY) {//目标在下半屏
                    int maxArrowAbsoluteTop = winHeight-arrowSize-cornerRadius;
                    if(arrowAbsoluteTop>maxArrowAbsoluteTop) arrowAbsoluteTop = maxArrowAbsoluteTop; //箭头底部被遮住，箭头下移
                    int minLayoutTop = arrowAbsoluteTop+arrowSize+cornerRadius-layoutHeight;
                    if (layoutLocation.top<minLayoutTop) layoutLocation.top = minLayoutTop;
                    if (layoutLocation.top>winHeight-layoutHeight) layoutLocation.top = winHeight-layoutHeight;
                }
                break;
        }
        int arrowRelateTop = aimCenterY-layoutLocation.top-arrowSize/2;
        int minArrowRelateTop = cornerRadius;
        int maxArrowRelatetop = layoutHeight-arrowSize-cornerRadius;
        if (arrowRelateTop>maxArrowRelatetop) arrowRelateTop = maxArrowRelatetop;
        if (arrowRelateTop<minArrowRelateTop) arrowRelateTop = minArrowRelateTop;
        View arrow = isLeftArrow?leftArrow:rightArrow;
        LinearLayout.LayoutParams arrowLp = (LinearLayout.LayoutParams) arrow.getLayoutParams();
        arrowLp.topMargin = arrowRelateTop;
        arrow.setLayoutParams(arrowLp);

        //布局
        FrameLayout.LayoutParams layoutLp = (FrameLayout.LayoutParams) layout.getLayoutParams();
        layoutLp.leftMargin = layoutLocation.left;
        layoutLp.topMargin = layoutLocation.top-sysBarHeight;
        layout.setLayoutParams(layoutLp);

        setVisibility(VISIBLE);
    }


}
