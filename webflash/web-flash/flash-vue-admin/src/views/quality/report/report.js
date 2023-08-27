import RaddarChart from '../components/RaddarChart'
import PanelGroup from '../components/PanelGroup'
import Tree from '../components/Tree'
import LineChart from '../components/LineChart'

export default {

  name: 'DashboardAdmin',
  components: {
    PanelGroup,
    RaddarChart,
    Tree,
    LineChart
  },
  data() {
    return {
      qualityaspect: null,
      proj_id: null,
      ver_tag: null
    }
  },
  created() {
    const pm = this.$route.params && this.$route.params.id
    const pms = pm.split('_v_')
    this.proj_id = pms[0]
    this.ver_tag = pms[1]
  },
  methods: {
    handleSetLineChartData(qan) {
      this.qualityaspect = qan
    }
  }
}