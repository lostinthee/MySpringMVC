/**
 * 　中科金财
 * Copyright (c) 2015-2018 zkjc,Inc.All Rights Reserved.
 */
package com.inthee.core.controller;

import com.inthee.annotation.MyController;
import com.inthee.annotation.MyRequestMapping;
import com.inthee.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author dingyi
 * @version $Id: TestController.java, v 0.1 2018年03月24日  上午0:11 dingyi Exp $
 */
@MyController
@MyRequestMapping("/test")
public class TestController {

    @MyRequestMapping("/doTest")
    public void test1(HttpServletRequest request, HttpServletResponse response, @MyRequestParam("param") String param){
        System.out.println(param);
        try {
            response.getWriter().write( "doTest method success! param:"+param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @MyRequestMapping("/doTest2")
    public void test2(HttpServletRequest request, HttpServletResponse response){
        try {
            response.getWriter().println("doTest2 method success!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
