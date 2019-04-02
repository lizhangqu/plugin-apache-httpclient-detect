### apache httpclient detect plugin

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "io.github.lizhangqu:plugin-apache-httpclient-detect:1.0.6"
    }
}

apply plugin: 'apache.httpclient.detect'

```

### 背景

 - [通告 | Android P新增检测项 应用热修复受重大影响](https://mp.weixin.qq.com/s?__biz=MzI0MjgxMjU0Mg==&mid=2247488357&idx=1&sn=d393bd028dfbf87998b80e06ca24bc94&scene=21#wechat_redirect)
 - [entrypoint_utils-inl.h#94](https://android.googlesource.com/platform/art/+/android-9.0.0_r16/runtime/entrypoints/entrypoint_utils-inl.h#94)
 - [android-9.0-changes-all apache-nonp](https://developer.android.com/about/versions/pie/android-9.0-changes-all?hl=zh-cn#apache-nonp)
 - [谷歌大动作影响大部分App！Android P版本推荐使用HttpURLConnection，弃用Apache HTTPClient](https://juejin.im/post/5b20bba551882513af6b7e66)
