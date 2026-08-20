const api = require('../../utils/api')
const { productImage } = require('../../utils/assets')

Page({
  data: { shops: [], shop: null, categories: [], activeCategoryId: null, loading: true },

  onLoad() { this.loadShops() },
  onShow() {
    if (this.getTabBar && this.getTabBar()) this.getTabBar().setData({ selected: 0 })
  },
  onPullDownRefresh() { this.loadShops().finally(() => wx.stopPullDownRefresh()) },

  async loadShops() {
    this.setData({ loading: true })
    try {
      const shops = await api.shops()
      this.setData({ shops })
      if (shops.length) await this.selectShopById(shops[0].id)
    } finally {
      this.setData({ loading: false })
    }
  },

  selectShop(event) { this.selectShopById(Number(event.currentTarget.dataset.id)) },

  async selectShopById(shopId) {
    const menu = await api.menu(shopId)
    const categories = menu.categories.map(category => ({
      ...category,
      products: category.products.map(product => ({
        ...product,
        initial: product.productName ? product.productName.charAt(0) : '食',
        localImage: productImage(product.productName)
      }))
    }))
    getApp().globalData.currentShopId = shopId
    wx.setStorageSync('currentShopId', shopId)
    this.setData({
      shop: menu.shop,
      categories,
      activeCategoryId: categories.length ? categories[0].id : null
    })
  },

  selectCategory(event) { this.setData({ activeCategoryId: Number(event.currentTarget.dataset.id) }) },

  async addSku(event) {
    if (!getApp().globalData.accessToken) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    const skuId = Number(event.currentTarget.dataset.sku)
    await api.addCartItem({ shopId: this.data.shop.id, skuId, quantity: 1 })
    wx.showToast({ title: '已加入购物车', icon: 'success' })
  }
})
