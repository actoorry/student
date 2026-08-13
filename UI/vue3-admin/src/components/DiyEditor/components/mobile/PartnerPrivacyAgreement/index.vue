<template>
  <div class="privacy-entry-card">
    <Icon :icon="iconName" :size="20" color="#9A655D" />
    <span class="privacy-entry-title">{{ normalized.entryTitle }}</span>
    <Icon icon="ep:arrow-right" :size="18" color="#B9A29D" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { normalizePartnerPrivacyAgreementProperty, type PartnerPrivacyAgreementProperty } from './config'

defineOptions({ name: 'PartnerPrivacyAgreement' })
const props = defineProps<{ property: PartnerPrivacyAgreementProperty }>()
const normalized = computed(() => normalizePartnerPrivacyAgreementProperty(props.property))
const iconName = computed(() => ({ LOCK: 'ep:lock', SHIELD: 'ep:shield', DOCUMENT: 'ep:document' })[normalized.value.entryIcon])
</script>

<style scoped lang="scss">
.privacy-entry-card { display: flex; height: 44px; align-items: center; gap: 12px; padding: 0 16px; border-radius: 12px; background: #fff7f4; color: #241818; }
.privacy-entry-title { flex: 1; font-size: 15px; font-weight: 600; }
</style>
