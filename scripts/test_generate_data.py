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

if __name__ == '__main__':
    unittest.main()
