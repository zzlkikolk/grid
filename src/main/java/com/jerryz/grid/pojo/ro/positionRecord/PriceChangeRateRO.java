package com.jerryz.grid.pojo.ro.positionRecord;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhangzhilin
 * @version 2026
 */
@Data
public class PriceChangeRateRO {

    private String assetCode;

    private String assetName;

    //平均净值幅度
    private BigDecimal averageCostRate;

    //净值幅度
    private BigDecimal costRate;

    //点数幅度
    private BigDecimal indexRate;

    //摊薄点数幅度
    private BigDecimal averageIndexRate;
}
