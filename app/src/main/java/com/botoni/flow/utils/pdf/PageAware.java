package com.botoni.flow.utils.pdf;


public interface PageAware {
    void setPageInfo(int currentPage, int totalPages);
}