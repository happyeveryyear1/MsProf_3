package cn.enilu.flash.api.controller.quality;

import cn.enilu.flash.api.controller.BaseController;
import cn.enilu.flash.bean.vo.front.Rets;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/quality")
public class QualityController extends BaseController {

//    private final String BASEURI = "http://127.0.0.1:8090";
    private final String BASEURI = "http://localhost:8090";
//    private final String BASEURI = "http://39.104.118.163:8090";

    @RequestMapping(value = "/project", method = RequestMethod.GET)
    public Object getProjects(@RequestParam(required = false) String page,
    @RequestParam(required = false) String limit) {
        String REQUEST_URI = BASEURI + "/sqm/projects/";
        String requestUri = REQUEST_URI + "?page={page}&limit={limit}";
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("page", page);
        urlParameters.put("limit", limit);
        RestTemplate restT = new RestTemplate();
        Map quote = restT.getForObject(requestUri, Map.class, urlParameters);     
        return Rets.success(quote);
    }

    @RequestMapping(value = "/qsys", method = RequestMethod.GET)
    public Object getQsys(@RequestParam(required = false) String proj_id) {
        String REQUEST_URI = BASEURI + "/sqm/qualitysys/";
        String requestUri = REQUEST_URI + "{proj_id}";
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("proj_id", proj_id);
        RestTemplate restT = new RestTemplate();
        Map quote = restT.getForObject(requestUri, Map.class, urlParameters);     
        return Rets.success(quote);
    }

    @RequestMapping(value = "/qsys", method = RequestMethod.POST)
    public Object setQsys(@RequestParam(required = false) String proj_id,
    @RequestBody Map pdata) {
        String REQUEST_URI = BASEURI + "/sqm/qualitysys/";
        String requestUri = REQUEST_URI + "{proj_id}";
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("proj_id", proj_id);
        System.out.println(pdata);
        RestTemplate restT = new RestTemplate();
        Map quote = restT.postForObject(requestUri, pdata, Map.class, urlParameters); 
        return Rets.success(quote);
    }

    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public Object getVersions(@RequestParam(required = false) String proj_id,
    @RequestParam(required = false) String page,
    @RequestParam(required = false) String limit) {
        String REQUEST_URI = BASEURI + "/sqm/versions/";
        String requestUri = REQUEST_URI + "{proj_id}?page={page}&limit={limit}";
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("proj_id", proj_id);
        urlParameters.put("page", page);
        urlParameters.put("limit", limit);
        RestTemplate restT = new RestTemplate();
        Map quote = restT.getForObject(requestUri, Map.class, urlParameters);     
        return Rets.success(quote);
    }

    @RequestMapping(value = "/measure", method = RequestMethod.GET)
    public Object measure(@RequestParam(required = false) String proj_id,
    @RequestParam(required = false) String ver_tag) {
        String REQUEST_URI = BASEURI + "/sqm/measure/";
        String requestUri = REQUEST_URI + "{proj_id}/{ver_tag}";
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("proj_id", proj_id);
        urlParameters.put("ver_tag", ver_tag);
        RestTemplate restT = new RestTemplate();
        Map quote = restT.getForObject(requestUri, Map.class, urlParameters);   
        return Rets.success(quote);
    }

    @RequestMapping(value = "/projectcmp", method = RequestMethod.POST)
    public Object cmp(@RequestBody Map pdata) {
        String REQUEST_URI = BASEURI + "/sqm/projectcmp/";
        String requestUri = REQUEST_URI;
        RestTemplate restT = new RestTemplate();
        Map quote = restT.postForObject(requestUri, pdata, Map.class);     
        return Rets.success(quote);
    }

    @RequestMapping(value = "/aproject", method = RequestMethod.GET)
    public Object getProject(@RequestParam(required = false) String proj_id,
        @RequestParam(required = false) String ver_tag) {
        String REQUEST_URI = BASEURI + "/sqm/project/";
        String requestUri = REQUEST_URI + "{proj_id}/{ver_tag}";
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("proj_id", proj_id);
        urlParameters.put("ver_tag", ver_tag);
        RestTemplate restT = new RestTemplate();
        Map quote = restT.getForObject(requestUri, Map.class, urlParameters);      
        return Rets.success(quote);
    }

    @RequestMapping(value = "/aproject", method = RequestMethod.POST)
    public Object newProject(@RequestBody Map pdata){
//        @RequestParam(required = false) String projectName,
//        @RequestParam(required = false) String projectID,
//        @RequestParam(required = false) String sonarID) {
        String REQUEST_URI = BASEURI + "/sqm/aproject/";
        String requestUri = REQUEST_URI;
//        Map pdata = new HashMap<>();
//        pdata.put("proj_name",projectName);
//        pdata.put("proj_id_name",projectID);
//        pdata.put("proj_sonar_key",sonarID);
//        pdata.put("proj_language","");
//        pdata.put("proj_ptype","");
        RestTemplate restT = new RestTemplate();
        Map quote = restT.postForObject(requestUri, pdata, Map.class);      
        return Rets.success(quote);
    }

    @RequestMapping(value = "/aproject", method = RequestMethod.DELETE)
    public Object delProject(@RequestParam(required = false) String projectName) {
        String REQUEST_URI = BASEURI + "/sqm/dproject/";
        String requestUri = REQUEST_URI + "{projectName}";
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("projectName", projectName);
        RestTemplate restT = new RestTemplate();
        restT.delete(requestUri, urlParameters);
        return Rets.success();
    }


    @RequestMapping(value = "/task", method = RequestMethod.POST)
    public Object newTask(@RequestParam(required = false) String projectName,
        @RequestParam(required = false) String verTag) {
        String REQUEST_URI = BASEURI + "/sqm/atask/";
        String requestUri = REQUEST_URI ;
        Map pdata = new HashMap<>();
        pdata.put("proj_id_name",projectName);
        pdata.put("ver_tag",verTag);
        RestTemplate restT = new RestTemplate();
        Map quote = restT.postForObject(requestUri, pdata, Map.class);      
        return Rets.success(quote);
    }

    @RequestMapping(value = "/task", method = RequestMethod.DELETE)
    public Object delTask(@RequestParam(required = false) String projectName,
        @RequestParam(required = false) String verTag) {
        String REQUEST_URI = BASEURI + "/sqm/dtask/";
        String requestUri = REQUEST_URI + "{proj_id}/{ver_tag}";
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("proj_id", projectName);
        urlParameters.put("ver_tag", verTag);
        RestTemplate restT = new RestTemplate();
        restT.delete(requestUri, urlParameters);
        return Rets.success();
    }

    @RequestMapping(value = "/qaspect", method = RequestMethod.GET)
    public Object getQA(@RequestParam(required = false) String proj_id) {
        String REQUEST_URI = BASEURI + "/sqm/qualityaspects/";
        String requestUri = REQUEST_URI + "{proj_id}";
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("proj_id", proj_id);
        RestTemplate restT = new RestTemplate();
        Map quote = restT.getForObject(requestUri, Map.class, urlParameters);     
        return Rets.success(quote);
    }


    @RequestMapping(value = "/checkSonarId", method = RequestMethod.GET)
    public Object checkSonarId(@RequestParam(required = false) String sonarId) {
        String REQUEST_URI = BASEURI + "/sqm/checksonarid/";
        String requestUri = REQUEST_URI + "{sonarid}";
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("sonarid", sonarId);
        RestTemplate restT = new RestTemplate();
        Map quote = restT.getForObject(requestUri, Map.class, urlParameters);
        return Rets.success(quote);
    }

    @RequestMapping(value = "/testmetric", method = RequestMethod.POST)
    public Object testMetric(@RequestBody Map pdata){
        String REQUEST_URI = BASEURI + "/sqm/testmetric/";
        String requestUri = REQUEST_URI;
        RestTemplate restT = new RestTemplate();
        Map quote = restT.postForObject(requestUri, pdata, Map.class);      
        return Rets.success(quote);
    }

}

