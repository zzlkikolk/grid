package com.jerryz.grid.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jerryz.grid.pojo.po.AssetCodeRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhangzhilin
 * @version 2026
 */
public interface AssetCodeRecordMapper extends BaseMapper<AssetCodeRecord> {

    AssetCodeRecord selectByAssetCode(@Param("assetCode") String assetCode);

    List<AssetCodeRecord> selectList(@Param("keyword") String keyword);
}
