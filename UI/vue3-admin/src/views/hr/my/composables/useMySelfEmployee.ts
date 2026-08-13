import { MySelfApi } from '@/api/hr/my/self'
import type { Employee } from '@/api/hr/employee'
import { useUserStore } from '@/store/modules/user'

/** 职工自助表单：加载本人档案只读展示（姓名/工号/科室） */
export function useMySelfEmployee() {
  const userStore = useUserStore()
  const selfEmployee = ref<Employee | null>(null)

  const employeeLabel = computed(() => {
    const e = selfEmployee.value
    if (!e) return ''
    const extra = e.employeeNo || e.employeeMobile
    return extra ? `${e.name}（${extra}）` : e.name || ''
  })

  const loadSelfEmployee = async (): Promise<Employee | null> => {
    if (!userStore.user?.id) {
      selfEmployee.value = null
      return null
    }
    try {
      selfEmployee.value = await MySelfApi.getMyEmployee()
      return selfEmployee.value
    } catch {
      selfEmployee.value = null
      return null
    }
  }

  const resetSelfEmployee = () => {
    selfEmployee.value = null
  }

  return { selfEmployee, employeeLabel, loadSelfEmployee, resetSelfEmployee }
}
