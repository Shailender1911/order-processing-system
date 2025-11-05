MERGE INTO inventory_items (product_code, product_name, stock_on_hand, reserved_quantity, version)
KEY (product_code)
VALUES ('SKU-123', 'Wireless Mouse', 100, 0, 0);

MERGE INTO inventory_items (product_code, product_name, stock_on_hand, reserved_quantity, version)
KEY (product_code)
VALUES ('SKU-999', 'Mechanical Keyboard', 50, 0, 0);

