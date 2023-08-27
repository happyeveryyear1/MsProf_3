<template>
  <div class="app-container">
    <div class="block">
      <el-row :gutter="24">
        <el-col :span="4">
          <el-date-picker
            v-model="listQuery.beginTime"
            type="datetime"
            size="mini"
            placeholder="创建起始日期"
            value-format="yyyyMMddHHmmss"
            style="width: 100%;"
            default-time="00:00:00"
          />
        </el-col>
        <el-col :span="4">
          <el-date-picker
            v-model="listQuery.endTime"
            type="datetime"
            size="mini"
            placeholder="创建结束日期"
            value-format="yyyyMMddHHmmss"
            style="width: 100%;"
            default-time="23:59:59"
          />
        </el-col>
        <el-col :span="3">
          <el-input v-model="search_taskName" placeholder="任务名" size="mini" clearable style="width: 100%" />
        </el-col>
        <el-col :span="3">
          <el-select v-if="list" v-model="listQuery.versionNum" size="mini" placeholder="版本号" clearable>
            <el-option v-for="item in version_unique(list)" :key="item.id" :label="item.versionNum" :value="item.versionNum" />
          </el-select>
          <el-input v-else placeholder="版本号" size="mini" clearable />
        </el-col>
        <!-- <el-col :span="3">
          <el-select v-if="list" v-model="query_tester" placeholder="测试人员" clearable class="filter-item" size="mini">
            <el-option v-for="item in tester_unique(list)" :key="item.id" :label="item.testerName" :value="item.testerName" />
          </el-select>
          <el-input v-else placeholder="测试人员" size="mini" clearable />
        </el-col> -->
        <el-col :span="7">
          <el-button type="success" size="mini" icon="el-icon-search" @click.native="search">{{ $t('button.search') }}</el-button>
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click.native="reset">{{ $t('button.reset') }}</el-button>
        </el-col>
      </el-row>
      <br>
      <el-row>
        <el-button v-permission="['/project/testTask/:testactivityName/add']" type="success" size="mini" icon="el-icon-plus" @click.native="add">{{ $t('button.add') }}</el-button>
        <el-button v-permission="['/project/testTask/:testactivityName/edit']" type="primary" size="mini" icon="el-icon-edit" @click.native="edit">{{ $t('button.edit') }}</el-button>
        <el-button v-permission="['/project/testTask/:testactivityName/delete']" type="danger" size="mini" icon="el-icon-delete" @click.native="remove">{{ $t('button.delete') }}</el-button>
        <el-button v-permission="['/project/testTask/:testactivityName/execute']" type="success" size="mini" icon="el-icon-caret-right" style="float:right" @click.native="execute">{{ $t('button.execute') }}</el-button>
        <el-button v-permission="['/project/testTask/:testactivityName/execute']" type="success" size="mini" style="float:right">{{ $t('button.exeProcess') }}</el-button>
      </el-row>
      <br>
    </div>

    <el-table
      ref="singleTable"
      v-loading="listLoading"
      :data="list"
      element-loading-text="Loading"
      border
      fit
      highlight-current-row
      @current-change="handleCurrentChange"
    >
      <el-table-column type="expand">
        <template slot-scope="props">
          <el-form label-position="left" class="demo-table-expand">
            <el-row>
              <el-col :span="11">
                <el-form-item label="任务名：">
                  <span>{{ props.row.taskName }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="11">
                <el-form-item label="任务ID：">
                  <span>{{ props.row.id }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="11">
                <el-form-item label="测试活动名：">
                  <span>{{ params_testactivityName }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="11">
                <el-form-item label="项目名：">
                  <span>{{ projectName }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="11">
                <el-form-item label="版本号：">
                  <span>{{ props.row.versionNum }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="11">
                <el-form-item label="任务描述：">
                  <span>{{ props.row.taskIntroduction }}</span>
                </el-form-item>
              </el-col>
              <!-- <el-col :span="11">
                <el-form-item label="所填旧版本号：">
                  <span>{{ props.row.oldVersion }}</span>
                </el-form-item>
              </el-col> -->
            </el-row>
            <el-row>
              <el-col :span="11">
                <el-form-item label="任务创建时间：">
                  <span>{{ props.row.createTime }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="11">
                <el-form-item label="测试员：">
                  <span>{{ props.row.testerName }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <!-- <el-form-item label="测试机配置：">
                <span>{{ props.row.testConfiguration.replace(new RegExp(';','g'),'；').replace(new RegExp(',','g'),'/') }}</span>
              </el-form-item> -->
              <el-col :span="11">
                <el-form-item label="项目地址">
                  <span>{{ props.row.baseUrl }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <!-- <el-row>
              <el-form-item label="war包组件信息：">
                <span>{{ props.row.artifactInfos.replace(new RegExp(';','g'),'；').replace(new RegExp(',','g'),'/') }}</span>
              </el-form-item>
            </el-row>
            <el-row>
              <el-col :span="11">
                <el-form-item label="测试用例git地址：">
                  <span>{{ gitAddress }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="11">
                <el-form-item label="测试用例所在目录：">
                  <span>{{ props.row.directory }}</span>
                </el-form-item>
              </el-col>
            </el-row> -->
            <!-- <el-row>
              <el-col :span="11">
                <el-form-item label="任务执行状态：">
                  <div v-if="props.row.exeStatus === '-1'">
                    <el-tag type="info" style="margin-right:10px">未执行</el-tag>
                    <el-button v-permission="['/project/testTask/:testactivityName/execute']" type="success" icon="el-icon-caret-right" circle size="mini" :disabled="exeBtn" @click="exeTask(props.$index)" />
                  </div>
                  <div v-else-if="props.row.exeStatus === '1'">
                    <el-tag type="success">执行完成</el-tag>
                    <el-tooltip v-if="props.row.harStatus === '-1'" class="item" effect="dark" content="har分析未成功，请重试" placement="top-start" style="margin-left:10px">
                      <i class="el-icon-odometer" style="cursor:pointer;color:red" @click="reHar(props.row.taskName,props.$index)" />
                    </el-tooltip>
                    <span v-if="props.row.harStatus === '-1'" style="color:red">( har分析未成功，请重试 )</span>
                  </div>
                  <div v-else>
                    <el-tag type="primary" style="margin-right:10px">执行中</el-tag>
                    <i v-if="isRefresh_task[props.row.taskName]" class="el-icon-loading" />
                    <i v-else class="el-icon-refresh" style="cursor:pointer" @click="getTaskResult(props.row.taskName,props.$index)" />
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="11">
                <el-form-item label="任务执行完成时间：">
                  <span v-if="props.row.executionTime">{{ props.row.executionTime }}</span>
                  <span v-else>未执行完成</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="11">
                <el-form-item label="测试用例个数：">
                  <el-button type="text" size="mini" @click="viewTestCase(props.row.id)">{{ props.row.testcases }}</el-button>
                </el-form-item>
              </el-col>
              <el-col :span="11">
                <el-form-item label="用例执行结果：">
                  <router-link v-if="props.row.exeStatus==='1'" :to="'/project/testCase/'+props.row.taskName">
                    <el-button type="text">{{ props.row.testResult.split(',')[0] }}成功，{{ props.row.testResult.split(',')[1] }}失败</el-button>
                  </router-link>
                  <span v-else>用例未执行完成</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="11">
                <el-form-item label="性能瓶颈分析状态：">
                  <div v-if="props.row.analyseStatus === '-1'">
                    <el-tag type="info" style="margin-right:10px">未进行分析</el-tag>
                    <el-button type="success" icon="el-icon-caret-right" circle size="mini" @click="analyse(props.row.taskName,props.row.testcases,props.$index)" />
                  </div>
                  <el-button v-else-if="props.row.analyseStatus === '1'" size="mini" type="success" plain @click="getPerformanceResult(props.row.taskName)">分析完成</el-button>
                  <div v-else>
                    <el-tag type="primary" style="margin-right:10px">正在分析中</el-tag>
                    <i v-if="isRefresh_analyse[props.row.taskName]" class="el-icon-loading" />
                    <i v-else class="el-icon-refresh" style="cursor:pointer" @click="refresh(props.row.taskName,props.$index)" />
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="11">
                <el-form-item label="质量评估状态：">
                  <router-link v-if="props.row.evaluateStatus==='1'" :to="'/quality/report/'+projectId+'_v_'+props.row.taskName">
                    <el-button size="mini" type="success" plain>
                      评估完成
                    </el-button>
                  </router-link>
                  <el-button v-else-if="props.row.evaluateStatus==='0' && evaluateLoading" size="mini" plain :disabled="true">
                    等待中
                  </el-button>
                  <el-button v-else-if="props.row.evaluateStatus==='0' && evaluateLoading === false" size="mini" plain :disabled="true">
                    执行中
                  </el-button>
                  <div v-else-if="props.row.evaluateStatus==='-1'">
                    <el-tag type="info" style="margin-right:10px">未进行评估</el-tag><el-button type="success" icon="el-icon-caret-right" circle size="mini" @click="evaluate(props.row.taskName,props.$index)" />
                  </div>
                </el-form-item>
              </el-col>
            </el-row> -->
          </el-form>
        </template>
      </el-table-column>
      <el-table-column label="序号" min-width="50px" align="center">
        <template slot-scope="scope">
          {{ scope.$index+(listQuery.page-1)*listQuery.limit+1 }}
        </template>
      </el-table-column>
      <el-table-column label="任务名" :show-overflow-tooltip="true" min-width="100px" align="center">
        <template slot-scope="scope">
          {{ scope.row.taskName }}
        </template>
      </el-table-column>
      <el-table-column label="版本号" min-width="80px" align="center">
        <template slot-scope="scope">
          {{ scope.row.versionNum }}
        </template>
      </el-table-column>
      <el-table-column label="分析员" min-width="80px" align="center">
        <template slot-scope="scope">
          {{ scope.row.testerName }}
        </template>
      </el-table-column>
      <el-table-column label="任务简介" :show-overflow-tooltip="true" min-width="150px" header-align="center">
        <template slot-scope="scope">
          {{ scope.row.taskIntroduction }}
        </template>
      </el-table-column>
      <!-- <el-table-column label="用例信息" :show-overflow-tooltip="true" min-width="100px" align="center">
        <template slot-scope="scope">
          <el-button size="mini" @click="viewTestCase(scope.row.id)">
            {{ scope.row.clicked ? '查看' : '分析' }}
          </el-button>
        </template>
      </el-table-column> -->

      <el-table-column label="Har信息" min-width="100px" align="center">
        <template slot-scope="scope">
          <el-button v-if="scope.row.harAnalyzeStatus==='1'" size="mini">
            <router-link :to="'/pro/viewHar/'+scope.row.taskName">
              查看
            </router-link>
          </el-button>
          <el-button v-else size="mini" :disabled="isAnalyzeHaring" @click="analyzeHar(scope.row.taskName)">
            分析
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="Swagger信息" min-width="100px" align="center">
        <template slot-scope="scope">
          <el-button v-if="scope.row.swaggerAnalyzeStatus==='1'" size="mini">
            <router-link :to="'/pro/viewSwagger/'+scope.row.taskName">
              查看
            </router-link>
          </el-button>
          <el-button v-else size="mini" :disabled="isAnalyzeSwaggering" @click="analyzeSwagger(scope.row.taskName)">
            分析
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" :show-overflow-tooltip="true" min-width="100px" header-align="center">
        <template slot-scope="scope">
          {{ scope.row.createTime }}
        </template>
      </el-table-column>
      <el-table-column label="完成时间" :show-overflow-tooltip="true" min-width="100px" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.executionTime">{{ scope.row.executionTime }}</span>
          <span v-else>---</span>
        </template>
      </el-table-column>

      <el-table-column label="项目地址" :show-overflow-tooltip="true" min-width="100px" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.baseUrl">{{ scope.row.baseUrl }}</span>
          <span v-else>---</span>
        </template>
      </el-table-column>

      <el-table-column label="统计信息" min-width="100px" align="center">
        <template slot-scope="scope">
          <!-- <el-button v-if="scope.row.exeStatus==='1'" size="mini">
            <router-link :to="'/pro/viewSvc/'+scope.row.taskName">
              查看
            </router-link> -->
          <el-button size="mini">
            <router-link :to="'/pro/viewSvc/'+scope.row.taskName">查看</router-link>

          <!-- </el-button>
          <el-button v-else size="mini" :disabled="true">
            查看
          </el-button> -->
          <!-- <el-button size="mini">
            <router-link :to="'/pro/viewSvc/TEST'" >查看</router-link> -->
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="分析结果" min-width="100px" align="center">
        <template slot-scope="scope">
          <!-- <router-link v-if="scope.row.exeStatus==='1'" :to="'/project/testCase/'+scope.row.taskName">
            <el-button type="text" size="mini">{{ scope.row.testResult.split(',')[0] }}成功，{{ scope.row.testResult.split(',')[1] }}失败</el-button>
          </router-link>
          <el-button v-else size="mini" :disabled="true">
            查看
          </el-button> -->
          <!-- <el-button size="mini">
            <router-link :to="'/pro/viewAnalyzeInfo/test'" size="mini">查看</router-link>
          </el-button> -->
          <el-button size="mini">
            <router-link :to="'/pro/viewAnalyzeInfo/'+scope.row.taskName" size="mini">查看</router-link>
          </el-button>
        </template>
      </el-table-column>

    </el-table>

    <el-pagination
      background
      layout="total, sizes, prev, pager, next, jumper"
      :page-sizes="[10, 20, 50, 100,500]"
      :page-size="listQuery.limit"
      :total="total"
      style="margin-top:20px"
      @size-change="changeSize"
      @current-change="fetchPage"
      @prev-click="fetchPrev"
      @next-click="fetchNext"
    />

    <el-dialog :visible.sync="formVisible" :close-on-click-modal="false" :show-close="false" :title="formTitle" :close-on-press-escape="false" @close="closeDialog">
      <el-form ref="form" :model="form" label-width="120px" label-position="right" :rules="formRules">
        <el-row>
          <el-col :span="11">
            <el-form-item
              label="任务名："
              prop="taskName"
            >
              <el-input
                v-model="form.taskName"
                style="width:100%"
                :disabled="true"
              />
            </el-form-item>
          </el-col>

          <el-col :span="11">
            <el-form-item
              label="版本号："
              prop="versionNum"
            >
              <el-input
                v-model="form.versionNum"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="22">
            <el-form-item
              label="项目地址："
              prop="baseUrl"
            >
              <el-input
                v-model="form.baseUrl"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="11">
            <el-form-item
              label="活动名："
              prop="testactivityName"
            >
              <el-input
                v-model="params_testactivityName"
                style="width:100%"
                :disabled="true"
              />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item
              label="负责人员："
              prop="tester"
            >
              <el-input
                v-model="form.tester"
                style="width:100%"
                :disabled="true"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- <el-row>
          <el-col :span="11">
            <el-form-item
              label="war包："
              prop="war"
            >
              <el-input style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row> -->

        <el-row>
          <el-col :span="22">
            <el-form-item label="任务描述：" prop="taskIntroduction">
              <el-input
                v-model="form.taskIntroduction"
                style="width:100%"
                type="textarea"
                rows="3"
                placeholder="请输入任务简介信息"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <div v-for="(item, index) in form.dynamicItem" :key="index">
          <el-row>
            <el-col :span="11">
              <el-form-item
                label="ip地址："
                :prop="'dynamicItem.' + index + '.ipAddress'"
                :rules="[
                  { required: true, message: 'ip地址不能为空', trigger: 'blur' },
                  { pattern: /^((2(5[0-5]|[0-4]\d))|[0-1]?\d{1,2})(\.((2(5[0-5]|[0-4]\d))|[0-1]?\d{1,2})){3}$/, message: 'ip地址格式有误' }
                ]"
              >
                <el-autocomplete
                  v-model="item.ipAddress"
                  class="inline-input"
                  style="width:100%"
                  :fetch-suggestions="querySearch"
                  placeholder="请输入测试服务器IP地址"
                  @select="handleSelect"
                />
              </el-form-item>
            </el-col>
            <el-col :span="11">
              <el-form-item
                label="Browser："
                :prop="'dynamicItem.' + index + '.browser'"
                :rules="[{ required: true, message: '浏览器不能为空', trigger: 'blur' }]"
              >
                <el-select
                  v-model="item.browser"
                  placeholder="请选择browser"
                  style="width:100%"
                >
                  <el-option label="IE" value="IE" />
                  <el-option label="Chrome" value="Chrome" />
                  <el-option label="Firefox" value="Firefox" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="2" style="text-align:center; line-height:40px">
              <el-button type="danger" icon="el-icon-delete" size="mini" circle @click="deleteItem(item, index)" />
            </el-col>
          </el-row>
        </div>

        <div v-if="cantEditCase" style="text-align:center;color:red;margin-bottom:30px">提示：任务已执行，不可更改测试用例</div>

        <el-row v-show="false">
          <el-col :span="22">
            <el-form-item label="用例集合：" style="margin-top:30px" prop="testcase">
              <el-tree
                ref="tree"
                :data="testList_tree_all"
                :show-checkbox="showCheckBox"
                node-key="id"
                class="permission-tree"
                default-expand-all
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div style="text-align: left; width: 150px;display:inline; margin-left:3px; visibility: hidden">
        <el-button type="primary" style="margin-left:0px;margin-bottom:20px" :disabled="cantEditCase" @click="importAllTestCase">导入用例</el-button>
      </div>
      <div style="float: right; margin-right: 60px; display:inline">

        <el-button v-show="isAdd" type="text" @click="clearForm">清空</el-button>
        <el-button v-show="isAdd" type="text" style="margin-right:20px" @click="saveLocal">保存</el-button>
        <el-button slot="reference" type="primary" :disabled="submitBtn" @click="save">提交</el-button>
        <el-button type="danger" :disabled="cancelBtn" @click.native="formVisible = false">{{ $t('button.cancel') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="caseVisible" title="用例列表" :close-on-press-escape="false">
      <el-tree
        ref="viewAllCase"
        :data="viewCase"
        class="permission-tree"
        default-expand-all
      />
    </el-dialog>

    <el-dialog
      :title="performanceResultTitle"
      :visible.sync="performanceResultVisible"
      width="70%"
    >
      <el-table
        :data="mapData"
        element-loading-text="Loading"
        border
        fit
        highlight-current-row
      >
        <el-table-column label="序号" min-width="50px" align="center">
          <template slot-scope="scope">
            {{ scope.$index+1 }}
          </template>
        </el-table-column>
        <el-table-column label="方法名" min-width="450px" header-align="center">
          <template slot-scope="scope">
            {{ scope.row.functionName }}
          </template>
        </el-table-column>
        <el-table-column label="瓶颈置信率" min-width="150px" align="center">
          <template slot-scope="scope">
            {{ scope.row.rate }}
          </template>
        </el-table-column>
      </el-table>

    </el-dialog>

    <el-dialog
      :title="functionResultTitle"
      :visible.sync="functionResultVisible"
      width="70%"
    >
      <el-table
        :data="mapData"
        element-loading-text="Loading"
        border
        fit
        highlight-current-row
      >
        <el-table-column label="序号" min-width="50px" align="center">
          <template slot-scope="scope">
            {{ scope.$index+1 }}
          </template>
        </el-table-column>
        <el-table-column label="方法名" min-width="450px" header-align="center">
          <template slot-scope="scope">
            {{ scope.row.functionName }}
          </template>
        </el-table-column>
        <el-table-column label="功能异常置信率" min-width="150px" align="center">
          <template slot-scope="scope">
            {{ scope.row.rate }}
          </template>
        </el-table-column>
      </el-table>

    </el-dialog>
  </div>
</template>

<script src="./testTask.js"></script>

<style rel="stylesheet/scss" lang="scss">
    @import "src/styles/common.scss";
    .permission-tree {
    height: 200px;
    width: 100%;
    border:1px solid rgb(214, 214, 214);
    border-radius: 4px;
    overflow: auto;
  }
  .el-tree-node__label {
    font-size: 14px;
    margin-left: 8px;
}
</style>

