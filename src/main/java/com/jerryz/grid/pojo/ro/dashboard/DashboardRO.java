package com.jerryz.grid.pojo.ro.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 首页大屏数据RO
 */
@Data
public class DashboardRO {
    /**
     * 总持仓金额
     */
    private BigDecimal totalAmount;
    /**
     * 总资产数量
     */
    private Long totalAssetCount;
    /**
     * 各资产类型统计列表
     */
    private List<AssetTypeStatRO> assetTypeStats;
    /**
     * 资产详情列表（用于二级展示）
     */
    private List<AssetDetailRO> assetDetails;
    /**
     * 当前选中的资产类型
     */
    private Integer selectedAssetType;
    /**
     * 当前选中的资产类型名称
     */
    private String selectedAssetTypeName;
}
