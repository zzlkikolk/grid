package com.jerryz.grid.pojo.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * 持仓记录实体类
 * 使用Lombok的@Data注解自动生成getter/setter等方法
 */
@Data
public class PositionRecord {
    /**
     * 主键ID
     */
    private Long id;

    // 资产信息
    /**
     * 资产类型: 1-股票 2-基金 3-债券 4-其他
     */
    private Integer assetType;
    /**
     * 资产代码(股票代码/基金代码)
     */
    private String assetCode;
    /**
     * 资产名称
     */
    private String assetName;

    // 交易信息
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

    // 持仓信息
    /**
     * 平均成本(仅买入时计算)
     */
    private BigDecimal averageCost;
    /**
     * 当前持有数量(动态计算)
     */
    private BigDecimal currentQuantity;



    // 状态信息
    /**
     * 状态: 1-持有中 2-已清仓
     */
    private Integer status;

    /**
     * 结算状态: 0-未结算 1-已结算
     * 字段用途：
     * 记录交易是否已完成资金和证券的清算
     * 区分交易确认和实际结算两个阶段
     * 状态说明：
     * 0-未结算：交易已发生但尚未完成清算
     * 例如：T日买入的股票，在T+1日才完成交割
     * 1-已结算：资金和证券已完成清算
     * 例如：资金已扣款，证券已到账
     * 使用场景示例：
     * 股票交易：T日买入，T+1日结算
     * 基金申赎：T日申购，T+1或T+2日确认份额
     * 债券交易：可能有不同的结算周期
     */
    private Integer settlementStatus;

    // 其他信息
    /**
     * 备注
     */
    private String remark;
    /**
     * 数据来源
     */
    private String source;
    /**
     * 外部系统ID
     */
    private String externalId;

    // 时间戳
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}