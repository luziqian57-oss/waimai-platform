App({
  globalData: {
    accessToken: '',
    currentShopId: null
  },

  onLaunch() {
    this.globalData.accessToken = wx.getStorageSync('accessToken') || ''
    this.globalData.currentShopId = wx.getStorageSync('currentShopId') || null
  },

  saveToken(token) {
    this.globalData.accessToken = token
    wx.setStorageSync('accessToken', token)
  },

  clearSession() {
    this.globalData.accessToken = ''
    wx.removeStorageSync('accessToken')
  }
})
