package com.buguagaoshu.tiktube.enums;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-18
 */
public enum NotificationType {
    // 消息类型
    REPLY_POST(0, "回复帖子"),
    REPLY_COMMENT(1, "回复评论"),
    LIKE_POST(2, "点赞帖子"),
    LIKE_COMMENT(3, "点赞评论"),
    // 不知道还有啥类型，先空着吧
    // 管理员发布的通知，如稿件被删除，稿件审核通过，账号消息等等
    SYSTEM(10, "系统通知"),
    ACCUSATION(11, "举报"),

    // 消息状态
    READ(1,"已读"),
    UNREAD(0, "未读")
    ;
    int type;
    String message;

    NotificationType(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }


    public static String createExamineContent(String title, int type, String message) {
        return "你发布的稿件《" +
                title +
                "》因为：" +
                ExamineTypeEnum.getMsg(type) +
                " 未通过审核，建议您：" +
                message;
    }


    public static String createExamineLink(String title, Long id) {
        return "<a href=\"/studio/upload?edit=" + id + "\">" + title + "</a>";
    }
}
