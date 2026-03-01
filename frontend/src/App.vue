<template>
  <div id="app">
    <router-view />
    <AIChat v-if="userRole === 'TENANT'" />
  </div>
</template>

<script>
import { computed } from 'vue'
import { useStore } from 'vuex'
import AIChat from './components/AIChat.vue'

export default {
  name: 'App',
  components: {
    AIChat
  },
  setup() {
    const store = useStore()
    const userRole = computed(() => store.state.auth.role)
    
    return {
      userRole
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  min-height: 100vh;
}
</style>

