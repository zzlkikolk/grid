package com.jerryz.grid.em;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易类型: 1-买入 2-卖出 3-分红 4-拆分
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/13 11:16
 */
@Getter
@RequiredArgsConstructor
public enum TransactionTypeEm {

    BUY(1),
    SELL(2),
    DIVIDENDS(3),
    SPLIT(4)
    ;

    private final int Code;
}
