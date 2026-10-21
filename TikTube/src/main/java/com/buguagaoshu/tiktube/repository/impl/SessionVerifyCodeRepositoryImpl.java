package com.buguagaoshu.tiktube.repository.impl;


import com.buguagaoshu.tiktube.repository.VerifyCodeRepository;
import org.springframework.stereotype.Repository;


import jakarta.servlet.http.HttpSession;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2019-11-26 17:07
 * 在内存中保存需要验证的验证码。
 * 过期时间随条目一起存：服务层虽然也会校验时间戳，但条目本身不该在会话里活到过期之后
 */
@Repository("sessionVerifyCodeRepositoryImpl")
public class SessionVerifyCodeRepositoryImpl implements VerifyCodeRepository {

    private static final long DEFAULT_TTL_MINUTES = 10L;

    private final HttpSession session;

    public SessionVerifyCodeRepositoryImpl(HttpSession session) {
        this.session = session;
    }

    private record Entry(String code, long expireAt) {
    }

    @Override
    public void save(String key, String code) {
        save(key, code, DEFAULT_TTL_MINUTES);
    }

    @Override
    public void save(String key, String code, Long time) {
        session.setAttribute(key, new Entry(code, System.currentTimeMillis() + time * 60_000L));
    }

    @Override
    public String find(String key) {
        Entry entry = (Entry) session.getAttribute(key);
        if (entry == null) {
            return null;
        }
        if (entry.expireAt() < System.currentTimeMillis()) {
            session.removeAttribute(key);
            return null;
        }
        return entry.code();
    }

    @Override
    public void remove(String key) {
        session.removeAttribute(key);
    }
}
