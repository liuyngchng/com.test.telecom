package com.demo.addrlist.controller;

import com.demo.addrlist.model.User;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * config database https://www.cnblogs.com/liangblog/p/5228548.html
 */
@Controller
@RequestMapping("/")
public class SampleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RequestMapping("")
    public ModelAndView index(HttpServletRequest request) {
        if (request.getSession().getAttribute("name") == null) {
            ModelAndView modelAndView =  new ModelAndView("login_index");
            return modelAndView;
        }
        final String user = String.valueOf(request.getSession().getAttribute("name"));
        LOGGER.info("login user {}", user);
        ModelAndView modelAndView =  new ModelAndView("index");
        modelAndView.addObject("name", "user");
        modelAndView.addObject("dae", new Date());
        return modelAndView;
    }

    @RequestMapping("view")
    public ModelAndView view() {
        LOGGER.info("hello, myview");
        ModelAndView modelAndView =  new ModelAndView("view");
        modelAndView.addObject("name", "whoAmI");
        modelAndView.addObject("dae", new Date());
        return modelAndView;
    }

    @RequestMapping("test")
    public ModelAndView test() {
        LOGGER.info("hello, test");
        String sql = "select * from test";
        List<Map<String, Object>> list =  this.jdbcTemplate.queryForList(sql);
        int result = this.jdbcTemplate.update("update table1 set b=66 where a=1;");
        LOGGER.info("update result is {}", result);
        ModelAndView modelAndView =  new ModelAndView("test");

        modelAndView.addObject("data", "");
        return modelAndView;
    }

    @RequestMapping("file")
    public ModelAndView file() {
        LOGGER.info("hello, file");
        ModelAndView modelAndView =  new ModelAndView("file");
        return modelAndView;
    }

    @RequestMapping("login")
    public ModelAndView login(HttpServletRequest request) {
        if (null != request.getSession().getAttribute("user.name")) {
            final String name = String.valueOf(request.getSession().getAttribute("user.name"));
            if (Strings.isNullOrEmpty(name)) {
                LOGGER.info("user name is null");
                return new ModelAndView("login_index");
            }
            LOGGER.info("user login {}", name);
            ModelAndView modelAndView =  new ModelAndView("index");
            modelAndView.addObject("name", name);
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            modelAndView.addObject("date", sf.format(new Date()));
            return modelAndView;
        }
        final String name = request.getParameter("name");
        final String password = request.getParameter("password");
        if (User.list.get(name) == null || User.list.get(name) != null && !User.list.get(name).equals(password)) {
            ModelAndView modelAndView = new ModelAndView("login_index");
            if (request.getMethod().toLowerCase().equals("post")) {
                modelAndView.addObject("data", "用户名或密码错误，请重新登录");
                LOGGER.info("user name or password error.");
            } else {
                LOGGER.info("login, http get");
            }
            return modelAndView;
        }
        LOGGER.info("user login {}", name);
        request.getSession().setAttribute("user.name", name);
        ModelAndView modelAndView =  new ModelAndView("index");
        modelAndView.addObject("name", name);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        modelAndView.addObject("date", sf.format(new Date()));
        return modelAndView;
    }

    @RequestMapping("logout")
    public ModelAndView logout(HttpServletRequest request) {
        LOGGER.info("hello, file");
        ModelAndView modelAndView =  new ModelAndView("login_index");
        modelAndView.addObject("data", "您已退出系统，欢迎下次再来");
        request.getSession().removeAttribute("user.name");
        return modelAndView;
    }


}