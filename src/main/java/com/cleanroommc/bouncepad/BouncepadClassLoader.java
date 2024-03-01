package com.cleanroommc.bouncepad;

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.net.URL;
import java.util.List;

public class BouncepadClassLoader extends LaunchClassLoader {

    public BouncepadClassLoader(List<URL> urls) {
        super(urls.toArray(new URL[0]));
    }
    public Class<?> define(String name, byte[] data) {return this.defineClass(name, data, 0, data.length);}

}
