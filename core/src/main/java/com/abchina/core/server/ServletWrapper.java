package com.abchina.core.server;


import com.abchina.http.servlet.Servlet;
import com.abchina.http.utils.EqualsUtils;

public class ServletWrapper {
    private final String name;
    private final String path;
    private final String className;
    private Integer loadOnStartup;
    private Class<? extends Servlet> servletClass;
    private Servlet servlet;


    public Servlet getServlet() {
        return servlet;
    }

    public ServletWrapper(String name, String path, String className, Integer loadOnStartup, Class<? extends Servlet> servletClass, Servlet servlet) {
        this.name = name;
        this.path = path;
        this.className = className;
        this.loadOnStartup = loadOnStartup;
        this.servletClass = servletClass;
        this.servlet = servlet;
    }

    public void setServletClass(Class<? extends Servlet> servletClass) {
        this.servletClass = servletClass;
    }

    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    public Integer getLoadOnStartup() {
        return loadOnStartup;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ServletWrapper)) {
            return false;
        }

        ServletWrapper that = (ServletWrapper) obj;
        try {
            return EqualsUtils.OneDepthContentEquals(this, that);
        } catch (IllegalAccessException e) {
            // TODO use logging
            e.printStackTrace();
        }
        return false;
    }

    public Class<? extends Servlet> getServletClass() {
        return servletClass;
    }

}
