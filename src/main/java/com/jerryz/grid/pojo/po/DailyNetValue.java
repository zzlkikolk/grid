package com.jerryz.grid.pojo.po;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日净值实体类
 */
@Data
public class DailyNetValue {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 资产代码(股票代码/基金代码)
     */
    private String assetCode;

    /**
     * 净值日期
     */
    private LocalDate netValueDate;

    /**
     * 单位净值
     */
    private BigDecimal netValue;

    /**
     * 日涨跌幅(%)
     */
    private BigDecimal changeRate;

    /**
     * 成交量(股/份)
     */
    private Long volume;

    /**
     * 成交金额
     */
    private BigDecimal amount;

    /**
     * 最高价
     */
    private BigDecimal highPrice;

    /**
     * 最低价
     */
    private BigDecimal lowPrice;

    /**
     * 开盘价
     */
    private BigDecimal openPrice;

    /**
     * 收盘价
     */
    private BigDecimal closePrice;

    /**
     * 数据来源
     */
    private String source;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
