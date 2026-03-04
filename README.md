# 持仓记录服务（Position Record Service）

## 一、项目简介

本服务用于管理用户持仓记录，并在新增持仓时自动计算：

- 平均持仓成本（加权平均）
- 平均净值（加权平均）

当前版本仅实现：

- 持仓记录新增
- 持仓成本自动摊薄计算
- 平均净值自动计算

---

## 二、数据库设计

当前涉及两张表：

- `daily_net_value` —— 每日净值表
- `position_record` —— 用户持仓记录表

---

### 1. 每日净值表

**表名：** `daily_net_value`

**用途：**

- 存储每日产品净值数据
- 为持仓记录提供净值来源

#### 建表 SQL

```sql
CREATE TABLE "public"."daily_net_value" (
                                            "id" int8 NOT NULL DEFAULT nextval('daily_net_value_id_seq'::regclass),
                                            "asset_code" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
                                            "net_value_date" date NOT NULL,
                                            "net_value" numeric(18,4) NOT NULL,
                                            "change_rate" numeric(10,4),
                                            "volume" int8,
                                            "amount" numeric(18,4),
                                            "high_price" numeric(18,4),
                                            "low_price" numeric(18,4),
                                            "open_price" numeric(18,4),
                                            "close_price" numeric(18,4),
                                            "source" varchar(32) COLLATE "pg_catalog"."default",
                                            "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            "updated_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            CONSTRAINT "daily_net_value_pkey" PRIMARY KEY ("id"),
                                            CONSTRAINT "uk_asset_date" UNIQUE ("asset_code", "net_value_date")
)
;

ALTER TABLE "public"."daily_net_value"
    OWNER TO "zhangzhilin";

COMMENT ON COLUMN "public"."daily_net_value"."id" IS '主键ID';

COMMENT ON COLUMN "public"."daily_net_value"."asset_code" IS '资产代码(股票代码/基金代码)';

COMMENT ON COLUMN "public"."daily_net_value"."net_value_date" IS '净值日期';

COMMENT ON COLUMN "public"."daily_net_value"."net_value" IS '单位净值';

COMMENT ON COLUMN "public"."daily_net_value"."change_rate" IS '日涨跌幅(%)';

COMMENT ON COLUMN "public"."daily_net_value"."volume" IS '成交量(股/份)';

COMMENT ON COLUMN "public"."daily_net_value"."amount" IS '成交金额';

COMMENT ON COLUMN "public"."daily_net_value"."high_price" IS '最高价';

COMMENT ON COLUMN "public"."daily_net_value"."low_price" IS '最低价';

COMMENT ON COLUMN "public"."daily_net_value"."open_price" IS '开盘价';

COMMENT ON COLUMN "public"."daily_net_value"."close_price" IS '收盘价';

COMMENT ON COLUMN "public"."daily_net_value"."source" IS '数据来源';

COMMENT ON COLUMN "public"."daily_net_value"."created_at" IS '创建时间';

COMMENT ON COLUMN "public"."daily_net_value"."updated_at" IS '更新时间';

COMMENT ON TABLE "public"."daily_net_value" IS '每日净值表';
```

### 2. 用户持仓记录表

**表名：** `position_record`

**用途：**

- 存储持仓历史数据
- 为后续分析提供数据来源

#### 建表 SQL

```sql
CREATE TABLE "public"."position_record" (
                                            "id" int8 NOT NULL DEFAULT nextval('position_record_id_seq'::regclass),
                                            "asset_type" int2 NOT NULL,
                                            "asset_code" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
                                            "asset_name" varchar(128) COLLATE "pg_catalog"."default",
                                            "transaction_type" int2 NOT NULL,
                                            "transaction_date" date NOT NULL,
                                            "transaction_time" timestamp(6) NOT NULL,
                                            "price" numeric(18,4) NOT NULL,
                                            "quantity" numeric(18,4) NOT NULL,
                                            "amount" numeric(18,4) NOT NULL,
                                            "fee" numeric(18,4) DEFAULT 0,
                                            "tax" numeric(18,4) DEFAULT 0,
                                            "average_cost" numeric(18,4),
                                            "current_quantity" numeric(18,4),
                                            "status" int2 DEFAULT 1,
                                            "settlement_status" int2 DEFAULT 0,
                                            "remark" varchar(512) COLLATE "pg_catalog"."default",
                                            "source" varchar(32) COLLATE "pg_catalog"."default",
                                            "external_id" varchar(64) COLLATE "pg_catalog"."default",
                                            "created_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            "updated_at" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            "track_index_code" varchar(128) COLLATE "pg_catalog"."default",
                                            "track_index" numeric(18,2),
                                            CONSTRAINT "position_record_pkey" PRIMARY KEY ("id"),
                                            CONSTRAINT "chk_transaction_type" CHECK (transaction_type >= 1 AND transaction_type <= 4),
                                            CONSTRAINT "chk_status" CHECK (status >= 1 AND status <= 2),
                                            CONSTRAINT "chk_settlement_status" CHECK (settlement_status >= 0 AND settlement_status <= 1),
                                            CONSTRAINT "chk_asset_type" CHECK (asset_type >= 1 AND asset_type <= 4)
)
;

ALTER TABLE "public"."position_record"
    OWNER TO "zhangzhilin";

CREATE INDEX "idx_position_record_asset" ON "public"."position_record" USING btree (
    "asset_code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

CREATE INDEX "idx_position_record_transaction_date" ON "public"."position_record" USING btree (
    "transaction_date" "pg_catalog"."date_ops" ASC NULLS LAST
    );

CREATE TRIGGER "trg_position_record_updated_at" BEFORE UPDATE ON "public"."position_record"
    FOR EACH ROW
    EXECUTE PROCEDURE "public"."update_updated_at_column"();

COMMENT ON COLUMN "public"."position_record"."id" IS '主键ID';

COMMENT ON COLUMN "public"."position_record"."asset_type" IS '资产类型: 1-股票 2-基金 3-债券 4-其他';

COMMENT ON COLUMN "public"."position_record"."asset_code" IS '资产代码(股票代码/基金代码)';

COMMENT ON COLUMN "public"."position_record"."asset_name" IS '资产名称';

COMMENT ON COLUMN "public"."position_record"."transaction_type" IS '交易类型: 1-买入 2-卖出 3-分红 4-拆分';

COMMENT ON COLUMN "public"."position_record"."transaction_date" IS '交易日期';

COMMENT ON COLUMN "public"."position_record"."transaction_time" IS '交易时间';

COMMENT ON COLUMN "public"."position_record"."price" IS '交易价格/净值';

COMMENT ON COLUMN "public"."position_record"."quantity" IS '交易数量/份额';

COMMENT ON COLUMN "public"."position_record"."amount" IS '交易金额(price*quantity)';

COMMENT ON COLUMN "public"."position_record"."fee" IS '交易费用(手续费等)';

COMMENT ON COLUMN "public"."position_record"."tax" IS '交易税费';

COMMENT ON COLUMN "public"."position_record"."average_cost" IS '平均成本(仅买入时计算)';

COMMENT ON COLUMN "public"."position_record"."current_quantity" IS '当前持有数量(动态计算)';

COMMENT ON COLUMN "public"."position_record"."status" IS '状态: 1-持有中 2-已清仓';

COMMENT ON COLUMN "public"."position_record"."settlement_status" IS '结算状态: 0-未结算 1-已结算';

COMMENT ON COLUMN "public"."position_record"."remark" IS '备注';

COMMENT ON COLUMN "public"."position_record"."source" IS '数据来源';

COMMENT ON COLUMN "public"."position_record"."external_id" IS '外部系统ID';

COMMENT ON COLUMN "public"."position_record"."created_at" IS '创建时间';

COMMENT ON COLUMN "public"."position_record"."updated_at" IS '更新时间';

COMMENT ON COLUMN "public"."position_record"."track_index_code" IS '跟踪的指数代码';

COMMENT ON COLUMN "public"."position_record"."track_index" IS '当日指数值';

COMMENT ON TABLE "public"."position_record" IS '个人持仓记录表';
```

