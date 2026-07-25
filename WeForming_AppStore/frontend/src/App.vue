<template>
  <v-app>
    <Navbar v-if="$route.name !== 'login'" />
    <v-main>
      <router-view />
    </v-main>
    <!-- Floating Logo -->
    <v-img
      v-if="$route.name !== 'login'"
      src="/WeForming-logo.webp"
      class="floating-logo"
      contain
      max-height="100"
      max-width="179"
      @click="$router.push('/')"
    />
  </v-app>
</template>



<script>
import Navbar from './components/Navbar.vue'

export default {
  name: 'App',
  components: {
    Navbar
  }
}
</script>

<style>
.floating-logo {
  position: absolute;
  bottom: 20px;
  right: 0px;
  z-index: 100;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.floating-logo:hover {
  transform: scale(1.2);
}
</style>
 