const assert = require('node:assert/strict')

const baseUrl = process.env.E2E_BASE_URL || 'http://localhost:8081'
const username = `e2e_${Date.now()}`
let token = ''

async function request(path, options = {}, expectedStatus = 200) {
  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers
    }
  })
  const body = response.status === 204 ? null : await response.json()
  assert.equal(response.status, expectedStatus, `${path}: ${response.status} ${JSON.stringify(body)}`)
  return body
}

async function run() {
  const checks = []
  const health = await request('/api/health')
  assert.deepEqual(health, { application: 'UP', mysql: 'UP', redis: 'PONG' })
  checks.push('服务/MySQL/Redis 健康')

  const shops = await request('/api/shops')
  assert.ok(shops.length > 0)
  const shopId = shops[0].id
  const menuBefore = await request(`/api/shops/${shopId}/menu`)
  const skuBefore = menuBefore.categories.flatMap(c => c.products).flatMap(p => p.skus)[0]
  assert.ok(skuBefore && skuBefore.stock > 0)
  checks.push('店铺与菜单加载')

  const auth = await request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, password: 'TestPass123!', nickname: '闭环测试用户', phone: '' })
  }, 201)
  token = auth.accessToken
  assert.ok(token)
  const currentUser = await request('/api/users/me')
  assert.equal(currentUser.username, username)
  checks.push('注册登录与 JWT 鉴权')

  const address = await request('/api/addresses', {
    method: 'POST',
    body: JSON.stringify({
      contactName: '张三', contactPhone: '13800138000', province: '上海市', city: '上海市',
      district: '浦东新区', detailAddress: '科技园 1 号楼 101', isDefault: true
    })
  }, 201)
  assert.equal(address.isDefault, true)
  checks.push('新增默认地址')

  const cart = await request('/api/cart/items', {
    method: 'POST',
    body: JSON.stringify({ shopId, skuId: skuBefore.id, quantity: 2 })
  }, 201)
  assert.equal(cart.items[0].quantity, 2)
  checks.push('加入购物车')

  const order = await request('/api/orders', {
    method: 'POST',
    body: JSON.stringify({ shopId, addressId: address.id, remark: '闭环测试' })
  }, 201)
  assert.equal(order.orderStatus, 'PENDING_PAYMENT')
  checks.push('创建主订单与订单明细')

  const paid = await request(`/api/orders/${order.orderNo}/pay`, { method: 'POST' })
  assert.equal(paid.orderStatus, 'PAID')
  const orderPage = await request('/api/orders?page=1&size=10')
  assert.ok(orderPage.items.some(item => item.orderNo === order.orderNo))
  checks.push('模拟支付与订单查询')

  const cancelled = await request(`/api/orders/${order.orderNo}/cancel`, { method: 'POST' })
  assert.equal(cancelled.orderStatus, 'CANCELLED')
  const menuAfter = await request(`/api/shops/${shopId}/menu`)
  const skuAfter = menuAfter.categories.flatMap(c => c.products).flatMap(p => p.skus)
    .find(sku => sku.id === skuBefore.id)
  assert.equal(skuAfter.stock, skuBefore.stock, '取消订单后库存未恢复')
  checks.push('取消订单与库存恢复')

  console.log(JSON.stringify({ success: true, username, orderNo: order.orderNo, checks }, null, 2))
}

run().catch(error => {
  console.error(error.stack || error)
  process.exitCode = 1
})
