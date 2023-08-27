<template>
  <div class="url-box">
    <div class="url" @click="toggleRequest">
      <div class="url-title">{{ url.method }} {{ url.url }}</div>
      <div v-show="showRequest" class="url-details">
        <div class="url-request">
          <div class="url-details-title" @click="togglePostData">Request</div>
          <pre v-show="showPostData" class="url-details-content">{{ prettyJson(url.request.postData) }}</pre>
        </div>
        <div class="url-response">
          <div class="url-details-title" @click="toggleResponse">Response</div>
          <pre v-show="showResponse" class="url-details-content">{{ prettyJson(url.response.content) }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
    name: 'UrlBox',
    props: {
        url: {
            type: Object,
            required: true
        }
    },
    data() {
        return {
            showRequest: false,
            showPostData: false,
            showResponse: false
        }
    },
    methods: {
        toggleRequest() {
            this.showRequest = !this.showRequest
        },
        togglePostData() {
            this.showPostData = !this.showPostData
        },
        toggleResponse() {
            this.showResponse = !this.showResponse
        },
        prettyJson(json) {
            return JSON.stringify(json, null, 2)
        }
    }
}
</script>

<style scoped>
.url-box {
  margin: 1rem 0;
  border: 1px solid #ccc;
  border-radius: 0.25rem;
  padding: 1rem;
}

.url {
  cursor: pointer;
}

.url-title {
  font-size: 1.2rem;
  font-weight: bold;
}

.url-details {
  margin-top: 0.5rem;
}

.url-details-title {
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.url-details-content {
  border: 1px solid #ccc;
  border-radius: 0.25rem;
  padding: 0.5rem;
  overflow-x: scroll;
}
</style>
