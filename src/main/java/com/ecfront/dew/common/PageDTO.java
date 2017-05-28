package com.ecfront.dew.common;

import java.io.Serializable;
import java.util.List;

/**
 * 分页对象
 */
public class PageDTO<E> implements Serializable {

    /**
     * 当前页，从1开始
     */
    private long pageNumber;
    /**
     * 每页记录数
     */
    private long pageSize;
    /**
     * 总页数
     */
    private long pageTotal;
    /**
     * 总记录数
     */
    private long recordTotal;
    /**
     * 实际对象
     */
    private List<E> objects;

    public static <S> PageDTO<S> build(long pageNumber, long pageSize, long recordTotal, List<S> objects) {
        PageDTO<S> dto = new PageDTO<>();
        dto.pageNumber = pageNumber;
        dto.pageSize = pageSize;
        dto.recordTotal = recordTotal;
        dto.pageTotal = (recordTotal + pageSize - 1) / pageSize;
        dto.objects = objects;
        return dto;
    }

    public long getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(long pageNumber) {
        this.pageNumber = pageNumber;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(long pageTotal) {
        this.pageTotal = pageTotal;
    }

    public long getRecordTotal() {
        return recordTotal;
    }

    public void setRecordTotal(long recordTotal) {
        this.recordTotal = recordTotal;
    }

    public List<E> getObjects() {
        return objects;
    }

    public void setObjects(List<E> objects) {
        this.objects = objects;
    }

}
