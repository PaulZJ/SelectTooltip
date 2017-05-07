package selecttooltip.zj.com.selecttooltip;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import selecttooltip.zj.com.selecttooltip.selectHelper.SelectTextHelper;

/**
 * Created by zhangjun on 17/3/20.
 */

public class MainActivity extends AppCompatActivity {

    private ViewGroup layout;

    private SelectTextHelper mSelectTextHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (ViewGroup) findViewById(R.id.layout);
        TextView tv1 = (TextView) findViewById(R.id.tv_01);
        TextView tv2 = (TextView) findViewById(R.id.tv_02);

        mSelectTextHelper = new SelectTextHelper(layout);
        mSelectTextHelper.registerTextView(tv1);
        mSelectTextHelper.registerTextView(tv2);


    }
}
