package com.buguagaoshu.tiktube.enums;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 21:26
 */
public enum ExamineTypeEnum {

    /**
     *
     * （一）反动、色情、低俗、暴力、血腥、赌博等违法内容；
     *
     * （二）宣扬邪教、封建迷信；
     *
     * （三）扰乱社会秩序，破坏民族团结；
     *
     * （四）违反公序良俗等不良导向内容；
     *
     * （五）人身攻击，侮辱、诽谤他人；
     *
     * （六）恶意引战，煽动对立、散播仇恨情绪；
     *
     * （七）危害未成年人身心健康成长；
     *
     * （八）侵犯网络版权及其他知识产权以及用户权益；
     *
     * （九）猎奇恶心等严重影响观感体验的内容；
     *
     * （十）有关法律、行政法规和国家规定禁止的其他内容；
     *
     * （十一）恶意规避审核规则为目的的异常投稿行为。
     * */
    PENDING_REVIEW(0, "待审核"),
    SUCCESS(1, "审核通过！"),
    GOVERNMENT(2, "反动、色情、低俗、暴力、血腥、赌博等违法内容；"),
    CULTS(3, "宣扬邪教、封建迷信；"),
    DISTURBING_SOCIAL_ORDER(4, "扰乱社会秩序，破坏民族团结"),
    OTHER(20, "其它原因")
    ;

    int code;
    String msg;

    ExamineTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsg(int code) {
        for (ExamineTypeEnum e : ExamineTypeEnum.values()) {
            if (e.getCode() == code) {
                return e.getMsg();
            }
        }
        return "待审核";
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
