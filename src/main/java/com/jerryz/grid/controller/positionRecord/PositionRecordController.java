package com.jerryz.grid.controller.positionRecord;

import com.jerryz.grid.pojo.po.PositionRecord;
import com.jerryz.grid.pojo.ro.PageResult;
import com.jerryz.grid.pojo.ro.Result;
import com.jerryz.grid.pojo.ro.positionRecord.PriceChangeRateRO;
import com.jerryz.grid.pojo.vo.PageVO;
import com.jerryz.grid.pojo.vo.PositionRecordPageVO;
import com.jerryz.grid.pojo.vo.PositionRecordVO;
import com.jerryz.grid.pojo.vo.positionRecord.PredictPriceChangeRateVO;
import com.jerryz.grid.service.IPositionRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhangzhilin
 * @version 2026
 */
@RestController
@RequiredArgsConstructor
public class PositionRecordController {

    private final IPositionRecordService positionRecordServiceImpl;


    @PostMapping("/position_record/save")
    public Result<Void> savePositionRecord(@Valid @RequestBody PositionRecordVO positionRecordVO){
        return positionRecordServiceImpl.save(positionRecordVO);
    }

    @GetMapping("/position_record/list")
    public PageResult<PositionRecord> selectPositionRecordList(@Valid  PositionRecordPageVO pageVO){
        return positionRecordServiceImpl.selectList(pageVO);
    }

    @GetMapping("/position_record/predict_price_change_rate")
    public Result<PriceChangeRateRO> predictPriceChangeRate(@Valid PredictPriceChangeRateVO predictPriceChangeRateVO){
        return positionRecordServiceImpl.predictPriceChangeRate(predictPriceChangeRateVO);
    }

}
