package com.jerryz.grid.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/06 18:06
 */
@Data
public class PositionRecordVO {

    // 资产类型 1-股票，2-基金，3-债券，4-其他
    private Integer assetType;

    // 资产代码
    private String assetCode;

    //资产名称
    private String assetName;

    /**
     * 交易类型: 1-买入 2-卖出 3-分红 4-拆分
     */
    private Integer transactionType;

    /**
     * 交易日期
     */
    private LocalDate transactionDate;
    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;
    /**
     * 交易价格/净值
     */
    private BigDecimal price;

    /**
     * 交易数量/份额
     */
    private BigDecimal quantity;
    /**
     * 交易金额(price*quantity)
     */
    private BigDecimal amount;

    /**
     * 交易费用(手续费等)
     */
    private BigDecimal fee;
    /**
     * 交易税费
     */
    private BigDecimal tax;


}
