locals {
  base_lambda_functions = [
    {
      name               = "java-function-jar"
      bucket_source_file = "java-function-jar/function.zip"
      handler            = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
      runtime            = "java21"
      snapstart_enabled  = false
    },
    {
      name               = "java-function-jar-snapstart"
      bucket_source_file = "java-function-jar/function.zip"
      handler            = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
      runtime            = "java21"
      snapstart_enabled  = true
    },
    {
      name               = "java-function-native"
      bucket_source_file = "java-function-native/function.zip"
      handler            = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
      runtime            = "provided.al2023"
      snapstart_enabled  = false
    },
    {
      name               = "kotlin-function-jar"
      bucket_source_file = "kotlin-function-jar/kotlin-function-jvm-1.0-all.jar"
      handler            = "org.lambda.performance.jvm.Handler"
      runtime            = "java21"
      snapstart_enabled  = false
    },
    {
      name               = "kotlin-function-jar-snapstart"
      bucket_source_file = "kotlin-function-jar/kotlin-function-jvm-1.0-all.jar"
      handler            = "org.lambda.performance.jvm.Handler"
      runtime            = "java21"
      snapstart_enabled  = true
    },
    {
      name               = "kotlin-function-js"
      bucket_source_file = "kotlin-function-js/kotlin-function-js.zip"
      handler            = "packages/kotlin-function/kotlin/kotlin-function.handler"
      runtime            = "nodejs22.x"
      snapstart_enabled  = false
    },
    {
      name               = "kotlin-function-native"
      bucket_source_file = "kotlin-function-native/kotlin-function.zip"
      handler            = "kotlin-function.kexe"
      layers = [data.aws_lambda_layer_version.libcrypt_layer.arn]
      runtime            = "provided.al2023"
      snapstart_enabled  = false
    },
  ]
  memory_configurations = [128, 256, 512, 1024, 2048]
  lambda_functions = flatten([
    for lambda in local.base_lambda_functions : [
      for memory in local.memory_configurations : {
        name               = "${lambda.name}-${memory}mb"
        handler            = lambda.handler
        runtime            = lambda.runtime
        bucket_source_file = lambda.bucket_source_file
        snapstart_enabled  = lambda.snapstart_enabled
        memory_size        = memory
        layers = try(lambda.layers, [])
      }
    ]
  ])
}