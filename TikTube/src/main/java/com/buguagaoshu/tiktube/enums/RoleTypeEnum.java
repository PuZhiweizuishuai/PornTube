package com.buguagaoshu.tiktube.enums;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-06-04 13:44
 * 角色
 */
public enum  RoleTypeEnum {
    /**
     * 角色
     * */
    ADMIN("ROLE_ADMIN"),
    VIP("ROLE_VIP"),
    USER("ROLE_USER"),
    ;

    String role;

    RoleTypeEnum(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public static boolean check(String role) {
        for (RoleTypeEnum r : RoleTypeEnum.values()) {
            if (r.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public static String getRoleMessage(String role) {
        if (role.equals("ROLE_ADMIN")) {
            return "管理员";
        }
        if (role.equals("ROLE_VIP")) {
            return "VIP";
        }
        return "普通用户";
    }

}
