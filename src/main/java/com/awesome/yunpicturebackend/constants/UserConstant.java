package com.awesome.yunpicturebackend.constants;

import lombok.Data;

/**
 * 用户常量
 */
@Data
public class UserConstant {

    public static final String USER_LOGIN_STATE = "login_user";

    public static final String USER_DEFAULT_AVATAR = "https://i1.hdslb.com/bfs/face/ccb70c9b39297a8f9ee698c81f8673363a98d272.jpg@96w_96h_1c_1s_!web-avatar.avif";

    /**
     * 图片默认审核人
     */
    public static final Long DEFAULT_PICTURE_REVIEWER_ID = 1867054181268410370L;

    public static final String ADMIN_ROLE = "admin";
}
