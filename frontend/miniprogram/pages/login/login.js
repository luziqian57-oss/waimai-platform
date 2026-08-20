const api = require('../../utils/api')

Page({
  data: {
    mode: 'login',
    username: '',
    password: '',
    nickname: '',
    loading: false
  },

  switchMode() {
    this.setData({ mode: this.data.mode === 'login' ? 'register' : 'login' })
  },

  onInput(event) {
    this.setData({ [event.currentTarget.dataset.field]: event.detail.value })
  },

  async submit() {
    const { mode, username, password, nickname } = this.data
    if (!username || !password || (mode === 'register' && !nickname)) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const response = mode === 'login'
        ? await api.login({ username, password })
        : await api.register({ username, password, nickname, phone: '' })
      getApp().saveToken(response.accessToken)
      wx.switchTab({ url: '/pages/home/home' })
    } finally {
      this.setData({ loading: false })
    }
  },

  wechatLogin() {
    wx.login({
      success: async result => {
        if (!result.code) return
        try {
          const response = await api.wechatLogin({ code: result.code, nickname: '微信用户' })
          getApp().saveToken(response.accessToken)
          wx.switchTab({ url: '/pages/home/home' })
        } catch (error) {}
      }
    })
  }
})
