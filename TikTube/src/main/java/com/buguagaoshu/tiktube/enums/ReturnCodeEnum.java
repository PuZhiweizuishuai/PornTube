package com.buguagaoshu.tiktube.enums;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-06-04 13:04
 * 系统返回错误代码
 */
public enum ReturnCodeEnum {
    /**
     * 系统错误吗
     * */
    SUCCESS(200, "Success!"),

    NOO_FOUND(404, "没有找到"),
    NO_LOGIN(403, "请先登录！"),

    SYSTEM_ERROR(500, "系统异常，请重试"),

    USER_ALREADY_EXISTS(1000, "用户已经存在！"),
    USER_NOT_FIND(1001, "请检查用户名或密码！"),
    USER_EMAIL_ALREADY_EXISTS(1002, "邮箱已被使用"),
    USER_ROLE_BAD(1003, "权限设置错误！"),
    USER_LOCK_TYPE_BAD(1004, "账号锁定或禁言类型错误"),
    NOT_LOGGED_IN(1005, "未登陆"),
    NO_POWER(1006, "没有权限"),
    VERIFY_FAILED(1007, "验证码错误"),
    USER_PHONE_ALREADY_EXISTS(1008, "该手机号已被使用"),
    INVITATION_ERROR(1009, "邀请码错误！"),
    CATEGORY_NOT_FOUND(1009, "分区设置错误！"),
    IMAGE_NO_POWER(1010, "图片设置错误或使用了他人的图片！"),
    VIDEO_NO_POWER(1011, "上传的视频已经有了或者使用了别人的视频！"),
    USER_NAME_TO_LONG(1012, "用户名过长，超过25个字符！"),
    USER_INTRODUCTION_LONG(1013, "用户简介过长，超过100个字符！"),
    ADMIN_DONT_RENAME(1014, "系统默认管理员不能改名！"),
    DONT_USER_NAME(1015,"不能使用管理员名"),
    DESCRIBE_TO_LONG(1016, "简介过长"),
    TITLE_TO_LONG(1017, "标题过长"),
    REVIEWED(1018, "已被其他人审核"),


    LACK_ID(4000, "缺少ID"),
    NOT_RUN(4001, "没有在运行"),
    DATA_VALID_EXCEPTION(4002, "数据校验错误"),
    PASSWORD_TO_SHORT(4003, "密码最少为6位！")

    ;
    int code;
    String msg;

    ReturnCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
