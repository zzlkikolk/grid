package com.jerryz.grid.pojo.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/24 15:24
 */
@Getter
@Setter
public class PositionRecordPageVO extends PageVO{

    @NotNull(message = "资产code不能为空")
    private String assetCode;

}
