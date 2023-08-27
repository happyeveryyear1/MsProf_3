<template>
  <div class="register-container" style=" background-image: url(/img/bg.jpg);">

    <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form" auto-complete="on" label-position="left">

      <div class="title-container">
        <h3 class="title">{{ $t('register.title') }}</h3>
        <lang-select class="set-language" />
      </div>

      <el-form-item prop="username">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          v-model="registerForm.username"
          :placeholder="$t('register.username')"
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
          v-model="registerForm.password"
          :type="pwdType"
          :placeholder="$t('register.password')"
          name="password"
          auto-complete="on"
          @keyup.enter.native="handleLogin"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="pwdType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <el-form-item prop="role">
        <span class="svg-container">
          <svg-icon icon-class="people" />
        </span>
        <el-select v-model="registerForm.role" placeholder="选择角色">
          <el-option label="项目管理员" value="项目管理员" />
          <el-option label="系统管理员" value="系统管理员" />
          <el-option label="测试员" value="测试员" />
        </el-select>
      </el-form-item>

      <el-button :loading="loading" type="primary" style="width:100%;margin-bottom:30px;" @click.native.prevent="handleRegister">{{ $t('register.register') }}</el-button>

      <div style="position:relative">
        <span style="text-align:right;color:white"><router-link to="/login">{{ $t('register.login') }}</router-link><router-view /></span>
      </div>
    </el-form>

  </div>
</template>

<script src="./register.js"></script>
<style rel="stylesheet/scss" lang="scss" src="./register.scss"></style>
