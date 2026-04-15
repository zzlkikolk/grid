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
 * 卖出策略单元测试（修复版）
 *
 * 测试场景：
 * 1. 部分卖出：持有数量减少但未清仓
 * 2. 全部卖出：触发清仓状态
 * 3. 多次买入后卖出：正确计算持有数量
 * 4. 卖出记录的平均成本保持不变
 * 5. 没有历史记录的情况
 *
 * @author zhangzhilin
 * @version 2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("卖出策略测试（修复版）")
class SellPositionRecordTransactionStrategyTest {

    @Mock
    private PositionRecordMapper positionRecordMapper;

    @InjectMocks
    private SellPositionRecordTransactionStrategy sellStrategy;

    private PositionRecordVO sellVO;
    private List<PositionRecord> allRecords;

    @BeforeEach
    void setUp() {
        sellVO = new PositionRecordVO();
        sellVO.setAssetType(1);
        sellVO.setAssetCode("000001");
        sellVO.setAssetName("平安银行");
        sellVO.setTransactionDate(LocalDate.now());
        sellVO.setTransactionTime(LocalDateTime.now());
        sellVO.setPrice(new BigDecimal("10.00"));
        sellVO.setQuantity(new BigDecimal("100"));
        sellVO.setAmount(new BigDecimal("1000.00"));
        sellVO.setFee(new BigDecimal("2.50"));
        sellVO.setTrackIndexCode("000001.SH");
        sellVO.setTrackIndex(new BigDecimal("3000.00"));

        allRecords = new ArrayList<>();
    }

    @Test
    @DisplayName("部分卖出：持有数量减少但未清仓")
    void testPartialSell() {
        // Given: 有买入记录
        PositionRecord buy1 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("9.00"), new BigDecimal("200"), 
            new BigDecimal("1800.00"), new BigDecimal("9.00"));
        
        PositionRecord buy2 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("11.00"), new BigDecimal("300"), 
            new BigDecimal("3300.00"), new BigDecimal("10.125"));
        
        allRecords.add(buy1);
        allRecords.add(buy2);
        
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(allRecords);

        // When: 卖出100股
        PositionRecord result = sellStrategy.handle(sellVO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(TransactionTypeEm.SELL.getCode(), result.getTransactionType());
        assertEquals("000001", result.getAssetCode());
        assertEquals(new BigDecimal("10.00"), result.getPrice());
        assertEquals(new BigDecimal("100"), result.getQuantity());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        
        // 验证平均成本保持最后一次买入的成本
        assertEquals(new BigDecimal("10.125"), result.getAverageCost());
        
        // 验证当前持有数量 = 200 + 300 - 100 = 400
        assertEquals(new BigDecimal("400.0000"), result.getCurrentQuantity());
        
        // 验证状态为持有中
        assertEquals(1, result.getStatus());
    }

    @Test
    @DisplayName("全部卖出：触发清仓状态")
    void testFullSell() {
        // Given: 有买入记录，总持有100股
        PositionRecord buyRecord = createBuyRecord("000001", "平安银行", 
            new BigDecimal("10.00"), new BigDecimal("100"), 
            new BigDecimal("1000.00"), new BigDecimal("10.00"));
        
        allRecords.add(buyRecord);
        
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(allRecords);

        // When: 卖出全部100股
        PositionRecord result = sellStrategy.handle(sellVO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result.getPrice());
        assertEquals(new BigDecimal("100"), result.getQuantity());
        
        // 验证平均成本
        assertEquals(new BigDecimal("10.00"), result.getAverageCost());
        
        // 验证当前持有数量 = 100 - 100 = 0
        assertEquals(new BigDecimal("0.0000"), result.getCurrentQuantity());
        
        // 验证状态为已清仓
        assertEquals(2, result.getStatus());
    }

    @Test
    @DisplayName("多次买入后卖出：正确计算持有数量")
    void testSellAfterMultipleBuys() {
        // Given: 多次买入记录
        PositionRecord buy1 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("8.00"), new BigDecimal("100"), 
            new BigDecimal("800.00"), new BigDecimal("8.00"));
        
        PositionRecord buy2 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("9.00"), new BigDecimal("200"), 
            new BigDecimal("1800.00"), new BigDecimal("8.6667"));
        
        PositionRecord buy3 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("10.00"), new BigDecimal("300"), 
            new BigDecimal("3000.00"), new BigDecimal("9.3333"));
        
        allRecords.add(buy1);
        allRecords.add(buy2);
        allRecords.add(buy3);
        
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(allRecords);

        // When: 卖出200股
        sellVO.setQuantity(new BigDecimal("200"));
        PositionRecord result = sellStrategy.handle(sellVO);

        // Then: 验证结果
        assertNotNull(result);
        
        // 验证平均成本保持最后一次买入的成本
        assertEquals(new BigDecimal("9.3333"), result.getAverageCost());
        
        // 验证当前持有数量 = 100 + 200 + 300 - 200 = 400
        assertEquals(new BigDecimal("400.0000"), result.getCurrentQuantity());
        
        // 验证状态为持有中
        assertEquals(1, result.getStatus());
    }

    @Test
    @DisplayName("卖出后再次卖出：累积计算")
    void testMultipleSells() {
        // Given: 有买入和卖出记录
        PositionRecord buy1 = createBuyRecord("000001", "平安银行", 
            new BigDecimal("10.00"), new BigDecimal("200"), 
            new BigDecimal("2000.00"), new BigDecimal("10.00"));
        
        PositionRecord sell1 = createSellRecord("000001", "平安银行", 
            new BigDecimal("15.00"), new BigDecimal("50"), 
            new BigDecimal("750.00"), new BigDecimal("10.00"), new BigDecimal("150"));
        
        allRecords.add(buy1);
        allRecords.add(sell1);
        
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(allRecords);

        // When: 再次卖出100股
        sellVO.setQuantity(new BigDecimal("100"));
        PositionRecord result = sellStrategy.handle(sellVO);

        // Then: 验证结果
        assertNotNull(result);
        
        // 验证平均成本
        assertEquals(new BigDecimal("10.00"), result.getAverageCost());
        
        // 验证当前持有数量 = 200 - 50 - 100 = 50
        assertEquals(new BigDecimal("50.0000"), result.getCurrentQuantity());
        
        // 验证状态为持有中
        assertEquals(1, result.getStatus());
    }

    @Test
    @DisplayName("无历史记录：卖出时默认值为0")
    void testSellWithoutHistory() {
        // Given: 没有历史记录
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(new ArrayList<>());

        // When: 卖出100股
        PositionRecord result = sellStrategy.handle(sellVO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(TransactionTypeEm.SELL.getCode(), result.getTransactionType());
        
        // 验证平均成本为0
        assertEquals(BigDecimal.ZERO, result.getAverageCost());
        
        // 验证当前持有数量为0（因为没有买入记录，所以计算为 0 - 100 = -100，但逻辑中为0）
        assertEquals(BigDecimal.ZERO, result.getCurrentQuantity());
        
        // 验证结算状态
        assertEquals(1, result.getSettlementStatus());
    }

    @Test
    @DisplayName("验证交易类型")
    void testTransactionType() {
        // When: 获取交易类型
        Integer type = sellStrategy.getType();

        // Then: 验证为卖出类型
        assertEquals(TransactionTypeEm.SELL.getCode(), type);
    }

    @Test
    @DisplayName("验证基本信息字段设置")
    void testBasicFieldsSetting() {
        // Given: 准备测试数据
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(allRecords);

        // When: 执行卖出
        PositionRecord result = sellStrategy.handle(sellVO);

        // Then: 验证基本信息字段
        assertEquals((short) 1, result.getAssetType());
        assertEquals("000001", result.getAssetCode());
        assertEquals("平安银行", result.getAssetName());
        assertEquals(LocalDate.now(), result.getTransactionDate());
        assertEquals(new BigDecimal("10.00"), result.getPrice());
        assertEquals(new BigDecimal("100"), result.getQuantity());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        assertEquals(new BigDecimal("2.50"), result.getFee());
        assertEquals("000001.SH", result.getTrackIndexCode());
        assertEquals(new BigDecimal("3000.00"), result.getTrackIndex());
    }

    @Test
    @DisplayName("精度测试：数量计算保留4位小数")
    void testPrecision() {
        // Given: 历史记录包含小数数量
        PositionRecord buyRecord = createBuyRecord("000001", "平安银行", 
            new BigDecimal("10.00"), new BigDecimal("100.1234"), 
            new BigDecimal("1001.234"), new BigDecimal("10.00"));
        
        allRecords.add(buyRecord);
        
        when(positionRecordMapper.selectAllRecordsByAssetCode("000001")).thenReturn(allRecords);

        // When: 卖出小数数量
        sellVO.setQuantity(new BigDecimal("50.5678"));
        PositionRecord result = sellStrategy.handle(sellVO);

        // Then: 验证精度保留4位小数
        // 当前持有数量 = 100.1234 - 50.5678 = 49.5556
        assertEquals(0, new BigDecimal("49.5556").compareTo(result.getCurrentQuantity()));
    }

    /**
     * 创建买入记录辅助方法
     */
    private PositionRecord createBuyRecord(String assetCode, String assetName, 
                                           BigDecimal price, BigDecimal quantity,
                                           BigDecimal amount, BigDecimal averageCost) {
        PositionRecord record = new PositionRecord();
        record.setAssetType( 1);
        record.setAssetCode(assetCode);
        record.setAssetName(assetName);
        record.setTransactionType(TransactionTypeEm.BUY.getCode());
        record.setTransactionDate(LocalDate.now());
        record.setTransactionTime(LocalDateTime.now());
        record.setPrice(price);
        record.setQuantity(quantity);
        record.setAmount(amount);
        record.setAverageCost(averageCost);
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
        record.setAssetType( 1);
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
