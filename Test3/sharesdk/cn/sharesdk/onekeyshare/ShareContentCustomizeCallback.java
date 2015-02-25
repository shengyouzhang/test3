package cn.sharesdk.onekeyshare;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;

/**
 * @description：设置用于分享过程中，根据不同平台自定义分享内容的回调
 * @author samy
 * @date 2014年6月25日 下午8:48:51
 */
public interface ShareContentCustomizeCallback {

	public void onShare(Platform platform, ShareParams paramsToShare);

}
