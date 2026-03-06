package com.jerryz.grid.util;

import com.jerryz.grid.pojo.po.PositionRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangzhilin
 * @version 2026
 */
public class ComputedAverageCostUtil {

    @Data
    public static class UserMemberConsumerResult {

        //平均净值幅度
        private BigDecimal averageCostRate;

        //净值幅度
        private BigDecimal costRate;

        //点数幅度
        private BigDecimal indexRate;

        //摊薄点数幅度
        private BigDecimal averageIndexRate;
    }

    /**
     * 计算累计幅度
     * @param list 用户持仓列表
     * @param todayIndex 当前点数
     * @param todayPrice 当前净值
     */
    public static UserMemberConsumerResult computed(List<PositionRecord> list, BigDecimal todayIndex, BigDecimal todayPrice){
        BigDecimal all = list.stream()
                .map(PositionRecord::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal count = list.stream()
                .map(PositionRecord::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        //加权点数
        BigDecimal totalWeightedIndex = list.stream()
                .map(u -> u.getTrackIndex()
                        .multiply(u.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //平均点数
        BigDecimal averageIndex = totalWeightedIndex
                .divide(count, 8, RoundingMode.HALF_UP);



        BigDecimal averageValue = all.divide(count, 4, RoundingMode.HALF_UP);

        //(B - A) / A × 100%
        BigDecimal startIndexNum = list.get(0).getTrackIndex();
        BigDecimal startValueNum = list.get(0).getPrice();

        BigDecimal index = todayIndex.subtract(startIndexNum).divide(startIndexNum, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

        BigDecimal value = todayPrice.subtract(startValueNum).divide(startValueNum, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

        BigDecimal newValue = todayPrice.subtract(averageValue).divide(averageValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

        BigDecimal indexAmplitude = todayIndex
                .subtract(averageIndex)
                .divide(averageIndex, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

        UserMemberConsumerResult result = new UserMemberConsumerResult();
        result.setAverageCostRate(newValue);
        result.setCostRate(value);
        result.setIndexRate(index);
        result.setAverageIndexRate(indexAmplitude);
        return result;
    }

}
