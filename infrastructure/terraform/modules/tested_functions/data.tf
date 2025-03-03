data "aws_lambda_layer_version" "libcrypt_layer" {
  layer_name = "libcrypt_layer"
}

data "aws_s3_object" "lambda_source_code" {
  for_each = { for lambda in local.lambda_functions : lambda.name => lambda }
  bucket   = aws_s3_bucket.mte_functions_code.bucket
  key      = each.value.bucket_source_file
}
