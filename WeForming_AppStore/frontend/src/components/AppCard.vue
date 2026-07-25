<template>
  <v-card max-width="300" class="mx-auto">
    <v-img
      v-if="iconSrc"
      :src="iconSrc"
      height="140px"
      contain
    ></v-img>
    <v-card-title>{{ app.name }}</v-card-title>
    <v-card-text>
      <p>{{ app.description }}</p>
      <p class="mt-2">
        Developer:
        <a
          v-if="app.developer_url"
          :href="app.developer_url"
          target="_blank"
          rel="noopener"
        >{{ app.developer_url }}</a>
        <a
          v-else-if="app.developer_email"
          :href="`mailto:${app.developer_email}`"
        >{{ app.developer_email }}</a>
        <span v-else>argovolt</span>
      </p>
      <p v-if="app.version" class="mt-1 text-caption text-medium-emphasis">Version {{ app.version }}</p>
      <p class="mt-2"><strong>{{ app.price }} EUR</strong></p>
      <v-chip
        v-if="showPlatformType && app.platform_type"
        size="small"
        color="secondary"
        variant="tonal"
        prepend-icon="mdi-cpu-64-bit"
        class="mt-2"
      >
        {{ app.platform_type }}
      </v-chip>
    </v-card-text>
    <v-card-actions>
      <v-btn color="primary" @click="installApp">
        Install
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useSettings } from '../composables/useSettings'
import { iconUrl } from '../services/api'

export default {
  props: {
    app: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const router = useRouter()
    const { showPlatformType } = useSettings()

    const iconSrc = computed(() => iconUrl(props.app))

    const installApp = () => {
      router.push({ name: 'install', params: { id: props.app.id } })
    }

    return { installApp, showPlatformType, iconSrc }
  }
}
</script>
