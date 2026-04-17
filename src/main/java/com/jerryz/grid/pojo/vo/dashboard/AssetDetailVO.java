package com.jerryz.grid.pojo.vo.dashboard;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产详情VO
 */
@Data
public class AssetDetailVO {
    /**
     * 资产代码
     */
    private String assetCode;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产类型
     */
    private Integer assetType;
    /**
     * 持仓金额
     */
    private BigDecimal totalAmount;
    /**
     * 当前持有数量
     */
    private BigDecimal currentQuantity;
    /**
     * 占比百分比
     */
    private BigDecimal percentage;
}
