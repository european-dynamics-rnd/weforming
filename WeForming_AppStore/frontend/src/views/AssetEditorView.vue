<template>
  <v-container fluid class="py-4">
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5 font-weight-bold">My Houses</h1>
      <v-spacer />
      <v-btn color="primary" prepend-icon="mdi-home-plus" :disabled="editing" @click="addHouse">
        Add House
      </v-btn>
    </div>

    <v-alert v-if="error" type="error" variant="tonal" density="compact" class="mb-3">{{ error }}</v-alert>

    <v-row>
      <!-- Map -->
      <v-col cols="12" md="7">
        <v-card variant="outlined">
          <div ref="mapEl" class="asset-map"></div>
        </v-card>
        <p class="text-caption text-medium-emphasis mt-2 mb-0">
          <v-icon size="small">mdi-map-marker</v-icon>
          {{ editing ? 'Click the map to set this house’s location.' : 'Markers show your houses — click one to edit it.' }}
        </p>
      </v-col>

      <!-- Editor / list -->
      <v-col cols="12" md="5">
        <!-- Editor form -->
        <v-card v-if="editing" class="pa-4" variant="outlined">
          <div class="text-h6 mb-3">{{ form.id ? 'Edit house' : 'Add house' }}</div>

          <v-text-field
            v-model="form.name" label="Name" variant="outlined" density="comfortable"
            prepend-inner-icon="mdi-home" :error-messages="nameError" class="mb-1"
          />
          <v-text-field
            v-model="form.address" label="Address" variant="outlined" density="comfortable"
            prepend-inner-icon="mdi-map-marker-outline" class="mb-1"
          />
          <v-row dense>
            <v-col cols="6">
              <v-text-field v-model="form.latitude" label="Latitude" type="number" step="any"
                variant="outlined" density="comfortable" />
            </v-col>
            <v-col cols="6">
              <v-text-field v-model="form.longitude" label="Longitude" type="number" step="any"
                variant="outlined" density="comfortable" />
            </v-col>
          </v-row>

          <div class="text-caption text-medium-emphasis mb-1">Supported platforms</div>
          <v-chip-group v-model="form.platforms" multiple column class="mb-2">
            <v-chip v-for="pt in platformTypes" :key="pt.id" :value="pt.name" filter variant="outlined">
              {{ pt.name }}
            </v-chip>
          </v-chip-group>

          <v-file-input
            v-model="imageFile" label="Building photo" accept="image/*"
            variant="outlined" density="comfortable" prepend-icon="" prepend-inner-icon="mdi-camera"
            show-size class="mb-1"
          />
          <v-img v-if="form.imagePreview" :src="form.imagePreview" max-height="170" cover class="rounded mb-2" />

          <div class="d-flex align-center mt-2">
            <v-btn color="primary" :loading="saving" @click="save">Save</v-btn>
            <v-btn variant="text" class="ml-2" @click="cancel">Cancel</v-btn>
            <v-spacer />
            <v-btn v-if="form.id" color="error" variant="text" @click="remove(form)">Delete</v-btn>
          </div>
        </v-card>

        <!-- House list -->
        <div v-else>
          <v-progress-circular v-if="loading" indeterminate color="primary" class="d-block mx-auto mt-6" />
          <v-alert v-else-if="houses.length === 0" type="info" variant="tonal">
            No houses yet. Click <strong>Add House</strong> to create one.
          </v-alert>
          <v-card v-for="h in houses" :key="h.id" class="mb-3" variant="outlined">
            <div class="d-flex">
              <v-img
                v-if="assetImageUrl(h)" :src="assetImageUrl(h)" width="96" height="96" cover
                class="flex-grow-0 bg-grey-lighten-3"
              />
              <div v-else class="house-noimg flex-grow-0"><v-icon color="grey">mdi-home-city</v-icon></div>
              <div class="pa-3 flex-grow-1">
                <div class="font-weight-bold">{{ h.name }}</div>
                <div class="text-caption text-medium-emphasis">{{ h.address || '—' }}</div>
                <div class="text-caption text-medium-emphasis" v-if="h.latitude != null && h.longitude != null">
                  {{ Number(h.latitude).toFixed(4) }}, {{ Number(h.longitude).toFixed(4) }}
                </div>
                <div class="mt-1">
                  <v-chip v-for="p in h.platforms" :key="p" size="x-small" color="secondary" variant="tonal" class="mr-1">
                    {{ p }}
                  </v-chip>
                  <span v-if="!h.platforms || h.platforms.length === 0" class="text-caption text-medium-emphasis">no platforms</span>
                </div>
              </div>
              <div class="pa-2 d-flex flex-column">
                <v-btn icon="mdi-pencil" size="small" variant="text" @click="editHouse(h)" />
                <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="remove(h)" />
              </div>
            </div>
          </v-card>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref, reactive, watch, onMounted, onUnmounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import {
  getAssets, createAsset, updateAsset, deleteAsset, getPlatformTypes, assetImageUrl,
} from '../services/api'
import { useAsset } from '../composables/useAsset'

const DEFAULT_CENTER = [45.05, 14.57]  // Krk, Croatia
const DEFAULT_ZOOM = 11

function escapeHtml(s) {
  return String(s || '').replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]))
}
function numOrEmpty(v) {
  if (v === '' || v === null || v === undefined) return ''
  const n = Number(v)
  return Number.isFinite(n) ? String(n) : ''
}

export default {
  name: 'AssetEditorView',
  setup() {
    const { refreshAssets } = useAsset()

    const mapEl = ref(null)
    const houses = ref([])
    const platformTypes = ref([])
    const loading = ref(true)
    const saving = ref(false)
    const error = ref('')
    const editing = ref(false)
    const nameError = ref('')
    const imageFile = ref(null)

    const blank = () => ({ id: null, name: '', address: '', latitude: '', longitude: '', platforms: [], imageFile: null, imagePreview: '' })
    const form = reactive(blank())

    let map = null
    let houseLayer = null
    let editMarker = null

    // --- map ---
    function renderMarkers() {
      if (!map || !houseLayer) return
      houseLayer.clearLayers()
      const pts = []
      for (const h of houses.value) {
        if (h.latitude == null || h.longitude == null) continue
        const m = L.circleMarker([h.latitude, h.longitude], {
          radius: 9, color: '#2f9e6a', weight: 2, fillColor: '#38b272', fillOpacity: 0.85,
        })
        m.bindPopup(`<b>${escapeHtml(h.name)}</b><br>${escapeHtml(h.address)}`)
        m.on('click', () => editHouse(h))
        m.addTo(houseLayer)
        pts.push([h.latitude, h.longitude])
      }
      if (pts.length && !editing.value) map.fitBounds(pts, { padding: [30, 30], maxZoom: 14 })
    }

    function setEditMarker(lat, lon) {
      if (!map) return
      if (editMarker) { editMarker.remove(); editMarker = null }
      const la = Number(lat), lo = Number(lon)
      if (Number.isFinite(la) && Number.isFinite(lo)) {
        editMarker = L.circleMarker([la, lo], {
          radius: 11, color: '#d98a1a', weight: 3, fillColor: '#f5b942', fillOpacity: 0.9,
        }).addTo(map)
      }
    }

    function onMapClick(e) {
      if (!editing.value) return
      form.latitude = Number(e.latlng.lat.toFixed(6))
      form.longitude = Number(e.latlng.lng.toFixed(6))
      setEditMarker(form.latitude, form.longitude)
    }

    // --- data ---
    async function load() {
      loading.value = true
      error.value = ''
      try {
        const [a, p] = await Promise.all([getAssets(), getPlatformTypes()])
        houses.value = a.data
        platformTypes.value = p.data
        renderMarkers()
      } catch (e) {
        error.value = e.response?.data?.detail || 'Failed to load houses.'
      } finally {
        loading.value = false
      }
    }

    // --- editing ---
    function addHouse() {
      Object.assign(form, blank())
      imageFile.value = null
      nameError.value = ''
      editing.value = true
      if (editMarker) { editMarker.remove(); editMarker = null }
    }

    function editHouse(h) {
      Object.assign(form, {
        id: h.id, name: h.name, address: h.address || '',
        latitude: h.latitude ?? '', longitude: h.longitude ?? '',
        platforms: [...(h.platforms || [])],
        imageFile: null, imagePreview: assetImageUrl(h),
      })
      imageFile.value = null
      nameError.value = ''
      editing.value = true
      setEditMarker(h.latitude, h.longitude)
      if (map && h.latitude != null && h.longitude != null) map.setView([h.latitude, h.longitude], Math.max(map.getZoom(), 13))
    }

    function cancel() {
      editing.value = false
      if (editMarker) { editMarker.remove(); editMarker = null }
      renderMarkers()
    }

    watch(imageFile, (val) => {
      const f = Array.isArray(val) ? val[0] : val
      form.imageFile = f || null
      if (f) form.imagePreview = URL.createObjectURL(f)
    })

    async function save() {
      nameError.value = ''
      if (!form.name || !form.name.trim()) { nameError.value = 'Name is required'; return }
      saving.value = true
      error.value = ''
      try {
        const fd = new FormData()
        fd.append('name', form.name.trim())
        fd.append('address', form.address || '')
        fd.append('latitude', numOrEmpty(form.latitude))
        fd.append('longitude', numOrEmpty(form.longitude))
        fd.append('platforms', JSON.stringify(form.platforms || []))
        if (form.imageFile) fd.append('image', form.imageFile)

        if (form.id) await updateAsset(form.id, fd)
        else await createAsset(fd)

        editing.value = false
        if (editMarker) { editMarker.remove(); editMarker = null }
        await load()
        await refreshAssets()   // keep the navbar asset selector in sync
      } catch (e) {
        error.value = e.response?.data?.detail || 'Failed to save the house.'
      } finally {
        saving.value = false
      }
    }

    async function remove(h) {
      if (!h.id) { cancel(); return }
      if (!window.confirm(`Delete "${h.name}"? This cannot be undone.`)) return
      try {
        await deleteAsset(h.id)
        if (form.id === h.id) cancel()
        await load()
        await refreshAssets()
      } catch (e) {
        error.value = e.response?.data?.detail || 'Failed to delete the house.'
      }
    }

    onMounted(async () => {
      map = L.map(mapEl.value).setView(DEFAULT_CENTER, DEFAULT_ZOOM)
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19, attribution: '© OpenStreetMap contributors',
      }).addTo(map)
      houseLayer = L.layerGroup().addTo(map)
      map.on('click', onMapClick)
      setTimeout(() => map && map.invalidateSize(), 200)
      await load()
    })

    onUnmounted(() => {
      if (map) { map.remove(); map = null }
    })

    return {
      mapEl, houses, platformTypes, loading, saving, error, editing, nameError, form, imageFile,
      addHouse, editHouse, cancel, save, remove, assetImageUrl,
    }
  },
}
</script>

<style scoped>
.asset-map {
  height: 460px;
  width: 100%;
  border-radius: 6px;
}
.house-noimg {
  width: 96px; height: 96px;
  display: flex; align-items: center; justify-content: center;
  background: #eceff1;
}
/* Leaflet controls should sit under Vuetify overlays/menus */
.asset-map :deep(.leaflet-pane),
.asset-map :deep(.leaflet-control) { z-index: 1; }
</style>
