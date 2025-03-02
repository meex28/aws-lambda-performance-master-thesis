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
