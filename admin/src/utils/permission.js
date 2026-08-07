/**
 * Permission Management Module
 *
 * Provides fine-grained permission checking system
 * Supports both role-based and permission-based access control
 *
 * Features:
 * - Database-driven permissions (fetched from backend)
 * - Hierarchical permission model (resource.action format)
 * - Role-based access control with permission assignment
 * - Local caching with fallback to defaults
 * - Real-time permission updates
 *
 * Permission Categories:
 * - dashboard: 仪表盘访问权限
 * - merchant: 商家管理权限 (view, create, update, delete, quota)
 * - device: 设备管理权限 (view, create, update, delete)
 * - ai: AI创作权限 (generate, corpus, config, merchant_config)
 * - marketing: 营销管理权限 (platforms, promotion, coupons, plans, orders)
 * - system: 系统管理权限 (settings, user, role, access)
 *
 * Permission Format: "resource.action" (e.g., "merchant.view", "merchant.create")
 */

import { ref, computed } from 'vue'
import { useUserStore } from '@/store/user'

// Permission definitions (local constants for frontend use)
export const PERMISSION_DEFINITIONS = {
  // Dashboard
  DASHBOARD_VIEW: 'dashboard.view',

  // Merchant Management
  MERCHANT_VIEW: 'merchant.view',
  MERCHANT_CREATE: 'merchant.create',
  MERCHANT_EDIT: 'merchant.update',
  MERCHANT_DELETE: 'merchant.delete',
  MERCHANT_QUOTA: 'merchant.quota',

  // Device Management
  DEVICE_VIEW: 'device.view',
  DEVICE_CREATE: 'device.create',
  DEVICE_EDIT: 'device.update',
  DEVICE_DELETE: 'device.delete',

  // AI Management
  AI_GENERATE: 'ai.generate',
  AI_CORPUS: 'ai.corpus',
  AI_CONFIG: 'ai.config',
  AI_MERCHANT_CONFIG: 'ai.merchant_config',

  // Marketing Management
  MARKETING_PLATFORMS: 'marketing.platforms',
  MARKETING_PROMOTION: 'marketing.promotion',
  MARKETING_COUPONS: 'marketing.coupons',
  MARKETING_PLANS: 'marketing.plans',
  MARKETING_ORDERS: 'marketing.orders',

  // System Management
  SYSTEM_SETTINGS: 'system.settings',
  SYSTEM_USER: 'system.user',
  SYSTEM_ROLE: 'system.role',
  SYSTEM_ACCESS: 'system.access'
}

// Route permission mappings
export const ROUTE_PERMISSIONS = {
  'Dashboard': PERMISSION_DEFINITIONS.DASHBOARD_VIEW,
  'MerchantList': PERMISSION_DEFINITIONS.MERCHANT_VIEW,
  'MerchantDetail': PERMISSION_DEFINITIONS.MERCHANT_VIEW,
  'MerchantQuota': PERMISSION_DEFINITIONS.MERCHANT_QUOTA,
  'DeviceList': PERMISSION_DEFINITIONS.DEVICE_VIEW,
  'AiGenerate': PERMISSION_DEFINITIONS.AI_GENERATE,
  'CorpusManage': PERMISSION_DEFINITIONS.AI_CORPUS,
  'AiConfig': PERMISSION_DEFINITIONS.AI_CONFIG,
  'MerchantConfig': PERMISSION_DEFINITIONS.AI_MERCHANT_CONFIG,
  'PlatformManage': PERMISSION_DEFINITIONS.MARKETING_PLATFORMS,
  'MerchantPromotion': PERMISSION_DEFINITIONS.MARKETING_PROMOTION,
  'PromotionDetail': PERMISSION_DEFINITIONS.MARKETING_PROMOTION,
  'CouponManage': PERMISSION_DEFINITIONS.MARKETING_COUPONS,
  'PlanManage': PERMISSION_DEFINITIONS.MARKETING_PLANS,
  'OrderManage': PERMISSION_DEFINITIONS.MARKETING_ORDERS,
  'SystemSettings': PERMISSION_DEFINITIONS.SYSTEM_SETTINGS,
  'UserManage': PERMISSION_DEFINITIONS.SYSTEM_USER,
  'RoleManage': PERMISSION_DEFINITIONS.SYSTEM_ROLE,
  'MerchantAccess': PERMISSION_DEFINITIONS.SYSTEM_ACCESS
}

// Default role permissions (fallback when server doesn't return permissions)
export const ROLE_PERMISSIONS = {
  'super_admin': ['*'], // Super admin has all permissions
  'admin': [
    PERMISSION_DEFINITIONS.DASHBOARD_VIEW,
    PERMISSION_DEFINITIONS.MERCHANT_VIEW,
    PERMISSION_DEFINITIONS.MERCHANT_CREATE,
    PERMISSION_DEFINITIONS.MERCHANT_EDIT,
    PERMISSION_DEFINITIONS.MERCHANT_DELETE,
    PERMISSION_DEFINITIONS.MERCHANT_QUOTA,
    PERMISSION_DEFINITIONS.DEVICE_VIEW,
    PERMISSION_DEFINITIONS.DEVICE_CREATE,
    PERMISSION_DEFINITIONS.DEVICE_EDIT,
    PERMISSION_DEFINITIONS.DEVICE_DELETE,
    PERMISSION_DEFINITIONS.AI_GENERATE,
    PERMISSION_DEFINITIONS.AI_CORPUS,
    PERMISSION_DEFINITIONS.AI_CONFIG,
    PERMISSION_DEFINITIONS.MARKETING_PROMOTION,
    PERMISSION_DEFINITIONS.MARKETING_COUPONS,
    PERMISSION_DEFINITIONS.MARKETING_PLANS,
    PERMISSION_DEFINITIONS.MARKETING_ORDERS,
    PERMISSION_DEFINITIONS.SYSTEM_SETTINGS,
    PERMISSION_DEFINITIONS.SYSTEM_USER,
    PERMISSION_DEFINITIONS.SYSTEM_ROLE
  ],
  'merchant': [
    PERMISSION_DEFINITIONS.DASHBOARD_VIEW,
    PERMISSION_DEFINITIONS.MERCHANT_VIEW,
    PERMISSION_DEFINITIONS.DEVICE_VIEW,
    PERMISSION_DEFINITIONS.DEVICE_CREATE,
    PERMISSION_DEFINITIONS.DEVICE_EDIT,
    PERMISSION_DEFINITIONS.DEVICE_DELETE,
    PERMISSION_DEFINITIONS.AI_GENERATE,
    PERMISSION_DEFINITIONS.AI_CORPUS,
    PERMISSION_DEFINITIONS.MARKETING_PROMOTION,
    PERMISSION_DEFINITIONS.MARKETING_COUPONS,
    PERMISSION_DEFINITIONS.MARKETING_PLANS,
    PERMISSION_DEFINITIONS.MARKETING_ORDERS
  ]
}

// Reactive state for database-driven permissions
const permissionsCache = ref(new Map())
const rolesCache = ref(new Map())
const lastFetched = ref(0)
const CACHE_DURATION = 10 * 60 * 1000 // 10 minutes

/**
 * Fetch permissions from backend API
 */
export async function fetchPermissionsFromServer() {
  try {
    const userStore = useUserStore()
    if (!userStore.token) return false

    const response = await fetch('/api/v1/permissions', {
      headers: {
        'Authorization': `Bearer ${userStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      console.warn('Failed to fetch permissions from server')
      return false
    }

    const data = await response.json()
    if (data.data && Array.isArray(data.data)) {
      // Build permission map
      const newCache = new Map()
      data.data.forEach(perm => {
        newCache.set(perm.code, perm)
      })
      permissionsCache.value = newCache
      lastFetched.value = Date.now()
      return true
    }
  } catch (error) {
    console.error('Error fetching permissions:', error)
  }
  return false
}

/**
 * Fetch role with permissions from backend
 */
export async function fetchRolePermissionsFromServer(roleCode) {
  try {
    const userStore = useUserStore()
    if (!userStore.token) return null

    const response = await fetch(`/api/v1/permissions/roles/${roleCode}/permissions`, {
      headers: {
        'Authorization': `Bearer ${userStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      console.warn(`Failed to fetch permissions for role ${roleCode}`)
      return null
    }

    const data = await response.json()
    if (data.data && Array.isArray(data.data)) {
      rolesCache.value.set(roleCode, data.data)
      return data.data
    }
  } catch (error) {
    console.error(`Error fetching role ${roleCode} permissions:`, error)
  }
  return null
}

/**
 * Check if cache is still valid
 */
function isCacheValid() {
  return (Date.now() - lastFetched.value) < CACHE_DURATION
}

/**
 * Get permissions for a role (from cache or server)
 */
export async function getRolePermissionsFromServer(roleCode) {
  // Check cache first
  if (rolesCache.value.has(roleCode)) {
    return rolesCache.value.get(roleCode)
  }

  // Fetch from server
  const permissions = await fetchRolePermissionsFromServer(roleCode)
  if (permissions) {
    return permissions
  }

  // Fallback to default permissions
  return ROLE_PERMISSIONS[roleCode] || []
}

/**
 * Check if user has permission
 * @param {string|array} requiredPermissions - Required permission(s)
 * @param {string} role - User role
 * @param {array} userPermissions - User's permissions list (from server or local cache)
 * @param {boolean} requireAll - Require all permissions (true) or any permission (false)
 * @returns {boolean} - True if user has required permission(s)
 */
export function checkPermission(requiredPermissions, role, userPermissions = [], requireAll = false) {
  // Super admin always has access
  if (role === 'super_admin') {
    return true
  }

  // Normalize requiredPermissions to array
  const permissions = Array.isArray(requiredPermissions) ? requiredPermissions : [requiredPermissions]

  // Get user's actual permissions
  const actualPermissions = userPermissions && userPermissions.length > 0
    ? userPermissions
    : (ROLE_PERMISSIONS[role] || [])

  // Check if user has wildcard permission
  if (actualPermissions.includes('*')) {
    return true
  }

  if (requireAll) {
    // User must have ALL required permissions
    return permissions.every(perm => hasPermissionInList(perm, actualPermissions))
  } else {
    // User must have ANY of the required permissions
    return permissions.some(perm => hasPermissionInList(perm, actualPermissions))
  }
}

/**
 * Check if permission exists in permission list
 * Supports wildcard and hierarchical permissions
 * @param {string} permission - Permission to check (e.g., "merchant.view")
 * @param {array} permissionList - List of user permissions
 * @returns {boolean}
 */
function hasPermissionInList(permission, permissionList) {
  return permissionList.some(p => {
    // Exact match
    if (p === permission) return true
    // Wildcard match
    if (p === '*') return true
    // Hierarchical match (parent permission covers child)
    // e.g., "merchant" covers "merchant.view"
    const [resource] = permission.split('.')
    if (p === resource) return true
    return false
  })
}

/**
 * Get permissions for a role
 * Uses fallback to default permissions
 * @param {string} role - User role
 * @returns {array} - Array of permissions
 */
export function getRolePermissions(role) {
  return ROLE_PERMISSIONS[role] || []
}

/**
 * Initialize permissions on app startup
 * Fetches permissions from backend and caches them
 * Called from main.js or App.vue setup
 */
export async function initializePermissions() {
  try {
    const success = await fetchPermissionsFromServer()
    if (success) {
      console.debug('Permissions initialized from server')
      return true
    } else {
      console.warn('Using fallback permissions')
      return false
    }
  } catch (error) {
    console.error('Failed to initialize permissions:', error)
    return false
  }
}

/**
 * Refresh permissions from server
 * Useful when permissions are updated dynamically
 */
export async function refreshPermissions() {
  return fetchPermissionsFromServer()
}

/**
 * Get route's required permission
 * @param {string} routeName - Route name
 * @returns {string|null} - Required permission or null if not restricted
 */
export function getRoutePermission(routeName) {
  return ROUTE_PERMISSIONS[routeName] || null
}

/**
 * Check if route requires specific role
 * @param {object} routeMeta - Route meta data
 * @param {string} role - User role
 * @returns {boolean} - True if user's role satisfies route requirement
 */
export function checkRouteRoleRequirement(routeMeta, role) {
  // Super admin always passes role checks
  if (role === 'super_admin') {
    return true
  }

  // Check superAdminOnly flag
  if (routeMeta.superAdminOnly) {
    return role === 'super_admin'
  }

  // Check specific roles requirement
  if (routeMeta.roles && Array.isArray(routeMeta.roles)) {
    return routeMeta.roles.includes(role)
  }

  return true
}

/**
 * Check if user can perform specific action on resource
 * @param {string} action - Action name (view, create, update, delete)
 * @param {string} resource - Resource name (merchant, device, etc.)
 * @param {string} role - User role
 * @param {array} userPermissions - User's permissions
 * @returns {boolean}
 */
export function canPerformAction(action, resource, role, userPermissions = []) {
  const requiredPermission = `${resource}.${action}`
  return checkPermission(requiredPermission, role, userPermissions)
}

/**
 * Get all accessible routes for a role
 * @param {string} role - User role
 * @param {object} userInfo - User info object
 * @param {array} userPermissions - User's permissions
 * @returns {array} - Accessible route names
 */
export function getAccessibleRoutes(role, userInfo = {}, userPermissions = []) {
  const accessible = []

  Object.entries(ROUTE_PERMISSIONS).forEach(([routeName, permission]) => {
    if (checkPermission(permission, role, userPermissions)) {
      accessible.push(routeName)
    }
  })

  // Special case: Merchant can only access their own detail page
  if (role === 'merchant' && userInfo.merchantId) {
    accessible.push(`MerchantDetail:${userInfo.merchantId}`)
  }

  return accessible
}

/**
 * Parse permission string to resource and action
 * @param {string} permission - Permission string (e.g., "merchant.view")
 * @returns {object} - {resource, action}
 */
export function parsePermission(permission) {
  const [resource, action] = permission.split('.')
  return { resource, action }
}

/**
 * Create permission string from resource and action
 * @param {string} resource - Resource name
 * @param {string} action - Action name
 * @returns {string} - Permission string
 */
export function createPermission(resource, action) {
  return `${resource}.${action}`
}

export default {
  PERMISSION_DEFINITIONS,
  ROUTE_PERMISSIONS,
  ROLE_PERMISSIONS,
  checkPermission,
  getRolePermissions,
  getRoutePermission,
  checkRouteRoleRequirement,
  canPerformAction,
  getAccessibleRoutes,
  parsePermission,
  createPermission
}
