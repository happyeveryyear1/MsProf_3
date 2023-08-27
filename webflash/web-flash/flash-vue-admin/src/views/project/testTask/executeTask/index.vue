<template>
  <div style="padding:20px">
    <!-- 测试信息 -->
    <div>
      <div style="margin-bottom:10px" />
      <el-button size="medium ">
        测试信息
      </el-button>
    </div>

    <div ref="message" class="message" v-html="message" />

  </div>

</template>

<script src="./executeTask.js"></script>

<style lang="scss">
.message {
  height: 700px;
  overflow-y: scroll;
  white-space: pre-wrap;
  font-family: Arial, sans-serif;
  font-size: 16px;
  line-height: 1.5;
  padding: 10px;
  margin: 20px 0;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-sizing: border-box;
  width: 100%;
  overflow-x: hidden; /* 隐藏水平滚动条 */
  word-break: break-all; /* 允许在单词内断开 */
  overflow-wrap: break-word; /* 在必要时允许单词断开 */
}
</style>
