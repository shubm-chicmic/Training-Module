package com.chicmic.trainingModule.Config.Security;

import lombok.Setter;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


public class CustomPermissionEvaluator implements PermissionEvaluator {
    public static Map<String, Boolean> permissions = null;
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
//        System.out.println("\u001B[43m");
////        System.out.println(targetDomainObject);
//        System.out.println("Permission bro " + permission);
//        System.out.println(permissions);
//        System.out.println("\u001B[0m");
//        if (permissions == null || !permissions.containsKey((String) permission)) {
//            return false;
//        }
//        boolean hasPermission = permissions.get((String) permission);
//        // Custom logic to check if the user has the specified permission
//        // Here, we assume that the value in the permissions map indicates whether the permission is granted
//        return hasPermission;
        return true;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // This method is not used in this example
        return false;
    }
}

