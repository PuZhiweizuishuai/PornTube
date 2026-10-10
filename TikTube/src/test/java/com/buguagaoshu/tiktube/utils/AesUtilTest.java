package com.buguagaoshu.tiktube.utils;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AesUtilTest {

    @Test
    void encryptDecryptRoundTrip() {
        String text = "12#34#" + (System.currentTimeMillis() + 1000) + "#filename.MP4";
        String encrypted = AesUtil.getInstance().encrypt(text);

        assertEquals(text, AesUtil.getInstance().decrypt(encrypted));
    }

    @Test
    void sameTextEncryptsToDifferentCipherText() {
        String encrypted1 = AesUtil.getInstance().encrypt("same-text");
        String encrypted2 = AesUtil.getInstance().encrypt("same-text");

        assertNotEquals(encrypted1, encrypted2);
        assertEquals("same-text", AesUtil.getInstance().decrypt(encrypted1));
    }

    @Test
    void tamperedCipherTextIsRejected() {
        byte[] decoded = Base64.getDecoder().decode(AesUtil.getInstance().encrypt("some-content"));
        // 篡改密文最后一个字节，GCM 校验值应发现
        decoded[decoded.length - 1] ^= 0x01;

        assertThrows(RuntimeException.class,
                () -> AesUtil.getInstance().decrypt(Base64.getEncoder().encodeToString(decoded)));
    }
}
