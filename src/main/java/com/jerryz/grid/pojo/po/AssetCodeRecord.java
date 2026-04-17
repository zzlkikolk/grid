package com.jerryz.grid.pojo.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基金/股票代码记录
 * @author zhangzhilin
 * @version 2026
 */
@Data
public class AssetCodeRecord {

    private Long id;

    /**
     * 资产类型: 1-股票 2-基金
     */
    private Integer assetType;

    /**
     * 资产代码
     */
    private String assetCode;

    /**
     * 资产名称
     */
    private String assetName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
