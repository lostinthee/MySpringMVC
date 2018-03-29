/**
 * 　中科金财
 * Copyright (c) 2015-2018 zkjc,Inc.All Rights Reserved.
 */
package com.inthee.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author dingyi
 * @version $Id: MyRequestParam.java, v 0.1 2018年03月24日  上午0:06 dingyi Exp $
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyRequestParam {

    String value() default "";
}
