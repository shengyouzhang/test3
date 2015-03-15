package com.zsy.frame.lib.extend.net.http.volley;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;

import com.zsy.frame.lib.net.http.encrypt.RsaHelper;
import com.zsy.frame.lib.net.http.volley.app.AjaxParams;
import com.zsy.frame.lib.net.http.volley.app.helps.ParamEncryptor;
import com.zsy.frame.lib.net.http.volley.app.samy.JSONEntity;

/**
 * @description：加密请求参数处理提交
 * @author samy
 * @date 2015-3-15 下午4:28:21
 */
public class EncryptWrapAjaxParams extends AjaxParams{
//	原来的数据通过json标签返回
//	@Override
//	protected void fillParams(final MultipartEntity multipartEntity) {
//		JSONObject jsonObject = convertParamToJSON();
//		multipartEntity.addPart("json", jsonObject.toString());
//	}
//	
//	@Override
//	protected List<BasicNameValuePair> getParamsList() {
//		List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();
//		
//		JSONObject jsonObject = convertParamToJSON();
//		
//		lparams.add(new BasicNameValuePair("json", jsonObject.toString()));
//		
//		return lparams;
//	}
//
//	private JSONObject convertParamToJSON() {
//		JSONObject jsonObject = new JSONObject();
//		for (HashMap.Entry<String, String> entry : getUrlParams().entrySet()) {
//			try {
//				jsonObject.put(entry.getKey(), entry.getValue());
//			}
//			catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//		return jsonObject;
//	}
	
	/**
	 * 当前项目的密钥，应放在当前项目的Lib中的.so里面安全
	 */
	private static String PUBLIC_KEY_VALUE = "<RSAKeyValue><Modulus>wVwBKuePO3ZZbZ//gqaNuUNyaPHbS3e2v5iDHMFRfYHS/bFw+79GwNUiJ+wXgpA7SSBRhKdLhTuxMvCn1aZNlXaMXIOPG1AouUMMfr6kEpFf/V0wLv6NCHGvBUK0l7O+2fxn3bR1SkHM1jWvLPMzSMBZLCOBPRRZ5FjHAy8d378=</Modulus><Exponent>AQAB</Exponent></RSAKeyValue>";
	public static PublicKey publicKey = RsaHelper.decodePublicKeyFromXml(PUBLIC_KEY_VALUE);
	
	public EncryptWrapAjaxParams() {
		super();
		
		//添加rsa加密字段
		initEncrypt(new ParamEncryptor() {
			@Override
			public String encrypt(Object o) {
				return RsaHelper.encryptDataFromStr(o.toString(), publicKey);
			}
		}, "password","oldPassword","newPassword");
		addEncrypt("userId");
		addEncrypt("productId");
		addEncrypt("activityId");
		addEncrypt("userName");
		addEncrypt("payPwd");
		addEncrypt("phone");
		addEncrypt("amount");
	}
	
	@Override
	protected HttpEntity getCommonEntity(List<BasicNameValuePair> paramsList) throws UnsupportedEncodingException {
		return new JSONEntity(paramsList);
	}
}
