package com.jerryz.grid.service.strategy.impl;

import com.jerryz.grid.mapper.PositionRecordMapper;
import com.jerryz.grid.pojo.po.PositionRecord;
import com.jerryz.grid.pojo.vo.PositionRecordVO;
import com.jerryz.grid.service.strategy.IPositionRecordTransactionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 买入策略数据处理
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/13 11:07
 */
@Component
@RequiredArgsConstructor
public class BuyInPositionRecordTransactionStrategy implements IPositionRecordTransactionStrategy {

    private final PositionRecordMapper positionRecordMapper;

    /**
     * 1.买入记录，根据当前代码历史记录，计算出当前平均成本和持有数量
     * @param positionRecordVO 请求参数
     * @return 记录
     */
    @Override
    public PositionRecord handle(PositionRecordVO positionRecordVO) {
        return null;
    }
}
