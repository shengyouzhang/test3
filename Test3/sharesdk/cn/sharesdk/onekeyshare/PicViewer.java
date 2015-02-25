package cn.sharesdk.onekeyshare;

import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import cn.sharesdk.framework.FakeActivity;

/**
 * @description：查看编辑页面中点击图片预览
 * @author samy
 * @date 2014年6月26日 下午3:02:13
 */
public class PicViewer extends FakeActivity implements OnClickListener {
	private ImageView ivViewer;
	private Bitmap pic;

	public void onCreate() {
		ivViewer = new ImageView(activity);
		ivViewer.setScaleType(ScaleType.CENTER_INSIDE);
		ivViewer.setBackgroundColor(0xc0000000);
		ivViewer.setOnClickListener(this);
		activity.setContentView(ivViewer);
		if (pic != null && !pic.isRecycled()) {
			ivViewer.setImageBitmap(pic);
		}
	}

	/**
	 * @description：设置图片用于浏览
	 * @author samy
	 * @date 2014年6月26日 下午3:02:44
	 */
	public void setImageBitmap(Bitmap pic) {
		this.pic = pic;
		if (ivViewer != null) {
			ivViewer.setImageBitmap(pic);
		}
	}

	public void onClick(View v) {
		finish();
	}

}
