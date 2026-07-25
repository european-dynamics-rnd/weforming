import { ref } from 'vue'

const showPlatformType = ref(localStorage.getItem('showPlatformType') === 'true')

export function useSettings() {
  function setShowPlatformType(value) {
    showPlatformType.value = value
    localStorage.setItem('showPlatformType', value)
  }

  return { showPlatformType, setShowPlatformType }
}
