const { API_BASE_URL } = require('../config')

function request(options) {
  const app = getApp()
  const token = app.globalData.accessToken || wx.getStorageSync('accessToken')
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${API_BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
          return
        }
        if (res.statusCode === 401 && !options.publicRequest) {
          app.clearSession()
          wx.reLaunch({ url: '/pages/login/login' })
        }
        const message = res.data && res.data.message ? res.data.message : `请求失败（${res.statusCode}）`
        wx.showToast({ title: message, icon: 'none' })
        reject(new Error(message))
      },
      fail(error) {
        wx.showToast({ title: '无法连接服务器', icon: 'none' })
        reject(error)
      }
    })
  })
}

module.exports = { request, API_BASE_URL }
