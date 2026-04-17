-- 基金/股票代码管理表
CREATE TABLE IF NOT EXISTS public.asset_code_record (
    id BIGSERIAL PRIMARY KEY,
    asset_type SMALLINT NOT NULL,
    asset_code VARCHAR(32) NOT NULL,
    asset_name VARCHAR(128) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_asset_code_record_code UNIQUE (asset_code),
    CONSTRAINT chk_asset_code_record_type CHECK (asset_type IN (1, 2))
);

COMMENT ON TABLE public.asset_code_record IS '基金/股票代码记录表';
COMMENT ON COLUMN public.asset_code_record.asset_type IS '资产类型: 1-股票 2-基金';
COMMENT ON COLUMN public.asset_code_record.asset_code IS '资产代码';
COMMENT ON COLUMN public.asset_code_record.asset_name IS '资产名称';
