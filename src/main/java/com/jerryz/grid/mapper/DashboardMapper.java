package com.jerryz.grid.mapper;

import com.jerryz.grid.pojo.vo.dashboard.AssetTypeStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 首页大屏数据Mapper
 */
@Mapper
public interface DashboardMapper {

    /**
     * 查询各资产类型统计
     * @return 各资产类型统计列表
     */
    List<AssetTypeStatVO> selectAssetTypeStats();

    /**
     * 查询总持仓金额
     * @return 总持仓金额
     */
    BigDecimal selectTotalAmount();

    /**
     * 查询总资产数量（不同资产代码的数量）
     * @return 总资产数量
     */
    Long selectTotalAssetCount();

    /**
     * 查询各资产的持仓金额和数量
     * @return 各资产统计列表
     */
    List<AssetDetailVO> selectAssetDetails();

    /**
     * 根据资产类型查询各资产的持仓金额和数量
     * @param assetType 资产类型
     * @return 指定类型的资产统计列表
     */
    List<AssetDetailVO> selectAssetDetailsByType(Integer assetType);

    /**
     * 资产详情VO
     */
    @lombok.Data
    class AssetDetailVO {
        private String assetCode;
        private String assetName;
        private Integer assetType;
        private BigDecimal totalAmount;
        private BigDecimal currentQuantity;
    }
}
