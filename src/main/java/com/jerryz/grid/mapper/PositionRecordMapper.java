package com.jerryz.grid.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jerryz.grid.pojo.po.PositionRecord;

import java.util.List;

/**
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/06 17:59
 */
public interface PositionRecordMapper extends BaseMapper<PositionRecord> {

    /**
     * 根据代码查找持仓记录
     * @param assetCode 代码
     * @return 持仓记录列表
     */
    List<PositionRecord> selectAddRecordByAssetCode(String assetCode);

    List<PositionRecord> selectPageListByAssetCode(Page<PositionRecord> page,String assetCode);
}
