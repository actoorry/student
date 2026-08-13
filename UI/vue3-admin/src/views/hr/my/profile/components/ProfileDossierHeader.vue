<template>
  <div class="profile-dossier">
    <div class="profile-dossier__bg"></div>
    <div class="profile-dossier__body">
      <div class="profile-dossier__main">
        <div class="profile-dossier__avatar-wrap">
          <ElAvatar :size="88" :src="avatar" class="profile-dossier__avatar" />
        </div>
        <div class="profile-dossier__info">
          <div class="profile-dossier__title-row">
            <h1 class="profile-dossier__name">{{ employee.name || '-' }}</h1>
            <span class="profile-dossier__badge">人事档案</span>
          </div>
          <div class="profile-dossier__meta">
            <span v-if="employee.deptName" class="profile-dossier__meta-item">
              <Icon icon="ep:office-building" :size="15" />
              {{ employee.deptName }}
            </span>
            <span v-if="employee.employeeNo" class="profile-dossier__meta-item">
              <Icon icon="ep:postcard" :size="15" />
              工号 {{ employee.employeeNo }}
            </span>
            <span v-if="employee.employeeMobile" class="profile-dossier__meta-item">
              <Icon icon="ep:phone" :size="15" />
              {{ employee.employeeMobile }}
            </span>
          </div>
          <div class="profile-dossier__tags">
            <DictTag :type="DICT_TYPE.SYSTEM_USER_SEX" :value="employee.sex" />
            <DictTag :type="DICT_TYPE.HR_PERSONNEL_CATEGORY" :value="employee.personnelCategory" />
            <DictTag :type="DICT_TYPE.HR_EMPLOYMENT_STATUS" :value="employee.employmentStatus" />
            <DictTag :type="DICT_TYPE.HR_PROFESSIONAL_TITLE" :value="employee.professionalTitle" />
          </div>
        </div>
      </div>
      <div class="profile-dossier__actions">
        <button type="button" class="profile-dossier__action" @click="emit('navigate', 'HrMyContract')">
          <Icon icon="ep:document" :size="18" />
          <span>维护合同</span>
        </button>
        <button type="button" class="profile-dossier__action" @click="emit('navigate', 'HrMyCertificate')">
          <Icon icon="ep:medal" :size="18" />
          <span>维护证书</span>
        </button>
        <button type="button" class="profile-dossier__action" @click="emit('navigate', 'HrMyResume')">
          <Icon icon="ep:calendar" :size="18" />
          <span>维护履历</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import avatarImg from '@/assets/imgs/avatar.gif'
import { DICT_TYPE } from '@/utils/dict'
import { useUserStore } from '@/store/modules/user'
import type { Employee } from '@/api/hr/employee'

defineOptions({ name: 'ProfileDossierHeader' })

const props = defineProps<{
  employee: Employee
}>()

const emit = defineEmits<{
  navigate: [name: string]
}>()

const userStore = useUserStore()
const avatar = computed(
  () => props.employee?.avatar || userStore.user?.avatar || avatarImg
)
</script>

<style lang="scss" scoped>
.profile-dossier {
  position: relative;
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 4px 20px rgba(15, 76, 129, 0.08);
}

.profile-dossier__bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    125deg,
    rgba(64, 158, 255, 0.14) 0%,
    rgba(64, 158, 255, 0.04) 42%,
    var(--el-bg-color) 72%
  );

  &::after {
    content: '';
    position: absolute;
    top: -40px;
    right: -20px;
    width: 200px;
    height: 200px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(64, 158, 255, 0.12) 0%, transparent 70%);
  }
}

.profile-dossier__body {
  position: relative;
  padding: 28px 28px 22px;
}

.profile-dossier__main {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.profile-dossier__avatar-wrap {
  padding: 4px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #79bbff);
  flex-shrink: 0;
}

.profile-dossier__avatar {
  border: 3px solid var(--el-bg-color);
}

.profile-dossier__info {
  flex: 1;
  min-width: 0;
}

.profile-dossier__title-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 10px;
}

.profile-dossier__name {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: var(--el-text-color-primary);
  line-height: 1.2;
}

.profile-dossier__badge,
.profile-dossier__tags :deep(.el-tag) {
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 500;
  color: #409eff !important;
  background: rgba(64, 158, 255, 0.12) !important;
  border: 1px solid rgba(64, 158, 255, 0.25) !important;
  border-radius: 999px;
}

.profile-dossier__tags :deep(.el-tag) {
  height: auto;
}

.profile-dossier__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
}

.profile-dossier__meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.profile-dossier__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.profile-dossier__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.profile-dossier__action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    color: #409eff;
    border-color: rgba(64, 158, 255, 0.45);
    background: rgba(64, 158, 255, 0.06);
    transform: translateY(-1px);
  }
}
</style>
