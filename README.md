## agp-support-plugin

本项目主要用于`Android Gradle Plugin(AGP)` 编译构建时期的优化。 尤其适用于单仓多模块的工程.

This project is used for build optimization of Android Gradle Plugin(AGP). It is necessary for multi-modules in one projects.

### 内容 | Content

- 配置阶段优化 

Optimization for configuration 

### 怎么使用 | How to apply this plugin?

如果是单仓多module的工程结构，请将插件引入到多工程根目录的`build.gradle`/`build.gradle.kts`文件下。如果根目录中包含`AGP`相关插件，请务必在相应插件前面引入我们的插件。

If your project has multi-modules, please apply this plugin in the root `build.gradle`/`build.gradle.kts` file. If this file contains Android-releated plugin, please apply our plugin before it. 
 
 ### 最佳实践 | Best Practise
 
方便起见，在根构建脚本的第一行引入此插件

Please apply this  plugin at the first line of root buildScript file.

```groovy
apply plugin: 'com.neighbwang.agp-support'
buildscript {
    ext.agp_support_version = '1.0.0'
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { url 'https://oss.sonatype.org/content/repositories/public' }
    }
    dependencies {
        classpath "com.neighbwang.gradle:agp-support-plugin:$agp_support_version" 
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { url 'https://oss.sonatype.org/content/repositories/public' }
    }
}
```

