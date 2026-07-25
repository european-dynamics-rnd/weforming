<template>
  <v-container>
    <v-row>
      <v-col v-for="app in apps" :key="app.id" cols="12" sm="6" md="4">
        <AppCard :app="app" />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { searchApps } from '../services/api'
import AppCard from '../components/AppCard.vue'

export default {
  components: { AppCard },
  setup() {
    const route = useRoute()
    const apps = ref([])

    onMounted(async () => {
      const query = route.query.q
      if (query) {
        const response = await searchApps(query)
        apps.value = response.data
      }
    })

    return { apps }
  }
}
</script>
