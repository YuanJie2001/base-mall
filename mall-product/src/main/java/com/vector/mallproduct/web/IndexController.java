package com.vector.mallproduct.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vector.mallproduct.entity.CategoryEntity;
import com.vector.mallproduct.service.CategoryService;
import com.vector.mallproduct.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName IndexController
 * 
 * @Author YuanJie
 * @Date 2022/8/5 20:35
 */
@Controller
public class IndexController {
    @Resource
    private CategoryService categoryService;
    @Resource
    private RedissonClient redissonClient;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        List<CategoryEntity> categorys = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categorys);
        return "index";
    }

    @GetMapping("/index/catalog.json")
    @ResponseBody
    public Map<String, List<Catalog2Vo>> getCatalogJson() throws JsonProcessingException {
        return categoryService.getCatalogJson();
    }

    /**
     * 读写锁保证一定可以读到最新数据 写锁是排他锁(互斥) 读锁是共享锁
     * 写锁没释放,读就必须等待
     * 写 + 读 读操作需要等待写锁释放
     * 写 + 写 排队阻塞
     * 读 + 读 不影响
     * 读 + 写 写操作需要等待读锁释放
     *
     * @return
     */

    @GetMapping("/write")
    @ResponseBody
    public String writeString() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RBucket<Object> writeValue = redissonClient.getBucket("writeValue");
        RLock rLock = readWriteLock.writeLock();
        try {
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            writeValue.set(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    // 读写锁保证一定可以读到最新数据
    @GetMapping("/read")
    @ResponseBody
    public String readString() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");

        RBucket<Object> writeValue = null;
        RLock rLock = readWriteLock.readLock();
        try {
            rLock.lock();
            writeValue = redissonClient.getBucket("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return (String) writeValue.get();
    }

    /**
     * redis应用层信号量
     * 可做分布式熔断限流
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.acquire(); // 获取一个信号,获取一个值
        return "ok";
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();
        return "ok";
    }

    /**
     * 闭锁
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.await(); // 等待闭锁都完成,否则阻塞等待
        return "...放假了";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();
        return id + "班的人都走了...";
    }
}
