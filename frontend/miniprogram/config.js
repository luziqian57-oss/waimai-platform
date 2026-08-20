const DEV_API_BASE_URL = 'http://localhost:8081'
const PROD_API_BASE_URL = 'https://api-production-0977b.up.railway.app'

function resolveApiBaseUrl() {
  try {
    const account = wx.getAccountInfoSync()
    const envVersion = account && account.miniProgram && account.miniProgram.envVersion
    return envVersion === 'release' || envVersion === 'trial' ? PROD_API_BASE_URL : DEV_API_BASE_URL
  } catch (error) {
    return DEV_API_BASE_URL
  }
}

module.exports = {
  API_BASE_URL: resolveApiBaseUrl(),
  DEV_API_BASE_URL,
  PROD_API_BASE_URL
}
