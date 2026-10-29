#!/usr/bin/env python3
"""Deterministic batched seed SQL for a fresh migrated demo DB with bootstrap admin."""
import argparse
import random
import sys
from pathlib import Path


def quote(v):
    return str(v) if isinstance(v, int) else "'" + str(v).replace("'", "''") + "'"


def generate(out, products=100000, customers=20000, orders=100000, batch=1000):
    def emit(table, columns, rows):
        pending = []
        for row in rows:
            pending.append('(' + ','.join(map(quote, row)) + ')')
            if len(pending) == batch:
                out.write(f'INSERT INTO {table}({columns}) VALUES ' + ','.join(pending) + ';\n')
                pending.clear()
        if pending:
            out.write(f'INSERT INTO {table}({columns}) VALUES ' + ','.join(pending) + ';\n')
    out.write('-- Run once on a fresh demo database. All FK and CHECK constraints stay enabled.\nSET NAMES utf8mb4;\nSTART TRANSACTION;\n')
    emit('employees', 'id,code,name,email,department,active', ((i, f'NV{i:05}', f'Nhân viên {i}', f'nv{i}@example.test', 'Bán hàng', 1) for i in range(1, 101)))
    categories = ['Bút viết', 'Giấy in', 'Sổ tay', 'Dụng cụ học tập', 'Hồ sơ văn phòng']
    emit('products', 'id,sku,name,category,unit,price,active', ((i, f'VPP{i:09}', f'{categories[i % 5]} mẫu {i}', categories[i % 5], 'Cái', (i % 200 + 1) * 1000, 1) for i in range(1, products + 1)))
    emit('customers', 'id,code,name,phone,email', ((i, f'KH{i:08}', f'Khách hàng {i}', f'09{i:08}', f'kh{i}@example.test') for i in range(1, customers + 1)))
    emit('inventory_balances', 'warehouse_id,product_id,quantity', ((w, p, 1000) for w in range(1, 4) for p in range(1, products + 1)))
    emit('stock_movements', 'warehouse_id,product_id,quantity,kind,document_id,created_at', ((w, p, 1000, 'OPENING', w, '2026-01-01 00:00:00') for w in range(1, 4) for p in range(1, products + 1)))
    out.write("SET @admin_id=(SELECT id FROM user_accounts WHERE username='admin');\n")
    if orders:
        out.write('CREATE TEMPORARY TABLE seed_orders (id BIGINT,reference VARCHAR(64),customer_id BIGINT,warehouse_id BIGINT,status VARCHAR(32),total DECIMAL(19,2),created_at DATETIME);\n')
        def headers():
            rng = random.Random(42)
            for i in range(1, orders + 1):
                p = rng.randint(1, products)
                yield i, f'DEMO-SO-{i:010}', (i - 1) % customers + 1, (i - 1) % 3 + 1, 'PLACED', (p % 200 + 1) * 2000, '2026-01-02 00:00:00'
        emit('seed_orders', 'id,reference,customer_id,warehouse_id,status,total,created_at', headers())
        out.write('INSERT INTO sales_orders(id,reference,customer_id,warehouse_id,status,total,created_at,created_by) SELECT id,reference,customer_id,warehouse_id,status,total,created_at,@admin_id FROM seed_orders;\nDROP TEMPORARY TABLE seed_orders;\n')
    def lines():
        rng = random.Random(42)
        for i in range(1, orders + 1):
            p = rng.randint(1, products)
            yield i, p, 2, (p % 200 + 1) * 1000
    emit('order_lines', 'order_id,product_id,quantity,unit_price', lines())
    if orders:
        out.write("INSERT INTO stock_receipts(id,reference,warehouse_id,supplier,status,approval_level,created_by,created_at) SELECT id,CONCAT('DEMO-IN-',id),warehouse_id,'Nhà cung cấp mẫu','PENDING',0,created_by,created_at FROM sales_orders WHERE id<=10000;\n")
        out.write("INSERT INTO receipt_lines(receipt_id,product_id,quantity,unit_cost) SELECT order_id,product_id,100,unit_price/2 FROM order_lines WHERE order_id<=10000;\n")
        out.write("INSERT INTO audit_logs(actor_id,action,entity_type,entity_id,details,created_at) SELECT created_by,'CREATE','SalesOrder',id,'Generated demo order',created_at FROM sales_orders;\n")
    out.write('COMMIT;\n')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--products', type=int, default=100000)
    parser.add_argument('--customers', type=int, default=20000)
    parser.add_argument('--orders', type=int, default=100000)
    parser.add_argument('--batch', type=int, default=1000)
    parser.add_argument('--output', type=Path, help='Write UTF-8 SQL directly; recommended on Windows (no PowerShell pipe)')
    args = parser.parse_args()
    if min(args.products, args.customers, args.batch) < 1 or args.orders < 0:
        parser.error('products/customers/batch > 0; orders >= 0')
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        with args.output.open('w', encoding='utf-8', newline='\n') as output:
            generate(output, args.products, args.customers, args.orders, args.batch)
    else:
        sys.stdout.reconfigure(encoding='utf-8')
        generate(sys.stdout, args.products, args.customers, args.orders, args.batch)
