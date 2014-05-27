package wang.vehicleterminal;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;

/**
 * 应用程序启动类：显示欢迎界面并跳转到主界面
 * @author wang
 * @version 1.0
 * @created 2014-7-14
 */
public class AppStart extends Activity {
    
	private static final String TAG  = "AppStart";
	 final MyApplication ac = (MyApplication) getApplication();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        final View view = View.inflate(this, R.layout.start, null);
		LinearLayout wellcome = (LinearLayout) view.findViewById(R.id.app_start_view);
		setContentView(view);
        
		//渐变展示启动屏
		AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
		aa.setDuration(3000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation arg0) {
				redirectTo();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
			
		});
    }
    
    /**
     * 跳转到...
     */
    private void redirectTo(){        
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}