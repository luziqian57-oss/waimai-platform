const api = require('../../utils/api')

const EMPTY_FORM = {
  id: null, contactName: '', contactPhone: '', region: ['北京市', '北京市', '朝阳区'],
  detailAddress: '', isDefault: false
}

Page({
  data: { addresses: [], loading: false, showForm: false, submitting: false, form: { ...EMPTY_FORM } },

  onShow() {
    if (!getApp().globalData.accessToken) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    this.load()
  },

  onPullDownRefresh() { this.load().finally(() => wx.stopPullDownRefresh()) },

  async load() {
    this.setData({ loading: true })
    try { this.setData({ addresses: await api.addresses() }) }
    finally { this.setData({ loading: false }) }
  },

  addAddress() { this.setData({ showForm: true, form: { ...EMPTY_FORM } }) },

  editAddress(event) {
    const address = this.data.addresses.find(item => item.id === Number(event.currentTarget.dataset.id))
    if (!address) return
    this.setData({
      showForm: true,
      form: {
        id: address.id, contactName: address.contactName, contactPhone: address.contactPhone,
        region: [address.province, address.city, address.district],
        detailAddress: address.detailAddress, isDefault: address.isDefault
      }
    })
  },

  closeForm() { this.setData({ showForm: false }) },
  stopPropagation() {},

  onInput(event) { this.setData({ [`form.${event.currentTarget.dataset.field}`]: event.detail.value }) },
  onRegionChange(event) { this.setData({ 'form.region': event.detail.value }) },
  onDefaultChange(event) { this.setData({ 'form.isDefault': event.detail.value }) },

  async save() {
    const form = this.data.form
    if (!form.contactName || !/^1[3-9]\d{9}$/.test(form.contactPhone) || !form.detailAddress) {
      wx.showToast({ title: '请填写姓名、正确手机号和详细地址', icon: 'none' })
      return
    }
    const payload = {
      contactName: form.contactName.trim(), contactPhone: form.contactPhone,
      province: form.region[0], city: form.region[1], district: form.region[2],
      detailAddress: form.detailAddress.trim(), isDefault: form.isDefault
    }
    this.setData({ submitting: true })
    try {
      if (form.id) await api.updateAddress(form.id, payload)
      else await api.createAddress(payload)
      this.setData({ showForm: false })
      wx.showToast({ title: form.id ? '已保存' : '地址已添加', icon: 'success' })
      await this.load()
    } finally { this.setData({ submitting: false }) }
  },

  async setDefault(event) {
    await api.setDefaultAddress(Number(event.currentTarget.dataset.id))
    await this.load()
  },

  remove(event) {
    const id = Number(event.currentTarget.dataset.id)
    wx.showModal({
      title: '删除地址', content: '确定删除这个收货地址吗？', confirmColor: '#ff3b30',
      success: async result => {
        if (!result.confirm) return
        await api.deleteAddress(id)
        await this.load()
      }
    })
  }
})
