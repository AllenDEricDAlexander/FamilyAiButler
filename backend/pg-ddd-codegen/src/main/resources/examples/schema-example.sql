CREATE TABLE demo_order
(
    id           bigserial PRIMARY KEY,
    order_no     varchar(64)    NOT NULL,
    buyer_id     bigint         NOT NULL,
    status       varchar(32)    NOT NULL DEFAULT 'CREATED',
    total_amount numeric(18, 2) NOT NULL,
    deleted      boolean        NOT NULL DEFAULT false,
    version      integer        NOT NULL DEFAULT 0,
    created_at   timestamptz    NOT NULL DEFAULT now(),
    updated_at   timestamptz    NOT NULL DEFAULT now(),
    CONSTRAINT uk_demo_order_order_no UNIQUE (order_no)
);

CREATE TABLE demo_order_item
(
    id         bigserial PRIMARY KEY,
    order_id   bigint      NOT NULL,
    sku_name   varchar(64) NOT NULL,
    quantity   integer     NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_demo_order_item_order FOREIGN KEY (order_id) REFERENCES demo_order (id)
);

COMMENT ON TABLE demo_order IS 'aggregate=Order;module=demo-order';
COMMENT ON COLUMN demo_order.id IS 'Order id;vo=OrderId';
COMMENT ON COLUMN demo_order.order_no IS 'Order number;vo=OrderNo;businessKey=true';
COMMENT ON COLUMN demo_order.buyer_id IS 'Buyer id;vo=BuyerId';
COMMENT ON COLUMN demo_order.status IS 'Order status';
COMMENT ON COLUMN demo_order.total_amount IS 'Total amount;vo=Money';
