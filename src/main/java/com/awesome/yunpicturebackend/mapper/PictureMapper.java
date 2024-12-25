package com.awesome.yunpicturebackend.mapper;

import com.awesome.yunpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【picture(图片表)】的数据库操作Mapper
 *
 * @author awesomeRHQ
 * @since 2024-12-23 13:22:58
 */
@Mapper
public interface PictureMapper extends BaseMapper<Picture> {


}
