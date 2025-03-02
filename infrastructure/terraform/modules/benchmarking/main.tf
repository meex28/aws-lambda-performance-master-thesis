resource "aws_s3_bucket" "results" {
  bucket = "mte-benchmark-results"
}

resource "aws_lambda_function" "invoker" {
  function_name = "${local.lambda_name_prefix}-invoker"
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
  function_name = "${local.lambda_name_prefix}-log-processor"
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
