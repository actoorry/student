# Product Category Top-Level Parent Option Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在管理端产品分类表单的“上级分类”选择器中补上“顶级分类”选项，并确保选择该项时提交 `parentId = 0`，同时将现有分类显示在其后。

**Architecture:** 仅修改前端 `CategoryForm.vue`，保持后端接口与其它分类使用场景不变。分类列表继续来自现有接口，前端在树形数据最前面插入一个虚拟根选项 `{ id: 0, name: '顶级分类' }`，并兼容新增、编辑回显与手动刷新。

**Tech Stack:** Vue 3, TypeScript, Element Plus Cascader, Vite 5, vue-tsc

## Global Constraints

- 仅改动 `UI/vue3-admin` 范围内与本表单直接相关的代码，不修改后端接口。
- 保持现有 `el-cascader`、`defaultProps`、`handleTree` 的使用模式，不引入新依赖。
- “顶级分类”必须是显式可选项，提交值固定为 `0`。
- 原有分类项必须保留，并整体排在“顶级分类”之后显示。
- 遵循项目现有 Vue 3 `<script setup lang="ts">` 与 Element Plus 写法。
- 默认不运行 Maven 编译；前端验证以 `vue-tsc --noEmit` 为主。

---

### Task 1: Patch product category parent selector

**Files:**
- Modify: `UI/vue3-admin/src/views/product/category/CategoryForm.vue:67-152`
- Verify: `UI/vue3-admin/package.json:7-26`

**Interfaces:**
- Consumes: `ProductCategoryApi.getCategoryList(params: any) => Promise<any[]>`, `ProductCategoryApi.getCategory(id: number) => Promise<CategoryVO>`, `handleTree(data: any[], id?: string, parentId?: string, children?: string) => any[]`
- Produces: `loadCategoryList(): Promise<void>` that sets `categoryList.value` to a cascader option array whose first item is `{ id: 0, name: '顶级分类' }`; `resetForm(): void` that resets `formData.value.parentId` to `0`

- [ ] **Step 1: Write the failing type/behavior expectation in the form file notes**

Add the following implementation target as an inline development note immediately above `loadCategoryList` while working, then remove it before finishing:

```ts
// Target behavior during implementation:
// 1. categoryList.value[0] must be the explicit top-level option { id: 0, name: '顶级分类' }
// 2. Existing category tree nodes must remain available after that option
// 3. resetForm() must default parentId to 0 so create mode can submit a top-level category
```

Expected failure before the real code change: current code still calls `getCategoryList({ parentId: 1 })`, does not prepend a top-level option, and resets `parentId` to `null`.

- [ ] **Step 2: Run a targeted inspection to verify the current code still lacks the top-level option**

Run:

```bash
grep -n "getCategoryList({ parentId: 1 })\|parentId: null" UI/vue3-admin/src/views/product/category/CategoryForm.vue
```

Expected: output includes the hard-coded `parentId: 1` fetch and `parentId: null` reset/defaults, confirming the current bug source.

- [ ] **Step 3: Implement the minimal code change in `CategoryForm.vue`**

Apply this exact shape of change, preserving surrounding style:

```ts
const TOP_LEVEL_CATEGORY = {
  id: 0,
  name: '顶级分类',
  children: []
}

const formData = ref({
  id: undefined,
  parentId: 0 as number,
  name: '',
  picUrl: '',
  sort: 0,
  status: CommonStatusEnum.ENABLE
})

const loadCategoryList = async () => {
  const data = await ProductCategoryApi.getCategoryList({})
  const tree = handleTree(data, 'id', 'parentId')
  categoryList.value = [TOP_LEVEL_CATEGORY, ...tree]
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    parentId: 0,
    name: '',
    picUrl: '',
    sort: 0,
    status: CommonStatusEnum.ENABLE
  }
  formRef.value?.resetFields()
}
```

And keep this existing edit behavior intact:

```ts
if (id) {
  formLoading.value = true
  try {
    formData.value = await ProductCategoryApi.getCategory(id)
  } finally {
    formLoading.value = false
  }
}
```

This preserves edit-time backend values, including `parentId = 0` for top-level categories.

- [ ] **Step 4: Remove the temporary implementation note and verify the final file content**

Run:

```bash
grep -n "TOP_LEVEL_CATEGORY\|getCategoryList({})\|parentId: 0\|顶级分类" UI/vue3-admin/src/views/product/category/CategoryForm.vue
```

Expected: output shows the new top-level constant, the unfiltered category list call, and `parentId: 0` defaults; the temporary note from Step 1 is gone.

- [ ] **Step 5: Run the TypeScript check for the admin UI**

Run:

```bash
cd UI/vue3-admin && pnpm ts:check
```

Expected: `vue-tsc --noEmit` exits successfully with no new type errors caused by the form change.

- [ ] **Step 6: Commit the focused front-end fix**

```bash
git add UI/vue3-admin/src/views/product/category/CategoryForm.vue docs/superpowers/plans/2026-06-26-product-category-top-level-parent-option.md
git commit -m "fix: restore top-level product category parent option"
```

Expected: one commit containing the parent selector fix and this plan document.
