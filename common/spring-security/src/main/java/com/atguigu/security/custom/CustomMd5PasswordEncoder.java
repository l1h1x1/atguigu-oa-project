package com.atguigu.security.custom;

import com.atguigu.common.utils.MD5;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @author AXiang
 * @create 2023/9/9 14:57
 **/
@Component
public class CustomMd5PasswordEncoder implements PasswordEncoder {
    public String encode(CharSequence rawPassword) {
        return MD5.encrypt(rawPassword.toString());
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encodedPassword.equals(MD5.encrypt(rawPassword.toString()));
    }
}
