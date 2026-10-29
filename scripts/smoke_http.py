#!/usr/bin/env python3
"""Read-only smoke: API_URL, API_USER, API_PASSWORD; expects large demo data imported."""
import base64
import json
import os
import urllib.request
import urllib.error


def run(base, username, password):
    authorization = 'Basic ' + base64.b64encode(f'{username}:{password}'.encode()).decode()
    # Only local/private API validation; do not route localhost through an outbound HTTP proxy.
    opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
    def get(path, auth=True):
        request = urllib.request.Request(base + path, headers={'Authorization': authorization} if auth else {})
        with opener.open(request, timeout=30) as response:
            return json.load(response)
    assert get('/actuator/health', False)['status'] == 'UP'
    assert get('/api/auth/me')['username'] == username
    first = get('/api/products?after=0&size=25')
    assert len(first['content']) == 25 and not first['last']
    last = first['content'][-1]['id']
    second = get(f'/api/products?after={last}&size=25')
    assert second['content'][0]['id'] > last
    for _ in range(2):
        stats = get('/api/dashboard')
        assert stats['products'] == 100000 and stats['orders'] == 100000
        assert stats['pendingReceipts'] == 10000
    assert get('/api/orders?customerId=1&size=25')['totalElements'] == 5
    try:
        get('/api/products', False)
        raise AssertionError('Anonymous API request was accepted')
    except urllib.error.HTTPError as e:
        assert e.code == 401
    print('PASS: health (MySQL/Redis), auth, keyset pages, cached dashboard, customer history, anonymous denial')

if __name__ == '__main__':
    run(os.environ.get('API_URL', 'http://127.0.0.1:18080'), os.environ['API_USER'], os.environ['API_PASSWORD'])
