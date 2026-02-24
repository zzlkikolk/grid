package com.jerryz.grid.pojo.ro;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author zhangzhilin
 * @version 2026
 */
@Getter
@Setter
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页数量
     */
    private Long pageSize;


    /**
     * 总页数
     */
    private Long pages;

    /**
     * 分页数据列表
     */
    private List<T> list;



    /**
     * 成功分页响应
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param total 总记录数
     * @param list 数据列表
     * @param <T> 数据类型
     * @return 分页响应对象
     */
    public static <T> PageResult<T> success(Long pageNum, Long pageSize, Long total, List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setPages((long) Math.ceil((double) total / pageSize));
        result.setList(list);
        return result;
    }

    /**
     * 转换为Result格式的响应
     * @return Result封装的分页响应
     */
    public Result<PageResult<T>> toResult() {
        return Result.success(this);
    }


}
