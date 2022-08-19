package com.vector.mallsearch.controller;

import com.vector.mallsearch.service.impl.MallSearchServiceImpl;
import com.vector.mallsearch.vo.SearchParam;
import com.vector.mallsearch.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName SearchController
 * 
 * @Author YuanJie
 * @Date 2022/8/11 23:57
 */
@Controller
public class SearchController {
    @Resource
    MallSearchServiceImpl mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request) {
        searchParam.set_queryString(request.getQueryString());
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
