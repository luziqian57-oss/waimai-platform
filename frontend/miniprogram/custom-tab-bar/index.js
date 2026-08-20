Component({
  data: {
    selected: 0,
    tabs: [
      { pagePath: '/pages/home/home', text: '点餐', icon: '⌂' },
      { pagePath: '/pages/cart/cart', text: '购物车', icon: '▱' },
      { pagePath: '/pages/orders/orders', text: '订单', icon: '◷' },
      { pagePath: '/pages/profile/profile', text: '我的', icon: '○' }
    ]
  },
  methods: {
    switchTab(event) {
      const index = Number(event.currentTarget.dataset.index)
      const url = this.data.tabs[index].pagePath
      wx.switchTab({ url })
      this.setData({ selected: index })
    }
  }
})
