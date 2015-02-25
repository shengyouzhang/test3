package cn.sharesdk.onekeyshare;

import java.util.HashMap;

import android.os.Handler;
import android.os.Message;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

public class OneKeyShareCallback implements PlatformActionListener {
	private Handler mHandler;

	public OneKeyShareCallback(Handler mHandler) {
		super();
		this.mHandler = mHandler;
	}

	public void onComplete(Platform platform, int action, HashMap<String, Object> res) {
		Message m = mHandler.obtainMessage();
		m.what = OnekeyShareTool.SHARE_SUCCESS;
		mHandler.sendMessage(m);
	}

	public void onError(Platform platform, int action, Throwable t) {
		Message msg = mHandler.obtainMessage();
		msg.what = OnekeyShareTool.SHARE_FAIL;
		msg.obj = t;
		msg.arg1 = action;
		mHandler.sendMessage(msg);
	}

	public void onCancel(Platform platform, int action) {
		Message m = mHandler.obtainMessage();
		m.what = OnekeyShareTool.SHARE_CANCEL;
		mHandler.sendMessage(m);
	}
}