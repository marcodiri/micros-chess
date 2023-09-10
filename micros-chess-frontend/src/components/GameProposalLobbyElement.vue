<script setup lang="ts">
defineProps({
  n: Number
})
</script>

<template>
  <tr>
    <th scope="row">{{ n }}</th>
    <td>Mark</td>
    <td>@mdo</td>
  </tr>
</template>
