import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    kotlin("multiplatform") version "2.1.0"

    kotlin("plugin.serialization") version "1.9.22"

    id("io.github.trueangle.plugin.lambda") version "0.0.1"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "org.lambda.performance"
version = "1.0"

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
            implementation("io.konform:konform:0.10.0")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        nativeMain.dependencies {
            implementation("io.github.trueangle:lambda-runtime:0.0.2")
        }

        jvmMain.dependencies {
            implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
        }
    }

    jvm()

    js {
        nodejs { }
        binaries.executable()
    }

    linuxX64().binaries {
        executable {
            entryPoint = "org.lambda.performance.native.main"
        }
    }

//    linuxArm64().binaries {
//        executable {
//            entryPoint = "org.lambda.performance.native.main"
//        }
//    }

    jvmToolchain(21)
}

tasks.register<ShadowJar>("shadowJar") {
    archiveBaseName.set("${project.name}-jvm")
    archiveVersion.set("1.0")
    archiveClassifier.set("all")

    // Use the output of jvmJar as the source for shadowJar
    from(tasks.named("jvmJar").get())

    // Include runtime dependencies
    configurations = listOf(project.configurations.getByName("jvmRuntimeClasspath"))

    // Optional: Merge service files and include a main class in the manifest
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "org.lambda.performance.jvm.Handler"
    }
}

tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions {
        target = "es2015"
    }
}

tasks.register<Zip>("buildJsLambdaRelease") {
    dependsOn("jsNodeProductionRun")

    from("build/js") {
        include("**/*")
    }

    archiveFileName.set("kotlin-function-js.zip")
    destinationDirectory.set(layout.buildDirectory.dir("lambda"))

    doFirst {
        if (!file("build/js").exists()) {
            throw GradleException("build/js directory not found. Run jsNodeProductionRun first.")
        }
    }
}

project.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin> {
    project.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec>().version = "22.13.0"
}
