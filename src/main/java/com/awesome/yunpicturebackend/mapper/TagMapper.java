package com.awesome.yunpicturebackend.mapper;

import com.awesome.yunpicturebackend.model.entity.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【tag(标签表)】的数据库操作Mapper
 *
 * @author awesomeRHQ
 * @since 2024-12-31 19:48:17
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {


}
