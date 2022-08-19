package com.vector.mallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vector.common.utils.PageUtils;
import com.vector.common.utils.Query;
import com.vector.mallproduct.dao.CategoryDao;
import com.vector.mallproduct.entity.CategoryEntity;
import com.vector.mallproduct.service.CategoryBrandRelationService;
import com.vector.mallproduct.service.CategoryService;
import com.vector.mallproduct.vo.Catalog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;
    @Resource
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1.查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 2.组装成父子的树形结构

        //2.1) 找到所有的一级分类
        return categoryEntities.stream().filter(entity ->
                entity.getParentCid() == 0
        ).map(menu -> {
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).sorted((menu1, menu2) -> (StringUtils.isBlank(String.valueOf(menu1.getSort())) ? 0 : menu1.getSort()) -
                (StringUtils.isBlank(String.valueOf(menu2.getSort())) ? 0 : menu2.getSort())
        ).collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO //1.检查当前删除菜单,是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatalogPath(Long catalogId) {
        List<Long> paths = new ArrayList<>();
        paths.add(catalogId);
        Long parentCid = this.getById(catalogId).getParentCid();
        while (parentCid != 0) {
            paths.add(parentCid);
            parentCid = this.getById(parentCid).getParentCid();
        }
        Collections.reverse(paths);
        return paths.toArray(new Long[0]);
    }

    /**
     * @CacheEvict:失效模式 级联更新关联的所有数据
     */

    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    /**
     * @return 指定每一个缓存需要放到哪个名字的缓存 (按业务类型分)
     * 缓存的value值,默认使用jdk序列化的数据
     * 默认时间-1 永不过期
     * <p>
     * 自定义:
     * 1)指定缓存使用的key key属性接受一个SpEl
     * 2)指定缓存的存活时间
     * 3)保存为json格式
     * @Cacheable key默认自动生成, 缓存的名字: SimpleKey [](自主生成的key)
     * <p>
     * springcache的不足
     * 读模式
     * 缓存穿透: 查询null数据. 解决:缓存空数据
     * 缓存击穿: 大量并发同时查询一个过期的数据.解决加锁:降低并发 sync = true 如果有多个线程访问相同key,就加锁
     * 缓存雪崩: 大量的key同时过期. 加随机时间
     * 写模式:
     * 读写加锁
     * 引入canal.感知mysql的更新去更新数据库
     * 读多写读,直接去数据库查询
     * <p>
     * 常规数据（读多写少，即时性，一致性要求不高的数据）﹔完全可以使用spring-Cache
     * 特殊薮据:特殊设计
     */
    // 代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。如果缓存中没有，会调用方法，最后将方法的结果放入缓存!
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        /**
         * 1.将多次查询变为一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        // 1.查所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        // 2.封装2级分类
        Map<String, List<Catalog2Vo>> catalogJson = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
            //1. 每一个1级分类的内容
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, l1.getCatId());
            // 封装指定格式的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (Optional.ofNullable(categoryEntities).isPresent()) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList, l2.getCatId());
                    if (Optional.ofNullable(level3Catalog).isPresent()) {
                        // 2.封装二级中的三级分类
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Catalog.stream().map(l3 -> {
                            return new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        return catalogJson;
    }


    /**
     * TODO 堆外内存溢出 outofDirectMemoryError
     * springboot2.e以后默认使用lettuce作为操作redis的客户端。它使用netty进行网络通信。
     * Lettuce的bug导致堆外内存溢出 默认使用vm控制-Xmx300m; netty如果没有指定堆外内存，默认使用-Xmx300m
     * 可以通过-Dio.netty.maxDirectMemory进行设置(不能解决,始终会达到对外内存溢出)
     * 解决方案: 不能使用-Dio.netty.maxDirectMemory只去调大堆外内存
     * 1)升级Lettuce客户端 2)切换使用jedis,Redisson
     *  redisTemplute:
     * Lettuce、jedis操作redis的底层客户端。Spring再次封装redisTemplute
     */

    // 从数据库查询并封装分类数据
//    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson2() throws JsonProcessingException {
        // 1.缓存中存json字符串
        // json跨语言跨平台兼容
        ObjectMapper objectMapper = new ObjectMapper();

        RBucket<Object> bucket = redissonClient.getBucket("catalogJSON");
        String catalogJSON = (String) bucket.get();
        if (StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2Vo>> catalogJsonDb = getCatalogJsonFromDbWithRedissonLock();
            String s = objectMapper.writeValueAsString(catalogJsonDb);
            bucket.set(s);
            return catalogJsonDb;
        }
        return objectMapper.readValue(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });

    }

    /**
     * 缓存数据和数据库数据最终一致性
     * 1.双写模式 多线程在数据库同步缓存时有时延,可能导致缓存---抢跑者路途居后.导致脏数据
     * 2.失效模式 第一线程写完删缓存,第二线程写完删缓存的途中,第三线程缓存没有读数据库删缓存. 线程二途中比线程三慢.导致脏数据
     * 解决方案
     * 1)分布式读写锁
     * 2)缓存的所有数据都有过期时间,主动更新脏数据
     * <p>
     * springCahe简化缓存开发
     * 1.引入spring-boot-starter-cache
     * 2.写配置
     * (1)自动配置
     * CacheAutoConfiguration会导入RediscacheConfiguration;
     * 自动配好缓存管理器RedisCaheManager
     * (2)
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        RLock lock = redissonClient.getLock("CatalogJson-lock");
        lock.lock();
        Map<String, List<Catalog2Vo>> dataFromDb;

        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    public Map<String, List<Catalog2Vo>> getDataFromDb() {

        /**
         * 1.将多次查询变为一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        // 1.查所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        // 2.封装2级分类
        Map<String, List<Catalog2Vo>> catalogJson = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
            //1. 每一个1级分类的内容
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, l1.getCatId());
            // 封装指定格式的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (Optional.ofNullable(categoryEntities).isPresent()) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList, l2.getCatId());
                    if (Optional.ofNullable(level3Catalog).isPresent()) {
                        // 2.封装二级中的三级分类
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Catalog.stream().map(l3 -> {
                            return new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        RBucket<Object> catalogJSON = redissonClient.getBucket("catalogJSON");
        catalogJSON.set(catalogJson, 1, TimeUnit.DAYS);
        return catalogJson;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        return selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
    }

    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildren(CategoryEntity root,
                                             List<CategoryEntity> all) {
        List<CategoryEntity> children =
                all.stream().filter(categoryEntity -> {
                            if (ObjectUtils.equals(categoryEntity, null)) {
                                return false;
                            }
                            return categoryEntity.getParentCid().equals(root.getCatId());
                        }
                ).map(categoryEntity -> {
                    // 1.找到子菜单  尾递归
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                    // 2.菜单的排序
                }).sorted((menu1, menu2) -> {
                    if (menu1.getSort() == null) {
                        return -1;
                    }
                    if (menu2 == null || menu2.getSort() == null) {
                        return 1;
                    }
                    return menu1.getSort() - menu2.getSort();
                }).collect(Collectors.toList());
        return children;
    }
}
