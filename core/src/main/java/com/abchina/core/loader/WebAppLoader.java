package com.abchina.core.loader;

import com.abchina.core.lifecycle.LifeCycle;
import com.abchina.core.server.ServletWrapper;
import com.abchina.core.utils.CastUtils;
import com.abchina.http.servlet.Servlet;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class WebAppLoader implements LifeCycle {
    private Map<String, ServletWrapper> router;
    private ClassLoader classLoader;

    public WebAppLoader(Map<String, ServletWrapper> router, ClassLoader classLoader) {
        this.router = router;
        this.classLoader = classLoader;
    }

    @Override
    public void init() throws Exception {
        loadOnStartUp();
    }

    @Override
    public void start() {

    }

    @Override
    public void destroy() {
        router = null;
        classLoader = null;
    }

    /**
     * load servlet class which is loaded on start up and create instance of it.
     * @return return a unmodifiableMap with servlet instances which is load on start up
     * @throws Exception
     */
    public Map<String, ServletWrapper> loadOnStartUp() throws Exception {
        Set<Map.Entry<String, ServletWrapper>> entrySet = router.entrySet();
        for (Map.Entry<String, ServletWrapper> wrapperEntry : entrySet) {
            ServletWrapper wrapper = wrapperEntry.getValue();
            Integer loadOnStartup = wrapper.getLoadOnStartup();
            if (loadOnStartup != null) {
                String className = wrapper.getClassName();
                Class<? extends Servlet> servletClass = CastUtils.cast(classLoader.loadClass(className));
                Servlet servletInstance = servletClass.newInstance();
                wrapper.setServlet(servletInstance);
                wrapper.setServletClass(servletClass);
            }

        }
        return Collections.unmodifiableMap(router);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
       return classLoader.loadClass(name);
    }


    Map<String, ServletWrapper> getRouter() {
        return Collections.unmodifiableMap(router);
    }


}
