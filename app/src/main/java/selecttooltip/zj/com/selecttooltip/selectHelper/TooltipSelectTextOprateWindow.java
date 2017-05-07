package selecttooltip.zj.com.selecttooltip.selectHelper;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import selecttooltip.zj.com.selecttooltip.R;
import selecttooltip.zj.com.selecttooltip.SelectTooltip;

/**
 * Created by zhangjun on 17/3/20.
 */

public class TooltipSelectTextOprateWindow extends SelectTooltip implements ISelectTextOprateWindow {

    private Context mContext;
    private SelectTextHelper mSelectTextHelper;
    private int[] mTempCoors = new int[2];

    private View contentView;
    private ViewGroup rootView;

    public TooltipSelectTextOprateWindow(@NonNull Context context, SelectTextHelper selectTextHelpter,ViewGroup root) {
        super(context);
        this.mContext = context;
        this.mSelectTextHelper = selectTextHelpter;
        this.rootView = root;

        contentView = LayoutInflater.
                from(context).inflate(R.layout.select_text_oprate_window, null);

        this.setContentView(contentView);
        this.setAlign(SelectTooltip.AlignScreenCenter);
        this.setDismissOnTouchOutSide(true);

        attachTo(root);
    }

    public void attachTo(ViewGroup root) {
        super.attachTo(root);
    }

    @Override
    public void show() {
        this.showAsVerticalArrow(mSelectTextHelper.mTextView);
//        mSelectTextHelper.mTextView.getLocationInWindow(mTempCoors);
//        this.showAsVerticalArrow(new Rect(mTempCoors[0], mTempCoors[1], mTempCoors[0], mTempCoors[1]));
    }

    @Override
    public void update() {

    }
}
