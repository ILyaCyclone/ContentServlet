package ru.unisuite.contentservlet;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

class NameCreator {

	final static private String HASH_ALGORITHM = "MD5";
	final static private String CHARSET = "UTF-8";

	public String createWithParameters(final RequestParameters requestParameters) {
		StringBuilder builder = new StringBuilder();
		if (requestParameters.getWebMetaId() != null)
			builder.append(String.format("wm=%s-", requestParameters.getWebMetaId()));

		if (requestParameters.getWebMetaAlias() != null)
			builder.append(String.format("wmAlias=%s-", requestParameters.getWebMetaAlias()));

		if (requestParameters.getFileVersionId() != null)
			builder.append(String.format("fw=%s-", requestParameters.getFileVersionId()));

		if (requestParameters.getClientId() != null)
			builder.append(String.format("cid=%s-", requestParameters.getClientId()));

		if (requestParameters.getEntryIdInPhotoalbum() != null)
			builder.append(String.format("entInPhAl=%s-", requestParameters.getEntryIdInPhotoalbum()));

		if (requestParameters.getWidth() != null)
			builder.append(String.format("width=%s-", requestParameters.getWidth()));

		if (requestParameters.getHeight() != null)
			builder.append(String.format("height=%s-", requestParameters.getHeight()));

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
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	public String encodeToBase64(final String stringToEncode) {

		return Base64.getEncoder().encodeToString(stringToEncode.getBytes()); // java 8

	}

	public String decodeFromBase64(final String stringToDecode) {

		return new String(Base64.getDecoder().decode(stringToDecode));

	}
}
