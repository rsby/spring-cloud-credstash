package com.catalyticds.credstash;

/**
 * @author reesbyars on 10/1/17.
 */
public interface CredStashStrings {

    interface Keys {
        String NAME = "name";
        String KEY = "key";
        String CONTENTS = "contents";
        String HMAC = "hmac";
        String DIGEST = "digest";
        String VERSION = "version";
    }

    interface Digests {
        String SHA256 = "SHA256";
    }


    String ENCODING = "UTF-8";

    static String padVersion(Integer version) {
        return String.format("%019d", version);
    }

}
