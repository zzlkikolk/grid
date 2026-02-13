package com.jerryz.grid.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 交易记录策略工厂
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/13 11:04
 */
@Service
public class PositionRecordTransactionStrategyFactory {

    private final Map<Integer,IPositionRecordTransactionStrategy> positionRecordTransactionStrategyMap;

    public PositionRecordTransactionStrategyFactory(List<IPositionRecordTransactionStrategy> positionRecordTransactionStrategyList) {
        this.positionRecordTransactionStrategyMap = positionRecordTransactionStrategyList.stream()
                .collect(Collectors.toMap(IPositionRecordTransactionStrategy::getType,p->p));
    }

    /**
     * 根据类型获取交易记录处理策略
     * @param type 类型
     * @return 策略实现
     */
    public IPositionRecordTransactionStrategy getTransactionStrategy(Integer type){
        return positionRecordTransactionStrategyMap.get(type);
    }
}
