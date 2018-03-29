/**
 * 　中科金财
 * Copyright (c) 2015-2018 zkjc,Inc.All Rights Reserved.
 */
package com.inthee.servlet;

import com.inthee.annotation.MyController;
import com.inthee.annotation.MyRequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author dingyi
 * @version $Id: MyDispatcherServlet.java, v 0.1  2018年03月24日  上午0:08 dingyi Exp $
 */
public class MyDispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = -7165723435763664600L;

    private Properties properties = new Properties();
    private List<String> classNames = new ArrayList<>();
    private Map<String, Object> ioc = new HashMap<>();
    private Map<String, Method> handlerMapping = new HashMap<>();
    private Map<String, Object> controllerMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) {
        this.doLoadConfig(config.getInitParameter("contextConfigLocation"));

        this.doScanner(properties.getProperty("scanPackage"));

        this.doInstance();

        this.initHandlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            this.doDispatcher(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500");
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (handlerMapping.isEmpty()) {
            return;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404");
        }

        Method method = this.handlerMapping.get(url);
        Class<?>[] parameterTypes = method.getParameterTypes();
        Map<String, String[]> parameterMap = req.getParameterMap();
        Object[] paramValues = new Object[parameterTypes.length];

        for (int i = 0; i<parameterTypes.length; i++) {
            String requestParam = parameterTypes[i].getSimpleName();

            if ("HttpServletRequest".equals(requestParam)) {
                paramValues[i] = req;
                continue;
            }

            if ("HttpServletResponse".equals(requestParam)) {
                paramValues[i] = resp;
                continue;
            }

            if ("String".equals(requestParam)) {
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue()).replaceAll("\\[|]", "").replace(",\\s", ",");
                    paramValues[i] = value;
                }
            }
        }

        try {
            method.invoke(controllerMap.get(url), paramValues);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        System.out.println(contextConfigLocation);
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation)) {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        assert url != null : "测试测试";
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                String className = packageName + "." + file.getName().replaceAll(".class", "");
                classNames.add(className);
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)) {
                    ioc.put(toLowerFirstWord(className), clazz.newInstance());
                } else {
                    continue;
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        try {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Class<?> clazz = entry.getValue().getClass();
                if (!clazz.isAnnotationPresent(MyController.class)) {
                    continue;
                }
                String baseUrl = "";
                if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                    MyRequestMapping annotation = clazz.getAnnotation(MyRequestMapping.class);
                    baseUrl = annotation.value();
                }

                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                        continue;
                    }

                    MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                    String url = annotation.value();
                    url = (baseUrl + "/" + url).replaceAll("/+", "/");
                    handlerMapping.put(url, method);
                    controllerMap.put(url, entry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toLowerFirstWord(String name) {
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }
}
