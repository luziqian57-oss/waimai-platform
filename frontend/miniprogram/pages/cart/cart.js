const api = require('../../utils/api')
const { productImage } = require('../../utils/assets')

Page({
  data: { cart: null, address: null, loading: false, submitting: false },

  onShow() {
    if (this.getTabBar && this.getTabBar()) this.getTabBar().setData({ selected: 1 })
    if (!getApp().globalData.accessToken) {
      this.setData({ cart: null, address: null })
      return
    }
    this.load()
  },

  async load() {
    const shopId = getApp().globalData.currentShopId || wx.getStorageSync('currentShopId')
    if (!shopId) return
    this.setData({ loading: true })
    try {
      const [cart, addresses] = await Promise.all([api.cart(shopId), api.addresses()])
      const displayCart = {
        ...cart,
        items: cart.items.map(item => ({
          ...item,
          initial: item.productName ? item.productName.charAt(0) : '食',
          localImage: productImage(item.productName)
        }))
      }
      this.setData({ cart: displayCart, address: addresses.find(item => item.isDefault) || addresses[0] || null })
    } finally {
      this.setData({ loading: false })
    }
  },

  async changeQuantity(event) {
    const itemId = Number(event.currentTarget.dataset.id)
    const quantity = Number(event.currentTarget.dataset.quantity)
    if (quantity <= 0) {
      await api.deleteCartItem(itemId)
    } else {
      await api.updateCartItem(itemId, quantity)
    }
    await this.load()
  },

  chooseAddress() { wx.navigateTo({ url: '/pages/addresses/addresses' }) },

  async checkout() {
    if (!this.data.address) {
      wx.showToast({ title: '请先添加收货地址', icon: 'none' })
      return
    }
    if (!this.data.cart || !this.data.cart.items.length) return
    this.setData({ submitting: true })
    try {
      const order = await api.createOrder({
        shopId: this.data.cart.shopId,
        addressId: this.data.address.id,
        remark: ''
      })
      wx.showToast({ title: '下单成功', icon: 'success' })
      setTimeout(() => wx.switchTab({ url: '/pages/orders/orders' }), 500)
      return order
    } finally {
      this.setData({ submitting: false })
    }
  },

  goLogin() { wx.navigateTo({ url: '/pages/login/login' }) },
  goOrder() { wx.switchTab({ url: '/pages/home/home' }) }
})
