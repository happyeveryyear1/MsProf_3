<template>
  <div class="login-container" style=" background-image: url(/img/bg.jpg);">

    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" auto-complete="on" label-position="left">

      <div class="title-container">
        <h3 class="title">{{ $t('login.title') }}</h3>
        <!-- <lang-select class="set-language" /> -->
      </div>

      <el-form-item prop="username">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          v-model="loginForm.username"
          :placeholder="$t('login.username')"
          name="username"
          type="text"
          auto-complete="on"
        />
      </el-form-item>

      <el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          v-model="loginForm.password"
          :type="pwdType"
          :placeholder="$t('login.password')"
          name="password"
          auto-complete="on"
          @keyup.enter.native="handleLogin"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="pwdType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <el-form-item prop="verifyCode">
       <el-col :span="16">
         <span class="svg-container">
           <svg-icon icon-class="password" />
         </span>
         <el-input
           v-model="loginForm.verifyCode"
           placeholder="验证码"
           name="verifyCode">
         </el-input>
       </el-col>
       <el-col :span="4">
         <img class="verifyCodeImg" :src="imageSrc" @click="refreshCode">
       </el-col>
      </el-form-item>

      <el-button :loading="loading" type="primary" style="width:100%;margin-bottom:30px;" @click.native.prevent="handleLogin">{{ $t('login.logIn') }}</el-button>

      <!-- <div style="position:relative">
        <div class="tips">
          <span>{{ $t('login.username') }} : admin</span>
          <span>{{ $t('login.password') }} : admin</span>
        </div>
        <div class="tips">
          <span style="margin-right:18px;">{{ $t('login.username') }} : developer</span>
          <span>{{ $t('login.password') }} : developer</span>
        </div>
      </div> -->
      <!-- <div style="position:relative">
        <span style="text-align:right;color:white"><router-link to="/register">{{ $t('login.register') }}</router-link><router-view /></span>
      </div> -->
    </el-form>

  </div>
</template>

<script src="./login.js"></script>
<style rel="stylesheet/scss" lang="scss" src="./login.scss"></style>
