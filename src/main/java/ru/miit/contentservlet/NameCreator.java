package ru.miit.contentservlet;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

public class NameCreator {

	final static private String HASH_ALGORITHM = "MD5";
	final static private String CHARSET = "UTF-8";

	public String createWithParameters(final RequestParameters requestParameters) {
		StringBuilder builder = new StringBuilder();
		if (requestParameters.getWebMetaId() != null)
			builder.append("wm=").append(requestParameters.getWebMetaId()).append("-");

		if (requestParameters.getFileVersionId() != null)
			builder.append("fw=").append(requestParameters.getFileVersionId()).append("-");

		if (requestParameters.getClientId() != null)
			builder.append("cid=").append(requestParameters.getClientId()).append("-");

		if (requestParameters.getEntryIdInPhotoalbum() != null)
			builder.append("entInPhAl=").append(requestParameters.getEntryIdInPhotoalbum()).append("-");

		if (requestParameters.getWidth() != null)
			builder.append("width=").append(requestParameters.getWidth()).append("-");

		if (requestParameters.getHeight() != null)
			builder.append("height=").append(requestParameters.getHeight()).append("-");

		return builder.toString();
	}

	public String createNameByURL(final HttpServletRequest request) {

		return request.getQueryString();
	}

	public String createNameByURLEncoder(final HttpServletRequest request) throws UnsupportedEncodingException {

		String url = request.getQueryString();
		URLEncoder.encode(url, CHARSET);

		return url;
	}

	public String md5(final String s) {
		try {
			MessageDigest m = MessageDigest.getInstance(HASH_ALGORITHM);
			m.update(s.getBytes(CHARSET));
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			return bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError();
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError();
		}
	}

	public String encodeToBase64(final String stringToEncode) {

		return Base64.getEncoder().encodeToString(stringToEncode.getBytes()); // java 8

	}

	public String decodeFromBase64(final String stringToDecode) {

		return new String(Base64.getDecoder().decode(stringToDecode));

	}
}
