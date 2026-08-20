const api = require('../../utils/api')

const STATUS_TEXT = {
  PENDING_PAYMENT: '待支付', PAID: '已支付', CONFIRMED: '商家已接单',
  PREPARING: '制作中', DELIVERING: '配送中', COMPLETED: '已完成', CANCELLED: '已取消'
}

Page({
  data: { orders: [], loggedIn: false, loading: false },

  onShow() {
    if (this.getTabBar && this.getTabBar()) this.getTabBar().setData({ selected: 2 })
    const loggedIn = Boolean(getApp().globalData.accessToken)
    this.setData({ loggedIn })
    if (loggedIn) this.load()
  },

  onPullDownRefresh() { this.load().finally(() => wx.stopPullDownRefresh()) },

  async load() {
    this.setData({ loading: true })
    try {
      const page = await api.orders()
      const orders = page.items.map(order => ({ ...order, statusText: STATUS_TEXT[order.orderStatus] || order.orderStatus }))
      this.setData({ orders })
    } finally {
      this.setData({ loading: false })
    }
  },

  async pay(event) {
    await api.payOrder(event.currentTarget.dataset.no)
    wx.showToast({ title: '支付成功', icon: 'success' })
    this.load()
  },

  cancel(event) {
    const orderNo = event.currentTarget.dataset.no
    wx.showModal({
      title: '取消订单', content: '取消后库存会自动恢复，确定继续吗？',
      success: async result => {
        if (!result.confirm) return
        await api.cancelOrder(orderNo)
        this.load()
      }
    })
  },

  goLogin() { wx.navigateTo({ url: '/pages/login/login' }) }
})
