package com.cointiger.gateway.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;


public class SignUtil {
	
	public static String SIGN_FIELD = "sign";
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String sign(final TreeMap params) {
		if (null == params || params.isEmpty()) {
			return (String) null;
		}
		StringBuilder sb = new StringBuilder();
		for (Iterator<Entry> it = params.entrySet().iterator(); it.hasNext(); ) {
			Entry entry = it.next();
			if(SIGN_FIELD.equals(entry.getKey())){
				continue;
			}
			sb.append(entry.getKey()).append(entry.getValue() == null ? "" : entry.getValue());
		}
		return MD5(sb.toString()).toUpperCase();
	}


	public static String MD5(String sourceStr) {
	    String result = "";
	    try {
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        md.update(sourceStr.getBytes());
	        byte b[] = md.digest();
	        int i;
	        StringBuffer buf = new StringBuffer("");
	        for (int offset = 0; offset < b.length; offset++) {
	            i = b[offset];
	            if (i < 0)
	                i += 256;
	            if (i < 16)
	                buf.append("0");
	            buf.append(Integer.toHexString(i));
	        }
	        result = buf.toString();
	    } catch (NoSuchAlgorithmException e) {
	        System.out.println(e);
	    }
	    return result;
	}

}
