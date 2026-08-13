/** 员工表单字段长度（与后端 HrEmployeeValidationConstants 对齐） */
export const HR_EMPLOYEE_FIELD_LIMIT = {
  name: 100,
  idCard: 100,
  detailAddress: 255,
  remark: 500,
  employeeNo: 30,
  employeeMobile: 11,
  text100: 100
} as const

export const HR_EMPLOYEE_EMPLOYEE_NO_PATTERN = /^[a-zA-Z0-9]*$/
export const HR_EMPLOYEE_MOBILE_PATTERN = /^1[3-9]\d{9}$/
