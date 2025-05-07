resource "aws_s3_bucket" "mte_functions_code" {
  bucket = "mte-functions-code"
}

resource "aws_lambda_function" "this" {
  for_each      = {for lambda in local.lambda_functions : lambda.name => lambda}
  function_name = "mte-tested-${each.key}"
  role          = aws_iam_role.this.arn

  s3_bucket        = aws_s3_bucket.mte_functions_code.bucket
  s3_key           = each.value.bucket_source_file
  source_code_hash = data.aws_s3_object.lambda_source_code[each.key].etag
  publish          = true

  handler = each.value.handler
  runtime = each.value.runtime
  layers = try(each.value.layers, [])

  memory_size = each.value.memory_size
  timeout     = 60

  dynamic "snap_start" {
    for_each = each.value.snapstart_enabled ? [1] : []
    content {
      apply_on = "PublishedVersions"
    }
  }
}
