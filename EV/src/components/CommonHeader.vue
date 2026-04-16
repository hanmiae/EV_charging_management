<template>
  <header class="common-header">
    
    <div class="left">
      <slot name="logo">
        <h1 class="logo">EV Charging Zone</h1>
      </slot>
    </div>

    <div class="right">
      <slot />
    </div>

  </header>
</template>

<script setup>
</script>

<style scoped>
.common-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 60px;
  background: #0d2b1f;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  z-index: 1000;
}

.logo {
  color: white;
  font-size: 1.8rem;
  font-weight: 900;
}

.left, .right {
  display: flex;
  align-items: center;
  gap: 15px;
}
</style>