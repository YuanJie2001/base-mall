package com.vector.mallware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vector.common.utils.PageUtils;
import com.vector.mallware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-09 17:31:39
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

