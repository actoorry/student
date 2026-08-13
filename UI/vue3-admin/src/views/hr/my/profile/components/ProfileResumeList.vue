<template>

  <div class="profile-block">

    <div class="profile-block__head">

      <div class="profile-block__title-wrap">

        <span class="profile-block__accent profile-block__accent--resume"></span>

        <span class="profile-block__title">工作履历</span>

        <el-tag v-if="resumes?.length" size="small" round type="info">{{ resumes.length }}</el-tag>

      </div>

      <el-link type="primary" :underline="false" @click="emit('navigate', 'HrMyResume')">

        维护履历 →

      </el-link>

    </div>

    <el-empty v-if="!resumes?.length" description="暂无工作履历" :image-size="72" />

    <div v-else class="resume-timeline">

      <div v-for="(item, index) in resumes" :key="item.id" class="resume-item">

        <div class="resume-item__rail">

          <div class="resume-item__dot"></div>

          <div v-if="index < resumes!.length - 1" class="resume-item__line"></div>

        </div>

        <div class="resume-item__card">

          <div class="resume-item__header">

            <span class="resume-item__dept">{{ item.deptName || '未填写部门' }}</span>

            <span class="resume-item__time">{{ formatDate(item.createTime) }}</span>

          </div>

          <div v-if="workRows(item.resumeContent).length" class="resume-item__works">

            <div v-for="(row, ri) in workRows(item.resumeContent)" :key="ri" class="resume-item__work">

              <Icon icon="ep:briefcase" :size="14" class="resume-item__work-icon" />

              <span>{{ formatWorkRowLabel(row) }}</span>

            </div>

          </div>

          <div v-if="item.awardsPunishments" class="resume-item__extra">

            <span class="resume-item__extra-label">奖惩</span>

            <span>{{ item.awardsPunishments }}</span>

          </div>

          <div v-if="item.annualReview" class="resume-item__extra">

            <span class="resume-item__extra-label">考核</span>

            <span>{{ item.annualReview }}</span>

          </div>

        </div>

      </div>

    </div>

  </div>

</template>



<script lang="ts" setup>

import type { Resume } from '@/api/hr/resume'

import {

  formatDate,

  parseResumeWorkRows,

  formatWorkRowLabel

} from '@/views/hr/my/utils/profileUtils'



defineOptions({ name: 'ProfileResumeList' })



defineProps<{

  resumes?: Resume[]

}>()



const emit = defineEmits<{

  navigate: [name: string]

}>()



const workRows = (content?: string) => parseResumeWorkRows(content)

</script>



<style lang="scss" scoped>

.profile-block {

  padding: 20px 22px;

  background: var(--el-bg-color);

  border: 1px solid var(--el-border-color-lighter);

  border-radius: 12px;

  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

}



.profile-block__head {

  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-bottom: 16px;

}



.profile-block__title-wrap {

  display: flex;

  align-items: center;

  gap: 10px;

}



.profile-block__accent {

  width: 4px;

  height: 18px;

  border-radius: 2px;



  &--resume {

    background: linear-gradient(180deg, #67c23a, #95d475);

  }

}



.profile-block__title {

  font-size: 16px;

  font-weight: 600;

}



.resume-timeline {

  display: flex;

  flex-direction: column;

  gap: 0;

}



.resume-item {

  display: flex;

  gap: 16px;

}



.resume-item__rail {

  display: flex;

  flex-direction: column;

  align-items: center;

  width: 16px;

  flex-shrink: 0;

  padding-top: 6px;

}



.resume-item__dot {

  width: 12px;

  height: 12px;

  border-radius: 50%;

  background: #67c23a;

  box-shadow: 0 0 0 4px rgba(103, 194, 58, 0.18);

  flex-shrink: 0;

}



.resume-item__line {

  flex: 1;

  width: 2px;

  min-height: 24px;

  margin: 6px 0;

  background: linear-gradient(180deg, rgba(103, 194, 58, 0.4), var(--el-border-color-lighter));

}



.resume-item__card {

  flex: 1;

  margin-bottom: 18px;

  padding: 16px 18px;

  border: 1px solid var(--el-border-color-lighter);

  border-radius: 10px;

  background: var(--el-fill-color-blank);

  transition: box-shadow 0.2s ease;



  &:hover {

    box-shadow: 0 6px 16px rgba(103, 194, 58, 0.1);

  }

}



.resume-item__header {

  display: flex;

  justify-content: space-between;

  align-items: center;

  gap: 12px;

  margin-bottom: 12px;

}



.resume-item__dept {

  font-size: 15px;

  font-weight: 600;

  color: var(--el-text-color-primary);

}



.resume-item__time {

  font-size: 12px;

  color: var(--el-text-color-placeholder);

  flex-shrink: 0;

}



.resume-item__works {

  display: flex;

  flex-direction: column;

  gap: 8px;

  margin-bottom: 10px;

}



.resume-item__work {

  display: flex;

  align-items: flex-start;

  gap: 8px;

  font-size: 13px;

  color: var(--el-text-color-regular);

  line-height: 1.5;

}



.resume-item__work-icon {

  margin-top: 3px;

  color: #67c23a;

  flex-shrink: 0;

}



.resume-item__extra {

  display: flex;

  gap: 10px;

  font-size: 13px;

  color: var(--el-text-color-regular);

  line-height: 1.5;

  padding-top: 8px;

  margin-top: 8px;

  border-top: 1px dashed var(--el-border-color-lighter);



  & + & {

    margin-top: 6px;

    padding-top: 0;

    border-top: none;

  }

}



.resume-item__extra-label {

  flex-shrink: 0;

  font-size: 12px;

  font-weight: 600;

  color: var(--el-text-color-secondary);

}

</style>


