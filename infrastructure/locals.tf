locals {
  lambda_functions = [
    {
      name = "java-function-jar"
      bucket_source_file = "java-function-jar/java-function-1.0.0-runner.jar"
      handler = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
      layers = []
      runtime = "java21"
    },
    # {
    #   name = "java-function-native"
    #   bucket_source_file = "java-function-native/function.zip"
    # },
    {
      name = "kotlin-function-jar"
      bucket_source_file = "kotlin-function-jar/kotlin-function-1.0-all.jar"
      handler = "org.lambda.performance.Handler::handleRequest"
      layers = []
      runtime = "java21"
    },
    # {
    #   name = "kotlin-function-js"
    #   bucket_source_file = "kotlin-function-js/kotlin-function-js.zip"
    # },
    {
      name = "kotlin-function-native"
      bucket_source_file = "kotlin-function-native/kotlin-function.zip"
      handler = "kotlin-function.kexe"
      layers = [data.aws_lambda_layer_version.libcrypt_layer.arn]
      runtime = "provided.al2023"
    },
  ]
}