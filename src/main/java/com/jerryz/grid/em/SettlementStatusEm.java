package com.jerryz.grid.em;

import lombok.Getter;

/**
 * 结算状态枚举
 */
@Getter
public enum SettlementStatusEm {

    /**
     * 未结算 - 交易已发生但资金/证券尚未完成清算
     */
    UNSETTLED(0, "未结算"),

    /**
     * 已结算 - 资金和证券已完成清算
     */
    SETTLED(1, "已结算");

    private final int code;
    private final String description;

    SettlementStatusEm(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SettlementStatusEm fromCode(int code) {
        for (SettlementStatusEm status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的结算状态代码: " + code);
    }
}
