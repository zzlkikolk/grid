package com.jerryz.grid.service;

import com.jerryz.grid.pojo.po.AssetCodeRecord;
import com.jerryz.grid.pojo.ro.Result;
import com.jerryz.grid.pojo.vo.assetCode.AssetCodeRecordSaveVO;

import java.util.List;

/**
 * @author zhangzhilin
 * @version 2026
 */
public interface IAssetCodeRecordService {

    Result<Void> save(AssetCodeRecordSaveVO saveVO);

    Result<List<AssetCodeRecord>> selectList(String keyword);

    Result<Void> delete(Long id);
}
