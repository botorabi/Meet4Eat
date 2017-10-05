/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.auth;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A collection of utilities for accessing AuthRole annotations.
 * 
 * @author boto
 * Date of creation Aug 23, 2017
 */
public class Annotations {

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
     * @param cls   Class which is checked for Path annotation
     * @return      Paths of all methods which were annotated with javax.ws.rs.Path
     */
    public Map<String /*method name*/, String /*path*/> getMethodsPath(Class cls) {
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
     * @param cls   Class which is checked for Path annotation
     * @return      Paths, access method, and AuthRole roles of all methods which were annotated accordingly
     */
    public Map<String /*path*/, Map<String /*access method*/, List<String /*roles*/>>> getMethodsAuthRoles(Class cls) {
        Map<String/*path*/, Map<String /*access method*/, List<String /*roles*/>>> rules = new HashMap<>();
        for(Method method : cls.getDeclaredMethods()){
            javax.ws.rs.Path p = method.getDeclaredAnnotation(javax.ws.rs.Path.class);
            net.m4e.app.auth.AuthRole authrole = method.getDeclaredAnnotation(net.m4e.app.auth.AuthRole.class);
            String path = (null == p) ? "" : p.value();
            String accessmethod = getAccessMethod(method);
            String[] rolesgrant = (null == authrole) ? null : authrole.grantRoles();

            if ((null != path) && (null != accessmethod) && (null != rolesgrant)) {
                Map<String /*access*/, List<String /*roles*/>> accessmethods = rules.get(path);
                if (null == accessmethods) {
                    accessmethods = new HashMap<>();
                    rules.put(path, accessmethods);
                }
                List<String /*roles*/> accessroles = accessmethods.get(accessmethod);
                if (null == accessroles) {
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
     * @param cls   Class which is checked for Path annotation
     * @return      Paths, access method, and AuthRole permissions of all methods which were annotated accordingly
     */
    public Map<String /*path*/, Map<String /*access method*/, List<String /*perms*/>>> getMethodsAuthPermissions(Class cls) {
        Map<String/*path*/, Map<String /*access method*/, List<String /*perms*/>>> rules = new HashMap<>();
        for(Method method : cls.getDeclaredMethods()){
            javax.ws.rs.Path p  = method.getDeclaredAnnotation(javax.ws.rs.Path.class);
            net.m4e.app.auth.AuthRole authrole = method.getDeclaredAnnotation(net.m4e.app.auth.AuthRole.class);
            String path = (null == p) ? "" : p.value();
            String accessmethod = getAccessMethod(method);
            String[] permsgrant = (null == authrole) ? null : authrole.grantPermissions();

            if ((null != path) && (null != accessmethod) && (null != permsgrant)) {
                Map<String /*access*/, List<String /*perms*/>> accessmethods = rules.get(path);
                if (null == accessmethods) {
                    accessmethods = new HashMap<>();
                    rules.put(path, accessmethods);
                }
                List<String /*perms*/> accessperms = accessmethods.get(accessmethod);
                if (null == accessperms) {
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
        
        if (null != get) {
            return "GET";
        }
        else if (null != post) {
            return "POST";
        }
        else if (null != put) {
            return "PUT";
        }
        else if (null != del) {
            return "DELETE";
        }

        return null;
    }
}
