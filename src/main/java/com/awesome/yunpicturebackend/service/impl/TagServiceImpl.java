package com.awesome.yunpicturebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.mapper.TagMapper;
import com.awesome.yunpicturebackend.model.bo.collection.AddAndDeleteResult;
import com.awesome.yunpicturebackend.model.bo.tag.ChangeTagBO;
import com.awesome.yunpicturebackend.model.dto.tag.TagQueryRequest;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.model.entity.Tag;
import com.awesome.yunpicturebackend.model.enums.SortOrderEnum;
import com.awesome.yunpicturebackend.service.PictureService;
import com.awesome.yunpicturebackend.service.TagService;
import com.awesome.yunpicturebackend.util.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 针对表【tag(标签表)】的服务Service实现类
 *
 * @author awesomeRHQ
 * @since 2024-12-31 19:48:17
 */
@Service("TagService")
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Resource
    private PictureService pictureService;

    /**
     * 保存标签
     * @param tag 标签
     * @param count 需要保存的次数，默认为1
     * @return 保存的个数
     */
    @Override
    public int saveTags(String tag, int count) {
        if (StrUtil.isBlank(tag)) {
            return 0;
        }
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("name", tag);
        Tag existTag = this.getOne(tagQueryWrapper);
        boolean result;
        // 存在就 +count
        if (existTag != null) {
            if (count > 1) {
                existTag.setUseCount(existTag.getUseCount() + count);
            } else {
                existTag.setUseCount(existTag.getUseCount() + 1);
            }
            result = this.updateById(existTag);
        } else {
            // 不存在就新增
            Tag newTag = new Tag();
            newTag.setName(tag);
            newTag.setUseCount(1);
            result = this.save(newTag);
        }
        if (result) return 1;
        else return 0;
    }

    /**
     * 保存标签
     * @param tagList 标签列表
     * @param count 需要保存的次数
     * @return 保存的个数
     */
    @Override
    public int saveTags(List<String> tagList, int count) {
        if (tagList == null || tagList.isEmpty()) {
            return 0;
        }
        int saveCount = 0;
        for (String tag : tagList) {
            int i = this.saveTags(tag, count);
            if (i == 1)
                saveCount++;
        }
        return saveCount;
    }

    /**
     * 判断图片标签是否发生变化
     *
     * @param pictureId  图片Id
     * @param newTagList 提交的图片标签列表
     * @return 变化结果
     */
    @Override
    public ChangeTagBO isPictureTagsChange(Long pictureId, List<String> newTagList) {
        // 1.校验数据
        if (pictureId == null || newTagList == null) {
            return null;
        }
        // 2.获取图片原始标签列表
        Picture picture = pictureService.getById(pictureId);
        ThrowUtil.throwIf(picture == null, ResponseCode.OPERATION_ERROR, "当前图片不存在");
        String tagsStr = picture.getTags();
        List<String> oldTagList = new ArrayList<>();
        // 若原始图片标签不为空，则获取到标签List
        if (StrUtil.isNotBlank(tagsStr)) {
            oldTagList = JSONUtil.toList(tagsStr, String.class);
        }
        // 3.原始标签列表与新列表对比
        ChangeTagBO changeTagBO = new ChangeTagBO();
        // 若原始标签列表为空
        if (oldTagList.isEmpty()) {
            if (!newTagList.isEmpty()) {
                // 且新标签列表不为空，则新标签列表全部为新增标签
                changeTagBO.setTagChangeStatus(true);
                changeTagBO.setAddTagList(newTagList);
            } else {
                // 新标签列表也为空，则表示既没新增也没删除
                changeTagBO.setTagChangeStatus(false);
            }
        } else { // 原始标签列表不为空
            if (newTagList.isEmpty()) {
                // 且新标签列表为空，则原标签列表全部删除
                changeTagBO.setTagChangeStatus(true);
                changeTagBO.setDeleteTagList(oldTagList);
                return changeTagBO;
            } else {
                // 原标签和新标签列表都不为空，需要记录新增或删除标签
                changeTagBO.setTagChangeStatus(true);
                AddAndDeleteResult differences = CollectionUtil.findDifferences(oldTagList, newTagList);
                changeTagBO.setAddTagList(differences.getAddList());
                changeTagBO.setDeleteTagList(differences.getDeleteList());
            }
        }
        // 4.返回结果
        return changeTagBO;
    }

    /**
     * 拼接查询条件
     *
     * @param tagQueryRequest 查询请求类
     * @return
     */
    @Override
    public QueryWrapper<Tag> getQueryWrapper(TagQueryRequest tagQueryRequest) {
        String name = tagQueryRequest.getName();
        Integer useCount = tagQueryRequest.getUseCount();
        String sortOrder = tagQueryRequest.getSortOrder();
        String sortField = tagQueryRequest.getSortField();

        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.like(!StrUtil.isNotBlank(name), "name", name);
        tagQueryWrapper.eq(useCount != null && useCount > 0, "useCount", useCount);
        tagQueryWrapper.orderBy(StrUtil.isNotBlank(sortField), SortOrderEnum.ASC.getValue().equals(sortOrder), sortField);
        return tagQueryWrapper;
    }


}
