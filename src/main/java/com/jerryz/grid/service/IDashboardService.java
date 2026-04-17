package com.jerryz.grid.service;

import com.jerryz.grid.pojo.ro.Result;
import com.jerryz.grid.pojo.vo.dashboard.DashboardVO;

/**
 * 首页大屏Service接口
 */
public interface IDashboardService {

    /**
     * 获取首页大屏数据
     * @return 大屏数据
     */
    Result<DashboardVO> getDashboardData();

    /**
     * 获取各资产类型占比数据（用于饼图）
     * @return 大屏数据
     */
    Result<DashboardVO> getAssetTypeRatio();

    /**
     * 获取各资产金额数据（用于柱状图）
     * @return 大屏数据
     */
    Result<DashboardVO> getAssetAmountStats();

    /**
     * 根据资产类型获取该类型下各资产的持仓占比
     * @param assetType 资产类型
     * @return 资产详情数据
     */
    Result<DashboardVO> getAssetDetailsByType(Integer assetType);
}
