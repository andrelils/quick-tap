import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * Page State Management Store
 *
 * Purpose:
 * - Persist pagination state (current page, page size) across navigation
 * - Store search/filter parameters to restore view on back navigation
 * - Maintain list view state when user returns from detail view
 *
 * Features:
 * - Per-page state tracking (e.g., merchant list, admin list, etc.)
 * - Automatic cleanup to prevent memory leaks
 * - localStorage integration for persistent state
 *
 * Usage:
 * // In list component
 * const pageStore = usePageStore()
 *
 * // Save state before navigation
 * const navigateToDetail = (record) => {
 *   pageStore.setPageState('merchant-list', {
 *     current: pagination.current,
 *     pageSize: pagination.pageSize,
 *     keyword: searchForm.keyword,
 *     status: searchForm.status
 *   })
 *   router.push(`/merchant/detail/${record.id}`)
 * }
 *
 * // Restore state on component mount
 * const savedState = pageStore.getPageState('merchant-list')
 * if (savedState) {
 *   pagination.current = savedState.current
 *   pagination.pageSize = savedState.pageSize
 *   searchForm.keyword = savedState.keyword
 *   searchForm.status = savedState.status
 * }
 */

export const usePageStore = defineStore('page', () => {
  // Store page states: key -> page name, value -> state object
  const pageStates = ref({})

  // Store last visited page to differentiate back navigation
  const lastVisitedPage = ref(null)

  /**
   * Save pagination and filter state for a specific page
   * @param {string} pageName - Unique page identifier (e.g., 'merchant-list', 'admin-list')
   * @param {object} state - State object containing pagination and filter info
   *   - current: current page number
   *   - pageSize: items per page
   *   - keyword: search keyword
   *   - status: filter status
   *   - and other filter fields
   */
  const setPageState = (pageName, state) => {
    pageStates.value[pageName] = {
      ...state,
      savedAt: Date.now()
    }
    // Also save to localStorage for persistence across browser sessions
    try {
      localStorage.setItem(`page_state_${pageName}`, JSON.stringify(pageStates.value[pageName]))
    } catch (e) {
      console.warn('Failed to save page state to localStorage', e)
    }
  }

  /**
   * Get saved state for a specific page
   * @param {string} pageName - Unique page identifier
   * @param {number} maxAge - Maximum age of state in milliseconds (default: 30 minutes)
   * @returns {object|null} - Saved state or null if not found/expired
   */
  const getPageState = (pageName, maxAge = 30 * 60 * 1000) => {
    let state = pageStates.value[pageName]

    // Try to restore from localStorage if not in memory
    if (!state) {
      try {
        const saved = localStorage.getItem(`page_state_${pageName}`)
        if (saved) {
          state = JSON.parse(saved)
          pageStates.value[pageName] = state
        }
      } catch (e) {
        console.warn('Failed to restore page state from localStorage', e)
      }
    }

    // Check if state has expired
    if (state && state.savedAt && Date.now() - state.savedAt > maxAge) {
      clearPageState(pageName)
      return null
    }

    return state || null
  }

  /**
   * Clear saved state for a specific page
   * @param {string} pageName - Unique page identifier
   */
  const clearPageState = (pageName) => {
    delete pageStates.value[pageName]
    try {
      localStorage.removeItem(`page_state_${pageName}`)
    } catch (e) {
      console.warn('Failed to clear page state from localStorage', e)
    }
  }

  /**
   * Clear all saved page states
   */
  const clearAllPageStates = () => {
    const keys = Object.keys(pageStates.value)
    keys.forEach(key => {
      delete pageStates.value[key]
      try {
        localStorage.removeItem(`page_state_${key}`)
      } catch (e) {
        console.warn('Failed to clear page state from localStorage', e)
      }
    })
  }

  /**
   * Set the last visited page (used to detect back navigation)
   * @param {string} pageName - Current page name
   */
  const setLastVisitedPage = (pageName) => {
    lastVisitedPage.value = pageName
  }

  /**
   * Check if current navigation is back navigation
   * @param {string} pageName - Current page name
   * @returns {boolean} - True if this is a back navigation
   */
  const isBackNavigation = (pageName) => {
    return lastVisitedPage.value !== null && lastVisitedPage.value !== pageName
  }

  /**
   * Get pagination state object
   * @param {string} pageName - Unique page identifier
   * @returns {object|null} - State with at least {current, pageSize} or null
   */
  const getPaginationState = (pageName) => {
    const state = getPageState(pageName)
    if (state && state.current && state.pageSize) {
      return {
        current: state.current,
        pageSize: state.pageSize
      }
    }
    return null
  }

  /**
   * Get filter state object
   * @param {string} pageName - Unique page identifier
   * @returns {object|null} - State with filter fields only or null
   */
  const getFilterState = (pageName) => {
    const state = getPageState(pageName)
    if (state) {
      const { current, pageSize, savedAt, ...filters } = state
      return Object.keys(filters).length > 0 ? filters : null
    }
    return null
  }

  return {
    pageStates,
    lastVisitedPage,
    setPageState,
    getPageState,
    clearPageState,
    clearAllPageStates,
    setLastVisitedPage,
    isBackNavigation,
    getPaginationState,
    getFilterState
  }
})
