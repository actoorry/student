export const DIY_PAGE_GROUP_NAME = '自定义页面'
export const DIY_PAGE_ROUTE = '/pages/index/page'
export const DIY_PAGE_BATCH_SIZE = 200

export interface StandaloneDiyPage {
  id?: number
  name: string
}

export interface StandaloneDiyPageResult<T extends StandaloneDiyPage = StandaloneDiyPage> {
  list: T[]
  total: number
}

export interface StandaloneDiyPageLink {
  name: string
  path: string
  exactQuery: true
}

type FetchStandaloneDiyPage<T extends StandaloneDiyPage> = (params: {
  pageNo: number
  pageSize: number
}) => Promise<StandaloneDiyPageResult<T>>

/** 使用后端允许的最大分页逐批读取，避免遗漏超过一页的独立装修页面。 */
export const loadAllStandaloneDiyPages = async <T extends StandaloneDiyPage>(
  fetchPage: FetchStandaloneDiyPage<T>
): Promise<T[]> => {
  const pages: T[] = []
  let pageNo = 1

  while (true) {
    const result = await fetchPage({ pageNo, pageSize: DIY_PAGE_BATCH_SIZE })
    const batch = Array.isArray(result?.list) ? result.list : []
    pages.push(...batch)

    const total = Number(result?.total)
    if (!Number.isFinite(total) || pages.length >= total || batch.length === 0) {
      return pages
    }
    pageNo += 1
  }
}

/** 只让服务端返回的有限正整数页面 ID 进入已编译的通用页面路由。 */
export const buildStandaloneDiyPageLinks = (
  pages: StandaloneDiyPage[]
): StandaloneDiyPageLink[] =>
  pages.flatMap((page) => {
    const id = Number(page.id)
    if (!Number.isSafeInteger(id) || id <= 0) {
      return []
    }
    return [
      {
        name: page.name,
        path: `${DIY_PAGE_ROUTE}?id=${id}`,
        exactQuery: true
      }
    ]
  })
