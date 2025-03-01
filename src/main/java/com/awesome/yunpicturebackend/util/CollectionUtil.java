package com.awesome.yunpicturebackend.util;

import com.awesome.yunpicturebackend.model.bo.collection.AddAndDeleteResult;

import java.util.*;
import java.util.stream.Collectors;

public class CollectionUtil {

    /**
     * 判断两个数组的变化
     * @param oldList 旧数组
     * @param newList 新数组
     * @return 不同点
     */
    public static AddAndDeleteResult findDifferences(List<String> oldList, List<String> newList) {
        // 将列表转换为集合
        Set<String> oldSet = new HashSet<>(oldList);
        Set<String> newSet = new HashSet<>(newList);

        // 计算新增元素
        Set<String> added = new HashSet<>(newSet);
        added.removeAll(oldSet);

        // 计算丢失元素
        Set<String> removed = new HashSet<>(oldSet);
        removed.removeAll(newSet);

        return new AddAndDeleteResult(new ArrayList<>(added), new ArrayList<>(removed));
    }

}
