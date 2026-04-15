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
     * 计算公式说明：
     * 
     * 【实际持有数量计算】
     * 实际持有数量 = Σ买入数量 - Σ卖出数量（包括本次）
     * 
     * 【平均成本保持不变】
     * 卖出时的average_cost = 最后一次买入时的average_cost
     * 注意：卖出操作不会重新计算平均成本
     *
     * 【清仓状态判断】
     * - 如果实际持有数量 = 0，则status = 2（已清仓）
     * - 如果实际持有数量 > 0，则status = 1（持有中）
     * 
     * 
     * 具体例子说明：
     * 
     * 【例子1：首次卖出（部分卖出）】
     * 历史记录：
     *   - 买入200股，价格9.00元，金额1800.00元，average_cost=9.00
     *   - 买入300股，价格11.00元，金额3300.00元，average_cost=10.125
     * 本次卖出：100股，价格15.00元，金额1500.00元
     * 
     * 计算：
     * - 最后一次买入的average_cost = 10.125
     * - 实际持有数量 = (200 + 300) - 100 = 400股
     * 
     * 结果：
     * - average_cost = 10.125（保持最后一次买入的成本）
     * - current_quantity = 400.0000
     * - status = 1（持有中）
     * 
     * 【例子2：全部卖出（清仓）】
     * 历史记录：
     *   - 买入100股，价格10.00元，金额1000.00元，average_cost=10.00
     * 本次卖出：100股，价格15.00元，金额1500.00元
     * 
     * 计算：
     * - 最后一次买入的average_cost = 10.00
     * - 实际持有数量 = 100 - 100 = 0股
     * 
     * 结果：
     * - average_cost = 10.00
     * - current_quantity = 0.0000
     * - status = 2（已清仓）
     * 
     * 【例子3：多次买入后卖出】
     * 历史记录：
     *   - 买入100股，价格8.00元，金额800.00元
     *   - 买入200股，价格9.00元，金额1800.00元
     *   - 买入300股，价格10.00元，金额3000.00元
     * 本次卖出：200股，价格12.00元，金额2400.00元
     * 
     * 计算：
     * - 最后一次买入的average_cost = 10.00（300股那笔）
     * - 实际持有数量 = (100 + 200 + 300) - 200 = 400股
     * 
     * 结果：
     * - average_cost = 10.00
     * - current_quantity = 400.0000
     * - status = 1（持有中）
     * 
     * 【例子4：卖出后再次卖出】
     * 历史记录：
     *   - 买入200股，价格10.00元，金额2000.00元，average_cost=10.00
     *   - 卖出50股，价格15.00元，金额750.00元，average_cost=10.00
     * 本次卖出：100股，价格16.00元，金额1600.00元
     * 
     * 计算：
     * - 最后一次买入的average_cost = 10.00
     * - 实际持有数量 = 200 - 50 - 100 = 50股
     * 
     * 结果：
     * - average_cost = 10.00
     * - current_quantity = 50.0000
     * - status = 1（持有中）
     * 
     * 【例子5：连续卖出至清仓】
     * 历史记录：
     *   - 买入500股，价格10.00元，金额5000.00元，average_cost=10.00
     *   - 卖出200股，价格15.00元，金额3000.00元，average_cost=10.00
     *   - 卖出200股，价格18.00元，金额3600.00元，average_cost=10.00
     * 本次卖出：100股，价格20.00元，金额2000.00元
     * 
     * 计算：
     * - 最后一次买入的average_cost = 10.00
     * - 实际持有数量 = 500 - 200 - 200 - 100 = 0股
     * 
     * 结果：
     * - average_cost = 10.00
     * - current_quantity = 0.0000
     * - status = 2（已清仓）
     * 
     * 【例子6：清仓后再次买入并卖出】
     * 历史记录：
     *   - 买入100股，价格10.00元，金额1000.00元，average_cost=10.00
     *   - 卖出100股，价格15.00元，金额1500.00元，average_cost=10.00（清仓）
     *   - 买入50股，价格12.00元，金额600.00元，average_cost=12.00（重新开始）
     * 本次卖出：50股，价格18.00元，金额900.00元
     * 
     * 计算：
     * - 最后一次买入的average_cost = 12.00（重新买入那笔）
     * - 实际持有数量 = 100 - 100 + 50 - 50 = 0股
     * 
     * 结果：
     * - average_cost = 12.00
     * - current_quantity = 0.0000
     * - status = 2（已清仓）
     * 
     * 注意事项：
     * - 卖出操作不重新计算平均成本，保持最后一次买入的平均成本不变
     * - 只更新current_quantity字段用于反映当前持仓状态
     * - current_quantity反映的是本次交易后的实际持有数量（包括本次卖出）
     * - 需要校验卖出数量不能超过当前持有数量（在业务层实现）
     * - 后续的买入会使用卖出时的average_cost来计算卖出对应的成本
     * - 卖出价格不影响平均成本，只影响利润计算（在其他地方实现）
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
