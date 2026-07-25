<template>
  <v-container>
    <v-card class="mx-auto" max-width="640" rounded="lg" elevation="3">
      <v-card-title class="text-h6 font-weight-bold pt-6 px-6">
        <v-icon start color="primary">mdi-rocket-launch-outline</v-icon>
        Publish a New App
      </v-card-title>
      <v-card-subtitle class="px-6">
        Add your app to the WeForming App Store. It will be attributed to you as the developer.
      </v-card-subtitle>

      <v-card-text class="px-6 pb-2">
        <v-alert
          v-if="errorMessage"
          type="error"
          variant="tonal"
          density="compact"
          class="mb-4"
        >
          {{ errorMessage }}
        </v-alert>

        <v-form ref="form" @submit.prevent="publish">
          <v-text-field
            v-model="name"
            label="App name"
            variant="outlined"
            prepend-inner-icon="mdi-application"
            :rules="[v => !!v || 'Name is required']"
            class="mb-3"
          />

          <v-textarea
            v-model="description"
            label="Description"
            variant="outlined"
            prepend-inner-icon="mdi-text"
            rows="3"
            auto-grow
            class="mb-3"
          />

          <v-select
            v-model="platformType"
            :items="platformOptions"
            label="Platform type"
            variant="outlined"
            prepend-inner-icon="mdi-cpu-64-bit"
            :rules="[v => !!v || 'Platform type is required']"
            class="mb-3"
          />

          <v-text-field
            v-model.number="price"
            label="Price (EUR)"
            type="number"
            min="0"
            variant="outlined"
            prepend-inner-icon="mdi-currency-eur"
            class="mb-3"
          />

          <v-text-field
            v-model="version"
            label="Version"
            placeholder="1.0.0"
            variant="outlined"
            prepend-inner-icon="mdi-tag-outline"
            class="mb-3"
          />

          <v-text-field
            v-model="developerUrl"
            label="Developer link (URL)"
            placeholder="https://your-company.example.com"
            type="url"
            variant="outlined"
            prepend-inner-icon="mdi-web"
            class="mb-3"
          />

          <v-text-field
            v-model="installUrl"
            label="Install URL"
            placeholder="https://your-app.example.com/install"
            type="url"
            variant="outlined"
            prepend-inner-icon="mdi-link-variant"
            hint="Called with the collected parameters when a user installs the app."
            persistent-hint
            class="mb-3"
          />

          <v-textarea
            v-model="paramsSchema"
            label="Installation parameters (JSON Forms schema)"
            variant="outlined"
            prepend-inner-icon="mdi-code-json"
            rows="6"
            auto-grow
            :rules="[validJsonRule]"
            hint="A JSON Schema shown as a form during installation. Leave empty for no parameters."
            persistent-hint
            placeholder='{
  "type": "object",
  "properties": {
    "api_key": { "type": "string", "title": "API key" },
    "interval": { "type": "integer", "title": "Poll interval (min)", "default": 15 }
  },
  "required": ["api_key"]
}'
            class="mb-3"
          />

          <v-file-input
            v-model="icon"
            label="App icon"
            accept="image/*"
            variant="outlined"
            prepend-icon=""
            prepend-inner-icon="mdi-image-outline"
            show-size
            class="mb-4"
          />

          <v-btn
            type="submit"
            color="primary"
            size="large"
            block
            :loading="loading"
          >
            <v-icon start>mdi-cloud-upload-outline</v-icon>
            Publish New App
          </v-btn>
        </v-form>
      </v-card-text>
    </v-card>

    <v-snackbar v-model="snackbar" :timeout="2500" color="success">
      {{ snackbarMessage }}
    </v-snackbar>
  </v-container>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createApp, getPlatformTypes } from '../services/api'

export default {
  name: 'CreateAppView',
  setup() {
    const router = useRouter()

    const form = ref(null)
    const name = ref('')
    const description = ref('')
    const platformType = ref(null)
    const price = ref(0)
    const version = ref('')
    const developerUrl = ref('')
    const installUrl = ref('')
    const paramsSchema = ref('')
    const icon = ref(null)
    // Platform options come from the catalog (extensible); fall back to defaults.
    const platformOptions = ref(['Docker', 'Web'])
    onMounted(async () => {
      try {
        const { data } = await getPlatformTypes()
        if (data?.length) platformOptions.value = data.map(p => p.name)
      } catch (e) { /* keep defaults */ }
    })

    const loading = ref(false)
    const errorMessage = ref('')
    const snackbar = ref(false)
    const snackbarMessage = ref('')

    const validJsonRule = (v) => {
      if (!v || !v.trim()) return true  // empty is allowed
      try {
        JSON.parse(v)
        return true
      } catch (e) {
        return 'Must be valid JSON'
      }
    }

    async function publish() {
      const { valid } = await form.value.validate()
      if (!valid) return

      loading.value = true
      errorMessage.value = ''
      try {
        const formData = new FormData()
        formData.append('name', name.value)
        formData.append('description', description.value)
        formData.append('rating', 0)
        formData.append('price', price.value || 0)
        formData.append('platform_type', platformType.value || '')
        formData.append('version', version.value || '')
        formData.append('developer_url', developerUrl.value || '')
        formData.append('install_url', installUrl.value || '')
        formData.append('params_schema', paramsSchema.value || '')
        // v-file-input yields an array in Vuetify 3
        const file = Array.isArray(icon.value) ? icon.value[0] : icon.value
        if (file) formData.append('icon', file)

        await createApp(formData)
        snackbarMessage.value = 'App published successfully!'
        snackbar.value = true
        setTimeout(() => router.push('/'), 1500)
      } catch (err) {
        if (err.response?.status === 403) {
          errorMessage.value = 'You need the developer role to publish apps.'
        } else {
          errorMessage.value = 'Failed to publish the app. Please try again.'
        }
      } finally {
        loading.value = false
      }
    }

    return {
      form, name, description, platformType, price,
      version, developerUrl, installUrl, paramsSchema,
      icon, platformOptions, validJsonRule,
      loading, errorMessage, snackbar, snackbarMessage, publish,
    }
  }
}
</script>
