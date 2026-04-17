package com.jerryz.grid.service.impl;

import com.jerryz.grid.mapper.DashboardMapper;
import com.jerryz.grid.pojo.ro.Result;
import com.jerryz.grid.pojo.vo.dashboard.AssetDetailVO;
import com.jerryz.grid.pojo.vo.dashboard.AssetTypeStatVO;
import com.jerryz.grid.pojo.vo.dashboard.DashboardVO;
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
    public Result<DashboardVO> getDashboardData() {
        DashboardVO dashboardVO = buildDashboardData();
        return Result.success(dashboardVO);
    }

    @Override
    public Result<DashboardVO> getAssetTypeRatio() {
        DashboardVO dashboardVO = buildDashboardData();
        return Result.success(dashboardVO);
    }

    @Override
    public Result<DashboardVO> getAssetAmountStats() {
        DashboardVO dashboardVO = buildDashboardData();
        return Result.success(dashboardVO);
    }

    @Override
    public Result<DashboardVO> getAssetDetailsByType(Integer assetType) {
        DashboardVO dashboardVO = new DashboardVO();
        
        // 设置选中的资产类型
        dashboardVO.setSelectedAssetType(assetType);
        dashboardVO.setSelectedAssetTypeName(ASSET_TYPE_NAMES.getOrDefault(assetType, "未知"));
        
        // 查询该类型下的资产详情
        List<DashboardMapper.AssetDetailVO> detailVOList = dashboardMapper.selectAssetDetailsByType(assetType);
        
        // 计算该类型的总金额
        BigDecimal typeTotalAmount = detailVOList.stream()
                .map(DashboardMapper.AssetDetailVO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 转换为VO并计算占比
        List<AssetDetailVO> assetDetails = detailVOList.stream()
                .map(detail -> {
                    AssetDetailVO vo = new AssetDetailVO();
                    vo.setAssetCode(detail.getAssetCode());
                    vo.setAssetName(detail.getAssetName());
                    vo.setAssetType(detail.getAssetType());
                    vo.setTotalAmount(detail.getTotalAmount());
                    vo.setCurrentQuantity(detail.getCurrentQuantity());
                    
                    // 计算占比
                    if (typeTotalAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal percentage = detail.getTotalAmount()
                                .divide(typeTotalAmount, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(2, RoundingMode.HALF_UP);
                        vo.setPercentage(percentage);
                    } else {
                        vo.setPercentage(BigDecimal.ZERO);
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        
        dashboardVO.setAssetDetails(assetDetails);
        dashboardVO.setTotalAmount(typeTotalAmount);
        dashboardVO.setTotalAssetCount((long) assetDetails.size());
        
        return Result.success(dashboardVO);
    }

    private DashboardVO buildDashboardData() {
        DashboardVO dashboardVO = new DashboardVO();

        // 查询总持仓金额
        BigDecimal totalAmount = dashboardMapper.selectTotalAmount();
        dashboardVO.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);

        // 查询总资产数量
        Long totalAssetCount = dashboardMapper.selectTotalAssetCount();
        dashboardVO.setTotalAssetCount(totalAssetCount != null ? totalAssetCount : 0L);

        // 查询各资产类型统计
        List<AssetTypeStatVO> assetTypeStats = dashboardMapper.selectAssetTypeStats();

        // 计算各资产类型占比
        if (assetTypeStats != null && !assetTypeStats.isEmpty()) {
            BigDecimal finalTotal = dashboardVO.getTotalAmount();
            for (AssetTypeStatVO stat : assetTypeStats) {
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

        dashboardVO.setAssetTypeStats(assetTypeStats);
        return dashboardVO;
    }
}
