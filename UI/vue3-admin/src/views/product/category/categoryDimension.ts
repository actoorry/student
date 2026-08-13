import * as ProductCategoryApi from '@/api/product/category'

/** 与后端 ProductCategoryDimensionConstants 保持一致的前端维度根。 */
export const SALES_CATEGORY_ROOT_ID = 1
export const WAREHOUSE_CATEGORY_ROOT_ID = 2

export interface VirtualCategoryNode extends ProductCategoryApi.CategoryVO {
  children: VirtualCategoryNode[]
  virtual?: boolean
  immutable?: boolean
  disabled?: boolean
}

export const isVirtualCategoryRoot = (category?: Pick<ProductCategoryApi.CategoryVO, 'id'>) =>
  category?.id === SALES_CATEGORY_ROOT_ID || category?.id === WAREHOUSE_CATEGORY_ROOT_ID

/** 管理树始终由两个只读的代码拥有根开始；API 仅返回当前租户实体后代。 */
export const loadManagementCategoryTree = async (name?: string) => {
  const categories = await ProductCategoryApi.getCategoryList({ name })
  const roots: VirtualCategoryNode[] = [
    { id: SALES_CATEGORY_ROOT_ID, parentId: 0, name: '销售分类', picUrl: '', sort: 0, status: 0,
      virtual: true, immutable: true, children: [] },
    { id: WAREHOUSE_CATEGORY_ROOT_ID, parentId: 0, name: '仓储分类', picUrl: '', sort: 1, status: 0,
      virtual: true, immutable: true, children: [] }
  ]
  const nodeMap = new Map<number, VirtualCategoryNode>(
    roots.map((root) => [root.id!, root] as [number, VirtualCategoryNode])
  )
  categories.forEach((category) => nodeMap.set(category.id!, { ...category, children: [] }))
  categories.forEach((category) => {
    const parent = nodeMap.get(category.parentId!)
    if (parent) parent.children.push(nodeMap.get(category.id!)!)
  })
  return roots
}

/** 禁用当前节点及其所有实体后代，避免更新时移动到自身或子树。 */
export const disableCategoryAndDescendants = (nodes: VirtualCategoryNode[], categoryId?: number) => {
  const clone = (node: VirtualCategoryNode): VirtualCategoryNode => {
    const children = node.children.map(clone)
    const disabled = Boolean(node.immutable || node.id === categoryId || children.some((child) => child.disabled))
    return { ...node, children, disabled }
  }
  return nodes.map(clone)
}
