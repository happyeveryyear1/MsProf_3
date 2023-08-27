package cn.enilu.flash.api.controller;

import cn.enilu.flash.bean.vo.front.Rets;
import cn.enilu.flash.utils.DrawImageUtil;
import cn.enilu.flash.utils.HttpUtil;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("/verifycode")
public class VerifyCodeController {
    private DrawImageUtil drawmageUtil=new DrawImageUtil();

    //生成验证码
    @GetMapping("/image")
    public Object image(@RequestParam(value = "type", required = false, defaultValue = "nl") String createTypeFlag, HttpServletRequest request, HttpServletResponse response) throws IOException, IOException {
    
//         String savePath = "/app_tmp/"; // 保存路径
//         for (int i = 0; i < 10000; i++) {
// //           /1.在内存中创建一张图片
//             BufferedImage bi = new BufferedImage(DrawImageUtil.WIDTH, DrawImageUtil.HEIGHT, BufferedImage.TYPE_INT_RGB);
//             //2.得到图片
//             Graphics g = bi.getGraphics();
//             //3.设置图片的背影色
//             drawmageUtil.setBackGround(g);
//             //4.设置图片的边框
//             drawmageUtil.setBorder(g);
//             //5.在图片上画干扰线
//             drawmageUtil.drawRandomLine(g);
//             //6.写在图片上随机数
//             //String random = drawRandomNum((Graphics2D) g,"ch");//生成中文验证码图片
//             //String random = drawRandomNum((Graphics2D) g,"nl");//生成数字和字母组合的验证码图片
//             //String random = drawRandomNum((Graphics2D) g,"n");//生成纯数字的验证码图片
//             //String random = drawRandomNum((Graphics2D) g,"l");//生成纯字母的验证码图片
//             String random = drawmageUtil.drawRandomNum((Graphics2D) g, "nl");//根据客户端传递的createTypeFlag标识生成验证码图片
//
//             String s = imageToString(bi);
//
//             byte[] bytes = Base64.getDecoder().decode(s);
//             String fileName = random+"_"+new Date().getTime()+".jpg";
//             Path path = Paths.get(savePath, fileName);
//             Files.write(path, bytes);
//
//         }

        //1.在内存中创建一张图片
        BufferedImage bi = new BufferedImage(DrawImageUtil.WIDTH, DrawImageUtil.HEIGHT, BufferedImage.TYPE_INT_RGB);
        //2.得到图片
        Graphics g = bi.getGraphics();
        //3.设置图片的背影色
        drawmageUtil.setBackGround(g);
        //4.设置图片的边框
        drawmageUtil.setBorder(g);
        //5.在图片上画干扰线
        drawmageUtil.drawRandomLine(g);
        //6.写在图片上随机数
        //String random = drawRandomNum((Graphics2D) g,"ch");//生成中文验证码图片
        //String random = drawRandomNum((Graphics2D) g,"nl");//生成数字和字母组合的验证码图片
        //String random = drawRandomNum((Graphics2D) g,"n");//生成纯数字的验证码图片
        //String random = drawRandomNum((Graphics2D) g,"l");//生成纯字母的验证码图片
        String random = drawmageUtil.drawRandomNum((Graphics2D) g, createTypeFlag);//根据客户端传递的createTypeFlag标识生成验证码图片
        //7.将随机数存在session中
        request.getSession().setAttribute("checkcode", random);
        //8.设置响应头通知浏览器以图片的形式打开
//        response.setContentType("image/jpeg");//等同于response.setHeader("Content-Type", "image/jpeg");
//        //9.设置响应头控制浏览器不要缓存
//        response.setDateHeader("expries", -1);
//        response.setHeader("Cache-Control", "no-cache");
//        response.setHeader("Pragma", "no-cache");
//        //10.将图片写给浏览器
//        ImageIO.write(bi, "jpg", response.getOutputStream());
        return Rets.success(imageToString(bi));
    }

    private String imageToString(BufferedImage bi) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpg", outputStream);
        byte[] bytes = outputStream.toByteArray();
        return Base64Utils.encodeToString(bytes);
    }


    //验证验证码是否正确
    @RequestMapping("/check")
    public boolean check(@RequestParam("validateCode")String validateCode){
        boolean br=false;
        String serverCheckcode = (String) HttpUtil.getRequest().getSession().getAttribute("checkcode");//从服务器端的session中取出验证码
        if (validateCode.equals(serverCheckcode)) { //将客户端验证码和服务器端验证比较，如果相等，则表示验证通过
            br=true;
        }
        HttpUtil.getRequest().getSession().removeAttribute("checkcode");
        return br;
    }
}
