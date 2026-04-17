package com.jerryz.grid.service.impl;

import com.jerryz.grid.handle.GlobalExceptionHandler;
import com.jerryz.grid.mapper.AssetCodeRecordMapper;
import com.jerryz.grid.pojo.po.AssetCodeRecord;
import com.jerryz.grid.pojo.ro.Result;
import com.jerryz.grid.pojo.vo.assetCode.AssetCodeRecordSaveVO;
import com.jerryz.grid.service.IAssetCodeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zhangzhilin
 * @version 2026
 */
@Service
@RequiredArgsConstructor
public class AssetCodeRecordServiceImpl implements IAssetCodeRecordService {

    private final AssetCodeRecordMapper assetCodeRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> save(AssetCodeRecordSaveVO saveVO) {
        String normalizedAssetCode = saveVO.getAssetCode().trim().toUpperCase();
        saveVO.setAssetCode(normalizedAssetCode);

        AssetCodeRecord dbRecord = assetCodeRecordMapper.selectByAssetCode(normalizedAssetCode);
        if (dbRecord != null) {
            throw new GlobalExceptionHandler.ServiceException("代码已存在");
        }

        AssetCodeRecord assetCodeRecord = new AssetCodeRecord();
        BeanUtils.copyProperties(saveVO, assetCodeRecord);
        assetCodeRecordMapper.insert(assetCodeRecord);
        return Result.success();
    }

    @Override
    public Result<List<AssetCodeRecord>> selectList(String keyword) {
        return Result.success(assetCodeRecordMapper.selectList(keyword));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(Long id) {
        assetCodeRecordMapper.deleteById(id);
        return Result.success();
    }
}
