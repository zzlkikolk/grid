package com.jerryz.grid.pojo.vo.assetCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author zhangzhilin
 * @version 2026
 */
@Data
public class AssetCodeRecordSaveVO {

    /**
     * 资产类型: 1-股票 2-基金
     */
    @NotNull(message = "资产类型不能为空")
    @Min(value = 1, message = "资产类型不合法")
    @Max(value = 2, message = "资产类型不合法")
    private Integer assetType;

    /**
     * 资产代码
     */
    @NotBlank(message = "资产代码不能为空")
    private String assetCode;

    /**
     * 资产名称
     */
    @NotBlank(message = "资产名称不能为空")
    private String assetName;
}
