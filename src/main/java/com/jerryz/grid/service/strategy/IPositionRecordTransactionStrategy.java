package com.jerryz.grid.service.strategy;

import com.jerryz.grid.pojo.po.PositionRecord;
import com.jerryz.grid.pojo.vo.PositionRecordVO;

/**
 * 交易策略
 * 根据不同交易类型，差异处理数据
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/13 11:00
 */
public interface IPositionRecordTransactionStrategy {

    /**
     * 根据不同的交易类型，计算交易记录
     * @param positionRecordVO 请求参数
     * @return 交易记录
     */
    PositionRecord handle(PositionRecordVO positionRecordVO);

    Integer getType();
}
