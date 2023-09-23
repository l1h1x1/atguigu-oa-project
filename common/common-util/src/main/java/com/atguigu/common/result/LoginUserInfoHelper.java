package com.atguigu.common.result;

/**
 * @author AXiang
 * @create 2023/9/13 17:24
 **/
public class LoginUserInfoHelper {
    /**
     * 获取当前用户信息帮助类
     * 和当前线程进行绑定
     * 用来存储用户id和用户名称
     */
        private static ThreadLocal<Long> userId = new ThreadLocal<Long>();
        private static ThreadLocal<String> username = new ThreadLocal<String>();

        public static void setUserId(Long _userId) {
            userId.set(_userId);
        }
        public static Long getUserId() {
            return userId.get();
        }
        public static void removeUserId() {
            userId.remove();
        }
        public static void setUsername(String _username) {
            username.set(_username);
        }
        public static String getUsername() {
            return username.get();
        }
        public static void removeUsername() {
            username.remove();
        }
}
