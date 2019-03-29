package io.github.lizhangqu.apache.httpclient.detect

import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project
import org.gradle.util.GFileUtils
import org.zeroturnaround.zip.ZipUtil


/**
 * apache httpclient detect
 * @author lizhangqu
 * @version V1.0
 * @since 2019-03-29 16:36
 */
class ApacheHttpClientDetectTransform implements Consumer<InputStream, OutputStream> {

    private Project project
    private Map<String, ClassPool> classPoolMap = new HashMap<>()
    private ClassPool apacheLegacyClassPool

    ApacheHttpClientDetectTransform(Project project) {
        this.project = project
    }

    @Override
    void accept(String variantName, String path, InputStream inputStream, OutputStream outputStream) {
        if (apacheLegacyClassPool == null) {
            File apacheJarFile = getApacheLegacyJarFile()
            project.logger.info("insertClassPath org.apache.http.legacy.jar ${apacheJarFile}")
            if (apacheJarFile != null) {
                apacheLegacyClassPool = new ClassPool()
                apacheLegacyClassPool.insertClassPath(apacheJarFile.getAbsolutePath())
            }
        }

        ClassPool classPool = classPoolMap.get(variantName)
        if (classPool == null) {
            classPool = new ClassPool(true)
            TransformHelper.updateClassPath(classPool, project, variantName)
            classPoolMap.put(variantName, classPool)
        }


        if (apacheLegacyClassPool == null) {
            return
        }

        CtClass ctClass = classPool.makeClass(inputStream, false)
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }

        detect(path, ctClass)

        TransformHelper.copy(new ByteArrayInputStream(ctClass.toBytecode()), outputStream)
    }


    @SuppressWarnings("GrMethodMayBeStatic")
    File getApacheLegacyJarFile() {
        String jarPath = "jar/org.apache.http.legacy.jar"
        try {
            //对应路径如果存在，则直接返回
            URL url = ApacheHttpClientDetectPlugin.class.getClassLoader().getResource(jarPath)
            if (url != null) {
                File apacheJarFile = new File(url.getFile())
                if (apacheJarFile.isFile() && apacheJarFile.exists()) {
                    return apacheJarFile
                }
                //取jar包中的文件
                URL jarUrl = ApacheHttpClientDetectPlugin.class.getProtectionDomain().getCodeSource().getLocation()
                if (jarUrl != null) {
                    File jarFile = new File(jarUrl.getFile())
                    File jarFolder = new File(jarFile.getParentFile(),
                            FilenameUtils.getBaseName(jarFile.getName()))
                    GFileUtils.mkdirs(jarFolder)
                    apacheJarFile = new File(jarFolder, "org.apache.http.legacy.jar")
                    GFileUtils.mkdirs(apacheJarFile.getParentFile())
                    if (apacheJarFile.isFile() && apacheJarFile.exists()) {
                        return apacheJarFile
                    }
                    //否则解压
                    ZipUtil.unpackEntry(jarFile, jarPath, apacheJarFile)
                    return apacheJarFile
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
        return null
    }


    void detect(String path, CtClass ctClass) {
        try {
            ctClass?.getRefClasses()?.each { String name ->
                if (apacheLegacyClassPool?.getOrNull(name) != null) {
                    project.logger.error("----------------------------------------Class Reference Start----------------------------------------")
                    project.logger.error("Apache HttpClient Class Reference: ")
                    project.logger.error("        └> [Class: ${name}]")
                    project.logger.error("        └> [Referenced By Class: ${path.replaceAll('/', '.')}]")
                    project.logger.error("----------------------------------------Class Reference End------------------------------------------\n\n")
                }
            }

            ctClass?.getDeclaredFields()?.each { CtField ctField ->
                if (apacheLegacyClassPool?.getOrNull(ctField.getType().getName()) != null) {
                    project.logger.error("----------------------------------------Field Reference Start----------------------------------------")
                    project.logger.error("Apache HttpClient Field Reference: ")
                    project.logger.error("        └> [Class: ${ctField.getType().getName()}]")
                    project.logger.error("        └> [Filed: ${ctField.getName()}]")
                    project.logger.error("        └> [Referenced By Class: ${path.replaceAll('/', '.')}]")
                    project.logger.error("----------------------------------------Field Reference End------------------------------------------\n\n")
                }
            }

            ctClass?.getDeclaredMethods()?.each {
                it.instrument(new ExprEditor() {
                    @Override
                    void edit(MethodCall methodCall) throws CannotCompileException {
                        super.edit(methodCall)
                        if (apacheLegacyClassPool?.getOrNull(methodCall.className) != null) {
                            project.logger.error("----------------------------------------Method Reference Start----------------------------------------")
                            project.logger.error("Apache HttpClient Method Reference: ")
                            project.logger.error("        └> [Class: ${methodCall.getClassName()}]")
                            project.logger.error("        └> [Method: ${methodCall.getMethodName()}${methodCall.getSignature()}]")
                            project.logger.error("        └> [Referenced By Class: ${path.replaceAll('/', '.')}, Line: ${methodCall.getLineNumber()}]")
                            project.logger.error("----------------------------------------Method Reference End------------------------------------------\n\n")
                        }
                    }
                })
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

}
