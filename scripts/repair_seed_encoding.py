#!/usr/bin/env python3
"""Preview/repair known mojibake forms of deterministic demo text, not arbitrary DB text.
No DB connection: writes UTF-8 SQL. Back up DB, preview and review before --apply.
"""
import argparse
from pathlib import Path
from generate_data import quote


def variants(text):
    values = set()
    for codec in ('cp437', 'cp850', 'cp1252', 'cp1258', 'latin1'):
        value = text.encode('utf-8').decode(codec, errors='replace')
        if value != text:
            values.add(value)
        double = value.encode('utf-8').decode('cp1252', errors='replace')
        if double != text:
            values.add(double)
    return sorted(values)


def generate(out, apply=False):
    out.write('-- Known demo encoding variants only. Review preview and back up before applying.\nSET NAMES utf8mb4;\n')
    out.write('CREATE TEMPORARY TABLE seed_encoding_map (field_name VARCHAR(32), good_text VARCHAR(255), bad_text VARCHAR(1000)) CHARACTER SET utf8mb4;\n')
    categories = ['Bút viết', 'Giấy in', 'Sổ tay', 'Dụng cụ học tập', 'Hồ sơ văn phòng']
    mappings = [('product_category', s) for s in categories]
    mappings += [('product_name', s + ' mẫu ') for s in categories]
    mappings += [('product_unit', 'Cái'), ('employee_name', 'Nhân viên '), ('department', 'Bán hàng'), ('customer_name', 'Khách hàng '), ('supplier', 'Nhà cung cấp mẫu')]
    rows = [(kind, good, bad) for kind, good in mappings for bad in variants(good)]
    out.write('INSERT INTO seed_encoding_map VALUES ' + ','.join('(' + ','.join(quote(v) for v in row) + ')' for row in rows) + ';\n')
    # Match bytes, not accent-insensitive MySQL collation; never globally reinterpret text.
    specs = [
        ('products', 'name', 'product_name', "p.sku=CONCAT('VPP',LPAD(p.id,9,'0'))", True),
        ('products', 'category', 'product_category', "p.sku=CONCAT('VPP',LPAD(p.id,9,'0'))", False),
        ('products', 'unit', 'product_unit', "p.sku=CONCAT('VPP',LPAD(p.id,9,'0'))", False),
        ('employees', 'name', 'employee_name', "p.code=CONCAT('NV',LPAD(p.id,5,'0')) AND p.email=CONCAT('nv',p.id,'@example.test')", True),
        ('employees', 'department', 'department', "p.code=CONCAT('NV',LPAD(p.id,5,'0')) AND p.email=CONCAT('nv',p.id,'@example.test')", False),
        ('customers', 'name', 'customer_name', "p.code=CONCAT('KH',LPAD(p.id,8,'0')) AND p.email=CONCAT('kh',p.id,'@example.test')", True),
        ('stock_receipts', 'supplier', 'supplier', "p.reference=CONCAT('DEMO-IN-',p.id)", False),
    ]
    if apply:
        out.write('START TRANSACTION;\n')
    for table, field, kind, guard, suffix in specs:
        bad = "CONCAT(m.bad_text,p.id)" if suffix else 'm.bad_text'
        good = "CONCAT(m.good_text,p.id)" if suffix else 'm.good_text'
        join = f"{table} p JOIN seed_encoding_map m ON m.field_name='{kind}' AND HEX(p.{field})=HEX({bad})"
        if apply:
            out.write(f'UPDATE {join} SET p.{field}={good} WHERE {guard};\n')
            out.write(f"SELECT '{table}.{field}' AS repaired_field, ROW_COUNT() AS changed_rows;\n")
        else:
            out.write(f"SELECT '{table}.{field}' AS affected_field, COUNT(DISTINCT p.id) AS matched_rows FROM {join} WHERE {guard};\n")
            out.write(f'SELECT p.id,p.{field} AS current_value,{good} AS proposed_value FROM {join} WHERE {guard} LIMIT 10;\n')
    if apply:
        out.write('COMMIT;\n')
    out.write('DROP TEMPORARY TABLE seed_encoding_map;\n')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--apply', action='store_true', help='Generate UPDATE SQL; default generates read-only previews')
    args = parser.parse_args()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open('w', encoding='utf-8', newline='\n') as output:
        generate(output, args.apply)
