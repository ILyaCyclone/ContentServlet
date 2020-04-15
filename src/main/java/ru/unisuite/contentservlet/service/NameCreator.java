package ru.unisuite.contentservlet.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NameCreator {

//	final static private String HASH_ALGORITHM = "MD5";
//	final static private String CHARSET = "UTF-8";

    // attributes do not affect result file
    private Set<String> ignoredAttributes = new HashSet<>(Arrays.asList("contentType", "contentDisposition", "noCache"));

    public String forContentRequest(ContentRequest contentRequest) {
        return contentRequest.values().entrySet().stream()
                .filter(entry -> !ignoredAttributes.contains(entry.getKey()))
                .map(entry -> entry.getKey()+"="+entry.getValue())
                .collect(Collectors.joining());
    }


//	public String createNameByURL(final HttpServletRequest request) {
//
//		return request.getQueryString();
//	}
//
//	public String createNameByURLEncoder(final HttpServletRequest request) throws UnsupportedEncodingException {
//
//		String url = request.getQueryString();
//		URLEncoder.encode(url, CHARSET);
//
//		return url;
//	}
//
//	public String md5(final String s) {
//		try {
//			MessageDigest m = MessageDigest.getInstance(HASH_ALGORITHM);
//			m.update(s.getBytes(CHARSET));
//			byte[] digest = m.digest();
//			BigInteger bigInt = new BigInteger(1, digest);
//			return bigInt.toString(16);
//		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
//			throw new AssertionError(e);
//		}
//	}
//
//	public String encodeToBase64(final String stringToEncode) {
//
//		return Base64.getEncoder().encodeToString(stringToEncode.getBytes()); // java 8
//
//	}
//
//	public String decodeFromBase64(final String stringToDecode) {
//
//		return new String(Base64.getDecoder().decode(stringToDecode));
//
//	}
}
