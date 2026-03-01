export default {
  namespaced: true,
  state: {
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    role: localStorage.getItem('role') || ''
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_USER(state, user) {
      state.user = user
      state.role = user?.role || ''
      localStorage.setItem('user', JSON.stringify(user))
      localStorage.setItem('role', user?.role || '')
    },
    CLEAR_AUTH(state) {
      state.token = ''
      state.user = null
      state.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('role')
    }
  },
  actions: {
    login({ commit }, { token, user }) {
      commit('SET_TOKEN', token)
      commit('SET_USER', user)
    },
    logout({ commit }) {
      commit('CLEAR_AUTH')
    }
  }
}

