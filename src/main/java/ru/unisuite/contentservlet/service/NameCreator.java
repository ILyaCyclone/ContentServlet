package ru.unisuite.contentservlet.service;

public class NameCreator {

//	final static private String HASH_ALGORITHM = "MD5";
//	final static private String CHARSET = "UTF-8";

    public String forContentRequest(ContentRequest contentRequest) {
        String name = contentRequest.toString();
        name = name.replaceFirst("ContentRequest\\{", "")
                .replace("\\}", "")
                .replaceAll(", ", "");
        return name;
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
