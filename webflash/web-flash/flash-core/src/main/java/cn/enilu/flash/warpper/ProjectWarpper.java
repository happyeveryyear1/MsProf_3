package cn.enilu.flash.warpper;

import cn.enilu.flash.service.system.impl.ConstantFactory;

import java.util.List;
import java.util.Map;

public class ProjectWarpper extends BaseControllerWarpper {
    public ProjectWarpper(List<Map<String, Object>> list) {
        super(list);
    }

    @Override
    public void warpTheMap(Map<String, Object> map) {
        map.put("proName", ConstantFactory.me().getProNameById(((Long) map.get("projectName"))));
    }
}
