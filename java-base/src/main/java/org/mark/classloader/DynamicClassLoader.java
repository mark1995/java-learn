package org.mark.classloader;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicClassLoader extends ClassLoader {

    private final Map<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();
    private final Path classesDir;

    public DynamicClassLoader(Path classesDir, ClassLoader parent) {
        super(parent);
        this.classesDir = classesDir;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> loadedClass = loadedClasses.get(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        byte[] classBytes;
        try {
            classBytes = loadClassBytes(name);
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed to load class: " + name, e);
        }

        return defineClass(name, classBytes, 0, classBytes.length);
    }

    private byte[] loadClassBytes(String className) throws IOException {
        String classFilePath = className.replace('.', '/') + ".class";
        Path classFile = classesDir.resolve(classFilePath);

        try (InputStream inputStream = Files.newInputStream(classFile);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        }
    }

    public void unloadClass(String name) {
        loadedClasses.remove(name);
    }

    public static void main(String[] args) throws Exception {
        // 假设我们将类文件存储在指定目录下
        String classesDirPath = "C:\\Users\\mark\\IdeaProjects\\java-learn\\java-base\\target\\classes";
        Path classesDir = Paths.get(classesDirPath);

        // 创建自定义ClassLoader
        DynamicClassLoader classLoader = new DynamicClassLoader(classesDir, ClassLoader.getSystemClassLoader());
//        DynamicClassLoader classLoader = new DynamicClassLoader(classesDir, Thread.currentThread().getContextClassLoader());

        // 设置当前线程 classClassLoader 为类加载器
        Thread.currentThread().setContextClassLoader(classLoader);

        // 加载类
        String className = "org.mark.classloader.XlassLoader";
        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
//        Class<?> clazz = classLoader.loadClass(className);

        // 使用加载的类
        Object obj = clazz.getDeclaredConstructor().newInstance();
        System.out.println(obj);

        // 卸载类
        classLoader.unloadClass(className);


        System.out.println(Thread.currentThread().getContextClassLoader());

        // 再次加载类
//        clazz = classLoader.loadClass(className);
//        clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
        obj = clazz.getDeclaredConstructor().newInstance();
        System.out.println(obj);

        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
        System.out.println(Thread.currentThread().getContextClassLoader());
        Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        System.out.println(aClass);
    }
}