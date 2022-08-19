package com.vector.mallsearch.service;

import com.vector.mallsearch.vo.SearchParam;
import com.vector.mallsearch.vo.SearchResult;

public interface MallSearchService {

    SearchResult search(SearchParam searchParam);
}
