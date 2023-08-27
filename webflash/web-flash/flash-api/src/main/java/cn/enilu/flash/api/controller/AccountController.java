package cn.enilu.flash.api.controller;

import cn.enilu.flash.api.utils.ApiConstants;
import cn.enilu.flash.bean.core.ShiroUser;
import cn.enilu.flash.bean.entity.system.User;
import cn.enilu.flash.bean.vo.front.Rets;
import cn.enilu.flash.core.log.LogManager;
import cn.enilu.flash.core.log.LogTaskFactory;
import cn.enilu.flash.dao.system.UserRepository;
import cn.enilu.flash.security.ShiroFactroy;
import cn.enilu.flash.service.system.UserService;
import cn.enilu.flash.utils.*;
import org.nutz.mapl.Mapl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Null;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.enilu.flash.utils.HttpUtil.getRequest;

/**
 * AccountController
 *
 * @author enilu
 * @version 2018/9/12 0012
 */
@RestController
@RequestMapping("/account")
public class AccountController extends BaseController{
    private Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final int ALLCOUNT = 5;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Resource
    private EhCacheCacheManager cacheManager;
    /**
     * 用户登录<br>
     * 1，验证没有注册<br>
     * 2，验证密码错误<br>
     * 3，登录成功
     * @param userName
     * @param password
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public Object login(@RequestParam("username") String userName,
                        @RequestParam("password") String password,
                        @RequestParam("verifyCode") String verifyCode,
                        HttpServletResponse response){
        if(verifyCode == null || !verifyCodeCheck(verifyCode)){
            return Rets.failure("验证码错误");
        }
        Cache passwordRetryCache = cacheManager.getCache("passwordRetryCache");
        try {
            //1, 用户名不存在
            User user = userService.findByAccount(userName);
            if (user == null) {
                return Rets.failure("用户名或密码错误!");
            }

            // 获取缓存中的登录次数
            SimpleValueWrapper retryCountWrapper = (SimpleValueWrapper) passwordRetryCache.get(userName);
            AtomicInteger retryCount;
            if (retryCountWrapper == null) {
                retryCount= new AtomicInteger(0);
                passwordRetryCache.put(userName,retryCount);
            }else {
                retryCount=(AtomicInteger) retryCountWrapper.get();
            }
            // 登录次数大于5，锁定账号
            if (retryCount.get() > ALLCOUNT) {
                logger.info("用户:{},锁定！", userName);
                LogManager.me().executeLog(LogTaskFactory.loginLog_f(user.getId(), "锁定中", HttpUtil.getIp()));
                return Rets.failure("账号锁定中，等5分钟再来吧");
            }

            String decryptPassword = AESUtil.desEncrypt(password).trim();
            String passwdMd5 = MD5.md5(decryptPassword, user.getSalt());
            //2, 密码错误，重试次数增一
            if (!user.getPassword().equals(passwdMd5)) {
                LogManager.me().executeLog(LogTaskFactory.loginLog_f(user.getId(), "密码错误", HttpUtil.getIp()));
                if (retryCount.incrementAndGet() > ALLCOUNT) {
                    logger.info("用户:{},锁定！", userName);
                    return Rets.failure("用户名或密码错误! (当前账号已超过5次, 锁定5分钟)");
                }
                if (retryCount.get()>2)
                    return Rets.failure("用户名或密码错误! (剩余次数："+(ALLCOUNT-retryCount.get()+1)+")");
                else
                    return Rets.failure("用户名或密码错误!");
            }
            passwordRetryCache.evict(userName);
            if(user.getRoleid()==null){
                LogManager.me().executeLog(LogTaskFactory.loginLog_f(user.getId(), "该用户未配置角色权限", HttpUtil.getIp()));
                return Rets.failure("该用户未配置角色权限");
            }
            if(user.getStatus()==2){
                LogManager.me().executeLog(LogTaskFactory.loginLog_f(user.getId(), "该用户已被冻结", HttpUtil.getIp()));
                return Rets.failure("该用户已被冻结");
            }
            if(user.getExpireday()==null || user.getExpireday().compareTo(new Date())<0){
                LogManager.me().executeLog(LogTaskFactory.loginLog_f(user.getId(), "密码已失效", HttpUtil.getIp()));
                return Rets.failure("密码已失效");
            }

            if (userService.kickOutAll(user)){
                logger.info(">>> >>> >>> 踢出不同浏览器登陆的用户", userName);
            }else {
                logger.info(">>> >>> >>> 不用踢了");
            }
            String token = userService.loginForToken(user);
            Map<String, String> result = new HashMap<>(1);
            result.put("token", token);
            LogManager.me().executeLog(LogTaskFactory.loginLog(user.getId(), HttpUtil.getIp()));
            return Rets.success(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            LogManager.me().executeLog(LogTaskFactory.loginLog_f(-1l, "登录异常", HttpUtil.getIp()));
            return Rets.failure("登录时失败"+e.getMessage());
        }
    }

    @RequestMapping(value = "/login_reset",method = RequestMethod.POST)
    public Object loginReset(@RequestParam("username") String userName,
                             @RequestParam("oldPassword") String password,
                             @RequestParam("newPassword") String newPassword,
                             @RequestParam("newPasswordConfirm") String newPassword2,
                             @RequestParam("verifyCode") String verifyCode,
                             HttpServletRequest request,
                             HttpServletResponse response){
        if(verifyCode == null || !verifyCodeCheck(verifyCode)){
            return Rets.failure("验证码错误");
        }
        Cache passwordRetryCache = cacheManager.getCache("passwordRetryCache");
        try {
            //1, 用户名不存在
            User user = userService.findByAccount(userName);
            if (user == null) {
                return Rets.failure("用户名或密码错误!");
            }

            // 获取缓存中的登录次数
            SimpleValueWrapper retryCountWrapper = (SimpleValueWrapper) passwordRetryCache.get(userName);
            AtomicInteger retryCount;
            if (retryCountWrapper == null) {
                retryCount= new AtomicInteger(0);
                passwordRetryCache.put(userName,retryCount);
            }else {
                retryCount=(AtomicInteger) retryCountWrapper.get();
            }
            // 登录次数大于5，锁定账号
            if (retryCount.get() > ALLCOUNT) {
                logger.info("用户:{},锁定！", userName);
                LogManager.me().executeLog(LogTaskFactory.loginLog_f(user.getId(), "锁定中", HttpUtil.getIp()));
                return Rets.failure("账号锁定中，等5分钟再来吧");
            }

            String decryptPassword = AESUtil.desEncrypt(password).trim();
            String passwdMd5 = MD5.md5(decryptPassword, user.getSalt());
            //2, 密码错误，重试次数增一
            if (!user.getPassword().equals(passwdMd5)) {
                LogManager.me().executeLog(LogTaskFactory.loginLog_f(user.getId(), "密码错误", HttpUtil.getIp()));
                if (retryCount.incrementAndGet() > ALLCOUNT) {
                    logger.info("用户:{},锁定！", userName);
                    return Rets.failure("用户名或密码错误! (当前账号已超过5次, 锁定5分钟)");
                }
                if (retryCount.get()>2)
                    return Rets.failure("用户名或密码错误! (剩余次数："+(ALLCOUNT-retryCount.get()+1)+")");
                else
                    return Rets.failure("用户名或密码错误!");
            }
            passwordRetryCache.evict(userName);
            if(user.getRoleid()==null){
                LogManager.me().executeLog(LogTaskFactory.loginLog_f(user.getId(), "该用户未配置角色权限", HttpUtil.getIp()));
                return Rets.failure("该用户未配置角色权限");
            }
            if(user.getStatus()==2){
                LogManager.me().executeLog(LogTaskFactory.loginLog_f(user.getId(), "登录异常", HttpUtil.getIp()));
                return Rets.failure("该用户已被冻结");
            }
            if (userService.kickOutAll(user)){
                logger.info(">>> >>> >>> 踢出不同浏览器登陆的用户", userName);
            }else {
                logger.info(">>> >>> >>> 不用踢了");
            }


            // reset password start
            String decryptOldPassword = AESUtil.desEncrypt(password).trim();
            String decryptNewPassword = AESUtil.desEncrypt(newPassword).trim();
            String decryptRePassword = AESUtil.desEncrypt(newPassword2).trim();
            if(StringUtil.isEmpty(decryptNewPassword) || StringUtil.isEmpty(decryptRePassword)){
                return Rets.failure("密码不能为空");
            }
            if(!decryptNewPassword.equals(decryptRePassword)){
                return Rets.failure("新密码前后不一致");
            }
            if(!MD5.md5(decryptOldPassword, user.getSalt()).equals(user.getPassword())){
                return Rets.failure("旧密码输入错误");
            }
//            if (user.getAccount().equals("intellitest")) {
                if (decryptPassword.length() < 8 || decryptPassword.length() > 20) {
                    return Rets.failure("账号密码长度在8到20个字符之间");
                } else {
                    String regex = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[~!@&%$^\\(\\)#_<>])[0-9a-zA-Z~!@&%$^\\(\\)#_<>]{8,20}$";
                    boolean res = decryptPassword.matches(regex);
                    if (!res) {
                        return Rets.failure("必须包含字母、数字、特殊字符(~!@&%$^()#_<>)");
                    }
                }
//            } else {
//                if (decryptPassword.length() < 6 || decryptPassword.length() > 20) {
//                    return Rets.failure("长度在6到20个字符之间");
//                } else {
//                    String regex = "^(?=.*[0-9])(?=.*[a-zA-Z])[0-9a-zA-Z~!@&%$^\\(\\)#_<>]{6,20}$";
//                    boolean res = decryptPassword.matches(regex);
//                    if (!res) {
//                        return Rets.failure("必须包含数字和字母");
//                    }
//                }
//            }
            user.setPassword(MD5.md5(decryptNewPassword, user.getSalt()));
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DAY_OF_MONTH, 90);
            user.setExpireday(now.getTime());
            userRepository.saveAndFlush(user);
            // reset password end

            String token = userService.loginForToken(user);
            Map<String, String> result = new HashMap<>(1);

            result.put("token", token);
            LogManager.me().executeLog(LogTaskFactory.loginLog(user.getId(), HttpUtil.getIp()));

            return Rets.success(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            LogManager.me().executeLog(LogTaskFactory.loginLog_f(-1l, "登录异常", HttpUtil.getIp()));
            return Rets.failure("登录时失败"+e.getMessage());
        }
    }

    @RequestMapping(value = "/info",method = RequestMethod.GET)
    public Object info( ){
        HttpServletRequest request = getRequest();
        Long idUser = null;
        try {
             idUser = getIdUser(request);
        }catch (Exception e){
            return Rets.expire();
        }
        if(idUser!=null){
            User user =  userService.get(idUser);
            if(StringUtil.isEmpty(user.getRoleid())){
                return Rets.failure("该用户未配置权限");
            }
            ShiroUser shiroUser = ShiroFactroy.me().shiroUser(user);
            Map map = Maps.newHashMap("name",user.getName(),"role","admin","roles", shiroUser.getRoleCodes());
            map.put("permissions",shiroUser.getUrls());
            Map profile = (Map) Mapl.toMaplist(user);
            profile.put("dept",shiroUser.getDeptName());
            profile.put("roles",shiroUser.getRoleNames());
            map.put("profile",profile);

            return Rets.success(map);
        }
        return Rets.failure("获取用户信息失败");
    }
    @RequestMapping(value = "/updatePwd",method = RequestMethod.POST)
    public Object updatePwd( String oldPassword,String password, String rePassword){
        try {
            String decryptOldPassword = AESUtil.desEncrypt(oldPassword).trim();
            String decryptPassword = AESUtil.desEncrypt(password).trim();
            String decryptRePassword = AESUtil.desEncrypt(rePassword).trim();
            if(StringUtil.isEmpty(decryptPassword) || StringUtil.isEmpty(decryptRePassword)){
                return Rets.failure("密码不能为空");
            }
            if(!decryptPassword.equals(decryptRePassword)){
                return Rets.failure("新密码前后不一致");
            }
            User user = userService.get(getIdUser(getRequest()));
//            if(ApiConstants.ADMIN_ACCOUNT.equals(user.getAccount())){
//                return Rets.failure("不能修改超级管理员密码");
//            }
            if(!MD5.md5(decryptOldPassword, user.getSalt()).equals(user.getPassword())){
                return Rets.failure("旧密码输入错误");
            }

//            if (user.getAccount().equals("intellitest")) {
                if (decryptPassword.length() < 8 || decryptPassword.length() > 20) {
                    return Rets.failure("账号密码长度在8到20个字符之间");
                } else {
                    String regex = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[~!@&%$^\\(\\)#_<>])[0-9a-zA-Z~!@&%$^\\(\\)#_<>]{8,20}$";
                    boolean res = decryptPassword.matches(regex);
                    if (!res) {
                        return Rets.failure("必须包含字母、数字、特殊字符(~!@&%$^()#_<>)");
                    }
                }
//            } else {
//                if (decryptPassword.length() < 6 || decryptPassword.length() > 20) {
//                    return Rets.failure("长度在6到20个字符之间");
//                } else {
//                    String regex = "^(?=.*[0-9])(?=.*[a-zA-Z])[0-9a-zA-Z~!@&%$^\\(\\)#_<>]{6,20}$";
//                    boolean res = decryptPassword.matches(regex);
//                    if (!res) {
//                        return Rets.failure("必须包含数字和字母");
//                    }
//                }
//            }

            user.setPassword(MD5.md5(decryptPassword, user.getSalt()));
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DAY_OF_MONTH, 90);
            user.setExpireday(now.getTime());
            userService.update(user);
            return Rets.success();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Rets.failure("更改密码失败");
    }


    private boolean verifyCodeCheck(String code){
        String serverCheckcode = (String) HttpUtil.getRequest().getSession().getAttribute("checkcode");
        if(code != null){
            HttpUtil.getRequest().getSession().removeAttribute("checkcode");
            return code.equals(serverCheckcode);
        }else {
            return false;
        }
    }
}
