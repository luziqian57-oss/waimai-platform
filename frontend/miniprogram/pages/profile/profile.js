const api = require('../../utils/api')
const { API_BASE_URL } = require('../../config')

Page({
  data: { loggedIn: false, user: null, loading: false, apiHost: '' },

  onShow() {
    if (this.getTabBar && this.getTabBar()) this.getTabBar().setData({ selected: 3 })
    const loggedIn = Boolean(getApp().globalData.accessToken)
    this.setData({ loggedIn, apiHost: API_BASE_URL.replace(/^https?:\/\//, '') })
    if (loggedIn) this.loadUser()
  },

  async loadUser() {
    this.setData({ loading: true })
    try {
      const user = await api.currentUser()
      this.setData({ user: { ...user, initial: user.nickname ? user.nickname.charAt(0) : '星' } })
    } finally {
      this.setData({ loading: false })
    }
  },

  goLogin() { wx.navigateTo({ url: '/pages/login/login' }) },
  goAddresses() { wx.navigateTo({ url: '/pages/addresses/addresses' }) },
  goOrders() { wx.switchTab({ url: '/pages/orders/orders' }) },

  logout() {
    wx.showModal({
      title: '退出登录', content: '确定要退出当前账号吗？', confirmColor: '#ff5b35',
      success: result => {
        if (!result.confirm) return
        getApp().clearSession()
        this.setData({ loggedIn: false, user: null })
        wx.showToast({ title: '已退出', icon: 'success' })
      }
    })
  }
})
