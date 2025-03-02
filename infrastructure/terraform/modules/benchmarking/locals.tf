locals {
  functions_arns_env_var = {
    for _, func in var.tested_functions :
    "FUNCTION_ARN_${replace(upper(func.name), "-", "_")}" => func.arn
  }

  lambda_name_prefix = "mte-benchmark"
}
