package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.cache.WebConfigCache;
import com.buguagaoshu.tiktube.config.MyConfigProperties;
import com.buguagaoshu.tiktube.service.TwoFactorAuthenticationServer;
import com.buguagaoshu.tiktube.vo.TwoFactorData;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-25
 */
@Service
public class TwoFactorAuthenticationServerImpl implements TwoFactorAuthenticationServer {

    private final SecretGenerator secretGenerator;

    private final RecoveryCodeGenerator recoveryCodeGenerator;

    private final WebConfigCache webConfigCache;

    private final SystemTimeProvider timeProvider;

    private final DefaultCodeGenerator codeGenerator;

    private final DefaultCodeVerifier verifier;

    @Autowired
    public TwoFactorAuthenticationServerImpl(MyConfigProperties myConfigProperties,
                                             WebConfigCache webConfigCache) {
        this.webConfigCache = webConfigCache;

        // 生成 128 位密钥
        this.secretGenerator = new DefaultSecretGenerator(128);
        this.recoveryCodeGenerator = new RecoveryCodeGenerator();
        this.timeProvider = new SystemTimeProvider();
        this.codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
        this.verifier = new DefaultCodeVerifier(this.codeGenerator, timeProvider);
        this.verifier.setTimePeriod(30);
    }

    @Override
    public TwoFactorData createTOTPInfo(Long userId, String email) {
        TwoFactorData twoFactorData = new TwoFactorData();
        twoFactorData.setSecret(secretGenerator.generate());
        twoFactorData.setUserId(userId);
        twoFactorData.setRecoveryCode(recoveryCodeGenerator.generateCodes(1)[0]);
        QrData data = new QrData.Builder()
                .label(email)
                .secret(twoFactorData.getSecret())
                .issuer(webConfigCache.getWebConfigData().getName())
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        twoFactorData.setQrData(data);
        return twoFactorData;
    }

    @Override
    public boolean verifyTOTPCode(Long userId, String secret, String userInputCode) {
        return verifier.isValidCode(secret, userInputCode);
    }
}
