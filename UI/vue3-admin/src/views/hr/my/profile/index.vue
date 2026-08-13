<template>
  <ContentWrap v-loading="loading" class="profile-page">
    <el-empty v-if="!profile.employee && !loading" description="当前账号未关联人事档案，无法查看个人档案" />
    <template v-else>
      <ProfileDossierHeader :employee="employee" @navigate="handleNavigate" />
      <ProfileInfoSections :employee="employee" />
      <ProfileCertificateList :certificates="profile.certificates" @navigate="handleNavigate" />
      <ProfileResumeList :resumes="profile.resumes" @navigate="handleNavigate" />
    </template>
  </ContentWrap>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import { MyProfileApi, type MyProfile } from '@/api/hr/my/profile'
import type { Employee } from '@/api/hr/employee'
import ProfileDossierHeader from './components/ProfileDossierHeader.vue'
import ProfileInfoSections from './components/ProfileInfoSections.vue'
import ProfileCertificateList from './components/ProfileCertificateList.vue'
import ProfileResumeList from './components/ProfileResumeList.vue'

/** 职工个人档案（只读） */
defineOptions({ name: 'HrMyProfile' })

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const profile = ref<MyProfile>({})

const employee = computed<Employee>(() => profile.value.employee || ({} as Employee))

const handleNavigate = (name: string) => {
  router.push({ name })
}

const getProfile = async () => {
  loading.value = true
  try {
    profile.value = await MyProfileApi.getMyProfile()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void userStore.user?.id
  getProfile()
})
</script>

<style scoped lang="scss">
.profile-page {
  :deep(.el-card) {
    border: none;
    box-shadow: none;
  }
}
</style>
