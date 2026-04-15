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
 * 卖出数据处理策略
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/13 11:11
 */
@Component
@RequiredArgsConstructor
public class SellPositionRecordTransactionStrategy implements IPositionRecordTransactionStrategy {

    private final PositionRecordMapper positionRecordMapper;

    /**
     * 卖出记录处理逻辑
     *
     * 主要步骤：
     * 1. 设置交易基本信息（资产类型、代码、价格、数量、金额等）
     * 2. 查询该资产代码的所有交易记录（包括买入和卖出）
     * 3. 计算当前实际持有数量 = 所有买入数量之和 - 所有卖出数量之和（包括本次）
     * 4. 获取最后一次买入记录的平均成本作为本次卖出的成本基准
     * 5. 判断清仓状态：
     *    - 如果卖出后持有数量为0，设置status=2（已清仓）
     *    - 否则设置status=1（持有中）
     * 6. 设置average_cost和current_quantity字段
     *
     * 注意事项：
     * - 卖出操作不重新计算平均成本，保持最后一次买入的平均成本不变
     * - 只更新current_quantity字段用于反映当前持仓状态
     * - current_quantity反映的是本次交易后的实际持有数量
     * - 需要校验卖出数量不能超过当前持有数量（在业务层实现）
     * - 后续的买入会使用卖出时的average_cost来计算卖出对应的成本
     *
     * @param positionRecordVO 请求参数，包含卖出的资产信息和交易详情
     * @return PositionRecord 构建好的卖出记录对象，包含计算后的持仓信息
     */
    @Override
    public PositionRecord handle(PositionRecordVO positionRecordVO) {

        PositionRecord positionRecord = new PositionRecord();
        positionRecord.setAssetType(positionRecordVO.getAssetType());
        positionRecord.setAssetCode(positionRecordVO.getAssetCode());
        positionRecord.setAssetName(positionRecordVO.getAssetName());
        positionRecord.setTransactionType(TransactionTypeEm.SELL.getCode());
        positionRecord.setTransactionDate(positionRecordVO.getTransactionDate());
        positionRecord.setTransactionTime(positionRecordVO.getTransactionTime());
        positionRecord.setPrice(positionRecordVO.getPrice());
        positionRecord.setQuantity(positionRecordVO.getQuantity());
        positionRecord.setAmount(positionRecordVO.getAmount());
        positionRecord.setFee(positionRecordVO.getFee());
        positionRecord.setSettlementStatus(1);
        positionRecord.setTrackIndexCode(positionRecordVO.getTrackIndexCode());
        positionRecord.setTrackIndex(positionRecordVO.getTrackIndex());

        //查询当前代码所有交易记录（包括买入和卖出），计算出当前实际持有数量
        List<PositionRecord> allRecords = positionRecordMapper.selectAllRecordsByAssetCode(positionRecordVO.getAssetCode());

        BigDecimal averageCost = BigDecimal.ZERO;
        BigDecimal currentQuantity = BigDecimal.ZERO;

        if(!allRecords.isEmpty()){
            //获取最后一次买入记录的平均成本
            averageCost = allRecords.stream()
                    .filter(r -> r.getTransactionType().equals(TransactionTypeEm.BUY.getCode()))
                    .reduce((first, second) -> second) //获取最后一个买入记录
                    .map(PositionRecord::getAverageCost)
                    .orElse(BigDecimal.ZERO);

            //计算当前实际持有数量 = 所有买入数量之和 - 所有卖出数量之和（包括本次卖出）
            BigDecimal buyQuantity = allRecords.stream()
                    .filter(r -> r.getTransactionType().equals(TransactionTypeEm.BUY.getCode()))
                    .map(PositionRecord::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(4, RoundingMode.HALF_UP);

            BigDecimal sellQuantity = allRecords.stream()
                    .filter(r -> r.getTransactionType().equals(TransactionTypeEm.SELL.getCode()))
                    .map(PositionRecord::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .add(positionRecordVO.getQuantity()) //加上本次卖出数量
                    .setScale(4, RoundingMode.HALF_UP);

            currentQuantity = buyQuantity.subtract(sellQuantity).setScale(4, RoundingMode.HALF_UP);

            //判断是否清仓
            if(currentQuantity.compareTo(BigDecimal.ZERO) == 0){
                positionRecord.setStatus(2); //已清仓
            }else {
                positionRecord.setStatus(1); //持有中
            }
        }

        positionRecord.setAverageCost(averageCost);
        positionRecord.setCurrentQuantity(currentQuantity);

        return positionRecord;
    }

    @Override
    public Integer getType() {
        return TransactionTypeEm.SELL.getCode();
    }
}
