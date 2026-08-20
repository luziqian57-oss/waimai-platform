const PRODUCT_IMAGES = {
  '黑椒牛肉饭': '/assets/images/beef-rice.jpg',
  '照烧鸡腿饭': '/assets/images/chicken-rice.jpg',
  '黄金鸡块': '/assets/images/chicken-bites.jpg',
  '青柠气泡水': '/assets/images/lime-soda.jpg'
}

function productImage(productName) {
  return PRODUCT_IMAGES[productName] || ''
}

module.exports = { productImage }
