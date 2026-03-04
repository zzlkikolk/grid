package com.jerryz.grid.util;

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
    public static class UserMemberConsumer {

        //净值
        private String value;

        //点数
        private String index;

        //数量
        private String count;

        //金额
        private String consumerAmt;
    }

    public static void computed(List<UserMemberConsumer> list, String localIndex, String localValue){
        BigDecimal all = list.stream()
                .map(u -> new BigDecimal(u.getConsumerAmt()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal count = list.stream()
                .map(u->new BigDecimal(u.getCount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        //初始值
        String startValue = list.get(0).getValue();
        String startIndex = list.get(0).getIndex();

        BigDecimal averageValue = all.divide(count, 4, RoundingMode.HALF_UP);

        //(B - A) / A × 100%
        BigDecimal startIndexNum = new BigDecimal(startIndex);
        BigDecimal localIndexNUm = new BigDecimal(localIndex);
        BigDecimal startValueNum = new BigDecimal(startValue);
        BigDecimal localValueNum = new BigDecimal(localValue);

        BigDecimal index = localIndexNUm.subtract(startIndexNum).divide(startIndexNum, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

        BigDecimal value = localValueNum.subtract(startValueNum).divide(startValueNum, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

        BigDecimal newValue = localValueNum.subtract(averageValue).divide(averageValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        System.out.println("净值幅度->"+value);
        System.out.println("平均净值幅度->"+newValue);
        System.out.println("点数幅度->"+index);
    }

    public static void main(String[] args) {
        UserMemberConsumer userMemberConsumer1 = new UserMemberConsumer();
        userMemberConsumer1.setConsumerAmt("5000");
        userMemberConsumer1.setCount("3091.31");
        userMemberConsumer1.setValue("1.6155");
        userMemberConsumer1.setIndex("4254.53");



        UserMemberConsumer userMemberConsumer2 = new UserMemberConsumer();
        userMemberConsumer2.setConsumerAmt("2000");
        userMemberConsumer2.setCount("1255.40");
        userMemberConsumer2.setValue("1.5912");
        userMemberConsumer2.setIndex("4188.98");

        List<UserMemberConsumer> list = new ArrayList<>();
        list.add(userMemberConsumer1);
        list.add(userMemberConsumer2);

        ComputedAverageCostUtil.computed(list,"4104.77","1.5611");
    }
}
