// 全局应用配置
// 项目名称统一在此维护；部署时如需覆盖，设置环境变量 VITE_APP_NAME 即可，未配置时使用默认值
export const APP_NAME = import.meta.env.VITE_APP_NAME || '晓居智能'

// 管理后台全称（浏览器标题等使用）
export const APP_TITLE = `${APP_NAME}管理系统`
