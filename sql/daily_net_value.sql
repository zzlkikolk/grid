CREATE TABLE IF NOT EXISTS "public"."daily_net_value" (
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