/*
 * Copyright (C) 2010 The MobileSecurePay Project
 * All right reserved.
 * author: shiqun.shi@alipay.com
 * 提示：如何获取安全校验码和合作身份者id
 * 1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 * 2.点击“商家服务”(https://b.alipay.com/order/myorder.htm)
 * 3.点击“查询合作者身份(pid)”、“查询安全校验码(key)”
 */

package com.alipay.android.app.lib.alipay;

//
// 请参考 Android平台安全支付服务(msp)应用开发接口(4.2 RSA算法签名)部分，并使用压缩包中的openssl RSA密钥生成工具，生成一套RSA公私钥。
// 这里签名时，只需要使用生成的RSA私钥。
// Note: 为安全起见，使用RSA私钥进行签名的操作过程，应该尽量放到商家服务器端去进行。
public final class Keys {

	// 合作身份者id，以2088开头的16位纯数字
	public static final String DEFAULT_PARTNER = "2088411130864349";

	// 收款支付宝账号
	public static final String DEFAULT_SELLER = "2356904015@qq.com";

	// 商户私钥，自助生成
	public static final String PRIVATE = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAOXBQIVlU3ji6upHs6Qmh+upBmK2kJ9j8UViV07jhql9SBJnnUg28evTKop02vGrtkd2P0pxxbyME6Pp3klFblxM6fzicg3Vx1hVZAxzTB2mE0hSjtQ5gNzPs0DHTCFFcfooXkK8TqpncEWamMJ4ISd9gi+PPhmDIgc7Y7q8yBuxAgMBAAECgYAnajbSa6adR3h7hp932rBYql+REbbP0Upz18IYo4nXi8mQdrwRxnNMPKbAp/ljmkykB9IlyEze4rz/0sAym7mpwr3D4yi3ZSvA7TG7h4d+LcmCvgRNjl2UUj4719aBNN56xDN+740VI6pFNFObt/A3itg9XswctA/t4ANckNnq0QJBAPlVvxcFtjoTOR6hL7OLRjmnEMcG2tyHsydLxGcyFndAOrLhaHnk24LM/Oi81C/uEV9R7oEDOd0RiaB29fpoP3UCQQDr5YPWBSj0o4VE3Qfv7vrHTcMFNiY/9ioMiGitjnClWkN2WNPn1b5kDEFfQTt1fXD9kH8xfUI81MRf8ibegb/NAkB3hZssthg8jqp6/FmZf9ISIPvx7F9OB97hn3hu35vVXnzE8zjZ9dMkSI+UIbC1qTG6t9PVFG7Qgm+u9FfFyeNhAkAKO7OjZifnrOxMF3aPrwNMABCUukugfLJIRuabmNFEKw1AJgxTQ092EZ4IXtEQgLeVGF6cK/3im6xFKUEMZ/6FAkBGzmn/jabnMfpvi/sCSGudlKrDb778tcxUAbQZX3zwUn1LoZBAc0k6fqxtPHZIlbGLuvEGi1Yi1kRxqGbDMe32";

	public static final String PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

}
