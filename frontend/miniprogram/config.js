const LOCAL_API_BASE_URL = 'http://localhost:8081'
const PROD_API_BASE_URL = 'https://api-production-0977b.up.railway.app'
const USE_LOCAL_API = false

function resolveApiBaseUrl() {
  return USE_LOCAL_API ? LOCAL_API_BASE_URL : PROD_API_BASE_URL
}

module.exports = {
  API_BASE_URL: resolveApiBaseUrl(),
  LOCAL_API_BASE_URL,
  PROD_API_BASE_URL
}
