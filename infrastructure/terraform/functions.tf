resource "aws_s3_bucket" "mte_functions_code" {
  bucket = "mte-functions-code"
}

resource "aws_iam_role" "this" {
  name = "mte-lambda-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy" "lambda_invoke_policy" {
  name = "mte-lambda-invoke-policy"
  role = aws_iam_role.this.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "lambda:*",
          "logs:*",
          "s3:*"
        ],
        Effect   = "Allow",
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_policy_attachment" "this" {
  name       = "lambda-basic-policy"
  roles = [aws_iam_role.this.name]
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

data "aws_s3_object" "lambda_source_code" {
  for_each = {for lambda in local.lambda_functions : lambda.name => lambda}
  bucket   = aws_s3_bucket.mte_functions_code.bucket
  key      = each.value.bucket_source_file
}

resource "aws_lambda_function" "this" {
  for_each      = {for lambda in local.lambda_functions : lambda.name => lambda}
  function_name = "mte-${each.key}"
  role          = aws_iam_role.this.arn

  s3_bucket        = aws_s3_bucket.mte_functions_code.bucket
  s3_key           = each.value.bucket_source_file
  source_code_hash = data.aws_s3_object.lambda_source_code[each.key].etag
  publish          = true

  handler = each.value.handler
  runtime = each.value.runtime
  layers = try(each.value.layers, [])

  timeout = 60
}
