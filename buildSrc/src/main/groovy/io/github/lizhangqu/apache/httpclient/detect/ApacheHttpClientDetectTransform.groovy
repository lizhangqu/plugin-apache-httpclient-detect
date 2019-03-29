package io.github.lizhangqu.apache.httpclient.detect

import org.gradle.api.Project

/**
 * apache httpclient detect
 * @author lizhangqu
 * @version V1.0
 * @since 2019-03-14 14:06
 */
class ApacheHttpClientDetectTransform implements Consumer<InputStream, OutputStream> {

    private Project project

    ApacheHttpClientDetectTransform(Project project) {
        this.project = project
    }

    @Override
    void accept(String variantName, String path, InputStream inputStream, OutputStream outputStream) {
        TransformHelper.copy(new ByteArrayInputStream(ctClass.toBytecode()), outputStream)
    }

}
