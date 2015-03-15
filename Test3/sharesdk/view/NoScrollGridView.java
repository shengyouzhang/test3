package view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * @description：设置不能滑动的的GridView
 * @date 2014年12月17日 下午8:26:01
 */
public class NoScrollGridView extends GridView {

	public NoScrollGridView(Context context) {
		super(context);
	}

	public NoScrollGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoScrollGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	// 通过重新dispatchTouchEvent方法来禁止滑动
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_MOVE) { return true;// 禁止Gridview进行滑动
		}
		return super.dispatchTouchEvent(ev);
	}
}
