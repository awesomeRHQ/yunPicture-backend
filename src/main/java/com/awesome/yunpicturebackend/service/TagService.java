package com.awesome.yunpicturebackend.service;

import com.awesome.yunpicturebackend.model.bo.tag.ChangeTagBO;
import com.awesome.yunpicturebackend.model.dto.tag.TagQueryRequest;
import com.awesome.yunpicturebackend.model.entity.Tag;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 针对表【tag(标签表)】的服务Service
 *
 * @author awesomeRHQ
 * @since 2024-12-31 19:48:17
 */
public interface TagService extends IService<Tag> {

    /**
     * 保存标签
     * @param tag 标签
     * @param count 需要保存的次数
     * @return 成功保存的个数
     */
    int saveTags(String tag, int count);

    /**
     * 保存标签
     * @param tagList 标签列表
     * @param count 需要保存的次数
     * @return 保存的个数
     */
    int saveTags(List<String> tagList, int count);

    /**
     * 判断图片标签是否发生变化
     * @param pictureId 图片Id
     * @param newTagList 提交的图片标签列表
     * @return 变化结果
     */
    ChangeTagBO isPictureTagsChange(Long pictureId, List<String> newTagList);

    /**
     * 拼接查询条件
     * @param tagQueryRequest 查询请求类
     * @return
     */
    QueryWrapper<Tag> getQueryWrapper(TagQueryRequest tagQueryRequest);

}
