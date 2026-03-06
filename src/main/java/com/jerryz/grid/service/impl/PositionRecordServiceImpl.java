package com.jerryz.grid.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jerryz.grid.mapper.PositionRecordMapper;
import com.jerryz.grid.pojo.po.PositionRecord;
import com.jerryz.grid.pojo.ro.PageResult;
import com.jerryz.grid.pojo.ro.Result;
import com.jerryz.grid.pojo.ro.positionRecord.PriceChangeRateRO;
import com.jerryz.grid.pojo.vo.PageVO;
import com.jerryz.grid.pojo.vo.PositionRecordPageVO;
import com.jerryz.grid.pojo.vo.PositionRecordVO;
import com.jerryz.grid.pojo.vo.positionRecord.PredictPriceChangeRateVO;
import com.jerryz.grid.service.IPositionRecordService;
import com.jerryz.grid.service.strategy.IPositionRecordTransactionStrategy;
import com.jerryz.grid.service.strategy.PositionRecordTransactionStrategyFactory;
import com.jerryz.grid.util.ComputedAverageCostUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author zhangzhilin
 * @version 2026
 */
@Service
@RequiredArgsConstructor
public class PositionRecordServiceImpl implements IPositionRecordService {

    private final PositionRecordMapper positionRecordMapper;

    private final PositionRecordTransactionStrategyFactory positionRecordTransactionStrategyFactory;

    /**
     * 保存持仓记录
     * 1.买入记录，根据当前代码历史记录，计算出当前平均成本和持有数量
     * 2.卖出记录，计算持有数量
     * 3.当全部卖出时，status为已清仓
     * @param positionRecordVO 持仓信息
     * @return 结果
     */
    @Override
    public Result<Void> save(PositionRecordVO positionRecordVO) {

        IPositionRecordTransactionStrategy positionRecordTransactionStrategy = positionRecordTransactionStrategyFactory
                .getTransactionStrategy(positionRecordVO.getTransactionType());

        if(positionRecordTransactionStrategy == null){
            throw new IllegalArgumentException("策略不存在");
        }

        PositionRecord positionRecord = positionRecordTransactionStrategy.handle(positionRecordVO);
        positionRecordMapper.insert(positionRecord);
        return Result.success();
    }

    @Override
    public PageResult<PositionRecord> selectList(PositionRecordPageVO pageVO) {
        Page<PositionRecord> page = new Page<>(pageVO.getPageNum(), pageVO.getPageSize());

        List<PositionRecord> positionRecordList = positionRecordMapper.selectPageListByAssetCode(page, pageVO.getAssetCode());

        return PageResult.success(page.getPages(),page.getSize(),page.getTotal(),positionRecordList);
    }

    /**
     * 预测价格变化率
     * @param predictPriceChangeRateVO 预测价格变化率参数
     * @return 结果
     */
    @Override
    public Result<PriceChangeRateRO> predictPriceChangeRate(PredictPriceChangeRateVO predictPriceChangeRateVO) {

        List<PositionRecord> positionRecordList = positionRecordMapper.selectAddRecordByAssetCode(predictPriceChangeRateVO.getAssetCode());
        if(CollectionUtils.isEmpty(positionRecordList)){
            return Result.success();
        }

        ComputedAverageCostUtil.UserMemberConsumerResult  userMemberConsumerResult = ComputedAverageCostUtil.computed(positionRecordList,predictPriceChangeRateVO.getTodayTrackIndex() ,predictPriceChangeRateVO.getLocalPrice());
        PriceChangeRateRO priceChangeRateRO = new PriceChangeRateRO();
        BeanUtils.copyProperties(userMemberConsumerResult,priceChangeRateRO);
        priceChangeRateRO.setAssetCode(predictPriceChangeRateVO.getAssetCode());
        priceChangeRateRO.setAssetName(positionRecordList.get(0).getAssetName());
        return Result.success(priceChangeRateRO);
    }
}
