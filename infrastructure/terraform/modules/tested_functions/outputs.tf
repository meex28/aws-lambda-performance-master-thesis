output "functions" {
  value = [
    for _, func in aws_lambda_function.this : {
      arn  = func.arn
      name = func.function_name
    }
  ]
  description = "Created lambda functions resources"
}
