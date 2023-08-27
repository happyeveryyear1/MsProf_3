package cn.enilu.flash.core.factory;

import cn.enilu.flash.bean.dto.UserDto;
import cn.enilu.flash.bean.entity.system.User;
import cn.enilu.flash.utils.AESUtil;
import cn.enilu.flash.utils.MD5;
import cn.enilu.flash.utils.RandomUtil;
import org.springframework.beans.BeanUtils;

/**
 * 用户创建工厂
 *
 * @author fengshuonan
 * @date 2017-05-05 22:43
 */
public class UserFactory {

    public static User createUser(UserDto userDto, User user){
        if(userDto == null){
            return null;
        }else{
            BeanUtils.copyProperties(userDto,user);
            return user;
        }
    }
    public static User updateUser(UserDto userDto,User user) throws Exception {
        if(userDto == null){
            return null;
        }else{
            user.setName(userDto.getName());
            user.setDeptid(userDto.getDeptid());
            user.setSex(userDto.getSex());
            user.setPhone(userDto.getPhone());
            user.setEmail(userDto.getEmail());
            user.setBirthday(userDto.getBirthday());
            if(userDto.getStatus()!=null){
                user.setStatus(userDto.getStatus());
            }
            user.setExpireday(userDto.getExpireday());
            user.setSalt(userDto.getSalt());
            user.setPassword(userDto.getPassword());
//            if(userDto.getPassword()!=""){
//                user.setSalt(RandomUtil.getRandomString(5));
//                String decryptPassword = AESUtil.desEncrypt(userDto.getPassword()).trim();
//                user.setPassword(MD5.md5(decryptPassword, user.getSalt()));
//            }
            return user;
        }
    }
}
