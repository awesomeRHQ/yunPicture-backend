package com.awesome.yunpicturebackend.aop;

import com.awesome.yunpicturebackend.model.bo.tag.ChangeTagBO;
import com.awesome.yunpicturebackend.model.dto.picture.PictureUpdateRequest;
import com.awesome.yunpicturebackend.model.entity.Tag;
import com.awesome.yunpicturebackend.service.TagService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 用户给图片做标签时，记录标签使用数据的切面
 */
@Aspect
@Component
public class UseTagInterceptor {

    @Resource
    private TagService tagService;

    @Around("execution(* com.awesome.yunpicturebackend.controller.PictureController.updatePicture(..))")
    public Object doIntercept(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1.获取参数
        Object[] args = joinPoint.getArgs();
        PictureUpdateRequest updateRequest = (PictureUpdateRequest) args[0];
        if (updateRequest == null) {
            return joinPoint.proceed();
        }
        List<String> newTagList = updateRequest.getTags();
        Long pictureId = updateRequest.getId();
        // 2.处理数据
        ChangeTagBO pictureTagChangeResult = tagService.isPictureTagsChange(pictureId, newTagList);
        if (pictureTagChangeResult == null) {
            return joinPoint.proceed();
        }
        // 标签存在改变
        if (pictureTagChangeResult.getTagChangeStatus()){
            // 存在添加标签
            List<String> addTagList = pictureTagChangeResult.getAddTagList();
            if (addTagList != null && !addTagList.isEmpty()){
                addTagList.forEach(tag -> {
                    QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
                    tagQueryWrapper.eq("name",tag);
                    tagQueryWrapper.or(i -> i.or(j -> j.eq("isDelete", 0).eq("isDelete", 1)));
                    Tag existTag = tagService.getOne(tagQueryWrapper);
                    if (existTag != null){
                        existTag.setUseCount(existTag.getUseCount()+1);
                        existTag.setUpdateTime(new Date());
                        tagService.updateById(existTag);
                    }else {
                        Tag newTag = new Tag();
                        newTag.setName(tag);
                        newTag.setUseCount(1);
                        newTag.setCreateTime(new Date());
                        newTag.setUpdateTime(new Date());
                        tagService.save(newTag);
                    }

                });
            }
            // 存在删除标签
            List<String> deleteTagList = pictureTagChangeResult.getDeleteTagList();
            if (deleteTagList != null && !deleteTagList.isEmpty()){
                // 遍历删除标签，统计标签使用次数
                deleteTagList.forEach(tag -> {
                    QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
                    tagQueryWrapper.eq("name",tag);
                    tagQueryWrapper.or(i -> i.or(j -> j.eq("isDelete", 0).eq("isDelete", 1)));
                    Tag existTag = tagService.getOne(tagQueryWrapper);
                    if (existTag != null){
                        existTag.setUseCount(existTag.getUseCount()-1);
                        if (existTag.getUseCount() <= 0){
                            existTag.setUseCount(0);
                        }
                        existTag.setUpdateTime(new Date());
                        tagService.updateById(existTag);
                    }
                });
            }
        }
        return joinPoint.proceed();
    }
}
