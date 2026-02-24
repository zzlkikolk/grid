package com.jerryz.grid.service.strategy.impl;

import com.jerryz.grid.em.TransactionTypeEm;
import com.jerryz.grid.mapper.PositionRecordMapper;
import com.jerryz.grid.pojo.po.PositionRecord;
import com.jerryz.grid.pojo.vo.PositionRecordVO;
import com.jerryz.grid.service.strategy.IPositionRecordTransactionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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

        PositionRecord positionRecord = new PositionRecord();
        positionRecord.setAssetType(positionRecordVO.getAssetType());
        positionRecord.setAssetCode(positionRecordVO.getAssetCode());
        positionRecord.setAssetName(positionRecordVO.getAssetName());
        positionRecord.setTransactionType(TransactionTypeEm.BUY.getCode());
        positionRecord.setTransactionDate(positionRecordVO.getTransactionDate());
        positionRecord.setTransactionTime(positionRecordVO.getTransactionTime());
        positionRecord.setPrice(positionRecordVO.getPrice());
        positionRecord.setQuantity(positionRecordVO.getQuantity());
        positionRecord.setAmount(positionRecordVO.getAmount());
        positionRecord.setFee(positionRecordVO.getFee());
        positionRecord.setStatus(1);
        positionRecord.setSettlementStatus(1);

        //查询当前代码历史记录，计算出当前平均成本和持有数量
        List<PositionRecord> positionRecordList = positionRecordMapper.selectAddRecordByAssetCode(positionRecordVO.getAssetCode());

        BigDecimal averageCost;
        BigDecimal currentQuantity;

        if(positionRecordList.isEmpty()){
            averageCost = positionRecordVO.getPrice();
            currentQuantity = positionRecordVO.getQuantity();
        }else {
            //新平均成本 = (原总持仓金额 + 新增持仓金额) / (原总持仓数量 + 新增持仓数量)
            BigDecimal allAmount = positionRecordList.stream()
                    .map(PositionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .add(positionRecordVO.getAmount())
                    .setScale(4, RoundingMode.HALF_UP);

            currentQuantity = positionRecordList.stream()
                    .map(PositionRecord::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .add(positionRecordVO.getQuantity())
                    .setScale(4, RoundingMode.HALF_UP);

            averageCost = allAmount.divide(currentQuantity, 4, RoundingMode.HALF_UP);
        }

        positionRecord.setAverageCost(averageCost);
        positionRecord.setCurrentQuantity(currentQuantity);

        return positionRecord;
    }

    @Override
    public Integer getType() {
        return TransactionTypeEm.BUY.getCode();
    }
}
