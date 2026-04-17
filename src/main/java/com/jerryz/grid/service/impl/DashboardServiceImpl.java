package com.jerryz.grid.service.impl;

import com.jerryz.grid.mapper.DashboardMapper;
import com.jerryz.grid.pojo.ro.Result;
import com.jerryz.grid.pojo.ro.dashboard.AssetDetailRO;
import com.jerryz.grid.pojo.ro.dashboard.AssetTypeStatRO;
import com.jerryz.grid.pojo.ro.dashboard.DashboardRO;
import com.jerryz.grid.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页大屏Service实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final DashboardMapper dashboardMapper;

    private static final Map<Integer, String> ASSET_TYPE_NAMES = Map.of(
        1, "股票",
        2, "基金",
        3, "债券",
        4, "其他"
    );

    @Override
    public Result<DashboardRO> getDashboardData() {
        DashboardRO dashboardRO = buildDashboardData();
        return Result.success(dashboardRO);
    }

    @Override
    public Result<DashboardRO> getAssetTypeRatio() {
        DashboardRO dashboardRO = buildDashboardData();
        return Result.success(dashboardRO);
    }

    @Override
    public Result<DashboardRO> getAssetAmountStats() {
        DashboardRO dashboardRO = buildDashboardData();
        return Result.success(dashboardRO);
    }

    @Override
    public Result<DashboardRO> getAssetDetailsByType(Integer assetType) {
        DashboardRO dashboardRO = new DashboardRO();

        // 设置选中的资产类型
        dashboardRO.setSelectedAssetType(assetType);
        dashboardRO.setSelectedAssetTypeName(ASSET_TYPE_NAMES.getOrDefault(assetType, "未知"));

        // 查询该类型下的资产详情
        List<DashboardMapper.AssetDetailRO> detailROList = dashboardMapper.selectAssetDetailsByType(assetType);

        // 计算该类型的总金额
        BigDecimal typeTotalAmount = detailROList.stream()
                .map(DashboardMapper.AssetDetailRO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 转换为RO并计算占比
        List<AssetDetailRO> assetDetails = detailROList.stream()
                .map(detail -> {
                    AssetDetailRO ro = new AssetDetailRO();
                    ro.setAssetCode(detail.getAssetCode());
                    ro.setAssetName(detail.getAssetName());
                    ro.setAssetType(detail.getAssetType());
                    ro.setTotalAmount(detail.getTotalAmount());
                    ro.setCurrentQuantity(detail.getCurrentQuantity());

                    // 计算占比
                    if (typeTotalAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal percentage = detail.getTotalAmount()
                                .divide(typeTotalAmount, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(2, RoundingMode.HALF_UP);
                        ro.setPercentage(percentage);
                    } else {
                        ro.setPercentage(BigDecimal.ZERO);
                    }
                    return ro;
                })
                .collect(Collectors.toList());

        dashboardRO.setAssetDetails(assetDetails);
        dashboardRO.setTotalAmount(typeTotalAmount);
        dashboardRO.setTotalAssetCount((long) assetDetails.size());

        return Result.success(dashboardRO);
    }

    private DashboardRO buildDashboardData() {
        DashboardRO dashboardRO = new DashboardRO();

        // 查询总持仓金额
        BigDecimal totalAmount = dashboardMapper.selectTotalAmount();
        dashboardRO.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);

        // 查询总资产数量
        Long totalAssetCount = dashboardMapper.selectTotalAssetCount();
        dashboardRO.setTotalAssetCount(totalAssetCount != null ? totalAssetCount : 0L);

        // 查询各资产类型统计
        List<AssetTypeStatRO> assetTypeStats = dashboardMapper.selectAssetTypeStats();

        // 计算各资产类型占比
        if (assetTypeStats != null && !assetTypeStats.isEmpty()) {
            BigDecimal finalTotal = dashboardRO.getTotalAmount();
            for (AssetTypeStatRO stat : assetTypeStats) {
                if (finalTotal != null && finalTotal.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal percentage = stat.getTotalAmount()
                            .divide(finalTotal, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .setScale(2, RoundingMode.HALF_UP);
                    stat.setPercentage(percentage);
                } else {
                    stat.setPercentage(BigDecimal.ZERO);
                }
            }
        }

        dashboardRO.setAssetTypeStats(assetTypeStats);
        return dashboardRO;
    }
}
