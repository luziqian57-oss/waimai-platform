const { request } = require('./request')

module.exports = {
  login: data => request({ url: '/api/auth/login', method: 'POST', data, publicRequest: true }),
  register: data => request({ url: '/api/auth/register', method: 'POST', data, publicRequest: true }),
  wechatLogin: data => request({ url: '/api/auth/wechat', method: 'POST', data, publicRequest: true }),
  currentUser: () => request({ url: '/api/users/me' }),
  shops: () => request({ url: '/api/shops', publicRequest: true }),
  menu: shopId => request({ url: `/api/shops/${shopId}/menu`, publicRequest: true }),
  cart: shopId => request({ url: `/api/cart?shopId=${shopId}` }),
  addCartItem: data => request({ url: '/api/cart/items', method: 'POST', data }),
  updateCartItem: (itemId, quantity) => request({ url: `/api/cart/items/${itemId}`, method: 'PUT', data: { quantity } }),
  deleteCartItem: itemId => request({ url: `/api/cart/items/${itemId}`, method: 'DELETE' }),
  addresses: () => request({ url: '/api/addresses' }),
  createAddress: data => request({ url: '/api/addresses', method: 'POST', data }),
  updateAddress: (id, data) => request({ url: `/api/addresses/${id}`, method: 'PUT', data }),
  setDefaultAddress: id => request({ url: `/api/addresses/${id}/default`, method: 'PUT' }),
  deleteAddress: id => request({ url: `/api/addresses/${id}`, method: 'DELETE' }),
  createOrder: data => request({ url: '/api/orders', method: 'POST', data }),
  orders: () => request({ url: '/api/orders?page=1&size=50' }),
  payOrder: orderNo => request({ url: `/api/orders/${orderNo}/pay`, method: 'POST' }),
  cancelOrder: orderNo => request({ url: `/api/orders/${orderNo}/cancel`, method: 'POST' })
}
