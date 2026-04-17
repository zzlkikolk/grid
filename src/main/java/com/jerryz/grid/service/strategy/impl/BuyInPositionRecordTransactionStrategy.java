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
     * 买入记录处理逻辑
     *
     * 主要步骤：
     * 1. 设置交易基本信息（资产类型、代码、价格、数量、金额等）
     * 2. 查询该资产代码的所有交易记录（包括买入和卖出）
     * 3. 计算实际持仓情况：
     *    - 实际持有数量 = 所有买入数量之和 - 所有卖出数量之和
     *    - 实际持有成本 = 所有买入金额 - 所有卖出对应的成本（使用卖出时的average_cost）
     * 4. 计算新的平均成本和持有数量：
     *    - 如果实际持有数量为0（已清仓）：平均成本 = 本次买入价格，持有数量 = 本次买入数量
     *    - 如果有实际持仓：使用加权平均法计算
     *      - 新平均成本 = (原实际持有成本 + 本次买入金额) / (原实际持有数量 + 本次买入数量)
     *      - 新持有数量 = 原实际持有数量 + 本次买入数量
     * 5. 设置average_cost和current_quantity字段
     * 6. 设置状态为持有中（status=1）
     *
     * 计算公式说明：
     * 
     * 【实际持有数量计算】
     * 实际持有数量 = Σ买入数量 - Σ卖出数量
     * 
     * 【实际持有成本计算】
     * 实际持有成本 = Σ买入金额 - Σ(卖出数量 × 卖出时的平均成本)
     * 注意：卖出对应的成本使用卖出时的average_cost，而不是卖出价格
     * 
     * 【新平均成本计算】
     * 新平均成本 = (原实际持有成本 + 本次买入金额) / (原实际持有数量 + 本次买入数量)
     * 
     * 
     * 具体例子说明：
     * 
     * 【例子1：首次买入】
     * 历史记录：无
     * 本次买入：100股，价格10.00元，金额1000.00元
     * 
     * 计算：
     * - 实际持有数量 = 0 - 0 = 0（首次买入）
     * - 实际持有成本 = 0 - 0 = 0
     * - 新平均成本 = 1000.00 / 100 = 10.00元
     * - 新持有数量 = 100股
     * 
     * 结果：
     * - average_cost = 10.00
     * - current_quantity = 100.0000
     * 
     * 【例子2：多次买入（无卖出）】
     * 历史记录：
     *   - 买入200股，价格9.00元，金额1800.00元
     *   - 买入300股，价格11.00元，金额3300.00元
     * 本次买入：100股，价格10.00元，金额1000.00元
     * 
     * 计算：
     * - 实际持有数量 = (200 + 300) - 0 = 500股
     * - 实际持有成本 = (1800.00 + 3300.00) - 0 = 5100.00元
     * - 新平均成本 = (5100.00 + 1000.00) / (500 + 100) = 6100.00 / 600 = 10.1667元
     * - 新持有数量 = 500 + 100 = 600股
     * 
     * 结果：
     * - average_cost = 10.1667
     * - current_quantity = 600.0000
     * 
     * 【例子3：部分卖出后买入】
     * 历史记录：
     *   - 买入100股，价格10.00元，金额1000.00元，average_cost=10.00
     *   - 买入100股，价格15.00元，金额1500.00元，average_cost=12.50
     *   - 卖出150股，价格20.00元，金额3000.00元，average_cost=12.50
     * 本次买入：50股，价格12.00元，金额600.00元
     * 
     * 计算：
     * - 实际持有数量 = (100 + 100) - 150 = 50股
     * - 实际持有成本 = (1000.00 + 1500.00) - (150 × 12.50) = 2500.00 - 1875.00 = 625.00元
     * - 新平均成本 = (625.00 + 600.00) / (50 + 50) = 1225.00 / 100 = 12.25元
     * - 新持有数量 = 50 + 50 = 100股
     * 
     * 结果：
     * - average_cost = 12.25
     * - current_quantity = 100.0000
     * 
     * 【例子4：全部清仓后重新买入】
     * 历史记录：
     *   - 买入100股，价格10.00元，金额1000.00元，average_cost=10.00
     *   - 卖出100股，价格15.00元，金额1500.00元，average_cost=10.00（清仓）
     * 本次买入：50股，价格12.00元，金额600.00元
     * 
     * 计算：
     * - 实际持有数量 = 100 - 100 = 0（已清仓）
     * - 实际持有成本 = 1000.00 - (100 × 10.00) = 0元
     * - 因为实际持有数量为0，按首次买入处理：
     *   - 新平均成本 = 本次买入价格 = 12.00元
     *   - 新持有数量 = 本次买入数量 = 50股
     * 
     * 结果：
     * - average_cost = 12.00
     * - current_quantity = 50.0000
     * 
     * 【例子5：多次买入卖出后买入】
     * 历史记录：
     *   - 买入100股，价格10.00元，金额1000.00元
     *   - 买入50股，价格12.00元，金额600.00元
     *   - 卖出80股，价格15.00元，金额1200.00元，average_cost=10.67
     *   - 买入30股，价格14.00元，金额420.00元
     *   - 卖出50股，价格18.00元，金额900.00元，average_cost=11.00
     * 本次买入：20股，价格11.00元，金额220.00元
     * 
     * 计算：
     * - 实际持有数量 = (100 + 50 + 30) - (80 + 50) = 180 - 130 = 50股
     * - 实际持有成本 = (1000.00 + 600.00 + 420.00) - (80 × 10.67) - (50 × 11.00)
     *                   = 2020.00 - 853.60 - 550.00 = 616.40元
     * - 新平均成本 = (616.40 + 220.00) / (50 + 20) = 836.40 / 70 = 11.9486元
     * - 新持有数量 = 50 + 20 = 70股
     * 
     * 结果：
     * - average_cost = 11.9486
     * - current_quantity = 70.0000
     * 
     * 注意事项：
     * - 必须考虑卖出记录对持仓数量和成本的影响
     * - 卖出对应的成本使用卖出时的average_cost，而非卖出价格
     * - 如果之前已全部清仓（实际持有数量为0），新的买入按首次买入处理
     * - 金额计算保留4位小数，避免精度丢失
     * - 每次买入都会重新计算平均成本和持有数量
     * - current_quantity字段记录的是本次交易后的实际持有数量，用于后续计算
     *
     * @param positionRecordVO 请求参数，包含买入的资产信息和交易详情
     * @return PositionRecord 构建好的买入记录对象，包含计算后的持仓信息
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
        positionRecord.setTrackIndexCode(positionRecordVO.getTrackIndexCode());
        positionRecord.setTrackIndex(positionRecordVO.getTrackIndex());

        //查询当前代码所有交易记录（包括买入和卖出），计算出当前实际持仓情况
        List<PositionRecord> allRecords = positionRecordMapper.selectAllRecordsByAssetCode(positionRecordVO.getAssetCode());

        BigDecimal averageCost;
        BigDecimal currentQuantity;

        if(allRecords.isEmpty()){
            //首次买入：平均成本 = 本次买入价格，持有数量 = 本次买入数量
            averageCost = positionRecordVO.getPrice();
            currentQuantity = positionRecordVO.getQuantity();
        }else {
            //计算实际持有数量 = 所有买入数量之和 - 所有卖出数量之和
            BigDecimal buyQuantity = allRecords.stream()
                    .filter(r -> r.getTransactionType().equals(TransactionTypeEm.BUY.getCode()))
                    .map(PositionRecord::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(4, RoundingMode.HALF_UP);

            BigDecimal sellQuantity = allRecords.stream()
                    .filter(r -> r.getTransactionType().equals(TransactionTypeEm.SELL.getCode()))
                    .map(PositionRecord::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(4, RoundingMode.HALF_UP);

            BigDecimal existingQuantity = buyQuantity.subtract(sellQuantity);

            //如果之前已清仓，按首次买入处理
            if(existingQuantity.compareTo(BigDecimal.ZERO) == 0){
                averageCost = positionRecordVO.getPrice();
                currentQuantity = positionRecordVO.getQuantity();
            }else {
                //计算实际持有成本 = 所有买入金额(含手续费) - 所有卖出对应的成本
                //买入金额应包含手续费，这样才能准确计算持仓成本
                //卖出对应的成本使用卖出时的average_cost
                BigDecimal buyAmount = allRecords.stream()
                        .filter(r -> r.getTransactionType().equals(TransactionTypeEm.BUY.getCode()))
                        .map(r -> {
                            BigDecimal amount = r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO;
                            BigDecimal fee = r.getFee() != null ? r.getFee() : BigDecimal.ZERO;
                            BigDecimal tax = r.getTax() != null ? r.getTax() : BigDecimal.ZERO;
                            return amount.add(fee).add(tax);
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(4, RoundingMode.HALF_UP);

                BigDecimal sellCost = allRecords.stream()
                        .filter(r -> r.getTransactionType().equals(TransactionTypeEm.SELL.getCode()))
                        .map(r -> r.getQuantity().multiply(r.getAverageCost() != null ? r.getAverageCost() : BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(4, RoundingMode.HALF_UP);

                BigDecimal existingCost = buyAmount.subtract(sellCost);

                //本次买入成本也要包含手续费
                BigDecimal currentBuyCost = positionRecordVO.getAmount()
                        .add(positionRecordVO.getFee() != null ? positionRecordVO.getFee() : BigDecimal.ZERO)
                        .add(positionRecordVO.getTax() != null ? positionRecordVO.getTax() : BigDecimal.ZERO)
                        .setScale(4, RoundingMode.HALF_UP);

                //新平均成本 = (原实际持有成本 + 本次买入成本(含手续费)) / (原实际持有数量 + 本次买入数量)
                BigDecimal newTotalCost = existingCost.add(currentBuyCost).setScale(4, RoundingMode.HALF_UP);
                currentQuantity = existingQuantity.add(positionRecordVO.getQuantity()).setScale(4, RoundingMode.HALF_UP);
                averageCost = newTotalCost.divide(currentQuantity, 4, RoundingMode.HALF_UP);
            }
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
