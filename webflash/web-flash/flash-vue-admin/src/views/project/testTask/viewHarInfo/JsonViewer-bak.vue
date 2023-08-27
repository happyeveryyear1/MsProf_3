<template>
  <div class="row">
    <div class="col-sm-6">
      <div v-for="(value, key) in json1" :key="key" class="mb-3">
        <div
          class="bg-light p-2"
          @click="toggle(key, value, true)"
          v-if="typeof value === 'object'"
        >
          <span v-if="openKeys.includes(key)">-</span>
          <span v-else>+</span>
          <strong>{{ key }}</strong>
        </div>
        <div v-else>
          <strong>{{ key }}:</strong> {{ value }}
        </div>
        <div v-if="typeof value === 'object' && openKeys.includes(key)">
          <JsonViewer :json="value" />
        </div>
      </div>
    </div>
    <div class="col-sm-6">
      <div v-for="(value, key) in json2" :key="key" class="mb-3">
        <div
          class="bg-light p-2"
          @click="toggle(key, value, false)"
          v-if="typeof value === 'object'"
        >
          <span v-if="openKeys.includes(key)">-</span>
          <span v-else>+</span>
          <strong>{{ key }}</strong>
        </div>
        <div v-else>
          <strong>{{ key }}:</strong> {{ value }}
        </div>
        <div v-if="typeof value === 'object' && openKeys.includes(key)">
          <JsonViewer :json="value" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "JsonViewer",
  props: {
    json: Object,
  },
  data() {
    return {
      openKeys: [],
    };
  },
  methods: {
    toggle(key, value, isJson1) {
      if (typeof value === "object") {
        const index = this.openKeys.indexOf(key);
        if (index >= 0) {
          this.openKeys.splice(index, 1);
        } else {
          this.openKeys.push(key);
        }
      }
    },
  },
};
</script>

<style>
.bg-light {
  background-color: #f8f9fa !important;
}
</style>
