import Vue from 'vue'
import Vuetify from 'vuetify/lib'

Vue.use(Vuetify)

export default new Vuetify({
  theme: {
    themes: {
      light: {
        primary: '#131111',    
        secondary: '#03da92',  
        accent: '#7cff40fb',   
        searchBar: '#0b649c',  // Custom color for search bar
      },
    },
  },
})
