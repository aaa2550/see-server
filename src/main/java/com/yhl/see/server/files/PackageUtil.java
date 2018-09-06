package com.yhl.see.server.files;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageUtil {

    public static void main(String[] args) throws Exception {
        String packageName = "com.yhl.see.server";
        // List<String> classNames = getClassName(packageName);
        List<FileNode> classNames = getClassName(packageName, false);
        if (classNames != null) {
            for (FileNode className : classNames) {
                System.out.println(className);
            }
        }
    }

    /**
     * 获取某包下（包括该包的所有子包）所有类
     *
     * @param packageName 包名
     * @return 类的完整名称
     */
    public static List<FileNode> getClassName(String packageName) {
        return getClassName(packageName, true);
    }

    /**
     * 获取某包下所有类
     *
     * @param packageName  包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<FileNode> getClassName(String packageName, boolean childPackage) {
        List<FileNode> fileNodes = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String type = url.getProtocol();
            if (type.equals("file")) {
                fileNodes = getClassNameByFile(url.getPath(), packagePath, childPackage);
            } else if (type.equals("jar")) {
                fileNodes = getClassNameByJar(url.getPath(), childPackage);
            }
        } else {
            fileNodes = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage);
        }
        return fileNodes;
    }

    /**
     * 从项目文件获取某包下所有类
     *
     * @param filePath     文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<FileNode> getClassNameByFile(String filePath, String packagePath, boolean childPackage) {
        List<FileNode> fileNodes = new ArrayList<>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        assert childFiles != null;
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                /*if (childPackage) {
                    fileNodes.addAll(getClassNameByFile(childFile.getPath(), true));
                }*/
                String absolutePath = childFile.getAbsolutePath().replaceAll("\\\\", "/");
                absolutePath = absolutePath.substring(absolutePath.indexOf(packagePath));
                if (absolutePath.length() > packagePath.length() + 1 && !absolutePath.replace(packagePath + "/", "").contains("/")) {
                    String entryName = absolutePath.replace("/", ".").substring(packagePath.length() + 1);
                    fileNodes.add(new FileNode(FileNodeEnum.FILE, packagePath.replaceAll("/", "."), entryName, false));
                }
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace("\\", ".");
                    fileNodes.add(new FileNode(FileNodeEnum.CLASS, childFilePath, childFilePath, true));
                }
            }
        }

        return fileNodes;
    }

    /**
     * 从jar获取某包下所有类
     *
     * @param jarPath      jar文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<FileNode> getClassNameByJar(String jarPath, boolean childPackage) {
        List<FileNode> fileNodes = new ArrayList<>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String packagePath = jarInfo[1].substring(1);
        try {
            JarFile jarFile = new JarFile(jarFilePath.replace("%20", " "));
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    if (childPackage) {
                        if (entryName.startsWith(packagePath)) {
                            entryName = entryName.replace("/", ".").substring(packagePath.length(), entryName.lastIndexOf("."));
                            fileNodes.add(new FileNode(FileNodeEnum.CLASS, packagePath.replaceAll("/", "."), entryName, true));
                        }
                    } else {
                        int index = entryName.lastIndexOf("/");
                        String myPackagePath;
                        if (index != -1) {
                            myPackagePath = entryName.substring(0, index);
                        } else {
                            myPackagePath = entryName;
                        }
                        if (myPackagePath.equals(packagePath)) {
                            entryName = entryName.replace("/", ".").substring(packagePath.length() + 1, entryName.lastIndexOf("."));
                            fileNodes.add(new FileNode(FileNodeEnum.CLASS, packagePath.replaceAll("/", "."), entryName, true));
                        }
                    }
                } else if (jarEntry.isDirectory()) {
                    if (childPackage) {
                        if (entryName.startsWith(packagePath)) {
                            entryName = entryName.replace("/", ".").substring(packagePath.length());
                            fileNodes.add(new FileNode(FileNodeEnum.FILE, packagePath.replaceAll("/", "."), entryName, false));
                        }
                    } else {
                        int index = entryName.lastIndexOf("/");
                        String myPackagePath;
                        if (index != -1) {
                            myPackagePath = entryName.substring(0, index);
                        } else {
                            myPackagePath = entryName;
                        }
                        if (myPackagePath.contains(packagePath) && myPackagePath.length() > packagePath.length() + 1 && !myPackagePath.replace(packagePath + "/", "").contains("/")) {
                            entryName = myPackagePath.replace("/", ".").substring(packagePath.length() + 1);
                            fileNodes.add(new FileNode(FileNodeEnum.FILE, packagePath.replaceAll("/", "."), entryName, false));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileNodes;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     *
     * @param urls         URL集合
     * @param packagePath  包路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<FileNode> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) {
        List<FileNode> fileNodes = new ArrayList<>();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                URL url = urls[i];
                String urlPath = url.getPath();
                // 不必搜索classes文件夹
                if (urlPath.endsWith("classes/")) {
                    continue;
                }
                String jarPath = urlPath + "!/" + packagePath;
                fileNodes.addAll(getClassNameByJar(jarPath, childPackage));
            }
        }
        return fileNodes;
    }
}