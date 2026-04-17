package com.jerryz.grid.controller.assetCode;

import com.jerryz.grid.pojo.po.AssetCodeRecord;
import com.jerryz.grid.pojo.ro.Result;
import com.jerryz.grid.pojo.vo.assetCode.AssetCodeRecordSaveVO;
import com.jerryz.grid.service.IAssetCodeRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 基金/股票代码管理
 * @author zhangzhilin
 * @version 2026
 */
@RestController
@RequiredArgsConstructor
public class AssetCodeRecordController {

    private final IAssetCodeRecordService assetCodeRecordService;

    @PostMapping("/asset_code/save")
    public Result<Void> save(@Valid @RequestBody AssetCodeRecordSaveVO saveVO) {
        return assetCodeRecordService.save(saveVO);
    }

    @GetMapping("/asset_code/list")
    public Result<List<AssetCodeRecord>> selectList(@RequestParam(required = false) String keyword) {
        return assetCodeRecordService.selectList(keyword);
    }

    @DeleteMapping("/asset_code/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return assetCodeRecordService.delete(id);
    }
}
