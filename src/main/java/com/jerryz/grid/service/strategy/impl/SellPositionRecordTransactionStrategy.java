package com.jerryz.grid.service.strategy.impl;

import com.jerryz.grid.pojo.po.PositionRecord;
import com.jerryz.grid.pojo.vo.PositionRecordVO;
import com.jerryz.grid.service.strategy.IPositionRecordTransactionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 卖出数据处理策略
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/13 11:11
 */
@Component
@RequiredArgsConstructor
public class SellPositionRecordTransactionStrategy implements IPositionRecordTransactionStrategy {

    /**
     * 卖出记录，计算持有数量
     * 当全部卖出时，status为已清仓
     * @param positionRecordVO 请求参数
     * @return 记录
     */
    @Override
    public PositionRecord handle(PositionRecordVO positionRecordVO) {
        return null;
    }
}
