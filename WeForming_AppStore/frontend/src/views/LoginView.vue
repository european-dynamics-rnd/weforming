<template>
  <v-container fluid class="login-container fill-height">
    <v-row align="center" justify="center" class="fill-height">
      <v-col cols="12" sm="8" md="5" lg="4">
        <div class="text-center mb-8">
          <a href="https://weforming.eu" target="_blank" rel="noopener">
            <v-img
              src="/WeForming-logo.webp"
              contain
              max-height="80"
              class="mx-auto mb-4 logo-link"
            />
          </a>
          <h1 class="text-h5 font-weight-bold text-grey-darken-3">WeForming App Store</h1>
        </div>

        <v-card class="login-card pa-6" rounded="lg" elevation="4">
          <v-card-title class="text-h6 font-weight-bold mb-2 px-0">Sign in</v-card-title>
          <v-card-subtitle class="px-0 mb-6">Enter your credentials to continue</v-card-subtitle>

          <v-alert
            v-if="errorMessage"
            type="error"
            variant="tonal"
            class="mb-4"
            density="compact"
          >
            {{ errorMessage }}
          </v-alert>

          <v-form @submit.prevent="handleLogin" ref="form">
            <v-text-field
              v-model="email"
              label="Email"
              type="email"
              variant="outlined"
              prepend-inner-icon="mdi-email-outline"
              :rules="[v => !!v || 'Email is required']"
              class="mb-3"
              autocomplete="username"
            />

            <v-text-field
              v-model="password"
              label="Password"
              :type="showPassword ? 'text' : 'password'"
              variant="outlined"
              prepend-inner-icon="mdi-lock-outline"
              :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
              @click:append-inner="showPassword = !showPassword"
              :rules="[v => !!v || 'Password is required']"
              class="mb-4"
              autocomplete="current-password"
            />

            <v-btn
              type="submit"
              color="primary"
              size="large"
              block
              :loading="loading"
              class="mb-4"
            >
              Sign In
            </v-btn>
          </v-form>
        </v-card>

        <div class="text-center mt-6">
          <a href="https://weforming.eu" target="_blank" rel="noopener" class="about-link">
            About WeForming project...
          </a>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const { login } = useAuth()

    const form = ref(null)
    const email = ref('')
    const password = ref('')
    const showPassword = ref(false)
    const loading = ref(false)
    const errorMessage = ref('')

    async function handleLogin() {
      const { valid } = await form.value.validate()
      if (!valid) return

      loading.value = true
      errorMessage.value = ''

      try {
        await login(email.value, password.value)
        router.push('/')
      } catch (err) {
        if (err.response?.status === 401) {
          errorMessage.value = 'Invalid email or password.'
        } else {
          errorMessage.value = 'Unable to connect. Please try again.'
        }
      } finally {
        loading.value = false
      }
    }

    return { form, email, password, showPassword, loading, errorMessage, handleLogin }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  /* Light wash over the photo keeps the card and header text readable. */
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0.3)),
    url('../assets/weforming-bg.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.logo-link {
  cursor: pointer;
  transition: opacity 0.2s;
}
.logo-link:hover {
  opacity: 0.8;
}

.about-link {
  color: #38b272;
  text-decoration: none;
  font-size: 0.875rem;
}
.about-link:hover {
  text-decoration: underline;
}
</style>
