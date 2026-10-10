package com.buguagaoshu.tiktube.utils;

import com.buguagaoshu.tiktube.config.WebConstant;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-11 16:34
 * update          2026-10-10 由 AES/ECB 改为 AES/GCM，随机 IV 与密文一起存放，带完整性校验
 * */
@Slf4j
public class AesUtil {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private static volatile AesUtil instance;
    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    // 私有构造函数
    private AesUtil(String secret) {
        this.secretKey = generateKey(secret);
    }

    /**
     * 获取单例实例
     * @return AesUtil实例
     */
    public static AesUtil getInstance() {
        if (instance == null) {
            synchronized (AesUtil.class) {
                if (instance == null) {
                    instance = new AesUtil(WebConstant.AES_KEY);
                }
            }
        }
        return instance;
    }

    /**
     * 生成密钥
     */
    private SecretKeySpec generateKey(String myKey) {
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // AES-128
            return new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            log.error("密钥生成失败", e);
            throw new RuntimeException("密钥生成失败", e);
        }
    }

    /**
     * 加密
     * @param strToEncrypt 原文
     * @return 加密后数据，Base64(IV + 密文 + GCM 校验值)
     */
    public String encrypt(String strToEncrypt) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            log.error("加密时出错", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密
     * @param strToDecrypt 密文
     * @return 解密后原文
     */
    public String decrypt(String strToDecrypt) {
        try {
            byte[] decoded = Base64.getDecoder().decode(strToDecrypt);
            if (decoded.length <= IV_LENGTH) {
                throw new IllegalBlockSizeException("密文长度不合法");
            }
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey,
                    new GCMParameterSpec(TAG_LENGTH_BIT, Arrays.copyOfRange(decoded, 0, IV_LENGTH)));
            byte[] decrypted = cipher.doFinal(decoded, IV_LENGTH, decoded.length - IV_LENGTH);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("解密失败 - 可能是密钥不正确或数据损坏", e);
            throw new RuntimeException("解密失败", e);
        } catch (Exception e) {
            log.error("解密时出错", e);
            throw new RuntimeException("解密失败", e);
        }
    }
}
