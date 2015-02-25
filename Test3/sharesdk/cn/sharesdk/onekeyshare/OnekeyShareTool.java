package cn.sharesdk.onekeyshare;

import android.content.Context;
import android.os.Handler;

public class OnekeyShareTool {
	public static final int SHARE_SUCCESS = 1;
	public static final int SHARE_FAIL = 2;
	public static final int SHARE_CANCEL = 3;

	/**
	 * context 上下文对象
	 * silent 是否直接分享
	 * title 标题，在印象笔记、邮箱、信息、微信（包括好友、朋友圈和收藏）、 易信（包括好友、朋友圈）、人人网和QQ空间使用，否则可以不提供
	 * titleUrl titleUrl是标题的网络链接，仅在人人网和QQ空间使用，否则可以不提供
	 * shareWord 提供的内容
	 * contentUrl 提供惠信的一个主链接
	 * url在微信（包括好友、朋友圈收藏）和易信（包括好友和朋友圈）中使用，否则可以不提供
	 * site是分享此内容的网站名称，仅在QQ空间使用，否则可以不提供
	 * icon 分享时Notification的图标和文字
	 * silent 是否直接分享
	 * platform 设置编辑页的初始化选中平台
	 * mHandler 回调自己写的handler
	 */
	public static void initShareInfo(Context context, String title, String titleUrl, String shareWord, String contentUrl, String url, String imageUrl, String site, String siteUrl, int icon, boolean silent, String platform, Handler mHandler) {
		OnekeyShare oks = new OnekeyShare();
		oks.setTitle(title); //
		if (null != titleUrl) {
			oks.setTitleUrl(titleUrl); // ShareSDKTools.WEBSITE_ADDR_LOADURI
		}
		oks.setText(shareWord + contentUrl); // context.getString(R.string.share_content_huixin_navigation)
		oks.setUrl(url); // ShareSDKTools.WEBSITE_ADDR_LOADURI
		oks.setImageUrl(imageUrl); // NET_IMAGE_URI
		oks.setSite(site); // "惠信"
		oks.setSiteUrl(siteUrl); // ShareSDKTools.WEBSITE_ADDR_LOADURI
		oks.setNotification(icon, "拼宝分享"); // R.drawable.app_logo_share_48
		oks.setSilent(silent);
		if (platform != null) {
			oks.setPlatform(platform);
		}
		if (null != mHandler) {
			oks.setCallback(new OneKeyShareCallback(mHandler));
		}
		oks.setShareContentCustomizeCallback(new ShareContentCustomize(context));
		// 在自动授权时可以禁用SSO方式
		oks.disableSSOWhenAuthorize();
		oks.show(context);
	}
}
