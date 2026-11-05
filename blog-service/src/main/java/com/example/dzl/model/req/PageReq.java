package com.example.dzl.model.req;


import lombok.Data;

@Data
public class PageReq {
    /**
     * 当前页码，默认1
     */
    private long pageNum = 1;
    /**
     * 每页大小，默认10
     */
    private long pageSize = 10;
}
