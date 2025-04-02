package com.buguagaoshu.porntube.repository.impl;


import com.buguagaoshu.porntube.repository.VerifyCodeRepository;
import org.springframework.stereotype.Repository;


import jakarta.servlet.http.HttpSession;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2019-11-26 17:07
 * 在内存中保存需要验证的验证码
 */
@Repository
public class SessionVerifyCodeRepositoryImpl implements VerifyCodeRepository {

    private static final int DEFAULT_VERIFY_CODE_LENGTH = 4;

    private final HttpSession session;

    public SessionVerifyCodeRepositoryImpl(HttpSession session) {
        this.session = session;
    }

    @Override
    public void save(String key, String code) {
        session.setAttribute(key, code);
    }

    @Override
    public void save(String key, String code, Long time) {
        session.setAttribute(key, code);
    }

    @Override
    public String find(String key) {
        return (String) session.getAttribute(key);
    }

    @Override
    public void remove(String key) {
        session.removeAttribute(key);
    }
}
