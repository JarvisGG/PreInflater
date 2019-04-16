# PreInflater
:8ball: Android 性能优化：布局预加载，提前 AsyncInflater Layout，用于提升布局加载速度
目前还是 alpha 版本，还在开发阶段

### Usage
Step 1:
``` Gradle

buildscript {
    repositories {
        // ...
        jcenter()
        maven { url 'https://jitpack.io' }
    }
        
    dependencies {
        // ...
        classpath 'com.github.JarvisGG.PreInflater:buildSrc:1.0-SNAPSHORT'
        classpath "com.jakewharton:butterknife-gradle-plugin:9.0.0"
    }
}

```
Step2:
``` Gradle
apply plugin: 'preinflater-plugin'
apply plugin: 'com.jakewharton.butterknife'

repositories {
    // ...
    maven { url "https://jitpack.io" }
}

dependencies {
    api 'com.github.JarvisGG.PreInflater:annotation:1.0.1-SNAPSHORT'
    annotationProcessor 'com.github.JarvisGG.PreInflater:compiler:1.0.1-SNAPSHORT'
    
    api 'com.github.JarvisGG.PreInflater:library:1.0.1-SNAPSHORT'
}

```

Step3:
``` xml
android {
    // ...
    defaultConfig {
        // ...
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                     moduleNameOfPreInflater: 'testmodule1'
                ]
            }
        }
    }
    // ...
}
```
