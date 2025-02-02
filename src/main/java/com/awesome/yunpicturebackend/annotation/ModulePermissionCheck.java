package com.awesome.yunpicturebackend.annotation;

import com.awesome.yunpicturebackend.constants.SpaceUserRoleConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModulePermissionCheck {

    String model() default "";

    String userRole() default SpaceUserRoleConstant.VIEWER;

}
