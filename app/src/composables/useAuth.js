import { ref } from 'vue'
import { requestUnlockCode, checkUnlockStatus } from '../api/auth.js'

export function useAuth() {
  const unlockState = ref({})

  /**
   * 生成或获取device_id
   */
  function getDeviceId() {
    let deviceId = localStorage.getItem('device_id')
    if (!deviceId) {
      deviceId = crypto.randomUUID()
      localStorage.setItem('device_id', deviceId)
    }
    return deviceId
  }

  /**
   * 请求全局解锁
   * @param {string} moduleType - 模块类型（NOTE/OTHER）
   * @returns {Promise<{code: string, expires_in: number, deviceId: string}>}
   */
  async function requestUnlock(moduleType) {
    const deviceId = getDeviceId()
    const result = await requestUnlockCode({
      device_id: deviceId,
      module_type: moduleType
      // 移除 target_id，改为全局解锁
    })
    return { ...result, deviceId }
  }

  /**
   * 轮询检查全局解锁状态
   * @param {string} moduleType - 模块类型
   * @param {Function} onSuccess - 解锁成功回调
   * @returns {Function} 停止轮询的函数
   */
  function startPolling(moduleType, onSuccess) {
    const deviceId = getDeviceId()
    const timer = setInterval(async () => {
      try {
        const status = await checkUnlockStatus({
          device_id: deviceId,
          module_type: moduleType
          // 移除 target_id，检查全局状态
        })
        if (status.unlocked) {
          clearInterval(timer)
          // 保存全局解锁状态到localStorage（12小时有效期）
          const unlockKey = `unlock_${moduleType}`
          localStorage.setItem(unlockKey, JSON.stringify({
            unlocked: true,
            expires: Date.now() + 12 * 60 * 60 * 1000
          }))
          onSuccess()
        }
      } catch (error) {
        console.error('轮询解锁状态失败:', error)
      }
    }, 3000)
    
    // 返回停止轮询的函数
    return () => clearInterval(timer)
  }

  /**
   * 检查全局解锁状态
   * @param {string} moduleType - 模块类型
   * @returns {boolean} 是否已解锁
   */
  function isLocallyUnlocked(moduleType) {
    const unlockKey = `unlock_${moduleType}`
    const state = localStorage.getItem(unlockKey)
    if (state) {
      const { unlocked, expires } = JSON.parse(state)
      if (unlocked && Date.now() < expires) {
        return true
      }
      // 已过期，清除本地状态
      localStorage.removeItem(unlockKey)
    }
    return false
  }

  /**
   * 清除全局解锁状态
   * @param {string} moduleType - 模块类型
   */
  function clearLocalUnlock(moduleType) {
    const unlockKey = `unlock_${moduleType}`
    localStorage.removeItem(unlockKey)
  }

  return {
    unlockState,
    getDeviceId,
    requestUnlock,
    startPolling,
    isLocallyUnlocked,
    clearLocalUnlock
  }
}
