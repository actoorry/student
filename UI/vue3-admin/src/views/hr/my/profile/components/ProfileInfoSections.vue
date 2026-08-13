<template>
  <div class="profile-sections">
    <div v-for="section in sections" :key="section.title" class="profile-section">
      <div class="profile-section__head">
        <span class="profile-section__accent"></span>
        <span class="profile-section__title">{{ section.title }}</span>
      </div>
      <div class="profile-section__grid">
        <div v-for="field in section.fields" :key="field.label" class="profile-field">
          <div class="profile-field__icon" :style="{ background: section.tint }">
            <Icon :icon="field.icon" :size="16" />
          </div>
          <div class="profile-field__content">
            <div class="profile-field__label">{{ field.label }}</div>
            <div class="profile-field__value">
              <DictTag v-if="field.dictType" :type="field.dictType" :value="field.dictValue" />
              <span v-else>{{ field.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import type { Employee } from '@/api/hr/employee'
import { formatDate } from '@/views/hr/my/utils/profileUtils'

defineOptions({ name: 'ProfileInfoSections' })

const props = defineProps<{
  employee: Employee
}>()

interface FieldItem {
  icon: string
  label: string
  value?: string
  dictType?: string
  dictValue?: string | number
}

interface SectionItem {
  title: string
  tint: string
  fields: FieldItem[]
}

const sections = computed<SectionItem[]>(() => {
  const e = props.employee
  return [
    {
      title: '身份信息',
      tint: 'rgba(64, 158, 255, 0.12)',
      fields: [
        { icon: 'ep:user', label: '姓名', value: e.name || '-' },
        { icon: 'ep:document', label: '身份证号', value: e.idCard || '-' },
        {
          icon: 'ep:user-filled',
          label: '性别',
          dictType: DICT_TYPE.SYSTEM_USER_SEX,
          dictValue: e.sex
        },
        { icon: 'ep:calendar', label: '出生日期', value: formatDate(e.birthday) },
        { icon: 'ep:phone', label: '员工手机号', value: e.employeeMobile || '-' }
      ]
    },
    {
      title: '岗位信息',
      tint: 'rgba(103, 194, 58, 0.12)',
      fields: [
        { icon: 'ep:office-building', label: '所在科室', value: e.deptName || '-' },
        { icon: 'ep:postcard', label: '工号', value: e.employeeNo || '-' },
        { icon: 'ep:position', label: '岗位', value: e.postName || '-' },
        { icon: 'ep:suitcase', label: '职务', value: e.duty || '-' },
        {
          icon: 'ep:medal',
          label: '职称',
          dictType: DICT_TYPE.HR_PROFESSIONAL_TITLE,
          dictValue: e.professionalTitle
        },
        {
          icon: 'ep:collection',
          label: '人员类别',
          dictType: DICT_TYPE.HR_PERSONNEL_CATEGORY,
          dictValue: e.personnelCategory
        },
        {
          icon: 'ep:flag',
          label: '在职状态',
          dictType: DICT_TYPE.HR_EMPLOYMENT_STATUS,
          dictValue: e.employmentStatus
        },
        {
          icon: 'ep:reading',
          label: '最高学历',
          dictType: DICT_TYPE.HR_EDUCATION,
          dictValue: e.highestEducation
        }
      ]
    },
    {
      title: '时间信息',
      tint: 'rgba(230, 162, 60, 0.12)',
      fields: [
        { icon: 'ep:timer', label: '参加工作时间', value: formatDate(e.careerStartDate) },
        { icon: 'ep:home-filled', label: '进入单位时间', value: formatDate(e.hireDate) }
      ]
    }
  ]
})
</script>

<style lang="scss" scoped>
.profile-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.profile-section {
  padding: 20px 22px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.25s ease;

  &:hover {
    box-shadow: 0 6px 20px rgba(15, 76, 129, 0.08);
  }
}

.profile-section__head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.profile-section__accent {
  width: 4px;
  height: 18px;
  border-radius: 2px;
  background: linear-gradient(180deg, #409eff, #79bbff);
}

.profile-section__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.profile-section__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.profile-field {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 14px;
  background: var(--el-fill-color-lighter);
  border-radius: 10px;
  min-height: 56px;
}

.profile-field__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  color: #409eff;
  flex-shrink: 0;
}

.profile-field__content {
  flex: 1;
  min-width: 0;
}

.profile-field__label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.profile-field__value {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  word-break: break-all;

  :deep(.el-tag) {
    color: var(--el-text-color-primary) !important;
    background: transparent !important;
    border: none !important;
    padding: 0;
    height: auto;
    font-size: inherit;
    font-weight: inherit;
  }
}
</style>
