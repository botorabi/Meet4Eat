/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * A collection of utilities for accessing annotation values.
 * 
 * @author boto
 * Date of creation Aug 23, 2017
 */
public class AnnotationUtils {

    /**
     * Get the path out of a class annotation javax.ws.rs.Path.
     * 
     * @param <T>   Class type
     * @param cls   Class which is checked for Path annotation
     * @return      Path annotation value, or empty string if no annotation exists.
     */
    public <T> String getClassPath(Class<T> cls) {
        javax.ws.rs.Path path = cls.getDeclaredAnnotation(javax.ws.rs.Path.class);
        return path == null ? "" : path.value();
    }

    /**
     * Get the path out of all method annotations using javax.ws.rs.Path.
     * 
     * @param <T>   Class type
     * @param cls   Class which is checked for Path annotation
     * @return      Paths of all methods which were annotated with javax.ws.rs.Path
     */
    public <T> Map<String /*method name*/, String /*path*/> getMethodsPath(Class<T> cls) {
        Map<String, String> paths = new HashMap<>();
        for(Method method : cls.getDeclaredMethods()){
            javax.ws.rs.Path path = method.getDeclaredAnnotation(javax.ws.rs.Path.class);
            if (path != null) {
                paths.put(method.getName(), path.value());
            }
        }       
        return paths;
    }

    /**
     * Get a list of methods with annotations Path, AuthRole having grantRoles entries, and access method (GET, POST, etc.).
     * 
     * @param <T>   Class type
     * @param cls   Class which is checked for Path annotation
     * @return      Paths, access method, and AuthRole roles of all methods which were annotated accordingly
     */
    public <T> Map<String /*path*/, Map<String /*access method*/, List<String /*roles*/>>> getMethodsAuthRoles(Class<T> cls) {
        Map<String/*path*/, Map<String /*access method*/, List<String /*roles*/>>> rules = new HashMap<>();
        for(Method method : cls.getDeclaredMethods()){
            javax.ws.rs.Path p = method.getDeclaredAnnotation(javax.ws.rs.Path.class);
            net.m4e.auth.AuthRole authrole = method.getDeclaredAnnotation(net.m4e.auth.AuthRole.class);
            String path = Objects.isNull(p) ? "" : p.value();
            String accessmethod = getAccessMethod(method);
            String[] rolesgrant = Objects.isNull(authrole) ? null : authrole.grantRoles();

            if (!Objects.isNull(path) && !Objects.isNull(accessmethod) && !Objects.isNull(rolesgrant)) {
                Map<String /*access*/, List<String /*roles*/>> accessmethods = rules.get(path);
                if (Objects.isNull(accessmethods)) {
                    accessmethods = new HashMap<>();
                    rules.put(path, accessmethods);
                }
                List<String /*roles*/> accessroles = accessmethods.get(accessmethod);
                if (Objects.isNull(accessroles)) {
                    accessroles = new ArrayList<>();
                    accessmethods.put(accessmethod, accessroles);
                }
                accessroles.addAll(Arrays.asList(rolesgrant));
            }
        }       
        return rules;
    }

    /**
     * Get a list of methods with annotations Path, AuthPermissions having grantPermissions entries, and access method (GET, POST, etc.).
     * 
     * @param <T>   Class type
     * @param cls   Class which is checked for Path annotation
     * @return      Paths, access method, and AuthRole permissions of all methods which were annotated accordingly
     */
    public <T> Map<String /*path*/, Map<String /*access method*/, List<String /*perms*/>>> getMethodsAuthPermissions(Class<T> cls) {
        Map<String/*path*/, Map<String /*access method*/, List<String /*perms*/>>> rules = new HashMap<>();
        for(Method method : cls.getDeclaredMethods()){
            javax.ws.rs.Path p  = method.getDeclaredAnnotation(javax.ws.rs.Path.class);
            net.m4e.auth.AuthRole authrole = method.getDeclaredAnnotation(net.m4e.auth.AuthRole.class);
            String path = Objects.isNull(p) ? "" : p.value();
            String accessmethod = getAccessMethod(method);
            String[] permsgrant = Objects.isNull(authrole) ? null : authrole.grantPermissions();

            if (!Objects.isNull(path) && !Objects.isNull(accessmethod) && !Objects.isNull(permsgrant)) {
                Map<String /*access*/, List<String /*perms*/>> accessmethods = rules.get(path);
                if (Objects.isNull(accessmethods)) {
                    accessmethods = new HashMap<>();
                    rules.put(path, accessmethods);
                }
                List<String /*perms*/> accessperms = accessmethods.get(accessmethod);
                if (Objects.isNull(accessperms)) {
                    accessperms = new ArrayList<>();
                    accessmethods.put(accessmethod, accessperms);
                }
                accessperms.addAll(Arrays.asList(permsgrant));
            }
        }       
        return rules;
    }

    /**
     * Get the access method which is annotated with GET, POST, PUT, and DELETE.
     * If no such annotation was found on given method, then null is returned.
     * 
     * @param method    The annotated method
     * @return          Access method, or null if no proper annotation was found
     */
    private String getAccessMethod(Method method) {
        javax.ws.rs.GET    get  = method.getDeclaredAnnotation(javax.ws.rs.GET.class);
        javax.ws.rs.POST   post = method.getDeclaredAnnotation(javax.ws.rs.POST.class);
        javax.ws.rs.PUT    put  = method.getDeclaredAnnotation(javax.ws.rs.PUT.class);
        javax.ws.rs.DELETE del  = method.getDeclaredAnnotation(javax.ws.rs.DELETE.class);
        
        if (!Objects.isNull(get)) {
            return "GET";
        }
        else if (!Objects.isNull(post)) {
            return "POST";
        }
        else if (!Objects.isNull(put)) {
            return "PUT";
        }
        else if (!Objects.isNull(del)) {
            return "DELETE";
        }

        return null;
    }
}
