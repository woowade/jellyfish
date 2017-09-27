package com.ngc.seaside.jellyfish.cli.gradle.plugins

import com.ngc.seaside.jellyfish.cli.gradle.JellyFishProjectGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project

class SystemDescriptorDerivedProjectPlugin implements Plugin<Project> {

    @Override
    void apply(Project p) {
        p.configure(p) {
            task('clean-gen') {
                doLast {
                    delete 'src'
                    delete 'build.generated.gradle'
                }
            }

            if (!file("build.generated.gradle").exists()) {
                logger.info(":generate")
                new JellyFishProjectGenerator(logger)
                      .setCommand('create-java-events')
                      .setInputDir(file("${systemDescriptorLocation}").absolutePath)
                      .setArguments(['model'               : "${modelName}",
                                     'outputDirectory'     : "${project.rootDir.absolutePath}",
                                     'updateGradleSettings': 'false'])
                      .generate()
            }

            apply from: 'build.generated.gradle'
        }
    }
}
