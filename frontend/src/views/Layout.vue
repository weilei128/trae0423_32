<template>
  <el-container class="layout-container" style="height: 100vh;">
    <el-aside width="200px">
      <div class="logo">
        <h3>影院管理系统</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical-demo"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item index="/dashboard">
          <i class="el-icon-data-line"></i>
          <span>营业报表</span>
        </el-menu-item>
        <el-menu-item index="/movies">
          <i class="el-icon-film"></i>
          <span>影片管理</span>
        </el-menu-item>
        <el-menu-item index="/halls">
          <i class="el-icon-office-building"></i>
          <span>影厅管理</span>
        </el-menu-item>
        <el-menu-item index="/schedules">
          <i class="el-icon-date"></i>
          <span>排片管理</span>
        </el-menu-item>
        <el-menu-item index="/tickets">
          <i class="el-icon-tickets"></i>
          <span>票务销售</span>
        </el-menu-item>
        <el-menu-item index="/verify">
          <i class="el-icon-postcard"></i>
          <span>取票核销</span>
        </el-menu-item>
        <el-menu-item index="/members">
          <i class="el-icon-user"></i>
          <span>会员管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-content">
          <span class="user-info">管理员</span>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  name: 'Layout',
  computed: {
    activeMenu() {
      return this.$route.path
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4b;

  h3 {
    color: #fff;
    font-size: 18px;
    margin: 0;
    font-weight: 600;
  }
}

.el-aside {
  background-color: #304156;
  overflow: hidden;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.user-info {
  font-size: 14px;
  color: #606266;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
