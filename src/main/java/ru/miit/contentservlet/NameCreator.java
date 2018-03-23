package ru.miit.contentservlet;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

public class NameCreator {

	final static public String hashAlgorithm = "MD5";
	final static public String charset = "UTF-8";

	public String createWithParameters(final RequestParameters requestParameters) {
		String id = "";
		if (requestParameters.getWebMetaId() != null)
			id += "wm=" + requestParameters.getWebMetaId() + "-";

		if (requestParameters.getFileVersionId() != null)
			id += "fw=" + requestParameters.getFileVersionId() + "-";

		if (requestParameters.getClientId() != null)
			id += "cid=" + requestParameters.getClientId() + "-";

		if (requestParameters.getEntryIdInPhotoalbum() != null)
			id += "entInPhAl=" + requestParameters.getEntryIdInPhotoalbum() + "-";

		if (requestParameters.getWidth() != null)
			id += "width=" + requestParameters.getWidth() + "-";

		if (requestParameters.getHeight() != null)
			id += "height=" + requestParameters.getHeight() + "-";

		return id;
	}

	public String createNameByURL(final HttpServletRequest request) {

		return request.getQueryString();
	}

	public String createNameByURLEncoder(final HttpServletRequest request) throws UnsupportedEncodingException {

		String url = request.getQueryString();
		URLEncoder.encode(url, charset);

		return url;
	}

	public String md5(final String s) {
		try {
			MessageDigest m = MessageDigest.getInstance(hashAlgorithm);
			m.update(s.getBytes(charset));
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
