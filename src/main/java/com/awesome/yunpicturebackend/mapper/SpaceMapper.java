package com.awesome.yunpicturebackend.mapper;

import com.awesome.yunpicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【space(空间)】的数据库操作Mapper
 *
 * @author awesomeRHQ
 * @since 2025-01-15 14:21:37
 */
@Mapper
public interface SpaceMapper extends BaseMapper<Space> {


}
