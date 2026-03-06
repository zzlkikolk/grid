package com.jerryz.grid.pojo.vo.positionRecord;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhangzhilin
 * @version 2026
 */
@Data
public class PredictPriceChangeRateVO {

    @NotBlank(message = "资产代码不能为空")
    private String assetCode;

    @NotNull(message = "今日指数点数不能为空")
    private BigDecimal todayTrackIndex;

    @NotNull(message = "当前净值不能为空")
    private BigDecimal localPrice;
}
