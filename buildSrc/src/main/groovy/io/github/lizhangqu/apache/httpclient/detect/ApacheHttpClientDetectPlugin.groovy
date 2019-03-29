package io.github.lizhangqu.apache.httpclient.detect

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author lizhangqu
 * @version V1.0
 * @since 2019-03-22 12:52
 */
class ApacheHttpClientDetectPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        BaseExtension android = project.getExtensions().getByType(BaseExtension.class)
        def transform = new CustomClassTransform(project, ApacheHttpClientDetectTransform.class)
        android.registerTransform(transform)
    }
}