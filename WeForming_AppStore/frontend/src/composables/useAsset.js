import { ref, computed } from 'vue'
import { getAssets } from '../services/api'

// Normalise an asset's `platforms` (JSON dict, array, legacy CSV string, or empty)
// into a plain array of supported platform names.
export function platformList(platforms) {
  if (!platforms) return []
  if (Array.isArray(platforms)) return platforms.map(String)
  if (typeof platforms === 'object') return Object.keys(platforms).filter(k => platforms[k])
  if (typeof platforms === 'string') {
    const s = platforms.trim()
    if (!s) return []
    try {
      return platformList(JSON.parse(s))
    } catch {
      return s.split(',').map(x => x.trim()).filter(Boolean)  // legacy comma-separated
    }
  }
  return []
}

// Singleton state shared across all component instances
const assets = ref([])
const selectedAsset = ref(null)
let fetched = false

export function useAsset() {
  const shortName = computed(() => selectedAsset.value?.name || 'Select asset')

  async function fetchAssets(force = false) {
    if (fetched && !force) return
    fetched = true
    try {
      const response = await getAssets()
      assets.value = response.data

      // Restore previously selected asset by id (keep current selection if still present)
      const savedId = parseInt(localStorage.getItem('selectedAssetId'))
      const keepId = selectedAsset.value?.id
      selectedAsset.value =
        assets.value.find(a => a.id === keepId) ??
        assets.value.find(a => a.id === savedId) ??
        assets.value[0] ?? null
    } catch (e) {
      console.error('Failed to load assets', e)
      fetched = false
    }
  }

  // Force a reload — call after the asset editor changes anything.
  function refreshAssets() {
    return fetchAssets(true)
  }

  function setAsset(asset) {
    selectedAsset.value = asset
    if (asset) {
      localStorage.setItem('selectedAssetId', asset.id)
    } else {
      localStorage.removeItem('selectedAssetId')
    }
  }

  return { assets, selectedAsset, shortName, fetchAssets, refreshAssets, setAsset }
}
