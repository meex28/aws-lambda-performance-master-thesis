plugins {
    kotlin("multiplatform") version "2.1.0"

    id("io.github.trueangle.plugin.lambda") version "0.0.1"
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
//            implementation("io.github.trueangle:lambda-events:0.0.2")
        }
    }

    jvm()

    js {
        nodejs()
    }

    listOf(
        linuxX64(),
    ).forEach {
        it.binaries {
            executable {
                entryPoint = "org.lambda.performance.native.main" // Link this to your main function entry point
//                freeCompilerArgs += listOf("-Xallocator=mimalloc") // to understand how the choice of allocator impacts the performance, refer https://medium.com/aws-tip/leveraging-kotlin-native-for-efficient-serverless-applications-on-aws-lambda-66d992c074cc
            }
        }
    }

    jvmToolchain(21)
}