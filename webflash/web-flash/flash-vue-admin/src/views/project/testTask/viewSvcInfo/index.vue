<template>
  <div style="padding: 20px">
    <div class="block" style="padding: 10px">
      <el-row>
        <el-col :span="24">
          <el-input
            v-model="fack_model"
            placeholder="活动名"
            size="mini"
            clearable
            class="filter-item"
            style="width: 150px"
          />
          <el-button
            type="primary"
            icon="el-icon-search"
            size="mini"
            style="margin-left: 20px"
            @click.native="search"
          >{{ $t("button.search") }}</el-button>
          <el-button
            type="primary"
            icon="el-icon-refresh"
            size="mini"
            @click.native="reset"
          >{{ $t("button.reset") }}</el-button>
          <el-button
            type="success"
            icon="el-icon-plus"
            size="mini"
            @click.native="add"
          >{{ $t("button.add") }}</el-button>
          <el-button
            type="warning"
            icon="el-icon-edit"
            size="mini"
            @click.native="edit"
          >{{ $t("button.edit") }}</el-button>
          <el-button
            type="danger"
            icon="el-icon-delete"
            size="mini"
            @click.native="remove"
          >{{ $t("button.delete") }}</el-button>
        </el-col>
      </el-row>
      <br>
    </div>

    <!-- <el-row v-for="(svc, svcIdx) in svclist2" :key="svcIdx">
      <el-col span="6">
        {{ svc.name }}
      </el-col>
      <el-col span="6">
        <el-row v-for="(i,interfaceIdx) in svc.interface" :key="interfaceIdx">{{ i }}</el-row>
      </el-col>
    </el-row> -->

    <el-table
      :data="svcList"
      style="width: 80%; margin-bottom: 20px"
      row-key="id"
      border
      :cell-style="{ paddingLeft: '30px' }"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column
        prop="svcName"
        label="服务名"
        sortable
        width="300"
        header-align="center"
      />
      <el-table-column
        prop="interfaceName"
        label="接口名"
        sortable
        width="600"
        header-align="center"
      />
      <el-table-column label="查看分析" header-align="center">
        <template slot-scope="scope">
          <div style="display: flex; justify-content: center">
            <div class="centered-button">
              <el-button
                v-if="scope.row.interfaceName"
                size="mini"
                style="margin-left: -30px"
              >
                <!-- <router-link
                  :to="'/pro/viewStatistic/' + scope.row.interfaceName.replaceAll('/','ภ')"
                > -->
                <router-link :to="{ path: '/pro/viewStatistic/interface', query: {interfaceName: scope.row.interfaceName.replaceAll('/','ภ'), taskName: taskName}}">
                  查看
                </router-link>
              </el-button>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="proportion" label="请求量" header-align="center">
        <template slot-scope="scope">
          <div style="display: flex; justify-content: center">
            <div class="centered-button">
              <div style="margin-left: -30px">{{ scope.row.proportion }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- <el-table
      :data="tableData2"
      :span-method="objectSpanMethod"
      border
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="id" label="ID" width="180" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="amount1" label="数值 1（元）" />
      <el-table-column prop="amount2" label="数值 2（元）" />
      <el-table-column prop="amount3" label="数值 3（元）" />
    </el-table> -->
  </div>
</template>

<script src="./viewSvcInfo.js"></script>

<style>
#container {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
  color: rgb(228, 226, 226);
}

.validUrl {
  margin-left: 10px;
  cursor: pointer;
  text-decoration: none;
}
.centered-button {
  width: 100%;
  text-align: center;
}
.centered-button button {
  margin: 0 auto;
}
</style>
