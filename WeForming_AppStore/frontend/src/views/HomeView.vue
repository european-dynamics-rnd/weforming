
<template>
  <v-container>
    <v-row v-if="isDeveloper" justify="end" class="mb-4">
      <v-col cols="auto">
        <v-btn color="primary" @click="$router.push('/create-app')">
          <v-icon left>mdi-plus</v-icon>
          Publish a New App
        </v-btn>
      </v-col>
    </v-row>

    <h1>Welcome to the WeForming App Store</h1>
    <h1>&nbsp;</h1>

    <v-row>
      <v-col v-for="app in filteredApps" :key="app.id" cols="12" sm="6" md="4">
        <AppCard :app="app" />
      </v-col>
    </v-row>

    <v-alert
      v-if="filteredApps.length === 0 && apps.length > 0"
      type="info"
      variant="tonal"
      class="mt-4"
    >
      No apps match the platforms available for <strong>{{ selectedAsset?.name }}</strong>.
    </v-alert>
  </v-container>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import AppCard from '../components/AppCard.vue'
import { getApps } from '../services/api'
import { useAsset, platformList } from '../composables/useAsset'
import { useAuth } from '../composables/useAuth'

export default {
  components: { AppCard },
  setup() {
    const apps = ref([])
    const { selectedAsset } = useAsset()
    const { isDeveloper } = useAuth()

    const filteredApps = computed(() => {
      const platforms = platformList(selectedAsset.value?.platforms)
      if (platforms.length === 0) return apps.value
      return apps.value.filter(app =>
        !app.platform_type || platforms.includes(app.platform_type)
      )
    })

    onMounted(async () => {
      try {
        const response = await getApps()
        apps.value = response.data
      } catch (error) {
        console.error('Error fetching apps:', error)
      }
    })

    return { apps, filteredApps, selectedAsset, isDeveloper }
  }
}
</script>
