data "archive_file" "invoker" {
  type        = "zip"
  source_dir  = "${path.root}/../benchmark/functions/build/invoker"
  output_path = "${path.module}/function-source/invoker.zip"
}

data "archive_file" "log_processor" {
  type        = "zip"
  source_dir  = "${path.root}/../benchmark/functions/build/log_processor"
  output_path = "${path.module}/function-source/log_processor.zip"
}

locals {
  functions_arns_env_var = {
    for idx, func in aws_lambda_function.this :
    replace("FUNCTION_ARN_${idx}", "-", "_") => func.arn
  }
}

resource "aws_s3_bucket" "benchmark_results" {
  bucket = "mte-benchmark-results"
}

resource "aws_lambda_function" "invoker" {
  function_name = "mte-benchmark-invoker-function"
  role          = aws_iam_role.this.arn

  filename         = data.archive_file.invoker.output_path
  source_code_hash = data.archive_file.invoker.output_base64sha256
  publish          = true

  handler     = "index.handler"
  runtime     = "nodejs22.x"
  memory_size = 1024

  timeout = 900

  environment {
    variables = local.functions_arns_env_var
  }
}

resource "aws_lambda_function" "log_processor" {
  function_name = "mte-benchmark-log-processor-function"
  role          = aws_iam_role.this.arn

  filename         = data.archive_file.log_processor.output_path
  source_code_hash = data.archive_file.log_processor.output_base64sha256
  publish          = true

  handler     = "index.handler"
  runtime     = "nodejs22.x"
  memory_size = 1024

  timeout = 900

  environment {
    variables = local.functions_arns_env_var
  }
}
