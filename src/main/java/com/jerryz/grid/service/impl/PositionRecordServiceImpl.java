package com.jerryz.grid.service.impl;

import com.jerryz.grid.mapper.PositionRecordMapper;
import com.jerryz.grid.pojo.po.PositionRecord;
import com.jerryz.grid.pojo.ro.PageResult;
import com.jerryz.grid.pojo.ro.Result;
import com.jerryz.grid.pojo.vo.PageVO;
import com.jerryz.grid.pojo.vo.PositionRecordVO;
import com.jerryz.grid.service.IPositionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/06 18:13
 */
@Service
@RequiredArgsConstructor
public class PositionRecordServiceImpl implements IPositionRecordService {

    private final PositionRecordMapper positionRecordMapper;

    @Override
    public Result<Void> save(PositionRecordVO positionRecordVO) {
        return null;
    }

    @Override
    public PageResult<PositionRecord> selectList(PageVO pageVO) {
        return null;
    }
}
