<template>
  <v-container class="py-6">
    <h1 class="text-h5 font-weight-bold mb-6">My Installed Apps</h1>

    <v-alert v-if="error" type="error" variant="tonal" class="mb-4">{{ error }}</v-alert>

    <v-progress-circular v-if="loading" indeterminate color="primary" class="d-flex mx-auto mt-8" />

    <v-alert v-else-if="!loading && userApps.length === 0" type="info" variant="tonal">
      You have not installed any apps yet.
    </v-alert>

    <v-row v-else>
      <v-col v-for="entry in userApps" :key="entry.id" cols="12" sm="6" md="4">
        <v-card elevation="2">
          <v-img
            v-if="iconUrl(entry.app)"
            :src="iconUrl(entry.app)"
            height="120"
            contain
            class="bg-grey-lighten-4"
          />
          <v-card-title class="text-body-1 font-weight-bold">{{ entry.app.name }}</v-card-title>
          <v-card-text>
            <p class="text-body-2 text-medium-emphasis mb-2">{{ entry.app.description }}</p>
            <v-chip
              size="small"
              class="mr-2 mb-1"
              :color="statusMeta(entry.status).color"
              :prepend-icon="statusMeta(entry.status).icon"
            >
              {{ statusMeta(entry.status).label }}
            </v-chip>
            <v-chip size="small" prepend-icon="mdi-home" class="mr-2 mb-1">
              {{ entry.asset.name }}
            </v-chip>
            <v-chip size="small" prepend-icon="mdi-tag-outline" class="mb-1">
              v{{ entry.installed_version }}
            </v-chip>
            <div v-if="parsedConfig(entry)" class="mt-3">
              <p class="text-caption text-medium-emphasis mb-1">Configuration</p>
              <v-table density="compact">
                <tbody>
                  <tr v-for="(val, key) in parsedConfig(entry)" :key="key">
                    <td class="text-caption text-medium-emphasis">{{ key }}</td>
                    <td class="text-caption">{{ val }}</td>
                  </tr>
                </tbody>
              </v-table>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref, onMounted } from 'vue'
import { getUserApps, iconUrl } from '../services/api'

export default {
  name: 'MyAppsView',
  setup() {
    const userApps = ref([])
    const loading = ref(true)
    const error = ref('')

    function statusMeta(status) {
      if (status === 'finished') return { color: 'success', icon: 'mdi-check-circle', label: 'Finished' }
      if (status === 'failed') return { color: 'error', icon: 'mdi-alert-circle', label: 'Failed' }
      return { color: 'warning', icon: 'mdi-progress-clock', label: 'Installing…' }
    }

    const INTERNAL_KEYS = ['install_uuid', 'secret_key', 'confirm_url']

    function parsedConfig(entry) {
      try {
        const obj = JSON.parse(entry.json_config || '{}')
        for (const k of INTERNAL_KEYS) delete obj[k]
        return Object.keys(obj).length ? obj : null
      } catch {
        return null
      }
    }

    onMounted(async () => {
      try {
        const response = await getUserApps()
        userApps.value = response.data
      } catch (err) {
        error.value = err.response?.data?.detail || 'Failed to load installed apps.'
      } finally {
        loading.value = false
      }
    })

    return { userApps, loading, error, parsedConfig, iconUrl, statusMeta }
  }
}
</script>
