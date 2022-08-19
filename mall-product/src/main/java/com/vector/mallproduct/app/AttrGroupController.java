package com.vector.mallproduct.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.vector.mallproduct.entity.AttrEntity;
import com.vector.mallproduct.service.AttrAttrgroupRelationService;
import com.vector.mallproduct.service.AttrService;
import com.vector.mallproduct.service.CategoryService;
import com.vector.mallproduct.vo.AttrGroupRelationVo;
import com.vector.mallproduct.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.vector.mallproduct.entity.AttrGroupEntity;
import com.vector.mallproduct.service.AttrGroupService;
import com.vector.common.utils.PageUtils;
import com.vector.common.utils.R;

import javax.annotation.Resource;


/**
 * 属性分组
 *
 * @author yuanjie
 * @email 782353676@qq.com
 * @date 2022-05-08 23:29:42
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Resource
    private CategoryService categoryService;
    @Resource
    private AttrService attrService;

    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> entityList = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entityList);
    }

    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);
        return R.ok().put("page", page);
    }

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        attrAttrgroupRelationService.saveBatch(vos);
        return R.ok();
    }

    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] attrGroupRelationVos) {
        attrService.deleteRelation(attrGroupRelationVos);
        return R.ok();

    }

    @GetMapping("{catalogId}/withattr")
    public R getAttrgroupWithAttrs(@PathVariable("catalogId") Long catlogId) {
        // 1.查出当前分类下的所有属性分组

        // 2.查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrgroupWithAttrsByCatalogId(catlogId);
        return R.ok().put("data", vos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catalogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable Long catalogId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catalogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catalogId = attrGroup.getCatalogId();
        Long[] path = categoryService.findCatalogPath(catalogId);
        attrGroup.setCatalogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
