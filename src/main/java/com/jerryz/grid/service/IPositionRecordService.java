package com.jerryz.grid.service;

import com.jerryz.grid.pojo.po.PositionRecord;
import com.jerryz.grid.pojo.ro.PageResult;
import com.jerryz.grid.pojo.ro.Result;
import com.jerryz.grid.pojo.ro.positionRecord.PriceChangeRateRO;
import com.jerryz.grid.pojo.vo.PageVO;
import com.jerryz.grid.pojo.vo.PositionRecordPageVO;
import com.jerryz.grid.pojo.vo.PositionRecordVO;
import com.jerryz.grid.pojo.vo.positionRecord.PredictPriceChangeRateVO;

import java.util.List;

/**
 * @author zhangzhilin
 * @version 2026
 */
public interface IPositionRecordService {

    Result<Void> save(PositionRecordVO positionRecordVO);

    PageResult<PositionRecord> selectList(PositionRecordPageVO pageVO);

    Result<PriceChangeRateRO> predictPriceChangeRate(PredictPriceChangeRateVO predictPriceChangeRateVO);
}
