<template>
  <router-view />
  <Footer />
  <ToastContainer />
</template>

<script setup>
import Footer from './components/Footer.vue'
import ToastContainer from './components/ToastContainer.vue'
</script>