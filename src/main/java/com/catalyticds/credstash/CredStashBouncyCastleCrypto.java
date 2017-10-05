package com.catalyticds.credstash;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * @author jcoyle on 2/1/16.
 */
class CredStashBouncyCastleCrypto implements CredStashCrypto {

    @Override
    public byte[] decrypt(byte[] key, byte[] contents) {
        // Credstash uses standard AES
        BlockCipher engine = new AESEngine();

        // Credstash uses CTR mode
        StreamBlockCipher cipher = new SICBlockCipher(engine);

        cipher.init(false, new ParametersWithIV(new KeyParameter(key), INITIALIZATION_VECTOR));

        byte[] resultBytes = new byte[contents.length];
        cipher.processBytes(contents, 0, contents.length, resultBytes, 0);
        return resultBytes;
    }

    @Override
    public byte[] digest(byte[] key, byte[] contents, String algorithm) {

        if (algorithm != null && !algorithm.equalsIgnoreCase(CredStashStrings.Digests.SHA256)) {
            throw new CredStashUnsupportedDigestException("Only SHA256 supported. Requested ==> " +
                    algorithm, algorithm);
        }

        // Credstash uses SHA-256
        SHA256Digest digest = new SHA256Digest();

        // Credstash uses HMAC
        HMac mac = new HMac(digest);

        byte[] resultBytes = new byte[mac.getMacSize()];

        mac.init(new KeyParameter(key));
        mac.update(contents, 0, contents.length);
        mac.doFinal(resultBytes, 0);

        return resultBytes;
    }

}
