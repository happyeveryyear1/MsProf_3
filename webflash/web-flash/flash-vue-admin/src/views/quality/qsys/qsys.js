import { getQsys, setQsys, testMetric } from '@/api/quality/qsys'

let id = 1;

export default {
  props: {
    proj_id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      data: null,
      check: {},
      value: false,
      selected: null,
      dialogVisible: false,
      name: null,
      radio: null,
      textarea_datahandler: null,
      textarea_datasource_path: null,
      textarea_description: null,
      radio_datasource_path_type: null,
      radio_datasource_format: null,
      is_disabled: true,
      is_metric: false,
      btn_title: "编辑",
      defaultProps: {
        children: 'children',
        label: 'label',
        status: 'status'
      }
    }
  },
  created() {
    this.proj_id = this.$route.params && this.$route.params.id
    this.getData(this.proj_id)
  },
  methods: {
    getData() {
      let queryData = {}
      queryData['proj_id'] = this.proj_id
      getQsys(queryData).then(response => {
        const cdata = response.data
        const qas = cdata['quality_aspects']
        let tdata = []
        this.records = {}
        for (const idx in qas) {
          const t_label_1 = qas[idx]['name']
          let tdata_1 = { id: id, label: t_label_1, status: qas[idx]['status']===1, description: qas[idx]['description'], children: [] }
          id = id + 1
          for (const idx2 in qas[idx]['metrics']) {
            const t_label_2 = qas[idx]['metrics'][idx2]['name']
            console.log(t_label_2)
            this.check[t_label_2] = 0
            tdata_1.children.push({ 
              id: id,
              label: t_label_2,
              status: qas[idx]['metrics'][idx2]['status']===1,
              description: qas[idx]['metrics'][idx2]['description'],
              data_source_path: qas[idx]['metrics'][idx2]['data_source_path'],
              data_source_path_type: qas[idx]['metrics'][idx2]['data_source_path_type'],
              data_source_format: qas[idx]['metrics'][idx2]['data_source_format'],
              handler_path: qas[idx]['metrics'][idx2]['handler_path']
            })
            id = id + 1
          }
          //tdata_1.children.push({ id: id, label: '...' })
          //id = id + 1
          tdata.push(tdata_1)
        }
        //tdata.push({ id: id, label: '...' })
        //id = id + 1
        this.data = tdata
      })
      .catch(function(error) {
        console.log(error)
      })
    },
    append(data) {
      const newChild = { id: id++, label: 'testtest', children: [] };
      if (!data.children) {
        this.$set(data, 'children', []);
      }
      data.children.push(newChild);
    },

    remove(node, data) {
      const parent = node.parent;
      const children = parent.data.children || parent.data;
      const index = children.findIndex(d => d.id === data.id);
      children.splice(index, 1);
    },
    select(data) {
      this.dialogVisible = true
      this.name = data.label
      this.selected = data
      if (this.selected.hasOwnProperty('children')) {
        this.is_metric = false
      }
      else {
        this.is_metric = true
      }
      this.textarea_description = data.description
      this.textarea_datasource_path = data.data_source_path
      this.radio_datasource_path_type = data.data_source_path_type
      this.radio_datasource_format = data.data_source_format
      if (data.handler_path === "") {
        this.textarea_datahandler = "内置"
      }
      else {
        this.textarea_datahandler = data.handler_path
      }
    },
    handleStatChange(node, data){
      let _status = false
      if (data.hasOwnProperty('children')) {
        let _status = data.status
        let flag = true
        for(let child in data.children) {
          if(this.check[data.children[child].label]!="success"){
            this.$notify({
              title: '提示',
              message: '度量项未测试或未通过测试，不能开启',
            });
            flag = false
          }
        }
        for(let child in data.children) {
          data.children[child].status = _status && flag
        }
        data.status = data.status && flag
      }
      else {
        let flag = true
        if(this.check[data.label]!="success"){
          this.$notify({
            title: '提示',
            message: '度量项未测试或未通过测试，不能开启',
          });
          flag = false
        }
        data.status = data.status && flag
        for(let child in node.parent.data.children) {
          if (node.parent.data.children[child].status) {
            _status = true
          }
        }
        node.parent.data.status = _status
      }
    },
    save(){
      this.$notify({
        title: '提示',
        message: '正在保存中',
      });
      let pdata = {"quality_aspects": []}
      const data = this.data
      for (let idx in data) {
        if (data[idx].label === '...') {
          continue
        }
        let pdata1 = { name: data[idx].label, status: data[idx].status, description: data[idx].description, metrics: [] }
        let tdata = data[idx]['children']
        for (let idx2 in tdata) {
          if (tdata[idx2].label === '...') {
            continue
          }
          let pdata2 = {
            name: tdata[idx2].label, status: tdata[idx2].status, description: tdata[idx2].description,
            data_source_path: tdata[idx2].data_source_path,
            data_source_path_type: tdata[idx2].data_source_path_type,
            data_source_format: tdata[idx2].data_source_format,
            handler_path: tdata[idx2].handler_path
          }
          pdata1.metrics.push(pdata2)
        }
        pdata.quality_aspects.push(pdata1)
      }
      let queryData = {}
      queryData['proj_id'] = this.proj_id
      setQsys(queryData,pdata).then(response => {
        const message = response.data['message']
        if (message === 'OK') {
          this.$notify({
            title: '提示',
            message: '保存成功',
          });
          this.btn_title = "编辑"
          this.is_disabled = true           
        }
        else {
          this.$notify({
            title: '提示',
            message: '保存失败',
          });
        }
      })
      .catch(function(error) {
        console.log(error)
        this.$notify({
          title: '提示',
          message: '保存失败',
        });
      })
       
    },
    edit(){
      this.btn_title = "编辑中"
      this.is_disabled = false
    },
    local_save(){
      // this.dialogVisible = false
      let tid = this.selected.id
      console.log(tid)
      // if(! this.name in this.check){
      //   this.$notify({
      //     title: '提示',
      //     message: '未测试',
      //   });
      //   return
      // }
      // if(this.check[this.name]!="success"){
      //   this.$notify({
      //     title: '提示',
      //     message: '测试未通过',
      //   });
      //   return
      // }
      this.check[this.name]="failed"
      this.$notify({
        title: '提示',
        message: '请重新测试',
      }); 
      for (let idx in this.data) {
        if (this.data[idx].id === tid) {
          this.data[idx].label = this.name
          this.data[idx].description = this.textarea_description
        }
        for (let idx2 in this.data[idx].children) {
          if (this.data[idx].children[idx2].id === tid) {
            console.log(this.data[idx].children[idx2].label)
            this.data[idx].children[idx2].label = this.name
            this.data[idx].children[idx2].description = this.textarea_description
            this.data[idx].children[idx2].data_source_path = this.textarea_datasource_path
            this.data[idx].children[idx2].data_source_path_type = this.radio_datasource_path_type
            this.data[idx].children[idx2].data_source_format =  this.radio_datasource_format
            if (this.textarea_datahandler === "内置"){
              this.data[idx].children[idx2].handler_path = ""
            }
            else{
              this.data[idx].children[idx2].handler_path =  this.textarea_datahandler  
            }      
          }
        }
      }
    },
    cancel(){
      this.dialogVisible = false
    },
    test_metric(){
      let pdata = {
        metric_name: this.name,
        project_id: this.proj_id,
        data_source_path: this.textarea_datasource_path,
        data_source_path_type: this.radio_datasource_path_type,
        data_source_format: this.radio_datasource_format,
        handler_path: this.textarea_datahandler
      }
      let queryData = {}
      testMetric(queryData,pdata).then(response => {
        const message = response.data['msg']
        this.check[this.name] = message
        if (message === 'success') {
          this.$notify({
            title: '提示',
            message: '通过',
          });         
        }
        else {
          this.$notify({
            title: '提示',
            message: '不通过',
          });
        }
      })
      .catch( error => {
        console.log(error)
        this.$notify({
          title: '提示',
          message: '不通过',
        });
      })
    }
  }
};


