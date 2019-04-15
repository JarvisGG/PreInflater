package jarvis.com.preinflater.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author yyf @ Zhihu Inc.
 * @since 03-30-2019
 */
class PreInflaterPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.android.registerTransform(new PreInflaterTransform(project))
    }
}
