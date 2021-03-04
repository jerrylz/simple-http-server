package com.abchina.core.loader;

import com.abchina.core.utils.FileUtils;

import java.net.URLClassLoader;

public class JarClassLoader extends URLClassLoader {

    public JarClassLoader(String jarPath) throws Exception {
        super(FileUtils.parseSinglePathToUrls(jarPath));
    }

}
