PreInflater
===========================
[![](https://jitpack.io/v/JarvisGG/PreInflater.svg)](https://jitpack.io/#JarvisGG/PreInflater)
![Platform](https://img.shields.io/badge/platform-android-blue.svg)
![SDK](https://img.shields.io/badge/SDK-12%2B-blue.svg)
[![](https://img.shields.io/badge/Author-JarvisGG-7AD6FD.svg)](http:\//jarvisgg.github.io/)

Android 性能优化：布局预加载，提前 AsyncInflater Layout，用于提升布局加载速度

****
	
|Author|Jarvis|
|---|---
|E-mail|yang4130qq@gmail.com


****

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
    api 'com.github.JarvisGG.PreInflater:annotation:1.0.2-SNAPSHORT'
    annotationProcessor 'com.github.JarvisGG.PreInflater:compiler:1.0.2-SNAPSHORT'
    
    api 'com.github.JarvisGG.PreInflater:library:1.0.2-SNAPSHORT'
}

```

Step3:
``` XML
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
假如希望对当前 Activity 的加载布局都可以使用预加载，需要在 Activity 加一个 空方法，preinflater-plugin 插件会帮你 hook 这个方法，并替换你的 LayoutInflater

``` Java
@Override
protected void attachBaseContext(Context newBase) {
}
```

> Tip
>> star star star ！！！！:blush:

### LICENSE
![](https://upload.wikimedia.org/wikipedia/commons/thumb/f/f8/License_icon-mit-88x31-2.svg/128px-License_icon-mit-88x31-2.svg.png)

This library is under the MIT license. check the [LICENSE](https://opensource.org/licenses/MIT) file for more detail.

Copyright (c) 2019 Jarvis
