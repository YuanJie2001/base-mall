package com.vector.mallmember.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vector.common.utils.PageUtils;
import com.vector.mallmember.entity.UndoLogEntity;

import java.util.Map;

/**
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-09 15:47:29
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

