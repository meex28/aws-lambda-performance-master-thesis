import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("multiplatform") version "2.1.0"

    id("io.github.trueangle.plugin.lambda") version "0.0.1"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "org.lambda.performance"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
}

kotlin {
    sourceSets {
        nativeMain.dependencies {
            implementation("io.github.trueangle:lambda-runtime:0.0.2")
        }

        jvmMain.dependencies {
            implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
        }
    }

    jvm()

    js().nodejs()

    linuxX64().binaries {
        executable {
            entryPoint = "org.lambda.performance.native.main" // Link this to your main function entry point
//                freeCompilerArgs += listOf("-Xallocator=mimalloc") // to understand how the choice of allocator impacts the performance, refer https://medium.com/aws-tip/leveraging-kotlin-native-for-efficient-serverless-applications-on-aws-lambda-66d992c074cc
        }
    }

    jvmToolchain(21)
}

tasks.register<ShadowJar>("shadowJar") {
    archiveBaseName.set(project.name)
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
