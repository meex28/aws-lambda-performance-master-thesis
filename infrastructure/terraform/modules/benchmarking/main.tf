resource "aws_s3_bucket" "results" {
  bucket = "mte-benchmark-results"
}

resource "aws_sns_topic" "this" {
  name = "mte-benchmark-invoker-topic"
}

resource "aws_lambda_function" "invoke_orchestrator" {
  function_name = "${local.lambda_name_prefix}-invoke-orchestrator"
  role          = aws_iam_role.this.arn

  filename         = data.archive_file.invoke_orchestrator.output_path
  source_code_hash = data.archive_file.invoke_orchestrator.output_base64sha256
  publish          = true

  handler     = "index.handler"
  runtime     = "nodejs22.x"
  memory_size = 1024

  timeout = 900

  environment {
    variables = merge(
      // local.functions_arns_env_var,
      {
        SNS_TOPIC_ARN = aws_sns_topic.this.arn
      }
    )
  }
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
}

resource "aws_lambda_permission" "with_sns" {
  statement_id  = "AllowExecutionFromSNS"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.invoker.arn
  principal     = "sns.amazonaws.com"
  source_arn    = aws_sns_topic.this.arn
}

resource "aws_sns_topic_subscription" "sns_subscription" {
  topic_arn = aws_sns_topic.this.arn
  protocol  = "lambda"
  endpoint  = aws_lambda_function.invoker.arn
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
}

resource "aws_sfn_state_machine" "this" {
  name     = "${local.lambda_name_prefix}-scheduler"
  role_arn = aws_iam_role.step_function_role.arn

  definition = jsonencode({
    Comment = "Run MTE benchmark for all functions tested configurations with different request bodies",
    StartAt = "Initialize",
    States = {
      "Initialize" : {
        Type = "Pass",
        Result = {
          index = 0,
          combinations = [
            {
              filters : {
                isSnapstart : true,
                language : "java"
              },
              functionStartType : "cold"
            },
            {
              filters : {
                isSnapstart : true,
                language : "java"
              },
              functionStartType : "warm"
            },
            {
              filters : {
                isSnapstart : true,
                language : "kotlin"
              },
              functionStartType : "cold"
            },
            {
              filters : {
                isSnapstart : true,
                language : "kotlin"
              },
              functionStartType : "warm"
            },
            {
              filters : {
                isSnapstart : false,
                language : "java"
              },
              functionStartType : "cold"
            },
            {
              filters : {
                isSnapstart : false,
                language : "java"
              },
              functionStartType : "warm"
            },
            {
              filters : {
                isSnapstart : false,
                language : "kotlin"
              },
              functionStartType : "cold"
            },
            {
              filters : {
                isSnapstart : false,
                language : "kotlin"
              },
              functionStartType : "warm"
            }
          ]
        },
        Next = "CheckIndex"
      },
      "CheckIndex" : {
        Type = "Choice",
        Choices = [
          {
            Variable        = "$.index",
            NumericLessThan = 8,
            Next            = "ExtractCombination"
          }
        ],
        Default = "Done"
      },
      "ExtractCombination" : {
        Type = "Pass",
        Parameters = {
          "execution.$" : "$.index",
          "current.$" : "States.ArrayGetItem($.combinations, $.index)",
          "index.$" : "$.index",
          "combinations.$" : "$.combinations"
        },
        Next = "InvokeLambda"
      },
      "InvokeLambda" : {
        Type     = "Task",
        Resource = "arn:aws:states:::lambda:invoke",
        Parameters = {
          FunctionName = aws_lambda_function.invoke_orchestrator.function_name,
          Payload = {
            "execution.$" : "$.execution",
            "filters.$" : "$.current.filters",
            "functionStartType.$" : "$.current.functionStartType"
          }
        },
        ResultPath = "$.lambdaResult",
        Next       = "Wait"
      },
      "Wait" : {
        Type    = "Wait",
        Seconds = 1800,
        Next    = "Increment"
      },
      "Increment" : {
        Type = "Pass",
        Parameters = {
          "index.$" : "States.MathAdd($.index, 1)",
          "combinations.$" : "$.combinations"
        },
        Next = "CheckIndex"
      },
      "Done" : {
        Type = "Succeed"
      }
    }
  })
}
