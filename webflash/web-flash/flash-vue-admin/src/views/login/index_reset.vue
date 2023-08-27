<template>
  <div class="login-container" style=" background-image: url(/img/bg.jpg); filter: hue-rotate(180deg)">

    <el-form ref="loginResetForm" :model="loginResetForm" :rules="loginRules" class="login-form" auto-complete="on" label-position="left">

      <!-- <div class="title-container">
        <h3 class="title">{{ $t('loginReset.title') }}</h3> -->
        <!-- <lang-select class="set-language" /> -->
      <!-- </div> -->
      <p style="color:grey">跳转原因：默认密码 和 过期密码 需要重置才能登录！<el-link href="/account/login" style="color:white">[返回]</el-link></p>


      <el-form-item prop="username">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          v-model="loginResetForm.username"
          :placeholder="$t('loginReset.username')"
          name="username"
          type="text"
          auto-complete="on"
        />
      </el-form-item>

      <el-form-item prop="oldPassword">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          v-model="loginResetForm.oldPassword"
          :type="pwdType"
          :placeholder="$t('loginReset.password')"
          name="oldPassword"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="pwdType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>
      <el-form-item prop="newPassword">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          v-model="loginResetForm.newPassword"
          :type="pwdType2"
          :placeholder="$t('loginReset.newPassword')"
          name="newPassword"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd2">
          <svg-icon :icon-class="pwdType2 === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>
      <el-form-item prop="newPasswordConfirm">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          v-model="loginResetForm.newPasswordConfirm"
          :type="pwdType2"
          :placeholder="$t('loginReset.newPasswordConfirm')"
          name="newPasswordConfirm"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd2">
          <svg-icon :icon-class="pwdType2 === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <el-form-item prop="verifyCode">
       <el-col :span="16">
         <span class="svg-container">
           <svg-icon icon-class="password" />
         </span>
         <el-input
           v-model="loginResetForm.verifyCode"
           placeholder="验证码"
           name="verifyCode">
         </el-input>
       </el-col>
       <el-col :span="4">
         <img class="verifyCodeImg" :src="imageSrc" @click="refreshCode">
       </el-col>
      </el-form-item>

      <el-button :loading="loading" type="primary" style="width:100%;margin-bottom:30px;" @click.native.prevent="handleLoginReset">{{ $t('loginReset.logIn') }}</el-button>

      <!-- <div style="position:relative">
        <div class="tips">
          <span>{{ $t('loginReset.username') }} : admin</span>
          <span>{{ $t('loginReset.password') }} : admin</span>
        </div>
        <div class="tips">
          <span style="margin-right:18px;">{{ $t('loginReset.username') }} : developer</span>
          <span>{{ $t('loginReset.password') }} : developer</span>
        </div>
      </div> -->
      <!-- <div style="position:relative">
        <span style="text-align:right;color:white"><router-link to="/register">{{ $t('loginReset.register') }}</router-link><router-view /></span>
      </div> -->
    </el-form>

  </div>
</template>

<script src="./login_reset.js"></script>
<style rel="stylesheet/scss" lang="scss" src="./login.scss"></style>
