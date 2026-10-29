import io
import unittest
from generate_data import generate

class SeedTest(unittest.TestCase):
    def test_deterministic_and_preserves_constraints(self):
        a, b = io.StringIO(), io.StringIO()
        generate(a, products=10, customers=3, orders=8, batch=4)
        generate(b, products=10, customers=3, orders=8, batch=4)
        self.assertEqual(a.getvalue(), b.getvalue())
        sql = a.getvalue()
        self.assertNotIn('FOREIGN_KEY_CHECKS', sql)
        self.assertEqual(sql.count('INSERT INTO inventory_balances'), 8)
        self.assertEqual(sql.count('INSERT INTO stock_movements'), 8)
        self.assertEqual(sql.count('INSERT INTO seed_orders'), 2)
        self.assertTrue(sql.endswith('COMMIT;\n'))

    def test_no_orders(self):
        out = io.StringIO()
        generate(out, products=1, customers=1, orders=0)
        self.assertNotIn('seed_orders', out.getvalue())
        self.assertNotIn('INSERT INTO order_lines', out.getvalue())


class EncodingTest(unittest.TestCase):
    def test_output_file_ignores_console_encoding(self):
        import os
        from pathlib import Path
        import subprocess
        import sys
        import tempfile
        with tempfile.TemporaryDirectory() as folder:
            output = Path(folder) / 'nested' / 'demo.sql'
            subprocess.run([sys.executable, str(Path(__file__).with_name('generate_data.py')), '--products', '5', '--customers', '1', '--orders', '0', '--output', str(output)], env={**os.environ, 'PYTHONIOENCODING': 'cp437'}, check=True)
            data = output.read_bytes()
            self.assertFalse(data.startswith(b'\xef\xbb\xbf'))
            text = data.decode('utf-8')
            self.assertIn('SET NAMES utf8mb4;', text)
            self.assertIn('Giấy in mẫu 1', text)
            self.assertIn('Cái', text)

    def test_repair_has_preview_and_byte_exact_guards(self):
        from repair_seed_encoding import generate as repair, variants
        self.assertIn('Giấy in'.encode('utf-8').decode('cp437'), variants('Giấy in'))
        preview, apply = io.StringIO(), io.StringIO()
        repair(preview)
        repair(apply, apply=True)
        self.assertNotIn('UPDATE ', preview.getvalue())
        self.assertIn('UPDATE products', apply.getvalue())
        self.assertIn('HEX(p.name)=HEX(', apply.getvalue())
        self.assertIn("p.sku=CONCAT('VPP'", apply.getvalue())
        self.assertNotIn('DELETE ', apply.getvalue())

if __name__ == '__main__':
    unittest.main()
