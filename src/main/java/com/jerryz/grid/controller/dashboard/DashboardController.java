package com.jerryz.grid.controller.dashboard;

import com.jerryz.grid.pojo.ro.Result;
import com.jerryz.grid.pojo.ro.dashboard.DashboardRO;
import com.jerryz.grid.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页大屏Controller
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardServiceImpl;

    /**
     * 获取首页大屏数据
     * @return 大屏数据
     */
    @GetMapping("/data")
    public Result<DashboardRO> getDashboardData() {
        return dashboardServiceImpl.getDashboardData();
    }

    /**
     * 获取各资产类型占比数据（用于饼图）
     * @return 资产类型占比数据
     */
    @GetMapping("/asset_type_ratio")
    public Result<DashboardRO> getAssetTypeRatio() {
        return dashboardServiceImpl.getAssetTypeRatio();
    }

    /**
     * 获取各资产金额数据（用于柱状图）
     * @return 资产金额数据
     */
    @GetMapping("/asset_amount_stats")
    public Result<DashboardRO> getAssetAmountStats() {
        return dashboardServiceImpl.getAssetAmountStats();
    }

    /**
     * 根据资产类型获取该类型下各资产的持仓占比
     * @param assetType 资产类型
     * @return 资产详情数据
     */
    @GetMapping("/asset_details")
    public Result<DashboardRO> getAssetDetailsByType(@RequestParam Integer assetType) {
        return dashboardServiceImpl.getAssetDetailsByType(assetType);
    }
}
