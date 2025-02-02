package com.awesome.yunpicturebackend.model.vo.spaceuser;

import com.awesome.yunpicturebackend.model.vo.space.SpaceVO;
import com.awesome.yunpicturebackend.model.vo.user.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间中用户的信息(主要针对用户）
 */
@Data
public class SpaceUserInfo implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户信息
     */
    private UserVO userVO;

    private static final long serialVersionUID = 1L;

}
