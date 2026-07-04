/**
 * 通用 API 客户端类型
 *
 * 用于 composable 和组件解耦，不依赖具体的 auth 实现。
 */
export interface ApiClient {
  <T = any>(path: string, options?: RequestInit): Promise<T>
}
