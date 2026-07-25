<template>
  <v-container class="py-8">
    <v-card class="mx-auto" max-width="700" elevation="3">
      <v-card-title class="pa-6 pb-0 text-h6">
        Install <strong class="ml-1">{{ app.name || 'App' }}</strong>
        <span v-if="app.version" class="text-caption text-medium-emphasis ml-2">v{{ app.version }}</span>
      </v-card-title>

      <!-- Success screen (after install) -->
      <div v-if="installed" class="pa-8 text-center">
        <v-icon color="success" size="72">mdi-check-circle</v-icon>
        <h2 class="text-h5 mt-4 mb-2">Installation Successful</h2>
        <p class="text-body-1 text-medium-emphasis mb-6">
          <strong>{{ app.name }}</strong> has been installed on
          <strong>{{ selectedAsset?.name }}</strong>.
        </p>
        <v-btn color="primary" @click="$router.push('/')">Back to Store</v-btn>
      </div>

      <!-- Waiting screen: install is running elsewhere -->
      <div v-else-if="waiting" class="pa-8 text-center">
        <v-progress-circular indeterminate color="primary" size="64" />
        <h2 class="text-h6 mt-4 mb-2">Waiting for the installation to finish…</h2>
        <p class="text-body-2 text-medium-emphasis mb-6">
          <strong>{{ app.name }}</strong> is being installed<span v-if="selectedAsset"> on <strong>{{ selectedAsset.name }}</strong></span>.
          This can take a while — you can leave this page and it will resume when you return.
        </p>
        <v-btn variant="text" @click="$router.push('/')">Back to Store</v-btn>
      </div>

      <!-- Failure screen: installer reported failure -->
      <div v-else-if="failed" class="pa-8 text-center">
        <v-icon color="error" size="72">mdi-alert-circle</v-icon>
        <h2 class="text-h5 mt-4 mb-2">Installation Failed</h2>
        <p class="text-body-1 text-medium-emphasis mb-6">
          The installer reported that <strong>{{ app.name }}</strong> could not be installed.
        </p>
        <v-btn color="primary" class="mr-2" @click="retryInstall">Try Again</v-btn>
        <v-btn variant="text" @click="$router.push('/')">Back to Store</v-btn>
      </div>

      <!-- Wizard -->
      <template v-else>
        <v-stepper v-model="step" flat>
          <v-stepper-header>
            <v-stepper-item title="Introduction" :value="1" :complete="step > 1" />
            <v-divider />
            <v-stepper-item title="Select Asset" :value="2" :complete="step > 2" />
            <v-divider />
            <v-stepper-item title="Parameters" :value="3" :complete="step > 3" />
            <v-divider />
            <v-stepper-item title="Install" :value="4" />
          </v-stepper-header>

          <v-stepper-window>
            <!-- Step 1: Introduction -->
            <v-stepper-window-item :value="1">
              <div class="pa-4">
                <p class="text-body-1 mb-4">
                  You are about to install <strong>{{ app.name }}</strong>.
                  Follow the steps in this wizard to configure and deploy the app.
                </p>
                <v-list lines="two" density="compact">
                  <v-list-item
                    prepend-icon="mdi-numeric-1-circle-outline"
                    title="Select an asset"
                    subtitle="Choose which property to install the app on."
                  />
                  <v-list-item
                    prepend-icon="mdi-numeric-2-circle-outline"
                    title="Configure parameters"
                    subtitle="Fill in the app-specific parameters required for installation."
                  />
                  <v-list-item
                    prepend-icon="mdi-numeric-3-circle-outline"
                    title="Install"
                    subtitle="Review the configuration and deploy."
                  />
                </v-list>
              </div>
            </v-stepper-window-item>

            <!-- Step 2: Select Asset -->
            <v-stepper-window-item :value="2">
              <div class="pa-4">
                <p class="text-body-2 text-medium-emphasis mb-4">
                  Select the property on which to install <strong>{{ app.name }}</strong>.
                </p>
                <v-select
                  v-model="selectedAsset"
                  :items="assets"
                  :item-title="a => a.address ? `${a.name}: ${a.address}` : a.name"
                  item-value="id"
                  return-object
                  label="Select asset"
                  variant="outlined"
                  prepend-inner-icon="mdi-home"
                  clearable
                />
                <v-alert
                  v-if="selectedAsset && !assetCompatible"
                  type="error"
                  variant="tonal"
                  density="compact"
                  class="mt-2"
                >
                  <strong>{{ selectedAsset.name }}</strong> — {{ selectedAsset.address }}
                  <div class="mt-1">
                    This asset does not support <strong>{{ app.platform_type }}</strong> installation type.
                  </div>
                </v-alert>
                <v-alert
                  v-else-if="selectedAsset && assetCompatible"
                  type="success"
                  variant="tonal"
                  density="compact"
                  class="mt-2"
                >
                  <strong>{{ selectedAsset.name }}</strong> — {{ selectedAsset.address }}
                  <div v-if="app.platform_type" class="mt-1">
                    This asset supports the <strong>{{ app.platform_type }}</strong> installation type.
                  </div>
                </v-alert>
              </div>
            </v-stepper-window-item>

            <!-- Step 3: Parameters (JSON Forms) -->
            <v-stepper-window-item :value="3">
              <div class="pa-4">
                <template v-if="paramsJsonSchema">
                  <p class="text-body-2 text-medium-emphasis mb-4">
                    Fill in the parameters for <strong>{{ app.name }}</strong>.
                  </p>
                  <div class="jsonforms-wrap">
                    <json-forms
                      :data="paramsData"
                      :schema="paramsJsonSchema"
                      :uischema="paramsUiSchema"
                      :renderers="renderers"
                      @change="onParamsChange"
                    />
                  </div>
                </template>
                <v-alert v-else type="info" variant="tonal" density="compact">
                  This app requires no additional parameters.
                </v-alert>
                <v-alert v-if="schemaError" type="warning" variant="tonal" density="compact" class="mt-3">
                  The developer's parameter form could not be loaded ({{ schemaError }}).
                </v-alert>
              </div>
            </v-stepper-window-item>

            <!-- Step 4: Review & Install -->
            <v-stepper-window-item :value="4">
              <div class="pa-4">
                <p class="text-body-2 text-medium-emphasis mb-4">
                  Review the configuration below and click <strong>Install</strong> to deploy.
                </p>
                <v-table density="compact" class="mb-4">
                  <tbody>
                    <tr>
                      <td class="text-medium-emphasis">App</td>
                      <td><strong>{{ app.name }}</strong><span v-if="app.version"> (v{{ app.version }})</span></td>
                    </tr>
                    <tr>
                      <td class="text-medium-emphasis">Asset</td>
                      <td>{{ selectedAsset?.name }}<span v-if="selectedAsset?.address"> — {{ selectedAsset.address }}</span></td>
                    </tr>
                    <tr v-if="paramsJsonSchema">
                      <td class="text-medium-emphasis">Parameters</td>
                      <td><pre class="params-preview">{{ JSON.stringify(paramsData, null, 2) }}</pre></td>
                    </tr>
                  </tbody>
                </v-table>
                <v-alert v-if="installError" type="error" variant="tonal" density="compact">
                  {{ installError }}
                </v-alert>
              </div>
            </v-stepper-window-item>
          </v-stepper-window>
        </v-stepper>

        <!-- Navigation -->
        <v-card-actions class="pa-6 pt-2">
          <v-btn v-if="step > 1" variant="text" @click="step--">Back</v-btn>
          <v-spacer />
          <v-btn
            v-if="step < 4"
            color="primary"
            :disabled="!canProceed"
            @click="step++"
          >
            Next
          </v-btn>
          <v-btn
            v-if="step === 4"
            color="success"
            :loading="installing"
            @click="handleInstall"
          >
            <v-icon start>mdi-download</v-icon>
            Install
          </v-btn>
        </v-card-actions>
      </template>
    </v-card>
  </v-container>
</template>

<script>
import { ref, computed, markRaw, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { JsonForms } from '@jsonforms/vue'
import { vanillaRenderers } from '@jsonforms/vue-vanilla'
import { getApp, installApp, getInstallStatus } from '../services/api'
import { useAsset, platformList } from '../composables/useAsset'

export default {
  name: 'InstallView',
  components: { JsonForms },
  setup() {
    const route = useRoute()
    const app = ref({})
    const step = ref(1)

    const { assets, selectedAsset: globalAsset, fetchAssets } = useAsset()
    const selectedAsset = ref(globalAsset.value)
    const installing = ref(false)
    const installed = ref(false)
    const installError = ref('')

    // Async install tracking (persisted so an interrupted install can resume).
    const waiting = ref(false)
    const failed = ref(false)
    const currentInstallUuid = ref(null)
    const pendingKey = `weforming_pending_install_${route.params.id}`
    let pollTimer = null

    function beginWaiting(installUuid) {
      currentInstallUuid.value = installUuid
      localStorage.setItem(pendingKey, installUuid)
      failed.value = false
      waiting.value = true
      pollStatus()
    }

    async function pollStatus() {
      if (!currentInstallUuid.value) return
      try {
        const { data } = await getInstallStatus(currentInstallUuid.value)
        if (data.status === 'finished' || data.status === 'failed') {
          waiting.value = false
          localStorage.removeItem(pendingKey)
          if (data.status === 'finished') installed.value = true
          else failed.value = true
          return
        }
      } catch (e) {
        // transient errors: keep polling
      }
      pollTimer = setTimeout(pollStatus, 3000)
    }

    function retryInstall() {
      failed.value = false
      currentInstallUuid.value = null
      step.value = 4  // back to the review step to re-run Install
    }

    // --- JSON Forms parameter step ---
    const renderers = markRaw([...vanillaRenderers])
    const paramsJsonSchema = ref(null)
    const paramsUiSchema = ref(undefined)
    const paramsData = ref({})
    const paramsErrors = ref([])
    const schemaError = ref('')

    function loadParamsSchema() {
      const raw = app.value?.params_schema
      if (!raw || !raw.trim()) return
      try {
        const parsed = JSON.parse(raw)
        // Support either a bare JSON Schema, or { schema, uischema }.
        if (parsed && parsed.schema) {
          paramsJsonSchema.value = parsed.schema
          paramsUiSchema.value = parsed.uischema || undefined
        } else {
          paramsJsonSchema.value = parsed
        }
      } catch (e) {
        schemaError.value = 'invalid JSON'
      }
    }

    function onParamsChange(event) {
      paramsData.value = event.data
      paramsErrors.value = event.errors || []
    }

    const assetCompatible = computed(() => {
      const appPlatform = app.value?.platform_type?.trim()
      if (!appPlatform) return true
      return platformList(selectedAsset.value?.platforms).includes(appPlatform)
    })

    const canProceed = computed(() => {
      if (step.value === 2) return !!selectedAsset.value && assetCompatible.value
      if (step.value === 3) return paramsErrors.value.length === 0
      return true
    })

    async function handleInstall() {
      installing.value = true
      installError.value = ''
      try {
        const jsonConfig = paramsJsonSchema.value ? paramsData.value : {}
        const { data } = await installApp(
          app.value.id,
          selectedAsset.value.id,
          jsonConfig,
          app.value.version || '1.0.0',
        )
        // Install runs elsewhere; wait for the installer to confirm completion.
        beginWaiting(data.install_uuid)
      } catch (err) {
        installError.value = err.response?.data?.detail || 'Installation failed. Please try again.'
      } finally {
        installing.value = false
      }
    }

    onMounted(async () => {
      await fetchAssets()
      if (!selectedAsset.value) selectedAsset.value = globalAsset.value
      try {
        const response = await getApp(route.params.id)
        app.value = response.data
        loadParamsSchema()
      } catch {
        // fall back to empty app, name shown as 'App'
      }
      // Resume a previously started install that hasn't finished yet.
      const pending = localStorage.getItem(pendingKey)
      if (pending) beginWaiting(pending)
    })

    onUnmounted(() => {
      if (pollTimer) clearTimeout(pollTimer)
    })

    return {
      app, step,
      selectedAsset, assets,
      assetCompatible, canProceed,
      renderers, paramsJsonSchema, paramsUiSchema, paramsData, schemaError, onParamsChange,
      installing, installed, installError, waiting, failed,
      handleInstall, retryInstall, JSON,
    }
  }
}
</script>

<style scoped>
.params-preview {
  font-size: 0.8rem;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}

/* Minimal styling for the framework-agnostic JSON Forms (vue-vanilla) renderers. */
.jsonforms-wrap :deep(.control) {
  margin-bottom: 14px;
}
.jsonforms-wrap :deep(label) {
  display: block;
  font-size: 0.85rem;
  font-weight: 500;
  margin-bottom: 4px;
}
.jsonforms-wrap :deep(input),
.jsonforms-wrap :deep(select),
.jsonforms-wrap :deep(textarea) {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid rgba(0, 0, 0, 0.3);
  border-radius: 4px;
  font: inherit;
}
.jsonforms-wrap :deep(input[type="checkbox"]) {
  width: auto;
}
.jsonforms-wrap :deep(.description) {
  font-size: 0.75rem;
  color: rgba(0, 0, 0, 0.6);
  margin-top: 2px;
}
</style>
