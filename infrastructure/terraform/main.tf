module "benchmarking" {
  source           = "./modules/benchmarking"
  tested_functions = [
    for _, func in aws_lambda_function.this : {
      arn  = func.arn
      name = func.function_name
    }
  ]
}
