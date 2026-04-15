package com.jerryz.grid.service.strategy.impl;

import com.jerryz.grid.em.TransactionTypeEm;
import com.jerryz.grid.mapper.PositionRecordMapper;
import com.jerryz.grid.pojo.po.PositionRecord;
import com.jerryz.grid.pojo.vo.PositionRecordVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 买入策略单元测试（修复版）
 *
 * 测试场景：
 * 1. 首次买入
 * 2. 多次买入的加权平均成本计算
 * 3. 卖出后重新买入：正确计算持仓
 * 4. 清仓后重新买入：按首次买入处理
 * 5. 多次买入卖出后买入：正确计算实际持仓
 * 6. 精度测试
 *
 * @author zhangzhilin
 * @version 2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("买入策略测试（修复版）")
class BuyInPositionRecordTransactionStrategyTest {

    @Mock
    private PositionRecordMapper positionRecordMapper;

    @InjectMocks
    private BuyInPositionRecordTransactionStrategy buyStrategy;

    private PositionRecordVO buyVO;
    private List<PositionRecord> allRecords;

    @BeforeEach
    void setUp() {
        buyVO = new PositionRecordVO();
        buyVO.setAssetType((short) 1);
        buyVO.setAssetCode("000001");
        buyVO.setAssetName("平安银行");
        buyVO.setTransactionDate(LocalDate.now());
        buyVO.setTransactionTime(LocalDateTime.now());
        buyVO.setPrice(new BigDecimal("10.00"));
        buyVO.setQuantity(new BigDecimal("100"));
        buyVO.setAmount(new BigDecimal("1000.00"));
        buyVO.setFee(new BigDecimal("2.50"));
        buyVO.setTrackIndexCode("000001.SH");
        buyVO.setTrackIndex(new BigDecimal("3000.00"));

        allRecords = new ArrayList<>();
    }

    @Test
    @DisplayName("首次买入：平均成本等于买入价格")
    void testFirstBuy() {
        // Given: 没有历史记录
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(new ArrayList<>());

        // When: 首次买入100股
        PositionRecord result = buyStrategy.handle(buyVO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(TransactionTypeEm.BUY.getCode(), result.getTransactionType());
        assertEquals(new BigDecimal("10.00"), result.getPrice());
        assertEquals(new BigDecimal("100"), result.getQuantity());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        
        // 验证平均成本等于买入价格
        assertEquals(new BigDecimal("10.00"), result.getAverageCost());
        
        // 验证持有数量等于买入数量
        assertEquals(new BigDecimal("100.0000"), result.getCurrentQuantity());
        
        // 验证状态为持有中
        assertEquals(1, result.getStatus());
    }

    @Test
    @DisplayName("多次买入：加权平均计算成本")
    void testMultipleBuys() {
        // Given: 已有一次买入记录（200股，9.00元）
        PositionRecord existingRecord = createBuyRecord("000001", "平安银行", 
            new BigDecimal("9.00"), new BigDecimal("200"), 
            new BigDecimal("1800.00"), new BigDecimal("200"));
        
        allRecords.add(existingRecord);
        
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(allRecords);

        // When: 再次买入100股，价格10.00元
        PositionRecord result = buyStrategy.handle(buyVO);

        // Then: 验证加权平均成本
        // 新平均成本 = (1800 + 1000) / (200 + 100) = 2800 / 300 = 9.3333
        assertEquals(0, new BigDecimal("9.3333").compareTo(result.getAverageCost()));
        
        // 验证新持有数量 = 200 + 100 = 300
        assertEquals(new BigDecimal("300.0000"), result.getCurrentQuantity());
    }

    @Test
    @DisplayName("卖出后重新买入：正确计算持仓")
    void testBuyAfterSell() {
        // Given: 有买入和卖出记录
        PositionRecord buy1 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("10.00"), new BigDecimal("200"), 
            new BigDecimal("2000.00"), new BigDecimal("200"));
        
        PositionRecord buy2 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("15.00"), new BigDecimal("100"), 
            new BigDecimal("1500.00"), new BigDecimal("100"));
        
        PositionRecord sell1 = createSellRecord("000001", "平安银行", 
            new BigDecimal("20.00"), new BigDecimal("150"), 
            new BigDecimal("3000.00"), new BigDecimal("15.00"), new BigDecimal("50"));
        
        allRecords.add(buy1);
        allRecords.add(buy2);
        allRecords.add(sell1);
        
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(allRecords);

        // When: 重新买入50股，价格12.00元
        buyVO.setPrice(new BigDecimal("12.00"));
        buyVO.setQuantity(new BigDecimal("50"));
        buyVO.setAmount(new BigDecimal("600.00"));
        
        PositionRecord result = buyStrategy.handle(buyVO);

        // Then: 验证计算
        // 实际持有数量 = 200 + 100 - 150 = 150
        // 实际持有成本 = 2000 + 1500 - (150 * 15) = 2000 + 1500 - 2250 = 1250
        // 新平均成本 = (1250 + 600) / (150 + 50) = 1850 / 200 = 9.25
        assertEquals(0, new BigDecimal("9.25").compareTo(result.getAverageCost()));
        
        // 新持有数量 = 150 + 50 = 200
        assertEquals(new BigDecimal("200.0000"), result.getCurrentQuantity());
    }

    @Test
    @DisplayName("清仓后重新买入：按首次买入处理")
    void testBuyAfterFullSell() {
        // Given: 有买入记录，但已全部清仓
        PositionRecord buy1 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("10.00"), new BigDecimal("100"), 
            new BigDecimal("1000.00"), new BigDecimal("100"));
        
        PositionRecord sell1 = createSellRecord("000001", "平安银行", 
            new BigDecimal("15.00"), new BigDecimal("100"), 
            new BigDecimal("1500.00"), new BigDecimal("10.00"), new BigDecimal("0"));
        
        allRecords.add(buy1);
        allRecords.add(sell1);
        
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(allRecords);

        // When: 清仓后重新买入50股，价格12.00元
        buyVO.setPrice(new BigDecimal("12.00"));
        buyVO.setQuantity(new BigDecimal("50"));
        buyVO.setAmount(new BigDecimal("600.00"));
        
        PositionRecord result = buyStrategy.handle(buyVO);

        // Then: 按首次买入处理
        // 平均成本 = 本次买入价格 = 12.00
        assertEquals(new BigDecimal("12.00"), result.getAverageCost());
        
        // 持有数量 = 本次买入数量 = 50
        assertEquals(new BigDecimal("50.0000"), result.getCurrentQuantity());
    }

    @Test
    @DisplayName("多次买入卖出后买入：正确计算实际持仓")
    void testBuyAfterMultipleTransactions() {
        // Given: 多次买入和卖出记录
        PositionRecord buy1 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("10.00"), new BigDecimal("100"), 
            new BigDecimal("1000.00"), new BigDecimal("100"));
        
        PositionRecord buy2 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("12.00"), new BigDecimal("50"), 
            new BigDecimal("600.00"), new BigDecimal("50"));
        
        PositionRecord sell1 = createSellRecord("000001", "平安银行", 
            new BigDecimal("15.00"), new BigDecimal("80"), 
            new BigDecimal("1200.00"), new BigDecimal("10.67"), new BigDecimal("70"));
        
        PositionRecord buy3 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("14.00"), new BigDecimal("30"), 
            new BigDecimal("420.00"), new BigDecimal("30"));
        
        PositionRecord sell2 = createSellRecord("000001", "平安银行", 
            new BigDecimal("18.00"), new BigDecimal("50"), 
            new BigDecimal("900.00"), new BigDecimal("11.00"), new BigDecimal("50"));
        
        allRecords.add(buy1);
        allRecords.add(buy2);
        allRecords.add(sell1);
        allRecords.add(buy3);
        allRecords.add(sell2);
        
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(allRecords);

        // When: 再次买入20股，价格11.00元
        buyVO.setPrice(new BigDecimal("11.00"));
        buyVO.setQuantity(new BigDecimal("20"));
        buyVO.setAmount(new BigDecimal("220.00"));
        
        PositionRecord result = buyStrategy.handle(buyVO);

        // Then: 验证计算
        // 实际持有数量 = 100 + 50 - 80 + 30 - 50 = 50
        // 实际持有成本 = 1000 + 600 - (80 * 10.67) + 420 - (50 * 11.00)
        //              = 1000 + 600 - 853.6 + 420 - 550 = 616.4
        // 新平均成本 = (616.4 + 220) / (50 + 20) = 836.4 / 70 ≈ 11.95
        assertEquals(0, new BigDecimal("11.95").compareTo(result.getAverageCost()));
        
        // 新持有数量 = 50 + 20 = 70
        assertEquals(new BigDecimal("70.0000"), result.getCurrentQuantity());
    }

    @Test
    @DisplayName("精度测试：小数计算保留4位小数")
    void testPrecision() {
        // Given: 包含小数的交易记录
        PositionRecord buy1 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("10.1234"), new BigDecimal("100.5678"), 
            new BigDecimal("1018.2335"), new BigDecimal("100.5678"));
        
        PositionRecord sell1 = createSellRecord("000001", "平安银行", 
            new BigDecimal("15.5678"), new BigDecimal("50.2345"), 
            new BigDecimal("781.9043"), new BigDecimal("10.1234"), new BigDecimal("50.3333"));
        
        allRecords.add(buy1);
        allRecords.add(sell1);
        
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(allRecords);

        // When: 使用小数买入
        buyVO.setPrice(new BigDecimal("12.3456"));
        buyVO.setQuantity(new BigDecimal("25.1234"));
        buyVO.setAmount(new BigDecimal("310.1234"));
        
        PositionRecord result = buyStrategy.handle(buyVO);

        // Then: 验证精度
        // 实际持有数量 = 100.5678 - 50.2345 = 50.3333
        // 实际持有成本 = 1018.2335 - (50.2345 * 10.1234) = 1018.2335 - 508.4558 = 509.7777
        // 新平均成本 = (509.7777 + 310.1234) / (50.3333 + 25.1234) = 819.9011 / 75.4567 ≈ 10.8669
        assertEquals(0, new BigDecimal("10.8669").compareTo(result.getAverageCost()));
        
        // 新持有数量 = 50.3333 + 25.1234 = 75.4567
        assertEquals(0, new BigDecimal("75.4567").compareTo(result.getCurrentQuantity()));
    }

    /**
     * 创建买入记录辅助方法
     */
    private PositionRecord createBuyRecord(String assetCode, String assetName, 
                                           BigDecimal price, BigDecimal quantity,
                                           BigDecimal amount, BigDecimal currentQuantity) {
        PositionRecord record = new PositionRecord();
        record.setAssetType((short) 1);
        record.setAssetCode(assetCode);
        record.setAssetName(assetName);
        record.setTransactionType(TransactionTypeEm.BUY.getCode());
        record.setTransactionDate(LocalDate.now());
        record.setTransactionTime(LocalDateTime.now());
        record.setPrice(price);
        record.setQuantity(quantity);
        record.setAmount(amount);
        record.setCurrentQuantity(currentQuantity);
        record.setStatus(1);
        record.setSettlementStatus(1);
        return record;
    }

    /**
     * 创建卖出记录辅助方法
     */
    private PositionRecord createSellRecord(String assetCode, String assetName, 
                                           BigDecimal price, BigDecimal quantity,
                                           BigDecimal amount, BigDecimal averageCost, 
                                           BigDecimal currentQuantity) {
        PositionRecord record = new PositionRecord();
        record.setAssetType((short) 1);
        record.setAssetCode(assetCode);
        record.setAssetName(assetName);
        record.setTransactionType(TransactionTypeEm.SELL.getCode());
        record.setTransactionDate(LocalDate.now());
        record.setTransactionTime(LocalDateTime.now());
        record.setPrice(price);
        record.setQuantity(quantity);
        record.setAmount(amount);
        record.setAverageCost(averageCost);
        record.setCurrentQuantity(currentQuantity);
        record.setStatus(currentQuantity.compareTo(BigDecimal.ZERO) == 0 ? 2 : 1);
        record.setSettlementStatus(1);
        return record;
    }
}
