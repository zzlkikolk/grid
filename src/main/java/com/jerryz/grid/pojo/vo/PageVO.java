package com.jerryz.grid.pojo.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/06 18:11
 */
@Data
public class PageVO {

    @NotNull(message = "pageNum不能为空")
    private Integer pageNum;

    @NotNull(message = "pageSize不能为空")
    private Integer pageSize;
}
