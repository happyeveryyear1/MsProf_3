import RaddarChart from '../components/RaddarChartComparison'
import { cmp } from '@/api/quality/qproject'

export default {

  name: 'Comparison',
  components: {
    RaddarChart
  },
  data() {
    return {
      projs: [],
      projs_score: [],
      list: null,
      headers: [],
      listLoading: true,
      vs: false,
    }
  },
  created() {
    this.projs = this.$route.query.projs
  },
  mounted() {
    cmp({"projs": this.projs}).then(response => {
        let _list = []
        const projs = response.data['projects']
        let _headers = []
        for (const idx in projs[0]["quality_aspects"]) {
          _headers.push({ col: projs[0]["quality_aspects"][idx]["name"], prop: projs[0]["quality_aspects"][idx]["name"] })
        }
        this.headers = _headers
        for (const idx in projs){
          let _row = {}
          _row["name"] = projs[idx]["name"]
          const qas = projs[idx]["quality_aspects"]
          let v = []
          let s = 0
          let l = 0
          for (const idx in qas) {    
            _row[qas[idx]['name']] = qas[idx]['score']
            s = s + qas[idx]['score']
            l = l + 1
          }
          // _row['score'] = (Math.round(s / l) - 50) / 10.0
          _row['score'] = Math.round(s / l / 20.0 * 10)/10
          _list.push(_row)
        }
        this.list = _list
        this.listLoading = false
      })
      .catch(function(error) {
        console.log(error)
      })
  },
}