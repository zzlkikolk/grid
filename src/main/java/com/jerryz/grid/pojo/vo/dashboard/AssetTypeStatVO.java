package com.jerryz.grid.pojo.vo.dashboard;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产类型统计VO
 */
@Data
public class AssetTypeStatVO {
    /**
     * 资产类型: 1-股票 2-基金 3-债券 4-其他
     */
    private Integer assetType;
    /**
     * 资产类型名称
     */
    private String assetTypeName;
    /**
     * 持有数量（不同资产的数量）
     */
    private Long assetCount;
    /**
     * 持仓金额
     */
    private BigDecimal totalAmount;
    /**
     * 占比百分比
     */
    private BigDecimal percentage;
}
