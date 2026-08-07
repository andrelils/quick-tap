/**
 * Router Permission Guard
 *
 * Comprehensive permission checking for route navigation
 * Handles role-based, permission-based, and dynamic access control
 */

import { checkPermission, checkRouteRoleRequirement, getRoutePermission } from './permission'

/**
 * Check if user can access route
 * @param {object} to - Target route
 * @param {object} userStore - User store instance
 * @returns {object} - {allowed: boolean, reason?: string}
 */
export function canAccessRoute(to, userStore) {
  // Public routes are always accessible
  if (to.meta?.public) {
    return { allowed: true }
  }

  // Must be logged in
  if (!userStore.isLoggedIn) {
    return { allowed: false, reason: 'NOT_LOGGED_IN' }
  }

  const role = userStore.userInfo?.role
  const permissions = userStore.permissions || []

  // Check role-based access
  const roleCheck = checkRouteRoleRequirement(to.meta || {}, role)
  if (!roleCheck) {
    return { allowed: false, reason: 'ROLE_REQUIRED' }
  }

  // Check permission-based access
  const requiredPermission = getRoutePermission(to.name)
  if (requiredPermission) {
    const permissionCheck = checkPermission(requiredPermission, role, permissions)
    if (!permissionCheck) {
      return { allowed: false, reason: 'PERMISSION_REQUIRED' }
    }
  }

  // Special check for merchant detail page
  if (to.name === 'MerchantDetail') {
    const merchantId = to.params?.id
    const userMerchantId = userStore.userInfo?.merchantId

    // Admin can access any merchant detail
    if (role === 'super_admin' || role === 'admin') {
      return { allowed: true }
    }

    // Merchant can only access their own detail
    if (role === 'merchant' && String(merchantId) === String(userMerchantId)) {
      return { allowed: true }
    }

    return { allowed: false, reason: 'RESOURCE_RESTRICTED' }
  }

  // Special check for promotion detail page
  if (to.name === 'PromotionDetail') {
    // Merchant can only access their own promotion details
    if (role === 'merchant') {
      const promotionId = to.params?.id
      // This would be verified on the component level since we don't have merchant's promotion IDs here
      // Trust component to validate
      return { allowed: true }
    }
    return { allowed: true }
  }

  return { allowed: true }
}

/**
 * Check if user can access specific page with given parameters
 * @param {string} routeName - Route name
 * @param {object} params - Route params
 * @param {object} userStore - User store
 * @returns {boolean}
 */
export function canAccessResourceRoute(routeName, params, userStore) {
  const role = userStore.userInfo?.role
  const permissions = userStore.permissions || []

  // For merchant-specific routes
  if (routeName === 'MerchantDetail') {
    if (role === 'super_admin' || role === 'admin') return true
    if (role === 'merchant') {
      return String(params.id) === String(userStore.userInfo?.merchantId)
    }
    return false
  }

  // For promotion routes
  if (routeName === 'PromotionDetail') {
    if (role === 'super_admin' || role === 'admin') return true
    if (role === 'merchant') {
      // In real scenario, verify merchant owns this promotion
      return true
    }
    return false
  }

  return true
}

/**
 * Get redirect path after permission denied
 * @param {string} reason - Denial reason
 * @returns {string} - Redirect path
 */
export function getRedirectPath(reason) {
  switch (reason) {
    case 'NOT_LOGGED_IN':
      return '/login'
    case 'ROLE_REQUIRED':
      return '/dashboard'
    case 'PERMISSION_REQUIRED':
      return '/dashboard'
    case 'RESOURCE_RESTRICTED':
      return '/dashboard'
    default:
      return '/dashboard'
  }
}

/**
 * Log access denied event
 * @param {object} params - Event parameters
 */
export function logAccessDenied({ reason, routeName, role, userId }) {
  console.warn(`[ACCESS DENIED] Route: ${routeName}, Reason: ${reason}, Role: ${role}, UserId: ${userId}`)
}

export default {
  canAccessRoute,
  canAccessResourceRoute,
  getRedirectPath,
  logAccessDenied
}
