package cn.enilu.flash.api.controller.system;

import cn.enilu.flash.api.controller.BaseController;
import cn.enilu.flash.bean.constant.Const;
import cn.enilu.flash.bean.constant.factory.PageFactory;
import cn.enilu.flash.bean.constant.state.ManagerStatus;
import cn.enilu.flash.bean.core.BussinessLog;
import cn.enilu.flash.bean.dictmap.UserDict;
import cn.enilu.flash.bean.dto.UserDto;
import cn.enilu.flash.bean.entity.project.projectList;
import cn.enilu.flash.bean.entity.system.User;
import cn.enilu.flash.bean.enumeration.BizExceptionEnum;
import cn.enilu.flash.bean.enumeration.Permission;
import cn.enilu.flash.bean.exception.ApplicationException;
import cn.enilu.flash.bean.vo.front.Rets;
import cn.enilu.flash.bean.vo.query.SearchFilter;
import cn.enilu.flash.core.factory.UserFactory;
import cn.enilu.flash.service.project.projectListService;
import cn.enilu.flash.service.system.UserService;
import cn.enilu.flash.utils.*;
import cn.enilu.flash.utils.factory.Page;
import cn.enilu.flash.warpper.UserWarpper;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * UserController
 *
 * @author enilu
 * @version 2018/9/15 0015
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private projectListService projectListService;
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @RequiresPermissions(value = {Permission.USER})
    public Object list(@RequestParam(required = false) String account,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String idDept){
        Page page = new PageFactory().defaultPage();
        if(StringUtil.isNotEmpty(name)){
            String decode_name = URLDecoder.decode(name);
            page.addFilter( "name", SearchFilter.Operator.LIKE, decode_name);
        }
        if(StringUtil.isNotEmpty(account)) {
            String decode_account = URLDecoder.decode(account);
            page.addFilter("account", SearchFilter.Operator.LIKE, decode_account);
        }
        page.addFilter( "status",SearchFilter.Operator.GT,0);
        page = userService.queryPage(page);
        List list = (List) new UserWarpper(BeanUtil.objectsToMaps(page.getRecords())).warp();
        page.setRecords(list);
        return Rets.success(page);
    }
//    @RequestMapping(method = RequestMethod.POST)
//    @BussinessLog(value = "编辑账号", key = "name", dict = UserDict.class)
//    @RequiresPermissions(value = {Permission.USER_EDIT})
//    public Object save( @Valid UserDto user,BindingResult result) throws Exception {
//        if(user.getId()==null) {
//            // 判断账号是否重复
//            User theUser = userService.findByAccount(user.getAccount());
//            if (theUser != null) {
//                throw new ApplicationException(BizExceptionEnum.USER_ALREADY_REG);
//            }
//            // 完善账号信息
//            String decryptPassword = AESUtil.desEncrypt(user.getPassword()).trim();
//            user.setSalt(RandomUtil.getRandomString(5));
////            user.setPassword(MD5.md5(user.getPassword(), user.getSalt()));
//            user.setPassword(MD5.md5(decryptPassword, user.getSalt()));
////            user.setStatus(ManagerStatus.OK.getCode());
//            userService.insert(UserFactory.createUser(user, new User()));
//        }else{
//            User oldUser = userService.get(user.getId());
//            userService.update(UserFactory.updateUser(user,oldUser));
//        }
//        return Rets.success();
//    }

    @RequestMapping(value = "/addUser",method = RequestMethod.POST)
    @BussinessLog(value = "新增账号", key = "name", dict = UserDict.class)
    @RequiresPermissions(value = {Permission.USER_ADD})
    public Object add( @Valid UserDto user,BindingResult result) throws Exception {
        // 判断账号是否重复
        User theUser = userService.findByAccount(user.getAccount());
        if (theUser != null) {
            throw new ApplicationException(BizExceptionEnum.USER_ALREADY_REG);
        }
        // 完善账号信息
        String decryptPassword = AESUtil.desEncrypt(user.getPassword()).trim();

//        if(user.getAccount().equals("intellitest")){
            if(decryptPassword.length()<8 || decryptPassword.length()>20){
                return Rets.failure("账号密码长度在8到20个字符之间");
            }else {
                String regex = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[~!@&%$^\\(\\)#_<>])[0-9a-zA-Z~!@&%$^\\(\\)#_<>]{8,20}$";
                boolean res = decryptPassword.matches(regex);
                if(!res){
                    return Rets.failure("必须包含字母、数字、特殊字符(~!@&%$^()#_<>)") ;
                }
            }
//        }else {
//            if(decryptPassword.length()<6 || decryptPassword.length()>20){
//                return Rets.failure("长度在6到20个字符之间");
//            }else {
//                String regex = "^(?=.*[0-9])(?=.*[a-zA-Z])[0-9a-zA-Z~!@&%$^\\(\\)#_<>]{6,20}$";
//                boolean res = decryptPassword.matches(regex);
//                if(!res){
//                    return Rets.failure("必须包含数字和字母") ;
//                }
//            }
//        }

        user.setSalt(RandomUtil.getRandomString(5));
        user.setPassword(MD5.md5(decryptPassword, user.getSalt()));

        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, 90);
        user.setExpireday(now.getTime());

        userService.insert(UserFactory.createUser(user, new User()));

        return Rets.success();
    }

    @RequestMapping(value = "/editUser",method = RequestMethod.POST)
    @BussinessLog(value = "编辑账号", key = "name", dict = UserDict.class)
    @RequiresPermissions(value = {Permission.USER_EDIT})
    public Object edit( @Valid UserDto user,BindingResult result) throws Exception {

        User oldUser = userService.get(user.getId());
        if(!user.getAccount().equals(oldUser.getAccount())){
            return Rets.failure("账号不可修改");
        }

        String decryptPassword = AESUtil.desEncrypt(user.getPassword()).trim();

        if(decryptPassword.length()>0) {
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
            user.setSalt(RandomUtil.getRandomString(5));
            user.setPassword(MD5.md5(decryptPassword, user.getSalt()));
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DAY_OF_MONTH, 90);
            user.setExpireday(now.getTime());
        }else {
            user.setExpireday(oldUser.getExpireday());
            user.setSalt(oldUser.getSalt());
            user.setPassword(oldUser.getPassword());
        }

        userService.update(UserFactory.updateUser(user,oldUser));
        return Rets.success();
    }

    @BussinessLog(value = "删除账号", key = "userId", dict = UserDict.class)
    @RequestMapping(method = RequestMethod.DELETE)
    @RequiresPermissions(value = {Permission.USER_DEL})
    public Object remove(@RequestParam Long userId){
        if (userId==null) {
            throw new ApplicationException(BizExceptionEnum.REQUEST_NULL);
        }
        if(userId.intValue()<=2){
            return Rets.failure("不能删除初始用户");
        }
//        User user = userService.get(userId);
//        user.setStatus(ManagerStatus.DELETED.getCode());
//        userService.update(user);

        List<projectList>proList = projectListService.queryAll(SearchFilter.build("projectLeader", SearchFilter.Operator.EQ, userId));
        if(proList.size() == 0){
            userService.delete(userId);
            return Rets.success("删除成功");
        }else {
            return Rets.failure("该用户绑定了项目或者任务，不可进行删除");
        }

    }
    @BussinessLog(value="设置账号角色",key="userId",dict=UserDict.class)
    @RequestMapping(value="/setRole",method = RequestMethod.GET)
    @RequiresPermissions(value = {Permission.USER_EDIT})
    public Object setRole(@RequestParam("userId") Long userId, @RequestParam("roleIds") String roleIds) {
        if (BeanUtil.isOneEmpty(userId, roleIds)) {
            throw new ApplicationException(BizExceptionEnum.REQUEST_NULL);
        }
        //不能修改超级管理员
        if (userId.intValue() == Const.ADMIN_ID.intValue()) {
            throw new ApplicationException(BizExceptionEnum.CANT_CHANGE_ADMIN);
        }
        User user = userService.get(userId);
        user.setRoleid(roleIds);
        userService.update(user);
        return Rets.success();
    }
    @BussinessLog(value = "冻结/解冻账号", key = "userId", dict = UserDict.class)
    @RequestMapping(value="changeStatus",method = RequestMethod.GET)
    @RequiresPermissions(value = {Permission.USER_EDIT})
    public Object changeStatus(@RequestParam Long userId){
        if (userId==null) {
            throw new ApplicationException(BizExceptionEnum.REQUEST_NULL);
        }
        User user = userService.get(userId);
        user.setStatus(user.getStatus().intValue() == ManagerStatus.OK.getCode()?ManagerStatus.FREEZED.getCode():ManagerStatus.OK.getCode());
        userService.update(user);
        return Rets.success();
    }

}
