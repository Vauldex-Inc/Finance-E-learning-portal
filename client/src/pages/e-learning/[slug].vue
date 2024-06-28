<template>
  <main class="grow basis-2/3 justify-center md:basis-4/5">
    <div>
      <Lesson @select-lesson="select" />
    </div>
  </main>
</template>

<script lang="ts" setup>
  definePageMeta({
    middleware: 'auth',
    layout: 'learner'
  })

  const emit = defineEmits(['selectLesson'])
  const select = (sec: string) => {
    emit('selectLesson', sec)
  }
</script>
