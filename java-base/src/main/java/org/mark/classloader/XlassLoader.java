package org.mark.classloader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class XlassLoader extends ClassLoader {


    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final String packageName= "lib";
        final String className = "Hello";
        final String methodName = "hello";

        Class<?> aClass = new XlassLoader().loadClass(packageName + "." + className);

        for (Method method : aClass.getMethods()) {
            System.out.println(aClass.getSimpleName() + "." + method.getName());
        }

        Object instance = aClass.getDeclaredConstructor().newInstance();
        Method method = aClass.getMethod(methodName);
        method.invoke(instance);
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 包名转换成相对名字
        String relativePath = name.replace(".", "/");
        // 后缀名
        String suffix = ".xlass";
        InputStream classFileStream = this.getClass().getClassLoader().getResourceAsStream(relativePath + suffix);
        try {
            // 读取数据
            int length = classFileStream.available();
            byte[] byteArray = new byte[length];
            classFileStream.read(byteArray);
            // 转换
            byte[] classBytes = decode(byteArray);
            // 通知底层定义这个类
            return defineClass(name, classBytes, 0, classBytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        } finally {
            close(classFileStream);
        }
    }

    // 解码
    private static byte[] decode(byte[] byteArray) {
        byte[] targetArray = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            targetArray[i] = (byte) (255 - byteArray[i]);
        }
        return targetArray;
    }

    // 关闭
    private static void close(Closeable res) {
        if (null != res) {
            try {
                res.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
