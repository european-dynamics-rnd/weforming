<template>
  <v-app-bar app color="#38b272" dark>
    <v-text-field
      v-model="searchQuery"
      label="Search WeForming app store..."
      hide-details
      single-line
      solo
      flat
      class="mt-6 custom-search-bar"
      @keyup.enter="handleSearch">

      <template v-slot:append>
        <v-icon @click="handleSearch">mdi-magnify</v-icon>
      </template>
    </v-text-field>

    <v-spacer></v-spacer>

    <v-btn variant="text" @click="$router.push('/my-apps')">
      <v-icon start>mdi-view-grid</v-icon>
      My Apps
    </v-btn>

    <v-btn variant="text" @click="$router.push('/assets')">
      <v-icon start>mdi-home-city</v-icon>
      My Houses
    </v-btn>

    <v-menu>
      <template v-slot:activator="{ props }">
        <v-btn v-bind="props" variant="text">
          <v-icon start>mdi-home</v-icon>
          {{ shortName }}
          <v-icon end>mdi-menu-down</v-icon>
        </v-btn>
      </template>
      <v-list>
        <v-list-item
          v-for="asset in assets"
          :key="asset.id"
          :title="asset.name"
          :subtitle="asset.address"
          :active="selectedAsset?.id === asset.id"
          active-color="primary"
          @click="setAsset(asset)"
        />
      </v-list>
    </v-menu>

    <v-switch
      v-model="showPlatformType"
      @update:model-value="setShowPlatformType"
      label="Show app platform type"
      density="compact"
      hide-details
      color="secondary"
      class="mx-3 platform-toggle"
    />

    <v-btn icon @click="handleLogout" title="Sign out">
      <v-icon>mdi-logout</v-icon>
    </v-btn>
</v-app-bar>
</template>


<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { useAsset } from '../composables/useAsset'
import { useSettings } from '../composables/useSettings'

export default {
  setup() {
    const searchQuery = ref('')
    const router = useRouter()
    const { logout } = useAuth()
    const { assets, selectedAsset, shortName, fetchAssets, setAsset } = useAsset()
    const { showPlatformType, setShowPlatformType } = useSettings()

    onMounted(fetchAssets)

    const handleSearch = () => {
      router.push({ path: '/search', query: { q: searchQuery.value } })
    }

    const handleLogout = () => {
      logout()
      router.push('/login')
    }

    return { searchQuery, handleSearch, handleLogout, assets, selectedAsset, shortName, setAsset, showPlatformType, setShowPlatformType }
  }
}
</script>



<style scoped>
.platform-toggle :deep(.v-label) {
  color: rgba(255, 255, 255, 0.87);
  font-size: 0.8rem;
  white-space: nowrap;
}

.custom-search-bar .v-input__control {
  min-height: 40px;
  border-radius: 8px;
}
.custom-search-bar .v-input__slot {
  background: linear-gradient(to right, #17cb11 0%, #060a112d 100%) !important;
  border-radius: 8px;
}
.custom-search-bar .v-input__slot input {
  color: white !important;
}
.custom-search-bar .v-input__slot .v-label {
  color: rgba(255, 255, 255, 0.7) !important;
}
</style>